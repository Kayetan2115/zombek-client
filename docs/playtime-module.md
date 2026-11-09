# Playtime Module — ZombekClient (Etap 8B3)

## 1. Przeznaczenie i Rola Modułu

Moduł `PlaytimeModule` (`"playtime"`) w ZombekClient monitoruje i prezentuje czas spędzony przez gracza w grze Minecraft. Rozróżnia czas bieżącej sesji klienta (od uruchomienia gry), czas bieżącej sesji w świecie (od wejścia na serwer/świat), całkowity czas w świecie jednoosobowym (SP World Playtime pobierany ze statystyk vanilla) oraz historyczny czas klienta.

Należy do kategorii `ModuleCategory.HUD` („HUD / Informacyjne”) i rejestruje się z priorytetem `HudPriorityResolver` równym 30.

Pliki źródłowe:
- Moduł HUD: [PlaytimeModule.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/playtime/PlaytimeModule.java)
- Formaty prezentacji: [PlaytimeFormat.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/playtime/PlaytimeFormat.java)
- Silnik czasu: [ZombekTimeService.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/ZombekTimeService.java)
- Statystyki i agregacje: [PlaytimeStats.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/PlaytimeStats.java)
- Rekord sesji: [PlaytimeSessionRecord.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/PlaytimeSessionRecord.java)

---

## 2. Architektura i Rodzaje Czasu

```mermaid
flowchart TD
    subgraph Engine & System
        Mono[Monotonic Clock / System.nanoTime]
        Stats[Vanilla Minecraft Stats: PLAY_TIME / TOTAL_WORLD_TIME]
    end

    subgraph Time Service State
        ClientSess[Client Session Duration]
        WorldSess[World Session Duration]
        HistPlay[Historical Client Playtime]
        SessHist[Session History Ring (Max 100)]
    end

    subgraph Aggregation Engine
        PlayStats[PlaytimeStats: avg, longest, total, count]
    end

    subgraph HUD Output
        PlayMod[PlaytimeModule]
    end

    Mono --> ClientSess & WorldSess
    Stats --> WorldSess
    ClientSess & WorldSess --> HistPlay
    WorldSess --> SessHist
    SessHist --> PlayStats
    ClientSess & WorldSess & HistPlay & PlayStats --> PlayMod
```

### 2.1. Rozróżnienie Rodzajów Czasu
1. **Client Session Playtime:** Czas od momentu uruchomienia procesu klienta (nigdy nie resetuje się przy zmianie serwera czy wyjściu do menu).
2. **World Session Playtime:** Czas od momentu dołączenia do bieżącego świata (`onWorldJoin`). Resetuje się przy przejściu na inny serwer lub rozłączeniu.
3. **SP World Playtime:** Całkowity czas spędzony w danym świecie Singleplayer, odczytywany ze statystyk Minecraft vanilla (`minecraft.custom:minecraft.play_time` lub `minecraft.total_world_time` przeliczany z ticków: $\text{sekundy} = \text{ticki} / 20$).
   - **Multiplayer Policy:** W trybie multiplayer statystyka ta **zawsze zwraca N/A**, ponieważ serwery multiplayer nie udostępniają klientowi pełnego profilu statystyk dla celów HUD. Klient nie zgaduje ani nie symuluje tego czasu.
4. **Historical Playtime:** Łączny czas działania ZombekClient zapisywany trwale w pliku `config/zombekclient/time_data.json`.

---

## 3. Formaty Czasu (`PlaytimeFormat`)

1. **`HH_MM_SS`:** `02:45:12` (lub `02:45` przy wyłączonych sekundach).
2. **`DD_HH_MM_SS`:** `1d 02h 45m 12s`.
3. **`HUMAN_READABLE`:** `2 godz. 45 min.` (przyjazny językowo format czytelny).
4. **`COMPACT`:** `2h 45m`.

---

## 4. Prywatność i Bezpieczeństwo Danych Sesji

System nagrywania historii sesji (`PlaytimeSessionRecord`) przestrzega rygorystycznych zasad prywatności ZombekClient:
- **Brak Danych Uwierzytelniających:** Żadne tokeny, nazwy kont, hasła ani identyfikatory sesji sieciowych nie są zapisywane.
- **Ochrona Adresów IP / Domen:** Adresy serwerów multiplayer (`serverAddress`) są domyślnie **ukrywane/anonimizowane** (`storeServerAddress = false`), a ich zapis wymaga świadomej zgody użytkownika w opcjach modułu.
- **Limit Historii:** Historia sesji przechowywana jest w buforze cyklicznym o maksymalnej pojemności 100 ostatnich sesji.

---

## 5. Konfiguracja Modułu

- **General (`group_general`):**
  - `display_type` (Enum: `CLIENT_SESSION`, `WORLD_SESSION`, `SP_WORLD_TOTAL`, `HISTORICAL_TOTAL`).
  - `format` (Enum: `HH_MM_SS`, `DD_HH_MM_SS`, `HUMAN_READABLE`, `COMPACT`).
  - `show_seconds` (Boolean, domyślnie `true`).
  - `show_prefix` (Boolean, domyślnie `true` z etykietą `Playtime:`).
- **Session Tracking (`group_tracking`):**
  - `track_history` (Boolean: nagrywanie historii sesji, domyślnie `true`).
  - `store_server_address` (Boolean: zapis adresów serwerów, domyślnie `false` — opt-in).
- **Appearance & Layout (`group_appearance`, `group_layout`):**
  - Kolory, ramka, tło, cienie, kotwica HUD i marginesy.
