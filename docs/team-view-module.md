# Moduł Team View — ZombekClient

## 1. Przeznaczenie i Rola w Kliencie

Moduł **Team View** (`TeamViewModule`, identyfikator: `team_view`, kategoria: `Category.HUD`) to taktyczny system nawigacji i lokalizacji sojuszników, stworzony z myślą o zorganizowanych trybach drużynowych, takich jak UHC Teams, Bedwars, SkyWars Squads, Mega Walls i rozgrywki turniejowe.

Moduł renderuje w przestrzeni świata gry (in-world 3D billboard) estetyczny znacznik graficzny ponad głowami zarejestrowanych sojuszników, zawierający wskaźnik kształtu, nick gracza, odległość w metrach (blokach) oraz wskaźnik zdrowia.

Główna klasa modułu znajduje się w:
[TeamViewModule.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/TeamViewModule.java)  
Śledzenie drużyny i provider kierunku:
[TeamTracker.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/TeamTracker.java)  
[ITeamDirectionProvider.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/ITeamDirectionProvider.java)

---

## 2. Wykrywanie Drużyny (Team Detection Layers)

Parametr `detection_mode` (`TeamDetectionMode`) definiuje sposób identyfikacji sojuszników:
1. **`SCOREBOARD_TEAMS` (Domyślny):** Automatyczne odczytywanie drużyn z oficjalnego systemu `Scoreboard` gry Minecraft (Minecraft Scoreboard Teams). Kolor drużyny jest pobierany bezpośrednio z koloru teamu zarejestrowanego na serwerze.
2. **`MANUAL_LIST`:** Własna lista nicków przyjaciół wprowadzana w konfiguracji (przydatne na serwerach bez formalnych scoreboard teams lub w grach niestandardowych).
3. **`BOTH`:** Połączenie obu metod — gracze z tej samej drużyny scoreboard ORAZ gracze z listy manualnej są traktowani jako sojusznicy.

---

## 3. Style Znaczników (Marker Styles)

Parametr `marker_style` (`MarkerStyle`) pozwala dobrać formę graficzną renderowanego wskaźnika:
- **`ARROW_DOWN` (Domyślny):** Dynamiczna strzałka skierowana w dół (np. `▼`), wskazująca precyzyjnie pozycję gracza.
- **`DIAMOND`:** Romb/diament (np. `◆`), klasyczny znacznik stylizowany na wskaźniki gier FPS.
- **`DOT`:** Subtelny punkt/kropka (np. `●`).
- **`SHIELD`:** Tarcza taktyczna (np. `🛡`).
- **`CHEVRON`:** Podwójny daszek wojskowy (np. `︾`).

---

## 4. Opcje Wyświetlania i Zakresy

W menu ZombekClient dostępne są szczegółowe opcje dostosowania widoku:
- **`show_name` (Boolean, domyślnie `true`):** Wyświetlanie nicku sojusznika nad znacznikiem.
- **`show_distance` (Boolean, domyślnie `true`):** Prezentacja dystansu w formacie np. `24m`.
- **`show_health` (Boolean, domyślnie `false`):** Prezentacja stanu zdrowia sojusznika (np. `18 HP`), jeśli dane są dostępne w scoreboardzie serwera.
- **`max_distance` (Double, 10.0 - 256.0, domyślnie 64.0):** Maksymalny zasięg renderowania znacznika. Zapobiega zaśmiecaniu ekranu odległymi jednostkami.
- **`scale` (Double, 0.5 - 2.0, domyślnie 1.0):** Rozmiar znacznika w świecie gry.

---

## 5. Rygorystyczne Zgodności z Regulaminami Serwerów (Anti-Cheat Compliance)

> [!IMPORTANT]
> **Ścisłe Zasady Przeciw Wallhackom / ESP:**
> Moduł **Team View** w ZombekClient został zaprojektowany z absolutnym poszanowaniem reguł Fair Play i regulaminów serwerów (Hypixel, Badlion, GommeHD):
> 1. **BRAK Wallhacka / X-Ray:** Znaczniki NIE są renderowane przez ściany bez ograniczeń. Moduł uwzględnia standardowy bufor widoczności oraz zasięg renderowania encji (`Entity Render Distance`).
> 2. **BRAK ESP Przeciwników:** Moduł kategorycznie NIE śledzi wrogich graczy. Opcja renderowania dotyczy WYŁĄCZNIE zweryfikowanych sojuszników z tej samej drużyny.
> 3. **Brak ukrytych pakietów:** Moduł pobiera pozycje wyłącznie z encji już zsynchronizowanych przez serwer w zasięgu widzenia klienta.

---

## 6. Architektura i Interfejs dla Przyszłego Direction HUD

Zgodnie z wymaganiami sekcji 6.4 Etapu 8A6, moduł implementuje publiczny kontrakt:
```java
public interface ITeamDirectionProvider {
    List<TeammateDirectionInfo> getTeammateDirections();
    Optional<TeammateDirectionInfo> getTeammateDirection(String playerName);
}
```
Zapewnia to gotowy, w pełni przetestowany interfejs programistyczny dla przyszłych modułów nawigacyjnych (np. kompasu HUD, radaru czy wskaźników kierunkowych), bez konieczności duplikowania logiki śledzenia drużyny.
