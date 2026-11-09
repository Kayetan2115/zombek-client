# Moduł 1.7 Visuals — ZombekClient

## 1. Przeznaczenie i Rola w Kliencie

Moduł **1.7 Visuals** (`OneSevenVisualsModule`, identyfikator: `one_seven_visuals`, kategoria: `Category.VISUAL`) to kompleksowy system przywracający klasyczne animacje, ułożenia rąk i przedmiotów oraz zachowania wizualne z wersji Minecraft 1.7 i 1.8, inspirowany wiodącymi rozwiązaniami znanymi z Lunar Client i Badlion Client.

W nowoczesnych wersjach gry (Minecraft 1.9+) zmieniono sposób trzymania przedmiotów w pierwszej osobie, usunięto blokowanie mieczem na rzecz tarcz, wprowadzono długie animacje wyciągania przedmiotów z paska podręcznego oraz zmieniono charakterystykę zamachu ręki i opadania kamery podczas kucania. Moduł **1.7 Visuals** pozwala graczowi na elastyczne i niezależne przywrócenie każdego z tych klasycznych zachowań, bez wpływu na mechaniki serwerowe.

Główna klasa modułu znajduje się w:
[OneSevenVisualsModule.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/oneseven/OneSevenVisualsModule.java)

---

## 2. Architektura Komponentów (Core)

Moduł został zaprojektowany z czystym podziałem odpowiedzialności:
- **`AnimationProfileManager`:** zarządzanie profilami i presetami (`MODERN`, `MINECRAFT_1_8`, `MINECRAFT_1_7`, `CUSTOM`), import/export JSON ze ścisłą walidacją.
- **`AnimationSettings`:** wersjonowany model parametrów z możliwością klonowania i zapisywania do `modules.json`.
- **`FirstPersonAnimationRenderer`:** silnik obliczania wypadkowej macierzy transformacji dla widoku pierwszej osoby (pozycje kategorii, blokowanie, blockhit, swing, łuk, wędka, jedzenie, picie, przełączanie przedmiotów).
- **`ThirdPersonAnimationRenderer`:** obsługa klasycznych póz modelu gracza w trzeciej osobie (blokowanie mieczem, ułożenie tułowia/ramion przy kucaniu).
- **`ItemTransformManager`:** precyzyjna kontrola pozycji, rotacji i skali dla 12 kategorii przedmiotów w widoku FPP.
- **`BlockingAnimationController`:** czysto wizualna poza blokowania mieczem i blockhitu przy użyciu prawego przycisku myszy.
- **`SwingAnimationController`:** klasyczny zamach ręki niezależny od attack cooldownu.
- **`SneakAnimationController`:** natychmiastowe obniżenie wysokości oczu (instant sneak down) z płynnym powrotem w górę.
- **`DamageVisualController`:** efekt czerwonego pancerza podczas otrzymywania obrażeń (Red Armor on Hit).

---

## 3. Podział na Grupy Ustawień

W menu Right Shift moduł udostępnia 13 dedykowanych sekcji:
1. **Profile i Presety (`presets`):** wybór presetu (`Modern`, `Minecraft 1.8`, `Minecraft 1.7`, `Custom`), przyciski akcji *Zastosuj Preset* i *Zresetuj Profil*.
2. **Old Blocking (`blocking`):** włączenie, styl blokowania (`Vanilla 1.7`, `Vanilla 1.8`, `Lunar`, `Custom`), pozycje X/Y/Z, kąty rotacji X/Y/Z, skala, prędkość przejścia, obsługa prawej/lewej ręki, widok FPP i TPP.
3. **Old Blockhit (`blockhit`):** włączenie, styl (`Classic 1.7`, `Classic 1.8`, `Smooth`, `Aggressive`, `Custom`), prędkość, kąt, przesunięcia X/Y, rotacja, skala, intensywność.
4. **Old Swing (`swing`):** włączenie, styl (`Vanilla 1.7`, `Vanilla 1.8`, `Modern`, `Custom`), czas trwania w tickach (domyślnie 6), prędkość, kąt, intensywność, obsługa lewej ręki.
5. **Old Sneak (`sneak`):** włączenie, poza modelu, animacja kamery, styl przejścia (`Instant Down, Smooth Up`, `Full Instant`, `Smooth 1.7`, `Modern`), szybkość przejścia, opcje FPP i TPP.
6. **Pozycje Przedmiotów (`item_positions`):** wybór edytowanej kategorii (12 typów), przesunięcia X/Y/Z, kąty X/Y/Z, skala X/Y/Z, reset pojedynczej kategorii, reset wszystkich.
7. **Wędka (`fishing_rod`):** klasyczne ułożenie pod kątem -15°, dynamiczny ruch wyrzutu.
8. **Łuk (`bow`):** klasyczne celowanie, przechył przy naciągu.
9. **Jedzenie i Picie (`eating_drinking`):** klasyczne animacje spożywania potraw i picia mikstur, intensywność kołysania.
10. **Zmiana Przedmiotów (`item_switching`):** klasyczne, szybkie wyciąganie przedmiotu z hotbara (mnożnik szybkości 1.8x - 2.0x).
11. **Efekty Obrażeń (`damage_visuals`):** Red Armor on Hit (czerwone zabarwienie zbroi przy ciosach), kolor, intensywność, czas błysku, płynne wygaszanie.
12. **HUD Zdrowia (`health`):** klasyczny obrys i odstępy serc, białe miganie, złote serca absorpcji, animacje regeneracji.
13. **Zgodność z Resource Packami (`resource_pack`):** tryby `Use Resource Pack`, `Override Resource Pack`, `Custom Priority (Additive)`.

---

## 4. Zasady Fair Play i Brak Ingerencji w Rozgrywkę

> [!IMPORTANT]
> **Ścisła Klientowość i Legalność:**
> - **Zero serwerowego bloku:** Old Blocking nie zmniejsza obrażeń, nie aktywuje mechanik tarczy i nie wysyła żadnych dodatkowych pakietów do serwera.
> - **Zero modyfikacji Attack Cooldownu:** Old Swing nie przyspiesza czasu przeładowania broni i nie zadaje dodatkowych obrażeń.
> - **Zero zmian hitboxów:** Old Sneak modyfikuje wyłącznie lokalną prezentację kamery i modelu gracza; bounding box postaci na serwerze jest w 100% zgodny z Vanilla.
