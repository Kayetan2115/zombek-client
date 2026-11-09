# Moduł Hit Color (Kolor Otrzymania Obrażeń) — ZombekClient

## 1. Przegląd i Cel

Moduł **Hit Color** (`hit-color`) modyfikuje wizualny efekt zabarwienia modeli bytów w momencie otrzymania obrażeń. W standardowym Minecraftcie byty zabarwiają się na stały czerwony odcień. ZombekClient umożliwia pełną personalizację koloru, czasu trwania, krzywej wygaszania (fade curve), intensywności oraz filtrowanie bytów.

Moduł działa **w 100% po stronie klienta (Client-Side Only)** — nie modyfikuje pakietów sieciowych, obrażeń, mechanik walki, zasięgu ataku ani żadnych wartości po stronie serwera.

---

## 2. Architektura i Komponenty

- **[`HitColorModule`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/HitColorModule.java)**: Moduł wizualny rejestrujący 5 grup ustawień (`general`, `colors`, `timing`, `appearance`, `advanced`).
- **[`HitColorTracker`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/tracking/HitColorTracker.java)**: Wątkowo bezpieczny menedżer śledzący aktywne trafienia bytów w czasie rzeczywistym. Przechowuje mapę identyfikatorów bytów do obiektów `HitColorRecord`.
- **[`HitColorRecord`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/tracking/HitColorRecord.java)**: Niemutowalny rekord przechowujący czas trafienia, klasyfikację bytu oraz obliczający stopień wygaszania `getFadeProgress(long currentTime, long durationMs, FadeCurve curve)`.
- **[`FadeCurve`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/FadeCurve.java)**:
  - `LINEAR`: Jednostajny spadek przez cały czas trwania.
  - `EASE_OUT`: Szybki początkowy efekt z łagodnym zanikaniem pod koniec.
  - `EXPONENTIAL`: Gwałtowne zanikanie potęgowe.
- **[`HitColorMode`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/HitColorMode.java)**: `CUSTOM_COLOR`, `DAMAGE_SCALED`, `RAINBOW`.

---

## 3. Wykrywanie Obrażeń i Sygnałów Uderzeń

System wykrywania działa dwutorowo:

1. **Bezpośrednie uderzenie przez lokalnego gracza**:
   - Fabric/Quilt: Zarejestrowany listener `AttackEntityCallback.EVENT`.
   - Forge/NeoForge: Subskrypcja zdarzenia `AttackEntityEvent`.
   - Wywołanie `HitColorTracker.getInstance().recordHit(target.getId(), type, true, true)` z flagą `causedByLocalPlayer = true`.

2. **Ogólne obrażenia bytu (obrażenia środowiskowe, ataki innych graczy/mobów)**:
   - Na ticku klienta (`onClientTick`) we wszystkich adapterach sprawdzana jest właściwość `living.hurtTime == 9`. Wartość 9 oznacza klatkę początkową animacji zranienia w Minecraftcie (gdzie hurtTime zaczyna się od 10 lub 9).
   - Rejestrowany jest wpis z flagą `causedByLocalPlayer = false` oraz informacją czy dany byt jest aktualnie na celowniku gracza (`isTargeted`).

3. **Filtry i Bezpieczeństwo**:
   - `only_targeted`: Efekt wyświetla się wyłącznie na bycie, na który aktualnie patrzy gracz.
   - `only_local_player`: Efekt aktywuje się wyłącznie, gdy uderzenie zadał sam gracz.
   - `filter_players`, `filter_hostiles`, `filter_passives`, `filter_neutrals`, `filter_other`: Selektywne włączanie/wyłączanie efektu dla poszczególnych kategorii bytów.
   - `max_effects`: Limit jednoczesnych efektów (domyślnie 16), zapobiegający narzutowi pamięciowemu przy farmach mobów.

---

## 4. Grupy Ustawień

| Grupa | Klucz | Typ | Opis | Domyślna wartość |
| :--- | :--- | :--- | :--- | :--- |
| **General** | `mode` | Enum (`HitColorMode`) | Tryb wyliczania koloru | `CUSTOM_COLOR` |
| **General** | `only_targeted` | Boolean | Tylko celowany byt | `false` |
| **General** | `only_local_player`| Boolean | Tylko uderzenia lokalnego gracza | `false` |
| **Colors** | `color` | Color | Główny kolor zabarwienia | `#FF0000` (Czerwony) |
| **Colors** | `player_color` | Color | Kolor trafienia gracza | `#FF0055` |
| **Colors** | `hostile_color` | Color | Kolor trafienia moba wrogiego | `#FF2200` |
| **Timing** | `duration` | Number (50 - 1000 ms) | Czas trwania efektu w ms | `300.0` |
| **Timing** | `fade_curve` | Enum (`FadeCurve`) | Krzywa zanikania koloru | `EASE_OUT` |
| **Appearance**| `intensity` | Number (0.1 - 1.0) | Maksymalna przezroczystość koloru | `0.7` |
| **Advanced** | `max_effects` | Number (1 - 64) | Maksymalna liczba śledzonych bytów | `16.0` |
