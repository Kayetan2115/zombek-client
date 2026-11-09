# Struktura Katalogów Projektu — ZombekClient

Poniższa specyfikacja definiuje przeznaczenie każdego katalogu w repozytorium oraz wyznacza standardy umieszczania nowego kodu.

```text
zombekclient/
│
├── apps/                               # Moduły aplikacji wykonywalnych
│   ├── launcher/                       # Aplikacja desktopowa launchera
│   │   ├── electron/                   # Kod procesu głównego Electrona i IPC
│   │   │   ├── main/                   # Inicjalizacja okna, zarządzanie cyklem życia
│   │   │   ├── preload/                # Bezpieczny mostek ContextBridge
│   │   │   └── ipc/                    # Obsługa kanałów IPC (ipcMain.handle)
│   │   │
│   │   ├── src/                        # Kod interfejsu React (Renderer Process)
│   │   │   ├── app/                    # Główny komponent App oraz punkty wejścia
│   │   │   ├── pages/                  # Widoki i strony aplikacji (logowanie, modpacki itp.)
│   │   │   ├── components/             # Reużywalne komponenty UI (przyciski, dialogi, karty)
│   │   │   ├── features/               # Moduły domenowe (auth, instance-manager, updater)
│   │   │   ├── hooks/                  # Dedykowane hooki React (useZombekApi itp.)
│   │   │   ├── stores/                 # Stan globalny aplikacji (stan launchera, profil)
│   │   │   ├── styles/                 # Style bazowe i motywy
│   │   │   ├── assets/                 # Zasoby statyczne, grafiki pikselowe, ikony
│   │   │   └── utils/                  # Funkcje pomocnicze i formatujące
│   │   │
│   │   ├── tests/                      # Testy jednostkowe i integracyjne launchera
│   │   └── package.json                # Konfiguracja pakietu launchera
│   │
│   └── minecraft-client/               # Moduły kodu uruchamianego wewnątrz gry
│       └── fabric/                     # Moduł moda dla loader Fabric (Minecraft Java)
│           ├── src/
│           │   ├── main/
│           │   │   ├── java/pl/zombek/client/
│           │   │   │   ├── core/       # Punkt wejścia (ClientModInitializer), stałe
│           │   │   │   ├── modules/    # Kontrakty i implementacje modułów klienta
│           │   │   │   ├── hud/        # Elementy HUD (nakładki ekranowe)
│           │   │   │   ├── config/     # Zapis i odczyt konfiguracji moda
│           │   │   │   ├── render/     # Potok renderowania, nakładki, shadery
│           │   │   │   └── integration/# Mostek komunikacyjny z launcherem
│           │   │   └── resources/      # fabric.mod.json, ikony, miksiny
│           │   └── test/               # Testy jednostkowe kodu Javy
│           │
│           ├── build.gradle            # Konfiguracja Fabric Loom i zależności
│           ├── settings.gradle         # Ustawienia projektu Gradle
│           └── gradle.properties       # Wersje Minecrafta, yarn, loom i bibliotek
│
├── packages/                           # Współdzielone pakiety TypeScript (monorepo)
│   ├── shared-types/                   # Typy domenowe (PlatformType, LoaderType, UserProfile)
│   ├── config-schema/                  # Schematy i walidatory konfiguracji (LauncherConfig)
│   └── contracts/                      # Kontrakty kanałów IPC (IPC_CHANNELS, ZombekDesktopApi)
│
├── services/                           # Samodzielne mikroserwisy zewnętrzne
│   └── cosmetics-api/                  # Specyfikacja serwisu kosmetyków i peleryn
│       └── README.md
│
├── docs/                               # Dokumentacja architektoniczna i standardy
│   ├── architecture.md                 # Opis architektury całego systemu
│   ├── folder-structure.md             # Ten dokument — mapa katalogów i zasady
│   ├── project-status.md               # Aktualny stan projektu i ukończone etapy
│   ├── roadmap.md                      # Plan rozwoju (Etap 1 - 8+)
│   ├── decisions.md                    # Architecture Decision Records (ADR)
│   └── testing.md                      # Wytyczne testowania każdego modułu
│
├── scripts/                            # Skrypty narzędziowe (lint, weryfikacja struktury)
│
├── package.json                        # Główny plik konfiguracyjny monorepo
├── pnpm-workspace.yaml                 # Definicja przestrzeni roboczych pnpm
├── tsconfig.json                       # Konfiguracja TypeScript Project References
├── .gitignore                          # Ignorowane pliki i katalogi
└── README.md                           # Podsumowanie i instrukcja uruchomienia
```

## Zasady dotyczące rozszerzania struktury

1. **Dodawanie nowych loaderów Minecrafta:**
   Tworzone jako rodzeństwo `apps/minecraft-client/fabric/` (np. `apps/minecraft-client/forge/`, `apps/minecraft-client/neoforge/`).
2. **Dodawanie nowych kanałów IPC:**
   Zawsze najpierw dodajemy definicję do `packages/contracts/src/index.ts`, a następnie implementujemy w `apps/launcher/electron/ipc/` i wystawiamy w `preload/`.
3. **Zakaz bezpośrednich odwołań względnych pomiędzy aplikacjami:**
   Launcher nigdy nie importuje plików moda, a mod nigdy nie zależy bezpośrednio od kodu JS.
