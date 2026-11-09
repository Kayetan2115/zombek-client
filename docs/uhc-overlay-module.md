# Moduł UHC Overlay (Item Scaling) — ZombekClient

## 1. Przeznaczenie i Rola w Kliencie

Moduł **UHC Overlay** (`UHCOverlayModule`, identyfikator: `uhc_overlay`, kategoria: `Category.SURVIVAL`) to zaawansowany system wizualnego powiększania upuszczonych na ziemię przedmiotów (dropped items in-world), inspirowany rozwiązaniami z Lunar Client i Badlion Client dla trybów Ultra Hardcore (UHC).

W dynamicznej rozgrywce UHC lub SpeedUHC, gdzie po zabiciu gracza na ziemię wypadają dziesiątki drobnych przedmiotów, kluczowe zasoby (złote jabłka, złote główki, perły, strzały, potki leczenia) mogą zginąć w gąszczu bruku, ziemi i śmieci. Moduł UHC Overlay modyfikuje macierz transformacji renderera przedmiotów (`ItemEntityRenderer`), renderując cenne przedmioty w zauważalnie większej skali.

Główna klasa modułu znajduje się w:
[UHCOverlayModule.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/survival/UHCOverlayModule.java)  
Kontroler skalowania i reguły niestandardowe:
[UhcItemScalingController.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/survival/UhcItemScalingController.java)  
[UhcScaleMode.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/survival/UhcScaleMode.java)

---

## 2. Tryby Skalowania (Scale Modes)

Parametr `scale_mode` (`UhcScaleMode`) pozwala wybrać strategię skalowania:
1. **`PER_ITEM` (Domyślny):** Każda kategoria przedmiotów posiada indywidualny mnożnik skali (np. złote jabłka 2.0x, bloki 1.2x).
2. **`GLOBAL_ONLY`:** Wszystkie upuszczone przedmioty są skalowane jednakowo za pomocą suwaka `global_scale`.
3. **`COMBINED`:** Skala indywidualna jest mnożona przez współczynnik globalny:
   $$S_{\text{final}} = S_{\text{item}} \cdot S_{\text{global}}$$

---

## 3. Domyślne Mnożniki Przedmiotów (Default Item Scales)

W trybie `PER_ITEM` moduł udostępnia suwaki z precyzyjnie dobranymi wartościami domyślnymi:
- **Złote jabłka (`gapple_scale`):** mnożnik **`2.0x`** (zakres 0.5 - 3.0).
- **Złote główki / głowy graczy (`ghead_scale`):** mnożnik **`2.0x`** (zakres 0.5 - 3.0).
- **Perły kresu (`pearl_scale`):** mnożnik **`1.8x`** (zakres 0.5 - 3.0).
- **Strzały (`arrow_scale`):** mnożnik **`1.5x`** (zakres 0.5 - 3.0).
- **Mikstury leczenia i regeneracji (`healing_scale`):** mnożnik **`1.6x`** (zakres 0.5 - 3.0).
- **Bloki budowlane (`blocks_scale`):** mnożnik **`1.2x`** (zakres 0.5 - 3.0).
- **Wiadra z wodą i lawą (`bucket_scale`):** mnożnik **`1.4x`** (zakres 0.5 - 3.0).
- **Krzesiwo (`flint_and_steel_scale`):** mnożnik **`1.4x`** (zakres 0.5 - 3.0).
- **Pozostałe przedmioty (`other_scale`):** mnożnik **`1.0x`** (brak modyfikacji).

---

## 4. Własne Reguły Skalowania (Custom Item Rules)

Pole `custom_rules` (String) pozwala graczowi na dopisanie dowolnych dodatkowych przedmiotów lub modyfikacji mnożników w formacie oddzielanym przecinkami:
```text
minecraft:diamond=2.5,minecraft:iron_ingot=1.8,minecraft:enchanted_golden_apple=2.2
```
Kontroler `UhcItemScalingController` parsuje reguły w locie, dając im pierwszeństwo przed domyślnymi kategoriami.

---

## 5. Implementacja Renderera i Zasady Fair Play

### 5.1. Transformacja Macierzy Modelu
Skalowanie jest aplikowane w potoku renderera `ItemEntityRenderer`:
```java
// Fabric & Quilt (ItemEntityRendererMixin):
matrices.push();
float scale = UhcItemScalingController.getInstance().resolveScale(itemStack);
matrices.scale(scale, scale, scale);
// ... standardowy rendering modelu przedmiotu ...
matrices.pop();
```

### 5.2. Absolutne Poszanowanie Fizyki Gry
> [!IMPORTANT]
> **Brak Modyfikacji Hitboxów i Fizyki:**
> - Skalowanie dotyczy **wyłącznie reprezentacji wizualnej (modelu)** w widoku gracza.
> - Hitbox encji (`Entity.getBoundingBox()`) pozostaje w 100% zgodny z kodem Vanilla.
> - Zasięg podnoszenia przedmiotu przez gracza nie ulega żadnej zmianie.
> - Grawitacja, kolizje z blokami i przepływ wody działają całkowicie po stronie serwera bez zakłóceń.
