# ZombekClient — Freelook Module

## 1. Wprowadzenie i Przeznaczenie

Moduł **Freelook** (`FreelookModule`, ID: `freelook`) w kategorii `Category.VISUAL` umożliwia graczowi swobodne obracanie kamery (360° rozglądanie się) bez zmiany fizycznego kierunku poruszania się oraz rotacji ciała (`yaw`/`pitch`) postaci na serwerze gry. Rozwiązanie to jest w 100% bezpieczne dla rozgrywki multiplayer:
- Model fizyczny postaci kontynuuje ruch w kierunku wyznaczonym przez sterowanie (WASD) i prawdziwy kąt ciała.
- Żadne pakiety rotacji wysyłane do serwera nie ulegają sfałszowaniu.
- Zasięg interakcji, raycasting ataków i hitboxy nie ulegają modyfikacji podczas aktywnego freelooka.

---

## 2. Architektura i Separacja Kamery

### 2.1 Singleton `FreelookCameraController`
Zarządzanie stanem kamery odbywa się poprzez wątkowo bezpieczny kontroler `FreelookCameraController`:
- Przechowuje stan aktywacji (`active`), kąt początkowy ciała (`originalYaw`, `originalPitch`), bieżące kąty kamery (`cameraYaw`, `cameraPitch`) oraz stan animacji powrotu (`snapbackInProgress`, `snapbackStartTime`, `snapbackDurationMs`).
- Metoda `handleMouseInput(deltaYaw, deltaPitch, ...)` modyfikuje wyłącznie kąty kamery, uwzględniając czułość osi X i Y, inwersję oraz opcjonalne limity kątowe (`maxPitch`, `maxYaw`).
- Metoda `updateSnapback(...)` realizuje płynną interpolację kamery z powrotem do rotacji gracza przy użyciu wybranego algorytmu.

### 2.2 Integracja z Platformami (Multi-loader)
1. **Fabric & Quilt**: Wstrzyknięcie przez Mixin `CameraMixin` na końcu metody `Camera.update(...)` (`@At("TAIL")`). Gdy `controller.isControllingCamera()` zwraca `true`, kamera przyjmuje kąty `controller.getCameraYaw()` i `controller.getCameraPitch()`. W handlerze wejścia (`FabricInputHandler` / `QuiltInputHandler`) mysz jest przekazywana do kontrolera, a rotacja gracza (`player.setYaw()`, `player.headYaw`, `player.bodyYaw`) jest blokowana na oryginalnych wartościach.
2. **Forge & NeoForge**: Subskrypcja zdarzenia `ViewportEvent.ComputeCameraAngles` na Event Busie klienta. Gdy `controller.isControllingCamera()` jest aktywne, `event.setYaw()` i `event.setPitch()` bezpośrednio nadpisują kąt rzutowania widoku bez konieczności modyfikacji bajtkodu gry.

---

## 3. Konfiguracja i Ustawienia

| Ustawienie | Klucz / ID | Typ | Wartość domyślna | Zakres / Opcje | Opis |
|---|---|---|---|---|---|
| **Activation Mode** | `activation_mode` | Enum | `HOLD` | `HOLD`, `TOGGLE` | Tryb aktywacji: przytrzymanie klawisza lub przełącznik. |
| **Perspective** | `perspective` | Enum | `THIRD_PERSON_BACK` | `FIRST_PERSON`, `THIRD_PERSON_BACK`, `THIRD_PERSON_FRONT` | Perspektywa widoku w trakcie aktywnego freelooka. |
| **Invert Pitch** | `invert_pitch` | Boolean | `false` | `true`, `false` | Odwrócenie osi pionowej (Y). |
| **Invert Yaw** | `invert_yaw` | Boolean | `false` | `true`, `false` | Odwrócenie osi poziomej (X). |
| **H Sensitivity** | `h_sensitivity` | Number | `1.0` | `0.1` – `5.0` | Mnożnik czułości poziomej myszy. |
| **V Sensitivity** | `v_sensitivity` | Number | `1.0` | `0.1` – `5.0` | Mnożnik czułości pionowej myszy. |
| **Snapback** | `snapback` | Boolean | `true` | `true`, `false` | Płynny powrót kamery po zwolnieniu klawisza. |
| **Snapback Duration**| `snapback_duration` | Number | `150` | `0` – `1000` ms | Czas trwania animacji powrotu kamery. |
| **Interpolation** | `interpolation` | Enum | `SMOOTH_STEP` | `LINEAR`, `EASE_OUT`, `SMOOTH_STEP` | Funkcja przejścia interpolacji kątów kamery. |
| **Clamp Pitch** | `clamp_pitch` | Boolean | `true` | `true`, `false` | Ograniczenie kąta patrzenia w górę/dół do ±90°. |
| **Clamp Yaw** | `clamp_yaw` | Boolean | `false` | `true`, `false` | Ograniczenie kąta obrotu wokół własnej osi. |
| **Max Pitch Angle** | `max_pitch_angle` | Number | `90.0` | `10.0` – `90.0`° | Maksymalne wychylenie osi pionowej. |
| **Max Yaw Angle** | `max_yaw_angle` | Number | `180.0` | `10.0` – `180.0`° | Maksymalne wychylenie osi poziomej (jeśli `clamp_yaw` = true). |
| **Reset On World Change**| `reset_on_world_change`| Boolean | `true` | `true`, `false` | Resetowanie stanu przy zmianie świata lub odłączeniu. |
| **Reset On Menu** | `reset_on_menu` | Boolean | `true` | `true`, `false` | Resetowanie stanu przy otwarciu GUI (np. menu, ekwipunek). |
| **Reset On Focus Lost**| `reset_on_focus_lost` | Boolean | `true` | `true`, `false` | Bezpieczne wyłączenie freelooka przy utracie fokusu okna. |

---

## 4. Bezpieczeństwo i Kompatybilność

1. **Anti-Cheat Safe**: Żadne pakiety ruchu gracza (`PlayerMoveC2SPacket`) ani rotacji nie są modyfikowane. Serwer widzi ruch zgodny z mechaniką gry.
2. **Auto-Reset**: Moduł reaguje natychmiast na zdarzenia:
   - Utrata fokusu okna GLFW (`isWindowFocused() == false`).
   - Otwarcie jakiegokolwiek ekranu GUI (`currentScreen != null`).
   - Śmierć gracza lub respawn.
   - Zmiana wymiaru bądź odłączenie z serwera.
3. **Domyślny skrót**: Klawisz `V` (możliwy do zmiany w konfiguracji modułu lub menu sterowania Minecraft).
