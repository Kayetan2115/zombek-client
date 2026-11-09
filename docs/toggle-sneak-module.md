# Moduł Toggle Sneak — ZombekClient

## 1. Przeznaczenie i Rola w Kliencie

Moduł **Toggle Sneak** (`ToggleSneakModule`, identyfikator: `toggle_sneak`, kategoria: `Category.PVP`) pozwala na stałe kucanie (sneak) bez konieczności nieprzerwanego trzymania klawisza `LSHIFT`.
Moduł realizuje standardową mechanikę `shiftKeyDown = true` w kliencie, gwarantując pełną zgodność z fizyką gry i brak modyfikacji pakietów ruchu.

Główna klasa modułu znajduje się w:
[ToggleSneakModule.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/pvp/ToggleSneakModule.java)

---

## 2. Tryby Działania

Moduł udostępnia 3 tryby aktywacji kontrolowane przez parametr `activation_mode` (`SneakActivationMode`):

1. **`TOGGLE_AND_HOLD` (Domyślny):**
   - Klawisz toggle (domyślnie `C`, GLFW keycode `67`) przełącza stałe kucanie.
   - Fizyczne przytrzymanie domyślnego klawisza gry `LSHIFT` zawsze wymusza kucanie (priorytet wejścia fizycznego).
   - Zwolnienie `LSHIFT` przy włączonym toggle powraca do kucania toggle.

2. **`TOGGLE`:**
   - Czysty przełącznik ON/OFF pod dedykowanym klawiszem.

3. **`HOLD`:**
   - Działa jako dodatkowy alternatywny klawisz przytrzymania kucania.

---

## 3. Zgodność z Vanilla i Ochrona Fair Play

Toggle Sneak w ZombekClient został zaprojektowany z zachowaniem najwyższych standardów bezpieczeństwa:
- **Respektowanie Vanilla Hitbox:** Wysokość oczu i bounding box gracza zmniejszają się dokładnie według reguł Minecraft Vanilla ($1.5$ m). Brak fałszywych zasięgów czy modyfikowanych pozycji.
- **Brak fake packetów:** Moduł operuje na wejściu lokalnym gracza (`input.sneaking = true` / `input.shiftKeyDown = true`).
- **Warunki blokujące kucanie:**
  - Latanie na survivalu lub creative (`disable_while_flying`): automatycznie odblokowuje kucanie, aby nie kolidować z opadaniem w locie.
  - Jazda konno / łódką (`disable_while_riding`): zabezpieczenie przed niezamierzonym zsiadaniem ze zwierząt lub pojazdów.
  - Pływanie i lot elytrą (`isSwimming()`, `isFallFlying()`): zachowanie poprawnych animacji Vanilla.

---

## 4. Wskaźnik HUD

Moduł implementuje interfejs `IHudElement` i renderuje status na ekranie za pośrednictwem wspólnego `IHudRenderer`.

### Style wskaźnika (`indicator_style`):
1. **`BRACKETED` (Domyślny):** np. `[Sneak (Toggled)]`, `[Sneak (Key Held)]`
2. **`COMPACT`:** np. `Sneak`, `Sneak (Off)`
3. **`DETAILED`:** np. `Sneak: Toggled`, `Sneak: Off`
4. **`TEXT_ONLY`:** np. `SNEAK`

### Zakotwiczenie i kolorystyka:
- Zakotwiczenie: `HudAnchor` (domyślnie `TOP_LEFT`).
- Domyślny offset: $X = 10$, $Y = 120$.
- Kolor aktywny: zielony (`#55FF55`, `0xFF55FF55`).
- Kolor nieaktywny: szary (`#AAAAAA`, `0xFFAAAAAA`).

---

## 5. Zabezpieczenia Cyklu Życia

- **Śmierć i odrodzenie (`onPlayerDeath`):** Natychmiastowe zdjęcie kucania.
- **Zmiana świata i rozłączenie (`onWorldChange`):** Zerowanie stanu.
- **Utrata fokusu okna (`onFocusLost`):** Zwolnienie stanów przytrzymania.
- **Otwarte menu / czat (`disable_in_menus`):** Blokada wymuszonego kucania podczas pisania wiadomości.
