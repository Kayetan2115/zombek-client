# Day Counter Module — ZombekClient (Etap 8B3)

## 1. Przeznaczenie i Rola Modułu

Moduł `DayCounterModule` (`"day_counter"`) w ZombekClient prezentuje aktualny dzień świata gry lub bieżącej sesji gracza. Działa w kategorii `ModuleCategory.HUD` („HUD / Informacyjne”), współpracując z rejestrem `HudElementRegistry` i priorytetem kolizji `HudPriorityResolver` (priorytet bazowy: 35).

Pliki źródłowe:
- Moduł HUD: [DayCounterModule.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/daycounter/DayCounterModule.java)
- Tryby kalkulacji: [DayCounterMode.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/daycounter/DayCounterMode.java)
- Formaty prezentacji: [DayDisplayFormat.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/daycounter/DayDisplayFormat.java)
- Silnik czasu: [ZombekTimeService.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/time/ZombekTimeService.java)

---

## 2. Matematyka Dni i Polityka Braku Danych

### 2.1. Formuła Matematyczna
Doba w Minecraft trwa dokładnie 24000 ticków silnika ($20 \text{ ticków/s} \times 1200 \text{ s} = 24000 \text{ ticków}$).
Dzień świata wyliczany jest według ścisłego wzoru:
$$\text{Day} = \left\lfloor \frac{\text{gameTime}}{24000} \right\rfloor + 1$$
gdzie $\text{gameTime}$ reprezentuje całkowity czas istnienia świata (`world.getTime()`), a dodanie $1$ zapewnia, że pierwszy dzień rozgrywki (ticki $0..23999$) prezentowany jest jako `Day 1`.

W trybie wieku świata (`WORLD_AGE`):
$$\text{AgeDays} = \left\lfloor \frac{\text{gameTime}}{24000} \right\rfloor$$
wskazuje liczbę pełnych ukończonych dób od założenia świata.

### 2.2. Restrykcyjna Polityka Braku Danych w Multiplayer
Zgodnie z zasadami ZombekClient, klient **nigdy nie odgaduje ani nie symuluje** dni gry na serwerach wieloosobowych, które nie wysyłają do klienta pakietu czasu lub blokują informację o wieku świata.
- Jeśli `ITimeDataProvider.isTimeAvailable() == false`: moduł natychmiast wyświetla:
  ```
  Day: N/A
  ```
- Klient nie estymuje czasu na podstawie zegara systemowego, co chroni przed błędnymi wskazaniami w trybach minigier i niestandardowych serwerach.

### 2.3. Wykrywanie Cofnięcia Czasu (Rollback)
W przypadku cofnięcia czasu (np. komenda serwera `/time set`, wczytanie backupu świata lub desynchronizacja pakietów):
$$\text{lastGameTime} - \text{currentGameTime} > 24000$$
Silnik `ZombekTimeService` wykrywa zdarzenie rollbacku i ustawia flagę ostrzegawczą, prezentowaną w HUD jako:
```
Day 5 [Reset]
```

### 2.4. Ręczny Offset Dni
Moduł umożliwia zdefiniowanie parametru `manual_offset` (np. dla światów przeniesionych z innych serwerów lub serwerów z przesuniętym licznikiem). Każde użycie niezerowego offsetu jest jawnie oznaczane etykietą:
```
Day 15 (Manual)
```

---

## 3. Tryby Pracy (`DayCounterMode`)

1. **`WORLD_DAY` (Domyślny):** Bieżący dzień świata liczony od 1 ($\lfloor \text{gameTime}/24000 \rfloor + 1$).
2. **`WORLD_AGE`:** Liczba ukończonych dób Minecrafta.
3. **`SESSION_DAY`:** Liczba dób Minecrafta spędzonych w bieżącej sesji połączenia ze światem.
4. **`CUSTOM`:** Użytkownik definiuje własny szablon ze znacznikiem `{day}`.

---

## 4. Formaty Prezentacji (`DayDisplayFormat`)

- `DAY_SPACE`: `Day 42`
- `DAY_COLON`: `Day: 42`
- `COMPACT`: `D42`
- `CUSTOM`: tekst zdefiniowany przez użytkownika w polu `custom_format`.

---

## 5. Konfiguracja Modułu

Moduł zawiera 5 grup ustawień:
- **General (`group_general`):**
  - `mode` (Enum: `WORLD_DAY`, `WORLD_AGE`, `SESSION_DAY`, `CUSTOM`).
  - `format` (Enum: `DAY_SPACE`, `DAY_COLON`, `COMPACT`, `CUSTOM`).
  - `manual_offset` (Number: domyślnie 0, zakres -100000..100000).
- **Format (`group_format`):**
  - `show_prefix` (Boolean: czy wyświetlać prefiks "Day", domyślnie `true`).
  - `show_subtext` (Boolean: dodatkowy opis fazy, domyślnie `false`).
  - `custom_format` (String: szablon).
- **Behavior (`group_behavior`):**
  - `highlight_time_reset` (Boolean: flaga `[Reset]`, domyślnie `true`).
  - `show_manual_offset_indicator` (Boolean: flaga `(Manual)`, domyślnie `true`).
- **Appearance (`group_appearance`):**
  - Kolory tekstu, tła i obramowania (`text_color`, `background_color`, `border_color`).
  - Efekty wizualne (`CHROMA`, `RAINBOW`, `SHADOW`, `BORDER`).
- **Layout (`group_layout`):**
  - Kotwica HUD (`anchor`), koordynaty `offset_x`, `offset_y`, `scale`.
