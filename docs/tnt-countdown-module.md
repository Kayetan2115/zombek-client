# ZombekClient — Moduł TNT Countdown (Etap 8A6)

## 1. Wprowadzenie i Cel Modułu

Moduł **TNT Countdown** (`tnt_countdown`) w kategorii `PVP` odpowiada za wyświetlanie nad aktywnymi encjami zapalonego TNT (`Primed TNT`) czytelnego, zorientowanego ku kamerze (billboard) odliczania czasu pozostałego do wybuchu.

Przykład wyświetlanego tekstu:
- `TNT 3.8s` (format `PREFIX_AND_TIME`)
- `3.8s` (format `TIME_ONLY`)

Wskaźnik ten znacząco ułatwia graczom szacowanie dystansu bezpieczeństwa, timing w potyczkach PvP, detonacje armatek TNT oraz mechaniki TNT-jump.

---

## 2. Źródła Danych i Architektura Odliczania

### 2.1. Odczyt z Encji Klienta
W wersji Minecraft 1.20.4 encja aktywnego TNT udostępnia bezpośrednio liczbę ticków zapalnika:
- **Fabric & Quilt (Yarn Mappings)**: `net.minecraft.entity.TntEntity.getFuse()`
- **Forge & NeoForge (Official Mojang Mappings)**: `net.minecraft.world.entity.item.PrimedTnt.getFuse()`

### 2.2. Obliczenia Czasu i Formatowanie (`TntCountdownCalculator`)
- Standardowy tickrate gry wynosi 20 ticków na sekundę:
  $$\text{seconds} = \frac{\max(0, \text{fuseTicks} + \text{offsetTicks})}{20.0}$$
- `fuse_offset_ticks` (`Display Offset in Ticks`) wpływa **wyłącznie na prezentację wizualną** (np. dla serwerów z opóźnieniami sieciowymi lub specyficznym timingiem ticku). Moduł pod żadnym pozorem nie modyfikuje rzeczywistego czasu zapalnika serwera ani pakietów sieciowych.
- Precyzja miejsc po przecinku jest w pełni konfigurowalna (`decimal_precision`: od 1 do 3 miejsc).

---

## 3. Dynamiczne i Statyczne Tryby Kolorystyczne

Moduł oferuje dwa tryby doboru barwy tekstu:
1. **Dynamic Color (`dynamic_color = true`)**:
   - $\text{fuse} > 50\text{ ticks}$ ($> 2.5\text{s}$): Bezpieczny zielony (`0xFF55FF55`)
   - $20\text{ ticks} \le \text{fuse} \le 50\text{ ticks}$ ($1.0\text{s} - 2.5\text{s}$): Ostrzegawczy żółty (`0xFFFFFF55`)
   - $\text{fuse} < 20\text{ ticks}$ ($< 1.0\text{s}$): Krytyczny czerwony (`0xFFFF5555`)
2. **Static Color (`dynamic_color = false`)**:
   - Kolor zdefiniowany przez gracza w parametrze `static_color`.

---

## 4. Renderowanie w Przestrzeni Świata (Billboard Text)

Etykieta jest rysowana w przestrzeni 3D świata gry w fazie `AFTER_ENTITIES`:
1. **Translacja**: Pozycja nad środkiem TNT z uwzględnieniem `vertical_offset` i `horizontal_offset`.
2. **Rotacja ku kamerze**: Macierz zostaje przemnożona przez obrót kamery (`camera.getRotation()` / `camera.rotation()`), gwarantując, że tekst jest zawsze czytelny i skierowany prostopadle do wzroku gracza.
3. **Skalowanie perspektywiczne**: Zastosowanie współczynnika `matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale)` zachowuje optymalną czytelność z różnych odległości.
4. **Warstwa cieniowania**: Zastosowanie `TextLayerType.SEE_THROUGH` (lub `Font.DisplayMode.SEE_THROUGH`) z półprzezroczystym tłem zapewnia kontrast nawet na jasnym tle nieba czy piasku.

---

## 5. Wydajność i Buforowanie (`TntTracker`)

- `TntTracker` zarządza pamięcią podręczną aktywnych encji TNT (`ConcurrentHashMap<Integer, TrackedEntry>`).
- Automatyczne usuwanie encji (`pruneStale(500L)`): jeżeli encja eksploduje lub opuści pole widzenia, jej wpis jest natychmiast czyszczony, co zapobiega wyciekom pamięci.
- Filtrowanie po dystansie (`max_distance`, domyślnie 48 bloków) eliminuje zbędne przetwarzanie odległych encji.

---

## 6. Struktura Ustawień Modułu

| Grupa | Klucz | Nazwa | Typ | Zakres / Wartość |
| :--- | :--- | :--- | :--- | :--- |
| **General** | `show_countdown` | Show TNT Countdown | Boolean | `true` |
| | `show_prefix` | Show Prefix | Boolean | `true` |
| | `show_decimal_places` | Show Decimal Places | Boolean | `true` |
| | `decimal_precision` | Decimal Precision | Number | $1.0$ ($1 - 3$) |
| | `display_format` | Display Format | Enum | `PREFIX_AND_TIME`, `TIME_ONLY` |
| | `fuse_offset_ticks` | Display Offset in Ticks | Number | $0.0$ ($-20 - 20$) |
| **Appearance** | `dynamic_color` | Dynamic Color | Boolean | `true` |
| | `static_color` | Static Countdown Color | Color | `0xFFFFFFFF` |
| | `prefix_color` | Prefix Color | Color | `0xFFFF5555` |
| | `text_color` | Text Color | Color | `0xFFFFFFFF` |
| | `background` | Background | Boolean | `true` |
| | `text_shadow` | Text Shadow | Boolean | `true` |
| | `background_opacity` | Background Opacity | Number | $60.0\%$ ($0 - 100$) |
| | `scale` | Scale | Number | $1.0$ ($0.5 - 3.0$) |
| | `text_size` | Text Size | Number | $1.0$ ($0.5 - 2.0$) |
| **Visibility** | `max_distance` | Maximum Render Distance | Number | $48.0\text{ b}$ ($4 - 128$) |
| | `hide_behind_blocks` | Hide Behind Blocks | Boolean | `false` |
| | `only_visible` | Only Show Visible TNT | Boolean | `true` |
| | `hide_in_gui` | Hide When GUI Open | Boolean | `false` |
| **Position** | `vertical_offset` | Vertical Offset | Number | $0.75\text{ b}$ ($0.0 - 3.0$) |
| | `horizontal_offset` | Horizontal Offset | Number | $0.0\text{ b}$ ($-2.0 - 2.0$) |
| | `custom_scale` | Custom Scale | Number | $1.0$ ($0.5 - 2.0$) |
