# Integracja Modloadera Quilt — ZombekClient

## 1. Wstęp i Założenia

W ramach **ETAPU 5D** do silnika launchera ZombekClient dodano pełną, natywną obsługę modloadera **Quilt** (rozwijanego przez projekt QuiltMC).

Quilt to nowoczesny, modułowy modloader forkowany z projektu Fabric, wykorzystujący zaawansowany silnik ładowania **Knot** (`org.quiltmc.loader.impl.launch.knot.KnotClient`).

Dzięki integracji użytkownik ZombekClient może:
1. Otworzyć ekran **Versions**.
2. Kliknąć **Nowa Instalacja**.
3. Wybrać z listy modloader **Quilt**.
4. Wybrać kompatybilną wersję Minecraft (np. 1.20.4, 1.21.1).
5. Wybrać wersję Quilt Loader (pobieraną dynamicznie z oficjalnego Quilt Meta API v3).
6. Zainstalować Quilt jednym kliknięciem.
7. Uruchomić grę zarówno z kontem Microsoft Premium, jak i lokalnym profilem Offline.
8. Korzystać z całkowicie izolowanego katalogu instancji bez ryzyka konfliktów modów.

---

## 2. Architektura Komponentów

```
                     ┌──────────────────────────────────────────────┐
                     │            Quilt Meta API v3                 │
                     │       (https://meta.quiltmc.org/v3)          │
                     └──────────────────────┬───────────────────────┘
                                            │ HTTP GET (z User-Agent)
                                            ▼
                     ┌──────────────────────────────────────────────┐
                     │              QuiltMetaService                │
                     │  - Pobieranie wersji gier i loaderów         │
                     │  - Pobieranie profilu launchera /profile/json│
                     │  - Cache dyskowy: quilt_meta_cache.json      │
                     └──────────────────────┬───────────────────────┘
                                            │
                                            ▼
┌──────────────────────┐     ┌──────────────────────────────────────┐     ┌──────────────────────┐
│  ModLoaderAdapter    │◄────┤          QuiltLoaderAdapter          ├────►│ InstallationRepo     │
│  (Wspólny kontrakt)  │     │  - Fuzja profilu Vanilla + Quilt     │     │ (Zapis do bazy       │
└──────────────────────┘     │  - KnotClient mainClass              │     │  i instance.json)    │
                             │  - Izolacja folderu instances/<id>/  │     └──────────────────────┘
                             └──────────────────────┬───────────────┘
                                                    │
                                                    ▼
                             ┌──────────────────────────────────────┐
                             │             GameLauncher             │
                             │  - Pobieranie brakujących JAR        │
                             │  - Weryfikacja Java Runtime          │
                             │  - Autoryzacja MSA / Offline         │
                             │  - Bezpieczny spawn JVM              │
                             └──────────────────────────────────────┘
```

---

## 3. Quilt Meta API v3 & Wymóg User-Agent

QuiltMC udostępnia oficjalne, wydajne API metadanych v3.

- **Bazowy URL:** `https://meta.quiltmc.org/v3`
- **Wymagany Nagłówek:** `User-Agent: ZombekClient/0.1.0 (desktop launcher)`
  - *Uwaga:* Serwery Quilt Meta rygorystycznie wymagają unikalnego nagłówka `User-Agent`. Żądania bez nagłówka lub z domyślnym agentem generic mogą zostać zablokowane kodem 403 Forbidden.

### Dostępne Punkty Końcowe:
1. `GET /v3/versions/game`
   - Zwraca listę wszystkich 440+ wydań gry wspieranych przez Quilt (flagi `version`, `stable`).
2. `GET /v3/versions/loader/{game_version}`
   - Zwraca listę kompatybilnych wersji Quilt Loader (np. `0.27.1-beta.1`, `0.26.0`), numer buildu, koordynaty Maven oraz flagę `stable`.
3. `GET /v3/versions/loader/{game_version}/{loader_version}/profile/json`
   - Zwraca gotowy profil launchera Mojang:
     - `id`: np. `quilt-loader-0.27.1-beta.1-1.20.4`,
     - `inheritsFrom`: `1.20.4`,
     - `mainClass`: `org.quiltmc.loader.impl.launch.knot.KnotClient`,
     - `libraries`: lista bibliotek Maven wraz z sumami kontrolnymi SHA-1 i URL do pobrania z `https://maven.quiltmc.org/repository/release/`.

### Odporność Offline & Cache Dyskowy:
Wszystkie zapytania są buforowane w pliku:
`%APPDATA%/ZombekClient/game-data/quilt_meta_cache.json`
W przypadku utraty połączenia sieciowego launcher natychmiast przełącza się na lokalny bufor dyskowy.

