# System Siatki Pomocniczej (Grid System) — ZombekClient (Etap 9B)

## 1. Wprowadzenie i Cel Modułu

System siatki pomocniczej (`HudGridManager`) zapewnia graczom wizualne odniesienie przestrzenne podczas projektowania układu interfejsu w edytorze HUD.
Umożliwia równomierne rozmieszczanie modułów i tworzenie spójnych kompozycji wizualnych.

---

## 2. Style Siatki (Grid Styles)

W edytorze zaimplementowano 5 stylów prezentacji siatki:

| Styl | Wartość Enum | Opis |
| :--- | :--- | :--- |
| **None** | `GridStyle.NONE` | Siatka całkowicie ukryta. |
| **Dots** | `GridStyle.DOTS` | Dyskretne punkty na skrzyżowaniach linii siatki (styl znany z Figma/Canva). |
| **Lines** | `GridStyle.LINES` | Ciągłe, półprzezroczyste linie pionowe i poziome. |
| **Crosshair Grid** | `GridStyle.CROSSHAIR_GRID` | Miniaturowe krzyżyki w punktach węzłowych. |
| **Custom** | `GridStyle.CUSTOM` | Użytkownik definiuje własny interwał pikseli. |

---

## 3. Parametry Konfiguracyjne i Presety

Menedżer siatki przechowuje parametry w stanie edytora (`HudEditorState`):
- **Rozmiar komórki (Grid Spacing):** Presety 2, 4, 8, 16, 24, 32 px. Domyślnie: **16 px**.
- **Przezroczystość (Opacity):** Zakres od `0.05` do `1.00`. Domyślnie: **0.15** (subtelne tło nieprzesłaniające komponentów).
- **Grubość linii (Thickness):** 1 px (domyślnie), skalowalne w zależności od preferencji.
- **Kolor podstawowy:** Domyślnie jasnoszary/biały `0x30FFFFFF` z uwzględnieniem kanału alfa.

---

## 4. Niezależność od Skali GUI i Wydajność

1. **Wirtualne Współrzędne:** Siatka jest renderowana w przestrzeni wirtualnego płótna (`HudCanvasViewport`), dzięki czemu jej rozmiar jest stały względem elementów gry bez względu na to, czy użytkownik ma ustawione GUI Scale `Auto`, `Normal` czy `Large`.
2. **Optymalizacja Frustum Culling:** Pętla renderująca oblicza granice widocznego fragmentu ekranu i rysuje tylko linie/punkty mieszczące się w aktualnym widoku po uwzględnieniu powiększenia (*Zoom*) i przesunięcia (*Pan*).
