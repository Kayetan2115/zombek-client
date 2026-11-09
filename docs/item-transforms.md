# Pozycje i Transformacje Przedmiotów (Item Transforms) — ZombekClient

## 1. Obsługiwane Kategorie Przedmiotów

System `ItemTransformManager` dzieli wszystkie przedmioty w grze na 12 wyspecjalizowanych kategorii (`ItemCategory`):
1. **`SWORD`:** Miecze (drewniane, kamienne, żelazne, złote, diamentowe, netherytowe).
2. **`AXE`:** Topory.
3. **`PICKAXE`:** Kilofy.
4. **`SHOVEL`:** Łopaty.
5. **`HOE`:** Motyki.
6. **`FISHING_ROD`:** Wędka.
7. **`BOW`:** Łuk.
8. **`CROSSBOW`:** Kusza.
9. **`FOOD`:** Jedzenie (jabłka, pieczenie, g-apple itp.).
10. **`POTION`:** Mikstury i butelki.
11. **`SHIELD`:** Tarcza.
12. **`OTHER`:** Pozostałe przedmioty i bloki.

Automatyczna klasyfikacja odbywa się w metodzie `ItemCategory.fromItemId(String itemId)` na podstawie identyfikatorów rejestru, dzięki czemu działa uniwersalnie na wszystkich loaderach.

---

## 2. Dostępne Parametry Transformacji

Dla każdej kategorii gracz ma do dyspozycji 10 precyzyjnych parametrów:
- **`cat_enabled` (Boolean):** Włączenie modyfikacji danej kategorii.
- **`cat_pos_x`, `cat_pos_y`, `cat_pos_z` (Double, -1.0 do 1.0):** Przesunięcie w przestrzeni 3D.
- **`cat_rot_x`, `cat_rot_y`, `cat_rot_z` (Double, -180° do 180°):** Trójosiowa rotacja kątowa.
- **`cat_scale_x`, `cat_scale_y`, `cat_scale_z` (Double, 0.2 do 3.0):** Skala modelu w dłoni.

---

## 3. Specjalne Pozycje i Animacje

- **Wędka (`fishing_rod`):** Kąt pochylenia `-15°` odtwarzający charakterystyczne trzymanie wędki z Minecraft 1.7 oraz dynamiczny ruch wyrzutu przy kliknięciu.
- **Łuk (`bow`):** Klasyczne ułożenie łuku z bocznym przesunięciem i kątem przechyłu `8°` podczas naciągania cięciwy.
- **Jedzenie i Picie (`eating_drinking`):** Szybkie sinusoidalne kołysanie przedmiotu przy ustach gracza podczas jedzenia i picia, eliminujące nowoczesne, sztywne animacje.
- **Zmiana Przedmiotów (`item_switching`):** Skrócenie czasu opuszczania i podnoszenia przedmiotu podczas przełączania slotów paska podręcznego (mnożnik od 1.0x do 4.0x).
