# HUD Layout Priority & Collision Resolution — ZombekClient (Etap 8B2)

## 1. Wprowadzenie i Cel Architektury

Wraz z rosnącą liczbą modułów informacyjnych i interfejsowych w ZombekClient (Scoreboard, Boss Bar, Potion Effects, Saturation, Coordinates, Direction HUD, FPS, Ping, Memory Usage) kluczowym wyzwaniem staje się **zapobieganie kolizjom i nakładaniu się elementów HUD** na ekranie gry.

Klasycznym problemem wielu modyfikacji jest sytuacja, w której:
- Scoreboard nakłada się na Boss Bar lub Paski efektów mikstur w prawym górnym rogu ekranu;
- Moduł Saturation nakłada się na pasek podręczny (Hotbar) lub paski pancerza/zdrowia.

Dla rozwiązania tego problemu powstał wyspecjalizowany komponent [HudPriorityResolver.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/component/HudPriorityResolver.java).

---

## 2. Hierarchia Priorytetów Renderowania (`Render Priority`)

Każdy komponent w systemie HUD (`HudComponentModel`) posiada przypisany numeryczny priorytet renderowania:

| Stała Priorytetu | Wartość | Typowe Elementy | Uzasadnienie |
| :--- | :---: | :--- | :--- |
| `PRIORITY_CRITICAL` | `100` | `BossBarModule` | Kluczowa mechanika walki z bossami, pasek na samej górze ekranu |
| `PRIORITY_HIGH` | `75` | `ScoreboardModule` | Główne informacje o trybie i statystykach meczu |
| `PRIORITY_MEDIUM` | `50` | `PotionEffectsModule` | Dynamiczne statusy z czasem trwania |
| `PRIORITY_STANDARD` | `25` | `SaturationModule`, `CoordinatesModule`, `DirectionHudModule` | Pomocnicze wskaźniki HUD |
| `PRIORITY_LOW` | `0` | Moduły statystyczne, `FpsModule`, `PingModule`, `MemoryUsageModule` | Drugorzędne statystyki techniczne |

---

## 3. Algorytm Rozwiązywania Kolizji

Algorytm `HudPriorityResolver.resolveCollisions(List<HudComponentModel> components, int screenWidth, int screenHeight, int spacing)` działa w następujących krokach:

1. **Filtrowanie:** Wybierane są wyłącznie aktywne i widoczne komponenty (`isVisible() == true`).
2. **Sortowanie:** Komponenty sortowane są malejąco według priorytetu:
   $$\text{priority}(A) > \text{priority}(B) \implies A \text{ zachowuje pozycję pierwotną}$$
3. **Wykrywanie Kolizji Prostokątów (AABB):** Dla każdego kolejnego komponentu sprawdzane jest przecięcie jego obwiedni $[X, Y, W, H]$ z wcześniej umieszczonymi komponentami wyższego priorytetu:
   $$\text{intersects} \iff (x_1 < x_2 + w_2) \land (x_1 + w_1 > x_2) \land (y_1 < y_2 + h_2) \land (y_1 + h_1 > y_2)$$
4. **Korekta Pozycji (Shifting):**
   - Dla elementów zakotwiczonych na dole ekranu (`BOTTOM_LEFT`, `BOTTOM_CENTER`, `BOTTOM_RIGHT`), element o niższym priorytecie jest automatycznie przesuwany **w górę** o odpowiedni offset:
     $$\text{posY}_{\text{new}} = \text{screenHeight} - \text{boxTopY} + \text{spacing}$$
   - Dla elementów zakotwiczonych u góry lub w centrum (`TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`, `CENTER`), element jest automatycznie przesuwany **w dół**:
     $$\text{posY}_{\text{new}} = \text{boxTopY} + \text{boxHeight} + \text{spacing}$$
5. **Ograniczenie do Ekranu (Clamping):** Po zakończeniu przesunięć, `clampToScreen` gwarantuje, że komponent nie wyjdzie poza widoczny obszar okna gry.

---

## 4. Przygotowanie pod Przyszły Wizualny HUD Editor

Wszystkie moduły i ich modele `HudComponentModel` implementują:
- pobieranie dokładnych współrzędnych ekranowych `getScreenX()`, `getScreenY()`, `getEffectiveWidth()`, `getEffectiveHeight()`;
- obsługę 7 punktów zakotwiczenia (`TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_CENTER`, `BOTTOM_RIGHT`, `CENTER`);
- bezpieczną obsługę skalowania GUI ($0.5-2.0$) oraz natychmiastową aktualizację po zmianie rozdzielczości okna gry.
Architektura jest w 100% przygotowana pod przyszły graficzny edytor przeciągania elementów (Drag-and-Drop HUD Editor), bez konieczności jakichkolwiek refaktoryzacji w modułach bazowych.
