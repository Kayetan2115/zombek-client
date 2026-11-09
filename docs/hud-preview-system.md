# System Podglądu Komponentów HUD (`HudComponentPreviewProvider`)

## 1. Cel i Założenia

Podczas konfiguracji interfejsu użytkownik musi widzieć, jak dany element będzie faktycznie wyglądał podczas rozgrywki.
Wiele elementów w standardowym środowisku (np. na serwerze poczekalni, w menu lub w świecie jednoosobowym) nie posiada bieżących danych:
- `Ping` w singleplayerze nie ma serwera (zwracałby N/A).
- `Bedwars Helper` nie ma aktywnego meczu Bedwars.
- `Net Graph` nie rejestruje ruchu sieciowego.
- `Scoreboard` nie ma załadowanych linii celów.
- `Armor Status` nie wyświetli zbroi, jeśli postać nie założyła ekwipunku.

Dlatego w ZombekClient zastosowano interfejs [`HudComponentPreviewProvider`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/editor/HudComponentPreviewProvider.java):
```java
@FunctionalInterface
public interface HudComponentPreviewProvider {
    void renderPreview(IHudRenderer renderer, HudComponentDescriptor descriptor, int x, int y, int width, int height, double scale);
}
```

Dzięki temu system renderowania podglądu jest w 100% odseparowany od bieżącego stanu gry i sieci.

---

## 2. Przykłady Renderowania Podglądów w ZombekClient

### 2.1. Keystrokes
Renderuje układ klawiszy W, A, S, D, LPM i PPM z eleganckimi ramkami i etykietami.

### 2.2. Ping Display
Renderuje realistyczne opóźnienie `Ping: 24 ms` z zielonym wskaźnikiem jakości.

### 2.3. FPS Display
Renderuje `FPS: 144` z kolorystycznym wyróżnieniem płynności.

### 2.4. Net Graph
Renderuje zminiaturyzowany wykres fali opóźnień sieciowych oraz metryki przepustowości `24ms | 0.8ms | In: 14 KB/s`.

### 2.5. Scoreboard
Renderuje typową tablicę wyników meczu Bedwars ze statusem łóżek drużyn czerwonej, niebieskiej, zielonej i żółtej.

### 2.6. Coordinates
Renderuje `XYZ: 145 / 68 / -312` oraz `Biom: Plains (North)`.

### 2.7. Armor Status
Renderuje ikony elementów pancerza wraz z procentowym stanem wytrzymałości (`⛑ 98%`, `🦺 85%`, `👖 92%`, `🥾 64%`).

---

## 3. Integralność i Bezpieczeństwo
- Dane demo wyświetlane są **wyłącznie** w trakcie działania HUD Editora (`preview mode`).
- Podczas rzeczywistej rozgrywki elementy korzystają z właściwych dostawców danych (`IInfoHudDataProvider`, `INetworkDataProvider`, `ZombekInventoryService` itd.).
- Podgląd nie wysyła zapytań sieciowych ani nie modyfikuje stanu encji w świecie.
