# Time Persistence System — ZombekClient (Etap 8B3)

## 1. Przeznaczenie i Rola Komponentu

`TimePersistenceManager` odpowiada za bezpieczny, atomowy i bezstratny zapis stanu silnika czasu oraz profili stopera w pliku konfiguracyjnym klienta:
```
config/zombekclient/time_data.json
```

Pliki źródłowe:
- Menadżer trwałości: [TimePersistenceManager.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/TimePersistenceManager.java)
- Testy trwałości: [TimePersistenceTest.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/test/java/pl/zombek/client/core/time/TimePersistenceTest.java)

---

## 2. Format Danych (`time_data.json`)

Struktura pliku JSON (`schemaVersion = 1`):
```json
{
  "schemaVersion": 1,
  "savedAtEpoch": 1798993200000,
  "totalHistoricalPlaytimeSeconds": 48290,
  "sessions": [
    {
      "sessionId": "4a71dc96-6e11-4048-b4b9-1d4f2b1d3101",
      "startTimeEpoch": 1798980000000,
      "endTimeEpoch": 1798987200000,
      "durationSeconds": 7200,
      "singleplayer": true,
      "worldName": "Survival_World_Hardcore"
    }
  ],
  "stopwatchProfiles": {
    "Speedrun": {
      "state": "STOPPED",
      "elapsedNanos": 0
    },
    "Building": {
      "state": "PAUSED",
      "elapsedNanos": 3600000000000
    }
  }
}
```

---

## 3. Zabezpieczenia Przed Utratą i Uszkodzeniem Danych

### 3.1. Atomowy Zapis Plikowy
Zapis nie następuje bezpośrednio do docelowego pliku `time_data.json`. Zamiast tego:
1. Dane serializowane są do pliku tymczasowego:
   `time_data.json.tmp`
2. Po poprawnym zamknięciu strumienia plik jest przenoszony atomowo przy użyciu:
   `Files.move(tempFile, dataFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)`
3. W przypadku nagłego zaniku zasilania lub crashu gry, docelowy plik nigdy nie pozostanie w stanie obciętym ani częściowo zapisanym.

### 3.2. Automatyczna Kopia Zapasowa Uszkodzonego Pliku (`.bak`)
Jeśli plik `time_data.json` ulegnie uszkodzeniu (np. z powodu awarii dysku użytkownika lub błędnej ręcznej edycji):
1. `TimePersistenceManager` wychwytuje wyjątek parsowania JSON.
2. Zmienia nazwę uszkodzonego pliku na:
   `time_data.json.bak`
3. Inicjalizuje stan domyślny, nie przerywając działania gry ani nie rzucając nieobsłużonych wyjątków.

### 3.3. Dławienie Zapisów (Throttling)
Aby zapobiec nadmiernemu obciążeniu dysków SSD/HDD podczas częstych ticków:
- Minimalny interwał okresowego zapisu wynosi 60 sekund (`MIN_SAVE_INTERVAL_MS = 60_000L`).
- Wymuszony, natychmiastowy zapis (`force = true`) następuje wyłącznie przy opuszczeniu świata (`onWorldLeave`) oraz zamknięciu gry (`shutdown`).
