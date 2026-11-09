# ZombekClient — Uniwersalny System Ustawień Modułów (Etap 7B)

## 1. Architektura Ustawień (`Setting<T>`)

Klasa bazowa `pl.zombek.client.settings.Setting<T>` definiuje uniwersalny interfejs dla każdego typu parametru konfiguracyjnego w ZombekClient:

- `id`: Unikalny identyfikator parametru w ramach danego modułu.
- `name`: Czytelna nazwa wyświetlana w GUI.
- `description`: Opis przeznaczenia ustawienia lub podpowiedź (tooltip).
- `defaultValue`: Wartość domyślna, do której można zresetować parametr (`reset()`).
- `value`: Aktualnie obowiązująca wartość.
- `visibilityPredicate`: Logika warunkowej widoczności (`BooleanSupplier`), pozwalająca na tworzenie parametrów zależnych.
- `changeListeners`: Lista odbiorców powiadomień o zmianie wartości (np. wyzwalających debounced auto-save).
- `serialize()` / `deserialize(String)`: Serializacja tekstowa do formatu JSON.

---

## 2. Obsługiwane Typy Ustawień

### 2.1. `BooleanSetting`
- **Typ**: `Boolean`
- **Zastosowanie**: Przełączniki typu WŁĄCZ / WYŁĄCZ (np. włączanie cieni, ramki).
- **Metody pomocnicze**: `toggle()` zmienia wartość na przeciwną.

### 2.2. `NumberSetting`
- **Typ**: `Double`
- **Zastosowanie**: Suwaki z precyzyjną kontrolą wartości liczbowych (np. przezroczystość, rozmiar HUD, limit FPS).
- **Właściwości**:
  - `min`, `max`: Sztywne granice (wartości spoza przedziału są automatycznie przycinane w `sanitize()`).
  - `step`: Krok skoku suwaka (kwantyzacja do najbliższego kroku).
  - `precision`: Liczba miejsc po przecinku (np. `0` dla liczb całkowitych).
  - `suffix`: Jednostka wyświetlana w interfejsie (np. `%`, `ms`, `px`).

### 2.3. `ColorSetting`
- **Typ**: `Integer` (ARGB packed dla natywnego renderera Minecraft)
- **Zastosowanie**: Wybór kolorów akcentów, tekstu i tła.
- **Właściwości**:
  - Obsługa kanałów: Czerwony (R), Zielony (G), Niebieski (B), Alfa (A).
  - Format zapisu heksadecymalnego: `#RRGGBB` lub `#RRGGBBAA`.
  - Flaga `supportAlpha`: Decyduje, czy parametr dopuszcza regulację przezroczystości.

### 2.4. `EnumSetting<E extends Enum<E>>`
- **Typ**: Typ wyliczeniowy Java `Enum`
- **Zastosowanie**: Wybór jednego wariantu z zamkniętej listy opcji (np. tryb wyświetlania `COMPACT`, `NORMAL`, `EXPANDED`).
- **Funkcje GUI**: Cykliczne przełączanie (`cycleNext()`, `cyclePrevious()`).

### 2.5. `StringSetting`
- **Typ**: `String`
- **Zastosowanie**: Dowolne dane tekstowe wprowadzane przez użytkownika (np. prefiksy, formaty daty).
- **Zabezpieczenia**:
  - `maxLength`: Limit długości tekstu.
  - `pattern`: Opcjonalny filtr walidacji wyrażeń regularnych (`Pattern`).

### 2.6. `KeybindSetting`
- **Typ**: `Integer` (Kod klawisza GLFW lub przycisku myszy)
- **Zastosowanie**: Skróty klawiszowe modułów.
- **Wartości specjalne**: `0` oraz `GLFW_KEY_UNKNOWN (-1)` oznaczają brak przypisania (`NONE`).
- **Formatowanie**: Czytelne nazwy klawiszy (np. `RSHIFT`, `SPACE`, `F5`, `M1`).

### 2.7. `MultiSelectSetting`
- **Typ**: `Set<String>`
- **Zastosowanie**: Wielokrotny wybór z predefiniowanej listy znaczników/opcji (np. lista flag graficznych `["Ramka", "Cień", "Zaokrąglenie"]`).

### 2.8. `SettingGroup`
- **Zastosowanie**: Logiczne grupowanie powiązanych parametrów pod wspólnym nagłówkiem sekcji w interfejsie.

---

## 3. Warunkowa Widoczność i Zależności (Conditional Visibility)

Ustawienia wspierają łańcuchowe definiowanie warunków logicznych za pomocą metody `.visibleWhen(BooleanSupplier)`:

```java
BooleanSetting masterToggle = registerSetting(new BooleanSetting(
    "enable_custom_glow", "Własna poświata", "Włącza renderowanie poświaty", false
));

NumberSetting glowRadius = registerSetting(new NumberSetting(
    "glow_radius", "Promień poświaty", "Wielkość rozmycia", 5.0, 1.0, 20.0, 1.0
).visibleWhen(masterToggle::getValue));
```

Gdy `masterToggle` ma wartość `false`, suwak `glowRadius` zostaje automatycznie ukryty w panelu ustawień modułu i pominięty podczas kalkulacji wysokości widoku.
