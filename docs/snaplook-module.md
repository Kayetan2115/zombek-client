# Moduł Snaplook — ZombekClient

## 1. Przeznaczenie i Rola w Kliencie

Moduł **Snaplook** (`SnaplookModule`, identyfikator: `snaplook`, kategoria: `Category.PVP`) to zaawansowana funkcja kontroli perspektywy kamery, pozwalająca graczowi na błyskawiczne zerknięcie za siebie lub pod określonym kątem po naciśnięciu dedykowanego klawisza (domyślnie klawisz `V`, `GLFW.GLFW_KEY_V`).

Jest to moduł całkowicie niezależny od modułu **Freelook** (który oferuje swobodny obrót głową bez zmiany kierunku ruchu gracza). Snaplook służy do natychmiastowego przełączenia kamery (np. z widoku pierwszej osoby do widoku zza pleców lub z przodu), a po zwolnieniu klawisza przywraca pierwotną perspektywę gracza.

Główna klasa modułu znajduje się w:
[SnaplookModule.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/pvp/SnaplookModule.java)  
Kontroler stanów i adapter perspektywy:
[SnaplookController.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/pvp/SnaplookController.java)  
[ISnaplookPerspectiveAdapter.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/pvp/ISnaplookPerspectiveAdapter.java)

---

## 2. Tryby Aktywacji (Activation Modes)

Parametr `activation_mode` (`SnaplookActivationMode`) pozwala dobrać mechanikę działania przycisku:
1. **`HOLD` (Domyślny):** Kamera zmienia perspektywę tak długo, jak gracz trzyma wciśnięty klawisz `V`. Po puszczeniu następuje natychmiastowy powrót do perspektywy wyjściowej.
2. **`TOGGLE`:** Pierwsze naciśnięcie klawisza przełącza do widoku Snaplook, drugie naciśnięcie przywraca poprzednią perspektywę.
3. **`TAP`:** Naciśnięcie klawisza utrzymuje perspektywę przez zdefiniowany czas (np. 1500 ms), po czym kamera automatycznie powraca do stanu początkowego.

---

## 3. Perspektywa Docelowa (Target Perspectives)

Parametr `target_perspective` (`SnaplookPerspective`) definiuje kąt widzenia kamery po aktywacji:
- **`THIRD_PERSON_BACK` (Domyślny):** Widok z trzeciej osoby z tyłu (klasyczny F5). Pozwala zobaczyć przeciwnika biegnącego za graczem podczas ucieczki w UHC/SpeedUHC/Bedwars.
- **`THIRD_PERSON_FRONT`:** Widok z trzeciej osoby z przodu (twarz gracza). Umożliwia sprawdzenie pancerza, statusu lub nagrywanie estetycznych ujęć.
- **`REVERSE_LOOK`:** Odwrócenie kąta yaw o 180 stopni w widoku trzeciej osoby.

---

## 4. Płynność Kamery i Łagodzenie (Camera Easing)

- **`smooth_camera` (Boolean, domyślnie `true`):** Włącza płynną interpolację przejścia perspektywy zamiast natychmiastowego skoku klatki.
- **`transition_speed` (Double, 0.05 - 1.0, domyślnie 0.25):** Szybkość interpolacji łagodzącej (`SnaplookEasing.LINEAR`, `SnaplookEasing.EASE_OUT_QUAD`, `SnaplookEasing.EASE_OUT_CUBIC`).

---

## 5. Rozwiązywanie Konfliktów i Bezpieczeństwo Stanu

W dynamicznej rozgrywce PvP gracz może równolegle korzystać z klawisza F5 (zmiana perspektywy Vanilla) lub otwierać ekrany menu. Kontroler Snaplook posiada wbudowane mechanizmy zabezpieczające:
1. **`respect_manual_changes` (Boolean, domyślnie `true`):** Jeśli gracz w trakcie aktywnego Snaplooka manualnie wciśnie F5, kontroler natychmiast anuluje stan Snaplook i akceptuje nową manualną perspektywę gracza, zapobiegając desynchronizacji.
2. **Reset przy utracie fokusu okna:** Wyjście z gry (Alt+Tab) lub otwarcie menu pauzy automatycznie przywraca perspektywę bazową.
3. **Reset przy śmierci i zmianie świata:** Przejście przez portal, respawn lub teleportacja anulują stan aktywny.

---

## 6. Architektura Adaptera Multi-Loader

Wszystkie cztery platformy implementują interfejs `ISnaplookPerspectiveAdapter`:
```java
public interface ISnaplookPerspectiveAdapter {
    SnaplookPerspective getCurrentPerspective();
    void setPerspective(SnaplookPerspective perspective);
}
```
- **Fabric & Quilt:** Połączenie z `MinecraftClient.getInstance().options.getPerspective()`.
- **Forge & NeoForge:** Połączenie z `Minecraft.getInstance().options.getCameraType()`.
Dzięki temu cała logika decyzyjna (maszyna stanów w `SnaplookController`) znajduje się w module `:core` i jest pokryta testami jednostkowymi.
