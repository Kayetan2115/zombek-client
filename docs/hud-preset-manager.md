# ZombekClient — HUD Preset Manager

## 1. Wprowadzenie

Moduł **HUD Preset Manager** (`HudPresetManager`) dostarcza zestaw gotowych, wzorcowych szablonów rozmieszczenia elementów na ekranie. W przeciwieństwie do profili (będących dynamicznymi przestrzeniami roboczymi zapisanymi na dysku użytkownika), **presety** stanowią niezmienne lub szablonowe schematy pozycjonowania i skalowania, które użytkownik może w dowolnym momencie zaaplikować na aktywny układ z pełną obsługą historii **Undo / Redo**.

---

## 2. Architektura Presetu

Struktura pojedynczego presetu (`HudPreset`) zawiera:
- `id` — unikalny identyfikator presetu (np. `pvp`, `streamer`).
- `name` — czytelna nazwa wyświetlana w interfejsie.
- `description` — opis zastosowania i rozmieszczenia.
- `author` — autor konfiguracji (np. `ZombekClient Team` lub nick gracza).
- `builtIn` — flaga określająca, czy preset jest wbudowany, czy stworzony przez użytkownika.
- `components` — mapa identyfikatorów komponentów do definicji ich położeń, kotwic, skal i widoczności.

---

## 3. Katalog Wbudowanych Presetów (8 System Presets)

Zgodnie z wymaganiami technicznymi Etapu 9C, wbudowano 8 szablonów bazowych:

| ID Presetu | Nazwa Wyświetlana | Przeznaczenie i Kluczowe Elementy |
|---|---|---|
| `default` | Domyślny | Zrównoważony układ: Keystrokes, CPS, FPS, Ping, Armor Status na obrzeżach ekranu. |
| `minimal` | Minimalistyczny | Wyczyszczony ekran: tylko małe FPS i Ping w lewym górnym rogu. Maksymalna widoczność świata. |
| `pvp` | PvP Pro | Wyeksponowane Keystrokes, CPS, Armor Status, Potion Effects, Ping, zoptymalizowane pod walkę wręcz. |
| `survival` | Survival / Przygoda | Coordinates, Direction HUD, Day Counter, Saturation, Biome, Clock, Inventory wskaźniki. |
| `uhc` | Ultra Hardcore | Rozbudowane Potion Effects, Armor Status, Saturation, Net Graph, dokładne koordynaty XYZ. |
| `building` | Budowanie / Architekt | Koordynaty XYZ, kąty patrzenia yaw/pitch, Direction HUD, ukryte elementy bojowe. |
| `performance` | Wydajność / Diagnostyka | FPS, Ping, Memory Usage, Net Graph, kompaktowe wymiary, odciążenie ekranu. |
| `streamer` | Streamer Safe | Elementy przesunięte z dala od typowych nakładek OBS/kamery (lewy dolny róg), brak wrażliwych danych. |

---

## 4. Mechanizm Aplikowania Presetu (`applyPreset`)

Aplikowanie presetu realizowane jest przez metodę:
```java
PresetApplyResult applyPreset(String presetId, HudLayout targetLayout)
```

### Przebieg operacji:
1. Pobranie definicji presetu z rejestru.
2. Weryfikacja dostępności komponentów w `HudComponentRegistry`.
3. Rejestracja stanu sprzed aplikacji w menedżerze historii jako pojedyncze polecenie `ApplyPresetCommand`.
4. Zaktualizowanie koordynatów `(posX, posY)`, kotwicy `anchor`, skali `scale`, z-indeksu `zIndex` oraz stanu widoczności `visible` dla każdego komponentu wskazanego w presecie.
5. Pozostawienie bez zmian komponentów zainstalowanych z zewnętrznych dodatków, jeśli preset ich nie definiuje.
6. Zwrócenie obiektu `PresetApplyResult` z listą zmodyfikowanych komponentów, listą nieznanych oraz statusem sukcesu.

---

## 5. Własne Presety Użytkownika

Gracz ma możliwość wyeksportowania swojego aktualnego układu jako własny szablon:
- Zapis do pliku JSON w katalogu `.minecraft/config/zombekclient/hud/presets/<presetId>.json`.
- Możliwość edycji nazwy, opisu i autora.
- Możliwość usunięcia własnego presetu bez wpływu na presety systemowe.
