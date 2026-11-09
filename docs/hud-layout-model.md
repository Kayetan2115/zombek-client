# Model Układu HUD (`HudLayout`) i Serializacja — ZombekClient

## 1. Struktura Danych `HudLayout`

Model [`HudLayout`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/editor/HudLayout.java) reprezentuje kompletny profil rozmieszczenia elementów interfejsu.

```json
{
  "schemaVersion": 1,
  "layoutId": "default",
  "name": "Domyślny Układ",
  "refWidth": 960,
  "refHeight": 540,
  "refGuiScale": 2,
  "createdAt": 1794145000000,
  "updatedAt": 1794145200000,
  "components": {
    "fps": {
      "componentId": "fps",
      "enabled": true,
      "visible": true,
      "posX": 10,
      "posY": 44,
      "width": 65,
      "height": 18,
      "scale": 1.0,
      "anchor": "TOP_LEFT",
      "zIndex": 0,
      "customProps": {}
    },
    "coordinates": {
      "componentId": "coordinates",
      "enabled": true,
      "visible": true,
      "posX": 10,
      "posY": 10,
      "width": 125,
      "height": 30,
      "scale": 1.0,
      "anchor": "TOP_LEFT",
      "zIndex": 0,
      "customProps": {}
    }
  }
}
```

---

## 2. Pola Składowe `HudComponentState`

| Pole | Typ | Opis |
|---|:---:|---|
| `componentId` | `String` | Unikalny identyfikator odpowiadający deskryptorowi w rejestrze. |
| `enabled` | `boolean` | Czy moduł źródłowy jest aktywny w ModuleManagerze. |
| `visible` | `boolean` | Czy element ma być rysowany na ekranie HUD w grze. |
| `posX` | `int` | Poziome przesunięcie względem wybranego punktu zakotwiczenia. |
| `posY` | `int` | Pionowe przesunięcie względem wybranego punktu zakotwiczenia. |
| `width` | `int` | Szerokość bazowa w koordynatach GUI Minecrafta. |
| `height` | `int` | Wysokość bazowa w koordynatach GUI Minecrafta. |
| `scale` | `double` | Mnożnik wielkości elementu (zakres 0.2x – 3.0x). |
| `anchor` | `HudAnchor` | Narożnik lub krawędź ekranu (`TOP_LEFT`, `TOP_CENTER`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_CENTER`, `BOTTOM_RIGHT`, `CENTER`). |
| `zIndex` | `int` | Poziom warstwy renderowania. |
| `locked` | `boolean` | Blokada elementu przed przypadkowym przesunięciem i zmianą rozmiaru myszą. |
| `lockAspectRatio` | `boolean` | Blokada proporcji szerokości do wysokości podczas operacji Resize. |
| `autoSize` | `boolean` | Automatyczne dopasowywanie rozmiaru do renderowanej zawartości (odpinane przy ręcznym Resize). |
| `editorVisible` | `boolean` | Widoczność podglądu komponentu na płótnie edytora (niezależna od widoczności w grze). |
| `positionMode` | `HudPositionMode` | Tryb interpretacji koordynatów (`ABSOLUTE_PIXEL`, `RELATIVE`, `NORMALIZED`). |
| `layerId` | `String` | Identyfikator warstwy logicznej (`background`, `main_hud`, `information`, `notifications` lub własna). |
| `groupId` | `String` | Opcjonalny identyfikator grupy logicznej (`HudGroup`), do której przypisany jest komponent. |
| `responsiveRule` | `HudResponsiveRule` | Reguła responsywnego zachowania na małych ekranach (`AUTO_SCALE`, `HIDE`, `MAINTAIN`). |
| `customProps`| `Map<String, String>` | Dodatkowe parametry specyficzne dla modułu. |

---

## 3. Kolekcje Warstw i Grup w `HudLayout` (Etap 9C)

Model `HudLayout` w Etapie 9C zawiera dodatkowe mapy zarządzające strukturą zaawansowaną:
- **`layers` (`Map<String, HudLayer>`):** Zestaw warstw logicznych wraz z ich bazami Z-index (`zIndexBase`), flagami blokady i widoczności.
- **`groups` (`Map<String, HudGroup>`):** Zestaw grup logicznych łączących wybrane komponenty wraz ze wspólną obwiednią i kolorem akcentu.

---

## 4. Walidacja i Przywracanie Błędów (`HudLayoutValidator`)

Klasa [`HudLayoutValidator`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/editor/HudLayoutValidator.java) chroni klienta przed awariami w wyniku uszkodzenia pliku lub błędnych wartości:
- Sprawdza minimalne dopuszczalne wymiary (szerokość >= 10, wysokość >= 10).
- Wymusza poprawne granice skali (przycinanie wartości poniżej 0.2 lub powyżej 3.0).
- Zapobiega ujemnym offsetom pozycji (`posX >= 0`, `posY >= 0`).
- Automatycznie uzupełnia brakujące komponenty z `HudComponentRegistry` z ich domyślnymi parametrami.

---

## 4. Trwałość i Bezpieczeństwo Zapisów (`HudLayoutSerializer`)

Klasa [`HudLayoutSerializer`](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/editor/HudLayoutSerializer.java) odpowiada za:
- **Zapis atomowy**: plik zapisywany jest najpierw do tymczasowego pliku `.tmp`, a następnie atomowo podmieniany na `hud_layout.json`.
- **Kopia zapasowa w razie uszkodzenia**: w przypadku wykrycia nieprawidłowej struktury JSON tworzony jest backup `hud_layout.json.corrupt.<timestamp>`, a klient powraca do stabilnych ustawień domyślnych bez przerwania działania gry.
- **Odporność na timestamps**: porównywanie stanu bufora edycji ignoruje różnice w znacznikach czasu `createdAt` / `updatedAt`, zapobiegając fałszywym komunikatom o niezapisanych zmianach.
