# Moduł Shiny Pots — ZombekClient

## 1. Przeznaczenie i Rola w Kliencie

Moduł **Shiny Pots** (`ShinyPotsModule`, identyfikator: `shiny_pots`, kategoria: `Category.VISUAL`) to funkcja wizualna znana z wiodących klientów PvP (Lunar Client, Badlion Client), dodająca efekt połysku zaklęcia (enchantment glint) do mikstur i powiązanych przedmiotów alchemicznych.

W standardowym Minecraft 1.20.4 mikstury (zwykłe, rzucane, trwające oraz zatrute strzały) nie posiadają domyślnie animacji glintu, co utrudnia ich błyskawiczne odróżnienie w dynamicznej walce PvP oraz zmniejsza czytelność ekwipunku. Moduł **Shiny Pots** w sposób czysto kliencki renderuje charakterystyczny fioletowy/lśniący połysk nad miksturami, bez ingerencji w stan NBT przedmiotu na serwerze.

Główna klasa modułu znajduje się w:
[ShinyPotsModule.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/ShinyPotsModule.java)  
Logika filtrująca i kontroler glintu:
[ShinyPotsController.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/ShinyPotsController.java)

---

## 2. Obsługiwane Typy Przedmiotów

System rozpoznaje kategorie mikstur poprzez `PotionType` oraz identyfikatory rejestru:
1. **Zwykłe mikstury do picia (`minecraft:potion`):** konfigurowalne przez `regular_potions` (domyślnie `true`).
2. **Mikstury miotane / rzucane (`minecraft:splash_potion`):** konfigurowalne przez `splash_potions` (domyślnie `true`).
3. **Mikstury trwające (`minecraft:lingering_potion`):** konfigurowalne przez `lingering_potions` (domyślnie `true`).
4. **Zatrute i nasycone strzały (`minecraft:tipped_arrow`):** konfigurowalne przez `tipped_arrows` (domyślnie `true`).
5. **Inne przedmioty alchemiczne / niestandardowe:** obsługa poprzez reguły whitelist/blacklist w `custom_rules`.

---

## 3. Konteksty Renderowania (Rendering Contexts)

Gracz może precyzyjnie określić, w których obszarach gry efekt ma być aktywny (`ShinyPotsContext`):
- **Ekwipunek gracza (`render_in_gui`):** inwentarz, crafting table, crafting 2x2 (domyślnie `true`).
- **Pasek szybkiego dostępu (`render_in_hotbar`):** dolny pasek HUD (domyślnie `true`).
- **Skrzynie i pojemniki (`render_in_containers`):** skrzynie zwykłe, podwójne, ender chest, shulker boxy (domyślnie `true`).
- **Trzymany przedmiot (`render_in_hand`):** widok pierwszej i trzeciej osoby w ręce gracza (domyślnie `true`).
- **Przedmioty upuszczone (`render_in_world`):** leżące jednostki `ItemEntity` w świecie gry (domyślnie `true`).

---

## 4. Opcje Wizualne i Animacja

W grupie `visual_settings` dostępne są parametry dostrajające wygląd połysku:
- **`glint_speed` (Double, 0.1 - 5.0, domyślnie 1.0):** mnożnik prędkości przesuwania się tekstury glintu.
- **`glint_intensity` (Double, 0.1 - 2.0, domyślnie 1.0):** intensywność/nasycenie światła połysku.
- **`pulsate` (Boolean, domyślnie `false`):** subtelna animacja pulsowania przezroczystości (alpha) w czasie oparta o funkcję sinusoidalną:
  $$\alpha(t) = \alpha_{\text{base}} \cdot (0.75 + 0.25 \cdot \sin(2\pi \cdot f \cdot t))$$
- **`custom_glint_color` (Color, domyślnie `0x8040CC`):** możliwość personalizacji odcienia połysku dla mikstur.

---

## 5. Implementacja Techniczna na Wszystkich Loaderach

Zastosowano bezinwazyjne podejście na poziomie mixinów i pipeline'u renderowania:
1. **Fabric & Quilt:**
   - `ItemStackMixin` przechwytuje metodę `hasGlint()` w klasie `net.minecraft.item.ItemStack`.
   - Gdy `ShinyPotsModule.isEnabled()` zwraca `true` oraz dany przedmiot kwalifikuje się jako mikstura w aktywnym kontekście, mixin zwraca `true`.
   - Żadne dane NBT ani pakiety sieciowe nie są modyfikowane.
2. **Forge & NeoForge:**
   - Wykorzystanie modyfikacji renderera `ItemRenderer` i metody `isFoil(ItemStack)` lub klienckich eventów renderowania modelu.
   - Kontroler `ShinyPotsController` weryfikuje kontekst i flagi modułu przed nałożeniem warstwy foil/glint.

---

## 6. Zgodność z Regulaminami Serwerów (Anti-Cheat Compliance)

Moduł **Shiny Pots** jest w 100% legalny na serwerach multiplayer (Hypixel, MMC, GommeHD, cubecraft):
- Działa wyłącznie po stronie klienta (Client-Side Only).
- Nie zmienia statystyk przedmiotów, czasu warzenia ani działania efektów mikstur.
- Nie wysyła żadnych dodatkowych pakietów na serwer.
- Nie modyfikuje hitboxów przedmiotów ani pozycji w ekwipunku.