---

## 4. Polityka QSL (Quilt Standard Libraries)

> [!IMPORTANT]
> **Oficjalne stanowisko projektu QuiltMC z grudnia 2025 r.:**
> Projekt QSL (Quilt Standard Libraries) został oficjalnie wycofany i uznany za zakończony (discontinued). Społeczność moderska przeszła na standardowe biblioteki Fabric API / Quilted Fabric API lub czyste implementacje modów.

### Decyzja Projektowa ZombekClient:
Standardowa instalacja Quilt w ZombekClient:
- **NIE wstrzykuje sztucznie biblioteki QSL** do profilu gry ani do katalogu `mods/`.
- Instaluje wyłącznie oficjalny, stabilny **Quilt Loader** wraz z jego wymaganymi zależnościami wykonawczymi (Knot, ASM, Quilt-JSON5, Mixin).
- Pozostawia użytkownikowi pełną swobodę w doborze ewentualnych bibliotek kompatybilności w katalogu `instances/<id>/mods/`.

---

## 5. Izolacja Środowisk & Struktura Instancji

ZombekClient gwarantuje stuprocentową separację wszystkich 5 środowisk (**Vanilla**, **Fabric**, **Forge**, **NeoForge**, **Quilt**). Żaden mod ani plik konfiguracyjny nie jest współdzielony globalnie.

Struktura katalogu instancji Quilt:
```
%APPDATA%/ZombekClient/
  └── instances/
      └── <instance-id>/
          ├── instance.json         <-- Konfiguracja instancji (loader: "quilt", wersja, RAM)
          ├── config/               <-- Niezależne konfiguracje modów
          ├── mods/                 <-- Pliki JAR modów Quilt i kompatybilnych Fabric
          ├── saves/                <-- Światy gry tej instancji
          ├── resourcepacks/        <-- Paczki zasobów
          ├── screenshots/          <-- Zrzuty ekranu
          ├── natives/              <-- Wypakowane biblioteki natywne LWJGL
          └── logs/
              └── latest.log        <-- Pełne logi z zamaskowanymi tokenami
```

---

## 6. Przygotowanie pod Menedżer Modpacków (ModCompatibilityTarget)

W ramach Etapu 5D przygotowano kompletny kontrakt typów w pakiecie `@zombek/shared-types` dla przyszłego modułu instalacji modów (Etap 6):

```typescript
export type ModCompatibilityTarget =
  | 'quilt'                  // Dedykowany wyłącznie dla Quilt
  | 'fabric'                 // Dedykowany dla Fabric
  | 'fabric-and-quilt'       // Mod Fabric zgodny również z Quilt
  | 'quilt-incompatible'     // Mod Fabric jawnie niekompatybilny z Quilt
  | 'forge'                  // Modyfikacja Minecraft Forge
  | 'neoforge';              // Modyfikacja NeoForged
```

Pozwoli to na automatyczną weryfikację pobieranych paczek modów z CurseForge / Modrinth bez ryzyka awarii gry.

---

## 7. Pokrycie Testowe

Dla integracji Quilt przygotowano zestaw 10 testów jednostkowych w `apps/launcher/tests/quilt.test.ts`:
1. `QuiltMetaService` — poprawne żądania z nagłówkiem User-Agent i zapis bufora dyskowego.
2. `QuiltMetaService` — odczyt z pamięci RAM (0 nadmiarowych zapytań HTTP).
3. `QuiltMetaService` — pobieranie wersji Quilt Loader dla wskazanego wydania gry.
4. `QuiltMetaService` — pobieranie i lokalne buforowanie profilu `profile/json`.
5. `QuiltLoaderAdapter` — sprawdzanie wsparcia wersji Minecrafta.
6. `QuiltLoaderAdapter` — scalanie profili Vanilla + Quilt (`mainClass: KnotClient`, deduplikacja bibliotek, argumenty).
7. `QuiltLoaderAdapter` — ścisła weryfikacja braku automatycznego wstrzykiwania QSL.
8. `QuiltLoaderAdapter` — tworzenie izolowanych podkatalogów instancji i zapis do repozytorium.
9. `GameArgumentsBuilder` — poprawność budowania argumentów JVM, Classpath i RAM dla Quilt.
10. `ModCompatibilityTarget` — poprawność definicji typów dla przyszłego menedżera modpacków.

Wszystkie testy przechodzą pomyślnie (`73/73` w całym projekcie).
