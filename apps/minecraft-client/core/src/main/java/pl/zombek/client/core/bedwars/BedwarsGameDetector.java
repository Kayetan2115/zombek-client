package pl.zombek.client.core.bedwars;

import java.util.List;
import java.util.Locale;

/**
 * Detects whether the client is in a Bedwars match, evaluating game phase and confidence.
 * Relies strictly on publicly visible scoreboard and chat information without server queries.
 */
public class BedwarsGameDetector {
    private BedwarsGameState currentState = BedwarsGameState.UNKNOWN;
    private DetectionConfidence confidence = DetectionConfidence.UNKNOWN;
    private BedwarsMode detectedMode = BedwarsMode.OTHER;
    private String detectedMap = "N/A";
    private long lastDetectionTime = 0L;

    public BedwarsGameDetector() {}

    /**
     * Evaluates scoreboard title and lines to update match state and confidence level.
     */
    public synchronized void evaluateScoreboard(String title, List<String> rawLines) {
        if (title == null && (rawLines == null || rawLines.isEmpty())) {
            this.currentState = BedwarsGameState.UNKNOWN;
            this.confidence = DetectionConfidence.UNKNOWN;
            return;
        }

        String cleanTitle = clean(title);
        boolean titleIsBedwars = cleanTitle.contains("bed wars") || cleanTitle.contains("bedwars") || cleanTitle.equals("bw");

        // Check if scoreboard explicitly identifies another game
        if (!titleIsBedwars && (cleanTitle.contains("skywars") || cleanTitle.contains("arcade") ||
                cleanTitle.contains("murder mystery") || cleanTitle.contains("build battle") ||
                cleanTitle.contains("duels") || cleanTitle.contains("blitz sg") || cleanTitle.contains("uhc"))) {
            this.currentState = BedwarsGameState.NOT_IN_BEDWARS;
            this.confidence = DetectionConfidence.HIGH;
            return;
        }

        int bedStatusLinesFound = 0;
        boolean hasWaitingText = false;
        boolean hasStartingText = false;
        boolean hasSuddenDeath = false;
        boolean hasGameOver = false;
        String modeCandidate = null;
        String mapCandidate = null;

        if (rawLines != null) {
            for (String raw : rawLines) {
                String line = clean(raw);
                if (line.isEmpty()) continue;

                // Detect team bed status indicators: e.g. "r red: ✓", "b: 2", "g green: [e]"
                if (isTeamBedLine(line)) {
                    bedStatusLinesFound++;
                }

                if (line.contains("waiting...") || line.contains("waiting for players") || line.contains("oczekiwanie na graczy")) {
                    hasWaitingText = true;
                }
                if (line.contains("starting in") || line.contains("gra wystartuje za") || line.contains("starts in")) {
                    hasStartingText = true;
                }
                if (line.contains("sudden death") || line.contains("nagla smierc") || line.contains("dragon spawn")) {
                    hasSuddenDeath = true;
                }
                if (line.contains("game over") || line.contains("koniec gry") || line.contains("winner:") || line.contains("zwyciezca:")) {
                    hasGameOver = true;
                }

                // Map & Mode detection
                if (line.startsWith("map:") || line.startsWith("mapa:")) {
                    mapCandidate = extractAfterColon(raw);
                }
                if (line.startsWith("mode:") || line.startsWith("tryb:")) {
                    modeCandidate = extractAfterColon(raw);
                }
            }
        }

        if (mapCandidate != null && !mapCandidate.trim().isEmpty()) {
            this.detectedMap = mapCandidate.trim();
        }
        if (modeCandidate != null) {
            this.detectedMode = BedwarsMode.parse(modeCandidate);
        }

        // Determine Game State & Confidence
        if (titleIsBedwars) {
            if (hasGameOver) {
                this.currentState = BedwarsGameState.GAME_ENDED;
                this.confidence = DetectionConfidence.HIGH;
            } else if (hasSuddenDeath) {
                this.currentState = BedwarsGameState.SUDDEN_DEATH;
                this.confidence = DetectionConfidence.HIGH;
            } else if (bedStatusLinesFound >= 2) {
                this.currentState = BedwarsGameState.BEDWARS_ACTIVE;
                this.confidence = DetectionConfidence.HIGH;
            } else if (hasStartingText) {
                this.currentState = BedwarsGameState.STARTING;
                this.confidence = DetectionConfidence.HIGH;
            } else if (hasWaitingText) {
                this.currentState = BedwarsGameState.WAITING;
                this.confidence = DetectionConfidence.HIGH;
            } else {
                this.currentState = BedwarsGameState.LOBBY;
                this.confidence = DetectionConfidence.MEDIUM;
            }
        } else if (bedStatusLinesFound >= 2) {
            // Title might be custom or server branded (e.g. "MINEMEN" or "BLOCKSMC"), but bed lines are distinctive
            this.currentState = BedwarsGameState.BEDWARS_ACTIVE;
            this.confidence = DetectionConfidence.MEDIUM;
        } else if (bedStatusLinesFound == 1) {
            this.currentState = BedwarsGameState.IN_GAME;
            this.confidence = DetectionConfidence.LOW;
        } else {
            this.currentState = BedwarsGameState.NOT_IN_BEDWARS;
            this.confidence = DetectionConfidence.LOW;
        }

        this.lastDetectionTime = System.currentTimeMillis();
    }

