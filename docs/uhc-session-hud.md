# Moduł UHC Session HUD — ZombekClient

## 1. Przeznaczenie i Rola w Kliencie

Moduł **UHC Session HUD** (`UHCSessionHudModule`, identyfikator: `uhc_session_hud`, kategoria: `Category.SURVIVAL`) to zaawansowany panel informacyjny 2D wyświetlany na ekranie gracza, dedykowany dla turniejów i rozgrywek Ultra Hardcore (UHC) oraz innych trybów survival/PvP.

W wyniku formalnej korekty architektury w Etapie 8A6:
- Dotychczasowy panel statystyk sesji (timery, status gracza, liczniki przedmiotów, parser scoreboardu) został precyzyjnie wydzielony do modułu `UHCSessionHudModule`.
- Nazwa identyfikatora `uhc_overlay` została zwolniona i przypisana do właściwego modułu skalowania modeli leżących przedmiotów (`UHCOverlayModule`).
- Moduł zawiera automatyczną migrację konfiguracji JSON w `ModuleConfigManager`, dzięki czemu istniejące profile graczy nie tracą swoich ustawień.

Główna klasa modułu znajduje się w:
[UHCSessionHudModule.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/survival/UHCSessionHudModule.java)  
Śledzenie parametrów sesji i parser scoreboardu:
[UhcSessionTracker.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/survival/UhcSessionTracker.java)  
[UhcScoreboardParser.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/survival/UhcScoreboardParser.java)

---

## 2. Główne Komponenty Panelu

Panel agreguje 4 kluczowe obszary danych:
1. **Stan Gracza (Player Status):**
   - Poziom zdrowia (HP) z uwzględnieniem absorpcji (złote serca).
   - Poziom głodu i nasycenia (saturation).
   - Czas trwania bieżącej sesji meczowej.
2. **Licznik Przedmiotów UHC (Item Tracker):**
   - Złote jabłka (G-Apples) oraz złote główki (G-Heads / Player Heads).
   - Strzały i perły kresu (Ender Pearls).
   - Mikstury leczenia i regeneracji.
   - Bloki budowlane (kamień, drewno, bruk).
   - Wiadra z wodą i lawą.
   - Krzesiwa (Flint & Steel).
3. **Zegary i Odliczania UHC (Timers):**
   - Game Time (łączny czas meczu liczony nanosekundowym zegarem monotonicznym).
   - PvP Grace Period Countdown (czas do włączenia obrażeń PvP).
   - Border Shrink Timer (czas do kolejnego zwężenia granicy świata).
   - Meetup / Deathmatch Countdown (czas do teleportacji na arenę finałową).
4. **Informacje o Meczu (Match / Scoreboard Info):**
   - Faza meczu, liczba żywych graczy (`Alive Players`), liczba drużyn, aktualny promień granicy odczytane w sposób pasywny i bezpieczny z prawego paska Scoreboardu.

---

## 3. Style Układu (Layout Styles)

- **`DETAILED` (Domyślny):** Kompletny widok ze wszystkimi sekcjami, czytelnymi nagłówkami i separatorami.
- **`COMPACT`:** Skondensowane dwukolumnowe zestawienie najważniejszych statystyk (HP, G-Apples, PvP Timer, Alive).
- **`VERTICAL`:** Czysta pionowa lista parametrów.
- **`HORIZONTAL`:** Pasek statusu u góry lub u dołu ekranu.

---

## 4. Automatyczna Migracja Konfiguracji (Backward Compatibility)

W klasie `ModuleConfigManager` zaimplementowano regułę transparentnej migracji:
```java
if (rootNode.has("uhc_overlay") && !rootNode.has("uhc_session_hud")) {
    JsonNode oldOverlayNode = rootNode.get("uhc_overlay");
    // Jeśli węzeł zawiera ustawienia layout_style lub show_player_status,
    // automatycznie przepisz go do uhc_session_hud:
    if (oldOverlayNode.has("layout_style") || oldOverlayNode.has("show_player_status")) {
        ((ObjectNode) rootNode).set("uhc_session_hud", oldOverlayNode.deepCopy());
        logger.info("Migrated legacy 'uhc_overlay' HUD settings to 'uhc_session_hud'");
    }
}
```
Dzięki temu gracze aktualizujący ZombekClient zachowują nienaruszone ustawienia interfejsu.
