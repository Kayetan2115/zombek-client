# ZombekClient — Responsive Layout System

## 1. Wprowadzenie

Moduł **Responsive Layout System** (`HudResponsiveLayoutManager`) odpowiada za prawidłowe zachowanie interfejsu HUD przy zmianach rozdzielczości ekranu, proporcji obrazu (Aspect Ratio), skali GUI Minecrafta oraz na monitorach ultrawide i formatach klasycznych. Zapewnia, że żaden element nie "ucieka" poza widoczny obszar ekranu ani nie nachodzi na newralgiczne strefy gry (np. czat, hotbar, paski życia).

---

## 2. Obsługiwane Proporcje Ekranu (`HudAspectRatio`)

W edytorze zaimplementowano mechanizm symulacji proporcji z letterboxingiem (czarnymi pasami podglądu):

| Format Proporcji | Stosunek | Zastosowanie |
|---|---|---|
| `RATIO_16_9` | 16:9 | Standardowe monitory 1080p, 1440p, 4K (domyślny). |
| `RATIO_16_10` | 16:10 | Laptopy gamingowe, MacBooki, ekrany 1920x1200, 2560x1600. |
| `RATIO_21_9` | 21:9 | Monitory Ultrawide (3440x1440p). |
| `RATIO_32_9` | 32:9 | Super Ultrawide (np. 5120x1440p). |
| `RATIO_4_3` | 4:3 | Klasyczne monitory retro, tryby turniejowe CS/Minecraft. |
| `CUSTOM` | Dowolny | Niestandardowe okna i skalowanie swobodne. |

Podgląd w edytorze pozwala graczowi przetestować, jak jego układ będzie wyglądał na ekranie laptopa lub monitorze kolegi bez konieczności zmiany ustawień systemu Windows.

---

## 3. Matematyka Kotwiczenia (`HudAnchor`)

Aby komponenty zachowywały logiczne położenie niezależnie od rozdzielczości, pozycjonowanie opiera się na 7 punktach kotwiczenia:
- `TOP_LEFT` — przesunięcie względem lewego górnego rogu.
- `TOP_CENTER` — wyśrodkowanie w poziomie względem góry ekranu.
- `TOP_RIGHT` — przesunięcie od prawej krawędzi do środka.
- `BOTTOM_LEFT` — przesunięcie od lewej krawędzi z dołu (powyżej czatu).
- `BOTTOM_CENTER` — wyśrodkowanie w poziomie z dołu (obok hotbara).
- `BOTTOM_RIGHT` — przesunięcie od prawej krawędzi z dołu.
- `CENTER` — absolutny środek ekranu (celownik/wskaźniki centralne).

Wzór na surową współrzędną $X$ dla kotwicy prawej:
$$X_{\text{raw}} = W_{\text{screen}} - W_{\text{effective}} - \text{offsetX}$$
Dla kotwicy środkowej:
$$X_{\text{raw}} = \frac{W_{\text{screen}} - W_{\text{effective}}}{2} + \text{offsetX}$$

---

## 4. Walidacja Układu i Ostrzeżenia (`LayoutValidationWarning`)

Przed zapisem układu lub przy przełączeniu symulacji proporcji menedżer uruchamia automatyczną diagnostykę `validateLayout()`:

```java
public class LayoutValidationWarning {
    public enum WarningType {
        OUT_OF_BOUNDS,         // Komponent wykracza poza widoczny ekran
        SAFE_AREA_VIOLATION,   // Komponent znajduje się w strefie marginesu (8px od krawędzi)
        OVERLAP,               // Dwa widoczne komponenty nakładają się na tym samym Z-Index
        MISSING_COMPONENT,     // Wskazany komponent nie jest zainstalowany w grze
        INVALID_SCALE          // Skala poza zalecanym zakresem (0.25x - 3.00x)
    }
}
```

Ostrzeżenia wyświetlane są w dolnym pasku statusu `HudEditorScreen` z żółtą (Warning) lub czerwoną (Error) ikoną oraz podpowiedzią, który komponent wymaga korekty.
