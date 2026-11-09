package pl.zombek.client.core.uhc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * Parser analyzing incoming visible chat messages for UHC phase events.
 * Only processes messages displayed to the player; does not send or intercept packets.
 */
public class UhcChatParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(UhcChatParser.class);
    private boolean enabled = true;
    private final UhcSessionTracker sessionTracker;

    public UhcChatParser(UhcSessionTracker sessionTracker) {
        this.sessionTracker = sessionTracker;
    }

    public synchronized void onChatMessage(String rawMessage) {
        if (!enabled || rawMessage == null || sessionTracker == null) {
            return;
        }

        String msg = UhcScoreboardParser.stripFormatting(rawMessage).trim().toLowerCase(Locale.ROOT);
        if (msg.isEmpty()) return;

        // 1. Match start
        if (msg.contains("game has begun") || msg.contains("match started")
                || msg.contains("the game has started") || msg.contains("gra wystartowala")
                || msg.contains("mecz rozpoczety")) {
            LOGGER.info("[UHC] Wykryto rozpoczęcie meczu z czatu.");
            sessionTracker.onMatchStart();
            return;
        }

        // 2. PvP enabled / Grace period end
        if (msg.contains("pvp is now enabled") || msg.contains("grace period has ended")
                || msg.contains("pvp enabled") || msg.contains("pvp wlaczone")
                || msg.contains("ochrona minela") || msg.contains("grace period ended")) {
            LOGGER.info("[UHC] Wykryto aktywację PvP z czatu.");
            sessionTracker.onPvpEnabled();
            return;
        }

        // 3. Border shrink
        if (msg.contains("border is shrinking") || msg.contains("border moving")
                || msg.contains("world border is shrinking") || msg.contains("granica sie zmniejsza")) {
            LOGGER.info("[UHC] Wykryto ruch granicy z czatu.");
            sessionTracker.onBorderShrinking();
            return;
        }

        // 4. Deathmatch / Meetup
        if (msg.contains("deathmatch will begin") || msg.contains("deathmatch started")
                || msg.contains("meetup time") || msg.contains("faza deathmatch")) {
            LOGGER.info("[UHC] Wykryto fazę Deathmatch z czatu.");
            sessionTracker.onDeathmatch();
            return;
        }

        // 5. Game Over / Victory
        if (msg.contains("has won the game") || msg.contains("winner is")
                || msg.contains("victory!") || msg.contains("zwyciezca meczu")) {
            LOGGER.info("[UHC] Wykryto zakończenie meczu z czatu.");
            sessionTracker.onGameOver();
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
