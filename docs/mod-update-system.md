# System Wykrywania i Zarządzania Aktualizacjami Modów

Dokumentacja techniczna silnika wsadowego sprawdzania aktualizacji, zarządzania politykami wydań oraz plikiem stanu `modpack.lock.json` w **ZombekClient**.

---

## 1. Wykrywanie Wsadowe przez Sumy SHA-512

Tradycyjne launchery odpytują API osobno dla każdego zainstalowanego modu, co przy paczce 150 modów powoduje wyczerpanie limitów HTTP (429 Rate Limit) i trwa kilkadziesiąt sekund.

W ZombekClient zastosowano wysoce zoptymalizowane podejście wsadowe oparte o oficjalny endpoint Modrinth:
`POST /version_files/update`

### Potok sprawdzania:
1. Obliczenie sumy SHA-512 dla każdego pliku `.jar` w katalogu `instances/<id>/mods/`.
2. Zbudowanie jednorazowego payloadu JSON:
   ```json
   {
     "hashes": ["a1b2c3...512hash", "d4e5f6...512hash"],
     "algorithm": "sha512",
     "loaders": ["fabric"],
     "game_versions": ["1.20.1"]
   }
   ```
3. Otrzymanie mapy odpowiedzi `{ [hash]: ModrinthVersion }` w jednym zapytaniu HTTP trwającym poniżej 300 ms.
4. Pliki, dla których Modrinth nie zwrócił nowszej wersji, są weryfikowane przez `POST /version_files`, aby rozróżnić mody aktualne (`up-to-date`) od plików autorskich/ręcznych (`manual-action`).

---

## 2. Plik Stanu: `modpack.lock.json` (Schema v1)

Każda instancja gry posiada deterministyczny plik blokady wersji `instances/<id>/modpack.lock.json`:

```json
{
  "schemaVersion": 1,
  "instanceId": "fabric-1-20-1",
  "gameVersion": "1.20.1",
  "loader": {
    "id": "fabric",
    "version": "0.15.11"
  },
  "createdAt": "2026-11-02T19:00:00.000Z",
  "updatedAt": "2026-11-02T20:15:00.000Z",
  "mods": [
    {
      "fileName": "sodium-fabric-0.5.8+mc1.20.1.jar",
      "name": "Sodium",
      "version": "0.5.8",
      "projectId": "AANobbMI",
      "versionId": "f7K9sLmQ",
      "sha512": "3a8c...91b2",
      "sha1": "71df...23a1",
      "source": "modrinth",
      "pinned": false,
      "updatePolicy": "stable-only",
      "enabled": true,
      "dependencies": ["fabric-api"],
      "installedAt": "2026-11-02T19:05:00.000Z"
    }
  ]
}
```

---

## 3. Blokowanie Wersji (Pinning)

Użytkownik może zablokować wybraną modyfikację przed jakimikolwiek automatycznymi aktualizacjami (np. gdy nowsza wersja psuje kompatybilność z innym modem lub shaderami):
- Kliknięcie przycisku **Przypnij** w panelu aktualizacji ustawia `pinned: true` w `modpack.lock.json`.
- Mod oznaczony jako przypięty otrzymuje złoty badge i jest automatycznie ignorowany przez operacje "Zaktualizuj wszystkie" oraz "Zaktualizuj zaznaczone".
- Próba wymuszenia aktualizacji modu przypiętego przez zależność innego modu jest sygnalizowana ostrzeżeniem o kolizji (`pinned-version`).

---

## 4. Polityki Aktualizacji (Update Policies)

Launcher wspiera konfigurację polityki aktualizacji per-mod oraz globalnie:

| Polityka | Identyfikator | Zasada Działania |
| :--- | :--- | :--- |
| **Tylko stabilne** | `stable-only` | (Domyślna) Akceptuje wyłącznie wydania oznaczone przez twórcę jako `release`. Wersje testowe są ignorowane. |
| **Stabilne i testowe** | `stable-and-beta` | Akceptuje wydania `release` oraz `beta`. Ignoruje wczesne kompilacje deweloperskie `alpha`. |
| **Wszystkie wydania** | `all-versions` | Prezentuje najnowsze kompilacje, w tym wczesne wersje `alpha` (dla testerów i deweloperów). |

---

## 5. Prezentacja Dziennika Zmian (Changelog)

Kliknięcie przycisku **Changelog** w widoku aktualizacji otwiera modal `ModChangelogModal`:
- Prezentuje pełny opis zmian przygotowany przez autora wydania.
- Wyświetla badge kanału wydania (Release / Beta / Alpha).
- Wskazuje dokładną datę publikacji oraz przyrost/spadek rozmiaru pliku (np. `+250 KB`).
- Wyszczególnia powiązane zależności i biblioteki.
