# Quilt Compatibility — Zgodność Ekosystemu ZombekClient

## 1. Wymagania Środowiskowe

| Komponent | Wymagana wersja | Uwagi |
|---|---|---|
| **Minecraft** | `1.20.4` | Ścisła kompatybilność |
| **Quilt Loader** | `>=0.25.0` (zalecany `0.27.1-beta.1`) | Zweryfikowany z oficjalnym repozytorium Quilt |
| **Java Runtime** | `Java 17 LTS` | Microsoft JDK 17 / Eclipse Temurin 17 |
| **ZombekClient Mod** | `0.1.0-alpha` | Wydanie Quilt (`zombekclient-quilt-0.1.0-alpha.jar`) |
| **ZombekClient Core** | `0.1.0-alpha` | Ten sam uniwersalny rdzeń co w Fabric/Forge/NeoForge |

---

## 2. Decyzja Architektoniczna Dotycząca QSL i Quilted Fabric API

W grudniu 2025 r. projekt **Quilt Standard Libraries (QSL)** został oficjalnie wygaszony i oznaczony jako przestarzały. Zależność od QSL lub Quilted Fabric API (QFAPI) w nowych projektach niesie za sobą ryzyko problemów z kompatybilnością oraz niepotrzebnego narzutu pamięciowego.

Z tego powodu adapter `apps/minecraft-client/quilt`:
1. **Nie wymaga QSL ani QFAPI** do podstawowego działania.
2. Korzysta z czystego `quilt-loader` API oraz minimalnego miksinu `MinecraftClientMixin`.
3. Jest w 100% kompatybilny z modami Fabric działającymi na Quilt (dzięki mechanizmowi pośrednich mapowań `intermediate_mappings: "net.fabricmc:intermediary"` w `quilt.mod.json`).

---

## 3. Kompatybilność Międzyloaderowa (Cross-Loader Matrix)

Wszystkie cztery warianty moda ZombekClient współdzielą identyczny format konfiguracji i wspólne moduły:

| Loader | Plik artefaktu | Punkty wejścia | Format konfiguracji | Domyślny klawisz |
|---|---|---|---|---|
| **Fabric** | `zombekclient-fabric-0.1.0-alpha.jar` | `ClientModInitializer` | `modules.json` (v1) | RIGHT SHIFT |
| **Forge** | `zombekclient-forge-0.1.0-alpha.jar` | `@Mod`, Mod/Forge Event Bus | `modules.json` (v1) | RIGHT SHIFT |
| **NeoForge** | `zombekclient-neoforge-0.1.0-alpha.jar` | `@Mod(dist = Dist.CLIENT)` | `modules.json` (v1) | RIGHT SHIFT |
| **Quilt** | `zombekclient-quilt-0.1.0-alpha.jar` | `PreLaunchEntrypoint`, `ClientModInitializer` | `modules.json` (v1) | RIGHT SHIFT |

---

## 4. Izolacja Katalogów i Zapisy Światów

Każda instalacja w launcherze ZombekClient posiada swój własny odizolowany katalog instancji (`instances/<instanceId>/`):
- Mody przechowywane są w dedykowanym katalogu `instances/<instanceId>/mods/`.
- Konfiguracje modułów ZombekClient zapisywane są w `instances/<instanceId>/config/zombekclient/modules.json`.
- Światy (`saves/`), zrzuty ekranu (`screenshots/`) i opcje gry (`options.txt`) są całkowicie odizolowane, co gwarantuje bezproblemowe przełączanie się pomiędzy wersjami Vanilla, Fabric, Forge, NeoForge i Quilt.
