# Zombek Player Info Service

## 1. Cel i Odpowiedzialność

`ZombekPlayerInfoService` (`pl.zombek.client.core.playerinfo.ZombekPlayerInfoService`) to uniwersalny serwis stanu graczy i encji w ramach modułu `:core`.

Zapewnia jedno centralne źródło prawdy (`single source of truth`) dla:
- Modułu `NameTagsModule`
- Modułu `TabEditorModule`
- Modułu `TeamViewModule`
- Przyszłych modułów informacyjnych i społecznościowych.

## 2. Dostępność Danych (`DataAvailability`)

W celu zachowania rygorystycznych standardów Fair Play i uniknięcia błędnych założeń (np. na serwerach z ukrytym pingiem lub ukrytym HP), każde pole w modelu `PlayerInfoData` posiada status:
- `AVAILABLE`: Dane są w 100% autentyczne i dostarczone przez serwer lub silnik gry.
- `UNKNOWN`: Serwer nie dostarcza tych danych w sposób bezpośredni, a encja nie znajduje się w zasięgu renderowania.
- `UNAVAILABLE`: Dane są zablokowane lub niedostępne na danym serwerze.

## 3. Model Danych (`PlayerInfoData`)

Niemutowalny rekord zawierający:
- `uuid` (UUID gracza lub wygenerowany dla encji)
- `username` (nazwa użytkownika)
- `displayName` (formatowana nazwa wyświetlana)
- `serverPrefix` i `serverSuffix` (prefiks/sufiks serwerowy)
- `teamName` i `teamColorArgb` (informacje o drużynie ze Scoreboardu lub TeamTrackera)
- `latency` i `latencyAvailability` (ping w milisekundach)
- `gameMode` i `gameModeAvailability` (tryb gry)
- `health`, `maxHealth`, `absorption`, `healthAvailability` (punkty życia)
- `armor`, `armorAvailability` (lista obiektów `PlayerArmorData` z procentem wytrzymałości)
- `heldItem`, `heldItemAvailability` (nazwa trzymanego przedmiotu)
- `distance`, `distanceAvailability` (odległość w blokach od lokalnego gracza)
- `isFriend` i `friendEntry` (lokalne dane znajomego z `NameTagFriendManager`)
- `skinHandle` (uchwyt tekstury skórki)

Metoda `getEffectiveName(boolean enableAlias)` zwraca lokalny alias jeśli gracz jest znajomym i opcja jest aktywna, lub standardowy nick.

## 4. Architektura Adapterów Platformowych (`IPlayerInfoDataProvider`)

Dostęp do specyfiki silnika danego loadera realizowany jest przez interfejs `IPlayerInfoDataProvider` rejestrowany w `PlayerInfoDataProviderRegistry`:
- `FabricPlayerInfoDataProvider` (Fabric 1.20.4 Yarn)
- `QuiltPlayerInfoDataProvider` (Quilt 1.20.4)
- `ForgePlayerInfoDataProvider` (Forge 1.20.4 Mojang Mappings)
- `NeoForgePlayerInfoDataProvider` (NeoForge 1.20.4 Mojang Mappings)
- `FallbackPlayerInfoDataProvider` (Makieta dla środowisk testowych i preview)

## 5. Cykl Życia i Pamięć Podręczna

- Pamięć podręczna jest odświeżana maksymalnie 4 razy na sekundę (`CACHE_TTL_MS = 250ms`).
- Wywołanie `onWorldLeave()` natychmiast czyści całą pamięć podręczną, uniemożliwiając wycieki danych pomiędzy sesjami.
- Brak zewnętrznych zapytań HTTP/API (np. Mojang API czy Hypixel API) — pełna zgodność z prywatnością i zero lagów sieciowych.
