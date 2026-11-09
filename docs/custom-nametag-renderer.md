# Custom NameTag Renderer

## 1. Przegląd

`CustomNameTagRenderer` (`pl.zombek.client.core.modules.visual.nametags.CustomNameTagRenderer`) to centralny silnik formatowania oraz podglądu 2D i 3D etykiet graczy i encji.

## 2. Model Zawartości (`NameTagFormattedContent`)

Formatowanie zwraca strukturę `NameTagFormattedContent` zawierającą:
- `headerLine`: opcjonalna linia górna (np. wskaźniki pancerza lub hełmu).
- `mainLine`: główna linia zawierająca ikonę przyjaciela, lokalny prefiks, nick lub alias, sufiks, wskaźnik zdrowia, dystans oraz ping.
- `subLine`: linia dolna prezentująca nazwę drużyny lub klanu.
- `mainTextColor`: kolor ARGB tekstu bazujący na drużynie lub lokalnym kolorze znajomego.
- `healthColor`: kolor ARGB wskaźnika punktów życia (zielony, żółty, czerwony).
- `isFriend`: flaga określająca status znajomego.

## 3. Style Prezentacji Zdrowia (`HealthDisplayStyle`)

1. **`NUMERIC`**: Wartość liczbowa punktów życia z opcjonalną absorpcją (np. `18.0 HP` lub `20.0+4.0 HP`).
2. **`HEARTS`**: Symboliczne serduszka (np. `❤❤❤❤❤`).
3. **`BAR`**: Pasek postępu znakowego (np. `[||||||....]`).
4. **`COMPACT`**: Zwięzła wartość liczbowa bez przyrostka `HP`.
5. **`CUSTOM`**: Format ułamkowy `aktualne / maksymalne` (np. `18.0 / 20.0`).

## 4. Formaty Odległości (`DistanceFormat`)

- **`BLOCKS`**: Zaokrąglenie do pełnych bloków (np. `24m`).
- **`DECIMAL`**: Wartość z jednym miejscem po przecinku (np. `24.5m`).
- **`ROUNDED`**: Zaokrąglenie do wielokrotności 5 metrów (np. `25m`).

## 5. Podgląd w Menu Ustawień (`renderPreview`)

Podczas edycji ustawień w menu Right Shift moduł implementuje interfejs `IHudElement` i renderuje realistyczny widok podglądu przykładowego NameTaga (`[PREVIEW] [VIP] ZombekPlayer 20.0 HP [15m] <Champions>`) z zachowaniem aktualnie wybranych kolorów, przezroczystości tła i skali.
