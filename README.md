# ZombekClient

ZombekClient to nowoczesny launcher oraz klient Minecraft Java Edition stworzony z myślą o społeczności graczy.

Projekt docelowo łączy:
- launcher inspirowany estetyką Lunar Client,
- unikalny świąteczny motyw wizualny z maskotką Ząbek (pixel-art),
- zaawansowany system modyfikacji in-game (HUD, Keystrokes, CPS),
- pełną obsługę kont Microsoft Premium oraz profili Offline,
- modułową obsługę loaderów (Fabric, Forge, NeoForge, Quilt),
- system personalizacji (skiny, peleryny, kosmetyki).

---

## Struktura Monorepo

Projekt zorganizowany jest jako modularne monorepo:

- **`apps/launcher`**: Natywna aplikacja desktopowa w oparciu o Electron, React 19, Vite i TypeScript.
- **`apps/minecraft-client/fabric`**: Moduł modyfikacji w grze oparty na Java 17, Gradle i Fabric Loom.
- **`packages/shared-types`**: Wspólne typy domenowe platformy.
- **`packages/config-schema`**: Schematy konfiguracyjne i walidacja ustawień.
- **`packages/contracts`**: Ścisłe kontrakty komunikacyjne IPC między Electron Main, Preload i Rendererem.
- **`services/cosmetics-api`**: Specyfikacja serwisu backendowego dla kosmetyków i peleryn.
- **`docs/`**: Pełna dokumentacja architektoniczna, decyzje projektowe (ADR) i roadmapa.

---

## Wymagania Środowiskowe

- **Node.js**: >= 20.0.0 (zalecana wersja 22 LTS lub 24)
- **pnpm**: >= 9.0.0 (lub v12+)
- **Java JDK**: 17+ (dla modułu Minecraft Client)

---

## Dostępne Polecenia

```bash
# Instalacja zależności
pnpm install

# Uruchomienie deweloperskie launchera (Vite)
pnpm dev

# Budowanie pakietów i aplikacji
pnpm build

# Weryfikacja typów TypeScript w całym monorepo
pnpm typecheck

# Uruchomienie testów jednostkowych i kontraktów
pnpm test

# Sprawdzenie poprawności struktury katalogów i plików
pnpm verify:structure

# Uruchomienie lintera
pnpm lint
```

---

## Status Realizacji

- **Etap 1: Fundamenty i struktura projektu** — **UKOŃCZONY**
  - Przygotowano czystą, stabilną strukturę monorepo.
  - Zaimplementowano bezpieczny mostek IPC Electrona z odizolowanym rendererem.
  - Skonfigurowano niezależny moduł Fabric z Gradle.
  - Przygotowano pełną dokumentację w katalogu `docs/`.
