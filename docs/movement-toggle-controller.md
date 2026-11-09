# Kontroler Ruchu (MovementToggleController) — ZombekClient

## 1. Cel i Architektura

Wspólna klasa `MovementToggleController` we wspólnym module `:core` (`pl.zombek.client.core.movement`) stanowi centralny, wielowątkowo bezpieczny (thread-safe singleton) punkt koordynacji wejścia gracza dla modułów `ToggleSprint` i `ToggleSneak`.

Główna klasa kontrolera znajduje się w:
[MovementToggleController.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/movement/MovementToggleController.java)

---

## 2. Odpowiedzialności Kontrolera

1. **Zarządzanie stanami aktywacji:**
   - Śledzenie stanów przełącznika (toggled on/off).
   - Śledzenie stanów fizycznego przytrzymania klawiszy dodatkowych i domyślnych (`physicalSprintDown`, `physicalSneakDown`).
   - Rozróżnianie zboczy wciśnięć (edge detection) bez powielania akcji w kolejnych klatkach.

2. **Ocena warunków środowiskowych (Game Conditions):**
   - Agregacja stanu gracza pobieranego z adapterów platformowych (`FabricInputHandler`, `QuiltInputHandler`, `ForgeInputHandler`, `NeoForgeInputHandler`).
   - Weryfikacja: parcie w przód, głód, ślepota, przeszkody, używanie przedmiotów, latanie, dosiadanie moba.

3. **Fuzja intencji wejścia (Input Merging):**
   - Bezkonfliktowe łączenie intencji gracza: priorytet fizycznego przytrzymania, zachowanie domyślnego klawisza Vanilla (`preserveVanillaSprint`, `preserveVanillaSneak`), obsługa trybu `ALWAYS_SPRINT`.

4. **Wskaźniki stanu i formatowanie tekstu:**
   - Generowanie ustandaryzowanych napisów HUD (`BRACKETED`, `COMPACT`, `DETAILED`, `TEXT_ONLY`) dla modułów graficznych.

5. **Gwarancje bezpieczeństwa (Safety Guards):**
   - Atomowe metody czyszczenia: `resetSprint()`, `resetSneak()`, `resetAll()`.
   - Zdarzeniowe zabezpieczenia: `onPlayerDeath()`, `onPlayerRespawn()`, `onWorldChange()`, `onFocusLost()`, `onMenuOpen()`.

---

## 3. Wzorce Integracji z Loaderami

Każdy adapter platformowy przekazuje w każdym cyklu klatki/ticka zaktualizowane flagi do kontrolera:

```java
MovementToggleController.getInstance().updateGameConditions(
    movingForward, canSprint, canSneak, usingItem, flying, riding
);
MovementToggleController.getInstance().setPhysicalSprintDown(ctrlDown);
MovementToggleController.getInstance().setPhysicalSneakDown(shiftDown);
```

Następnie moduły odpytują kontroler o wynikową decyzję:
```java
boolean sprintActive = controller.isSprintActive(
    enabled, mode, alwaysSprint, preserveVanilla, disableSneaking, disableUsingItems
);
if (sprintActive) {
    player.setSprinting(true);
}
```
