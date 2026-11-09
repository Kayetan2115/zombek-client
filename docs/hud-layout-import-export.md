# ZombekClient — Layout Import / Export System

## 1. Wprowadzenie

Moduł **HUD Layout Import / Export** (`HudLayoutImportExportManager`) zapewnia możliwość bezpiecznego dzielenia się konfiguracjami interfejsu HUD pomiędzy graczami, tworzenia kopii archiwalnych na zewnętrznych nośnikach oraz przenoszenia układów pomiędzy instancjami klienta.

---

## 2. Standard Formatu Wymiany (JSON Schema v1)

Struktura pliku eksportu (`.json`) posiada wersjonowany nagłówek zgodny z oficjalnym schematem ZombekClient:

```json
{
  "$schema": "https://zombekclient.pl/schemas/hud-layout.v1.json",
  "schemaVersion": 1,
  "layoutId": "shared_pvp_layout",
  "name": "Moja Konfiguracja PvP",
  "exportedAt": "2026-11-08T15:30:00Z",
  "clientVersion": "0.1.0-alpha",
  "layers": [ ... ],
  "groups": [ ... ],
  "components": {
    "fps": {
      "posX": 10,
      "posY": 10,
      "width": 60,
      "height": 20,
      "scale": 1.0,
      "anchor": "TOP_LEFT",
      "visible": true,
      "zIndex": 200,
      "layerId": "information"
    }
  }
}
```

---

## 3. Bezpieczeństwo i Sanityzacja Danych (Data Sanitization)

Pliki układów HUD mogą być pobierane z nieznanych źródeł (np. Discord, fora społecznościowe). Aby zagwarantować pełne bezpieczeństwo graczy, moduł realizuje rygorystyczne zabezpieczenia:

1. **Brak danych wrażliwych (Zero-Leak Policy):**
   - Eksportowany JSON zawiera **wyłącznie** właściwości interfejsu graficznego (`HudComponentState`, `HudLayer`, `HudGroup`).
   - Podczas generowania pliku proces weryfikuje brak tokenów autoryzacyjnych, adresów serwerów, loginów, kluczy API, haseł ani konfiguracji modułów rozgrywkowych.
2. **Limit rozmiaru pliku (5 MB Guard):**
   - Parser odrzuca pliki o wielkości przekraczającej `5 242 880 bajtów` (5 MB), zabezpieczając przed atakami typu Memory Bomb / DoS.
3. **Blokada Path Traversal (`assertSafePath`):**
   - Ścieżki zapisu i odczytu są weryfikowane pod kątem obecności sekwencji `..`, znaków specjalnych oraz prób zapisu poza dozwolonym katalogiem konfiguracji `.minecraft/config/zombekclient/`.
4. **Weryfikacja wersji schematu:**
   - Próba zaimportowania pliku z `schemaVersion > 1` skutkuje bezpiecznym komunikatem o konieczności zaktualizowania klienta ZombekClient do nowszej wersji.

---

## 4. Tryby Importu: Replace vs Merge

Przed zaimportowaniem gracz ma do wyboru dwa tryby:

| Tryb Importu | Działanie |
|---|---|
| **Zastąp (Replace)** | Całkowicie czyści obecny układ (wszystkie komponenty są ukrywane), po czym aplikowane są definicje z importowanego pliku. Gwarantuje idealne odwzorowanie układu autora. |
| **Połącz (Merge)** | Nadpisuje tylko te komponenty, które znajdują się w pliku importu. Pozostałe aktywne komponenty gracza zachowują swoje pozycje i widoczność. |

---

## 5. Raport z Importu (`ImportResult`) i Kopia Bezpieczeństwa

Każda operacja importu zwraca szczegółowy obiekt `ImportResult`:
- `isSuccess()` — czy import się powiódł.
- `getImportedCount()` — liczba zaimportowanych komponentów.
- `getUnknownComponents()` — lista komponentów obecnych w pliku, których brak w bieżącej instalacji gry (np. elementy z modów, których gracz nie zainstalował).
- `getMessage()` — czytelny komunikat statusu.

**Automatyczna kopia zapasowa:** Przed dokonaniem jakichkolwiek modyfikacji docelowego profilu, system w tle tworzy plik `.bak` obecnego stanu. W przypadku wystąpienia błędu I/O w trakcie zapisu, stan wyjściowy jest natychmiast przywracany.
