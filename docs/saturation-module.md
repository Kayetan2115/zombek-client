# Saturation Module — ZombekClient (Etap 8B2)

## 1. Przeznaczenie i Rola Modułu

Moduł `SaturationModule` (`"saturation"`) w ZombekClient prezentuje w czasie rzeczywistym poziom najedzenia (Hunger $0-20$), nasycenia (Saturation $0-20$) oraz opcjonalnie wyczerpania (Exhaustion $0-4.0$) gracza.

Pliki źródłowe:
- Klasa modułu: [SaturationModule.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/saturation/SaturationModule.java)
- Provider danych i interfejs: [ISaturationDataProvider.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/saturation/ISaturationDataProvider.java), [SaturationDataProviderRegistry.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/saturation/SaturationDataProviderRegistry.java)
- Układy prezentacji: [SaturationLayoutMode.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/hud/saturation/SaturationLayoutMode.java)

---

## 2. Rygorystyczna Polityka Dostępności Danych (Data Availability Policy)

W standardowym protokole sieciowym Minecraft Java Edition pakiet `PlayServerboundSetHealthPacket` (lub odpowiednik) wysyła do klienta jedynie `foodLevel` oraz `saturationLevel`. Jednak na wielu dedykowanych serwerach multiplayer (zwłaszcza Vanilla lub starszych silnikach proxy) wartość nasycenia nie jest synchronizowana lub jest maskowana przez oprogramowanie serwerowe.

> [!IMPORTANT]
> **Zasada Prawdziwych Danych (Zero-Fabrication):**
> Jeśli serwer nie wysyła wartości nasycenia do klienta, `SaturationModule` bezwzględnie wyświetla status `Niedostępne` (lub `N/A`) oraz informuje w opisie statusu: `Dane Nasycenia: Niedostępne (Serwer Vanilla)`.
> Moduł **nigdy nie zgaduje, nie estymuje, nie fabrykuje ani nie wyświetla wymyślonych liczb**.

---

## 3. Układy Prezentacji (`SaturationLayoutMode`)

1. `COMPACT` — zwarty, czytelny dwuliniowy tekst z wartościami liczbowymi najedzenia i nasycenia.
2. `DETAILED` — rozbudowany panel z dedykowanymi paskami postępu dla głodu, nasycenia oraz zmęczenia (exhaustion).
3. `MINIMAL` — pojedyncza, wąska linia z podstawowymi informacjami.
4. `CLASSIC` — tradycyjny format z etykietami tekstowymi.

---

## 4. Ostrzeżenia i Alarmy

- **Niski Poziom Głodu:** Gdy `food <= low_hunger_threshold` (domyślnie 6 punktów, czyli 3 udka), moduł automatycznie zmienia kolor wskaźnika głodu na alarmowy czerwony `#FF3333`.
- **Niski Poziom Nasycenia:** Gdy `saturation <= sat_warning_threshold` (domyślnie 3.0), wskaźnik nasycenia ostrzega o ryzyku szybkiego spadku punktów głodu.

---

## 5. Konfiguracja w Menu Right Shift

- **`general`:** `show_hunger`, `show_saturation`, `show_exhaustion`, `show_numeric`, `preview_mode`.
- **`hunger`:** `show_hunger_bar`, `highlight_low_hunger`, `low_hunger_threshold` ($1.0-14.0$).
- **`saturation`:** `show_saturation_bar`, `saturation_precision` ($0-2$), `sat_warning_threshold` ($0.0-10.0$).
- **`appearance`:** `layout_mode`, `bar_width` ($40-160$px), `bar_height` ($3-12$px), `hud_style`.
- **`colors`:** `hunger_color`, `saturation_color`, `exhaustion_color`, `warning_color`, `text_color`, `bg_color`.
- **`layout`:** `anchor` (`BOTTOM_RIGHT` domyślnie), `pos_x`, `pos_y`, `scale`.