    /**
     * Handles chat-based triggers (e.g. victory or sudden death announcements).
     */
    public synchronized void evaluateChatMessage(String rawMessage) {
        if (rawMessage == null) return;
        String line = clean(rawMessage);

        if (line.contains("bed wars") || line.contains("bedwars")) {
            if (currentState == BedwarsGameState.UNKNOWN || currentState == BedwarsGameState.NOT_IN_BEDWARS) {
                // Low confidence signal; wait for scoreboard confirmation
                this.confidence = DetectionConfidence.LOW;
            }
        }
        if (line.contains("victory!") || line.contains("zwycięstwo!") || line.contains("1st killer -")) {
            this.currentState = BedwarsGameState.GAME_ENDING;
            this.confidence = DetectionConfidence.HIGH;
        }
        if (line.contains("sudden death has begun") || line.contains("nagła śmierć rozpoczęta")) {
            this.currentState = BedwarsGameState.SUDDEN_DEATH;
            this.confidence = DetectionConfidence.HIGH;
        }
        if (line.contains("the game has begun") || line.contains("gra została rozpoczęta")) {
            this.currentState = BedwarsGameState.BEDWARS_ACTIVE;
            this.confidence = DetectionConfidence.HIGH;
        }
    }

    public static boolean isTeamBedLine(String cleanLine) {
        if (cleanLine == null || cleanLine.length() < 3) return false;
        // Typical Hypixel format: "R Red: ✓", "B Blue: ✕", "G: [E]", "Y: 2"
        return cleanLine.matches("(?i)^[rbgyawps]\\s+(?:red|blue|green|yellow|aqua|white|pink|gray|szar|czerw|nieb|ziel|zolt|żółt|bial|biał|roz|róż)?:?\\s*([✓✕✔xX\\?]|\\d+|\\[e\\]).*")
                || cleanLine.matches("(?i)^[rbgyawps]:?\\s*([✓✕✔xX\\?]|\\d+|\\[e\\]).*");
    }

    private static String clean(String input) {
        if (input == null) return "";
        return input.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim().toLowerCase(Locale.ROOT);
    }

    private static String extractAfterColon(String raw) {
        String stripped = raw.replaceAll("§[0-9a-fk-orA-FK-OR]", "");
        int idx = stripped.indexOf(':');
        if (idx >= 0 && idx < stripped.length() - 1) {
            return stripped.substring(idx + 1).trim();
        }
        return stripped.trim();
    }

    public synchronized void reset() {
        this.currentState = BedwarsGameState.UNKNOWN;
        this.confidence = DetectionConfidence.UNKNOWN;
        this.detectedMode = BedwarsMode.OTHER;
        this.detectedMap = "N/A";
        this.lastDetectionTime = 0L;
    }

    public BedwarsGameState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(BedwarsGameState state) {
        this.currentState = state;
    }

    public DetectionConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(DetectionConfidence confidence) {
        this.confidence = confidence;
    }

    public BedwarsMode getDetectedMode() {
        return detectedMode;
    }

    public void setDetectedMode(BedwarsMode mode) {
        this.detectedMode = mode;
    }

    public String getDetectedMap() {
        return detectedMap;
    }

    public void setDetectedMap(String map) {
        this.detectedMap = map;
    }

    public long getLastDetectionTime() {
        return lastDetectionTime;
    }

    /**
     * Decides whether the Bedwars HUD overlay should be visible according to settings.
     */
    public boolean shouldDisplayOverlay(boolean autoDetect, boolean autoEnable, boolean manualMode, boolean requireHighConfidence) {
        if (manualMode) {
            return true;
        }
        if (!autoDetect || !autoEnable) {
            return false;
        }
        if (requireHighConfidence && confidence != DetectionConfidence.HIGH) {
            return false;
        }
        if (!requireHighConfidence && !confidence.isReliable()) {
            return false;
        }
        return currentState.isPlaying() || currentState == BedwarsGameState.STARTING || currentState == BedwarsGameState.GAME_ENDING;
    }
}
