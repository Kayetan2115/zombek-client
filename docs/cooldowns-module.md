# Moduł Cooldowns — Specyfikacja Techniczna i Architektura

## 1. Cel i Wymagania

Moduł `CooldownsModule` (`pl.zombek.client.core.modules.pvp.CooldownsModule`) odpowiada za graficzną prezentację aktywnych czasów odnowienia przedmiotów w grze Minecraft (np. Perły Kresu, Tarcza, Owoce Refrenu, Złote Jabłka).

Moduł:
- Korzysta z rzeczywistego menedżera cooldownów Minecrafta (`ItemCooldownManager` w Yarn / `ItemCooldowns` w Mojang);
- **Nie zgaduje ani nie wpisuje na sztywno czasów odnowienia**;
- Wyświetla ikony przedmiotów, nazwy, animowane paski postępu, pozostały czas w sekundach oraz wartości procentowe;
- Obsługuje układ pionowy i poziomy;
- Posiada opcję automatycznego ukrywania przedmiotów, których czas odnowienia dobiegł końca (`hide_completed`);
- Sortuje aktywne czasy odnawiania według pozostałego czasu.

---

## 2. Źródło Danych i Odczyt z Silnika Minecraft

W grze Minecraft gracz może posiadać aktywny cooldown na dowolny przedmiot.
Aby uniknąć ciężkich zapytań i zachować maksymalną wydajność:
1. Adapter platformowy (`FabricCooldownDataProvider`, `QuiltCooldownDataProvider`, `ForgeCooldownDataProvider`, `NeoForgeCooldownDataProvider`) iteruje po aktualnym ekwipunku gracza (36 slotów głównych + 4 zbroi + 1 druga ręka).
2. Dla unikalnych typów przedmiotów sprawdza stan:
   - Fabric/Quilt: `client.player.getItemCooldownManager().isCoolingDown(item)`
   - Forge/NeoForge: `minecraft.player.getCooldowns().isOnCooldown(item)`
3. W przypadku aktywnego cooldownu pobiera postęp:
   - `getCooldownProgress(item, tickDelta)` / `getCooldownPercent(item, partialTick)` zwracający wartość od `1.0f` (początek) do `0.0f` (koniec).
4. Dane pakowane są do obiektu `CooldownItemData` i przekazywane do renderera w `core`.

---

## 3. Style Prezentacji

1. **`DETAILED` (Szczegółowy):** Ikona przedmiotu, nazwa (np. "Perła Kresu"), pasek postępu z ciemnym tłem oraz pozostały czas (`3.4s`).
2. **`COMPACT` (Kompaktowy):** Ikona przedmiotu oraz wąski pasek postępu pod/obok ikony.
3. **`BAR_ONLY` (Tylko Pasek):** Czyste paski postępu z cyfrowym licznikiem sekund.

Orientacja:
- `VERTICAL`: Lista ułożona pionowo.
- `HORIZONTAL`: Kafelki ułożone poziomo.

---

## 4. Konfiguracja i Ustawienia

Moduł udostępnia pełen zestaw typowanych parametrów:
- **General:** `show_icon`, `show_name`, `show_time`, `show_bar`, `show_percentage`.
- **Display:** `display_style`, `icon_size` (12–32 px), `text_size`, `scale`, `spacing`.
- **Behavior:** `hide_completed`, `sort_remaining`, `show_only_active`.
- **Colors:** `text_color`, `progress_color` (domyślnie `#EF4444`), `progress_bg`, `bg_color`, `border_color`, `opacity`.
- **Layout:** `orientation`, `position_x`, `position_y`, `anchor` (domyślnie `TOP_LEFT`), `padding`, `show_bg`, `show_border`.
