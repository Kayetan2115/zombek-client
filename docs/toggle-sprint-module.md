# Moduł Toggle Sprint — ZombekClient

## 1. Przeznaczenie i Rola w Kliencie

Moduł **Toggle Sprint** (`ToggleSprintModule`, identyfikator: `toggle_sprint`, kategoria: `Category.PVP`) umożliwia automatyczny lub przełączany sprint bez konieczności ciągłego przytrzymywania fizycznego klawisza biegu (np. `LCTRL`).
Moduł integruje się bezpośrednio z silnikiem gry na 4 modloaderach (Fabric, Quilt, Forge, NeoForge) i w pełnym stopniu respektuje mechaniki gry Vanilla Minecraft.

Główna klasa modułu znajduje się w:
[ToggleSprintModule.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/pvp/ToggleSprintModule.java)

---

## 2. Tryby Działania

Moduł obsługuje dwa podstawowe tryby aktywacji kontrolowane przez ustawienie `activation_mode` (`SprintActivationMode`):

1. **`TOGGLE` (Domyślny):**
   - Jednokrotne wciśnięcie przypisanego klawisza (domyślnie `LEFT ALT`, GLFW keycode `342`) włącza stan przełączony (`sprintToggled = true`).
   - Kolejne wciśnięcie klawisza wyłącza stan (`sprintToggled = false`).
   - Sprint jest aktywny dopóki gracz porusza się w przód i spełnione są warunki gry.

2. **`HOLD`:**
   - Działa jak konfigurowalny klawisz biegu — gracz sprintuje tylko w momencie trzymania klawisza.
   - Zwolnienie klawisza natychmiast wyłącza sprint.

3. **Opcja `Always Sprint` (`always_sprint`):**
   - Po włączeniu sprint aktywuje się automatycznie przy każdym ruchu w przód, bez konieczności wciskania klawiszy toggle.

---

## 3. Zgodność z Mechaniką Vanilla i Warunki Gry

Moduł nie modyfikuje fizyki gracza na serwerze ani nie wysyła niedozwolonych pakietów. Zgodnie z zasadami Fair Play sprint jest aktywowany wyłącznie gdy spełnione są wszystkie warunki Vanilla Minecraft:

| Warunek | Wymaganie gry | Zachowanie modułu |
| :--- | :--- | :--- |
| **Kierunek ruchu** | `forwardImpulse > 0` | Wymaga parcia w przód; bieg w tył lub bok nie sprintuje. |
| **Głód (Hunger)** | `foodLevel > 6` | Przy głodzie $\le 6$ sprint jest niemożliwy i blokowany. |
| **Ślepota (Blindness)** | Brak efektu | Przy statusie Blindness sprint jest natychmiast anulowany. |
| **Kolizja pozioma** | `!horizontalCollision` | Uderzenie w ścianę lub przeszkodę zatrzymuje sprint. |
| **Kucanie (Sneak)** | `disable_while_sneaking` | Jeśli włączone, kucanie ma wyższy priorytet i wyłącza sprint. |
| **Używanie przedmiotów** | `disable_while_using_items` | Konfigurowalne wyłączanie sprintu podczas jedzenia lub naciągania łuku. |

---

## 4. Wskaźnik HUD

Moduł implementuje interfejs `IHudElement` i renderuje status na ekranie za pośrednictwem wspólnego `IHudRenderer`.

### Style wskaźnika (`indicator_style`):
1. **`BRACKETED` (Domyślny):** np. `[Sprint (Toggled)]`, `[Sprint (Key Held)]`, `[Sprint (Always)]`
2. **`COMPACT`:** np. `Sprint`, `Sprint (Off)`
3. **`DETAILED`:** np. `Sprint: Toggled`, `Sprint: Disabled`
4. **`TEXT_ONLY`:** np. `SPRINT`

### Zakotwiczenie i kolorystyka:
- Zakotwiczenie: `HudAnchor` (domyślnie `TOP_LEFT`).
- Domyślny offset: $X = 10$, $Y = 100$.
- Kolor aktywny: zielony (`#55FF55`, `0xFF55FF55`).
- Kolor nieaktywny: szary (`#AAAAAA`, `0xFFAAAAAA`).
- Półprzezroczyste tło ze współczynnikiem opacity (domyślnie $0.6$).

---

## 5. Bezpieczeństwo i Auto-Reset

W celu zapobiegania niekontrolowanemu sprintowi po zakończeniu rozgrywki, moduł posiada automatyczne zabezpieczenia:
- **Śmierć gracza (`onDeathOrRespawn`):** Natychmiastowe zerowanie stanu sprintu.
- **Zmiana świata / rozłączenie (`onWorldChange`):** Pełny reset kontrolera ruchu.
- **Otwarcie GUI / Menu (`onFocusLost`):** Zwolnienie klawiszy przytrzymania w menu i czacie.
- **Wyłączenie modułu (`onDisable`):** Bezpieczne usunięcie wskaźnika z `HudElementRegistry` i reset stanu.
