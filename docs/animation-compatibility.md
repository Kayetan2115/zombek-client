# Kompatybilność Animacji i Integracja Międzymodułowa — ZombekClient

## 1. Zgodność z Modułami Kamery (Freelook i Snaplook)

- **Separacja Odpowiedzialności:**
  - Moduły **Freelook** i **Snaplook** przejmują kontrolę nad rotacją kątów kamery (`yaw` i `pitch`) w `Camera.update()` lub `ViewportEvent.ComputeCameraAngles`.
  - Moduł **1.7 Visuals** w ramach animacji kucania (`SneakAnimationController`) modyfikuje wyłącznie wysokość oczu kamery w osi pionowej Y (`eyeHeightDelta`).
  - Dzięki temu 1.7 Visuals nie narusza, nie resetuje ani nie desynchronizuje swobodnego obrotu kamery w trybie Freelook ani błyskawicznego podglądu Snaplook.

---

## 2. Zgodność z Toggle Sneak

- Moduł **Toggle Sneak** zarządza stanem wejścia i logiczną flagą kucania gracza (`isSneaking`).
- Moduł **1.7 Visuals** modyfikuje wyłącznie **sposób prezentacji wizualnej** tego stanu (krzywą opadania kamery w dół oraz pochylenie modelu w widoku TPP).
- Oba moduły współpracują harmonijnie: gracz może używać Toggle Sneak z klasyczną, natychmiastową animacją kucania z 1.7.

---

## 3. Zgodność z Shiny Pots

- Moduł **Shiny Pots** nakłada warstwę połysku zaklęcia (enchantment glint) w potoku renderera przedmiotu (`ItemStackMixin.hasGlint()` lub `ItemRenderer`).
- Moduł **1.7 Visuals** modyfikuje macierz transformacji modelu (`matrices.translate()`, `matrices.multiply()`, `matrices.scale()`).
- Transformacje 1.7 są aplikowane przed renderowaniem geometrii i połysku, dzięki czemu mikstury zachowują lśniący efekt w pozycjach 1.7.

---

## 4. Zgodność z Hit Color i Red Armor on Hit

- Przy włączonym module **Hit Color**, kontroler `DamageVisualController` odpytuje `HitColorModule` o aktualny kolor podświetlenia obrażeń i stosuje go również na warstwach pancerza.
- Eliminuje to konflikty podwójnego nakładania kolorów i zapewnia jednolitą estetykę ciosów.

---

## 5. Kompatybilność z Resource Packami

Wprowadzono ustawienie `resource_pack_mode` (`ResourcePackTransformMode`):
1. **`USE_RESOURCE_PACK` (Domyślny):** Jeżeli dany model w resource packu posiada zdefiniowane niestandardowe transformacje JSON (`firstperson_righthand`), 1.7 Visuals nie nadpisuje tego modelu, zachowując zamysł twórcy tekstur (np. modele 3D mieczy, niestandardowe animowane bronie).
2. **`OVERRIDE_RESOURCE_PACK`:** 1.7 Visuals wymusza klasyczne pozycje 1.7 dla wszystkich przedmiotów, ignorując definicje display z resource packa.
3. **`CUSTOM_PRIORITY`:** Transformacje 1.7 Visuals są nakładane jako addytywne przesunięcie (delta) na transformacje resource packa.
