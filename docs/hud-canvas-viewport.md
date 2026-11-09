# Przestrzeń Robocza, Zoom i Panowanie (HudCanvasViewport) — ZombekClient (Etap 9B)

## 1. Wprowadzenie i Założenia

Podczas projektowania złożonego interfejsu użytkownik potrzebuje zarówno całościowego podglądu ekranu (*Fit Canvas*), jak i precyzyjnego przybliżenia do pojedynczego piksela (*Pixel Peeping*).
Moduł **HudCanvasViewport** implementuje kompletną wirtualną kamerę 2D z obsługą powiększania (*Zoom*), przesuwania widoku (*Pan*) oraz matematycznych transformacji współrzędnych.

---

## 2. Model Matematyczny Transformacji

Wszystkie operacje renderowania i detekcji kliknięć opierają się na dwukierunkowym mapowaniu przestrzeni wirtualnej (piksele gry) na przestrzeń ekranową (piksele GUI):

### Z przestrzeni wirtualnej do ekranowej:
$$X_{screen} = (X_{virtual} \times \mathrm{zoom}) + \mathrm{panX}$$
$$Y_{screen} = (Y_{virtual} \times \mathrm{zoom}) + \mathrm{panY}$$

### Z przestrzeni ekranowej do wirtualnej (Odwrotna transformacja):
$$X_{virtual} = \frac{X_{screen} - \mathrm{panX}}{\mathrm{zoom}}$$
$$Y_{virtual} = \frac{Y_{screen} - \mathrm{panY}}{\mathrm{zoom}}$$

Dzięki precyzyjnemu zaokrąglaniu testy jednostkowe (`HudCanvasViewportTest`) potwierdzają brak dryfu współrzędnych w obie strony (*zero drift round-trip*).

---

## 3. Sterowanie Kamerą (Zoom & Pan)

1. **Zoom pod kursor myszy (`zoomAt`):**
   Gdy użytkownik obraca kółkiem myszy (`mouseScrolled`), punkt wirtualny leżący pod kursorem zachowuje swoje ekranowe położenie:
   $$\mathrm{panX}_{new} = X_{mouse} - (X_{virtual} \times \mathrm{zoom}_{new})$$
   $$\mathrm{panY}_{new} = Y_{mouse} - (Y_{virtual} \times \mathrm{zoom}_{new})$$
2. **Panowanie (Przesuwanie widoku):**
   - Środkowy przycisk myszy (MMB / Scroll click) aktywuje natychmiastowe przesuwanie widoku bez zmiany wybranego narzędzia.
   - Narzędzie **PAN** na pasku narzędzi pozwala na przesuwanie lewym przyciskiem myszy.
3. **Poziomy Zoomu:** Od 25% (`0.25`) do 400% (`4.00`).
4. **Narzędzia Widoku:**
   - **Fit Canvas:** Automatycznie dobiera współczynnik powiększenia i offsety, aby cały ekran gry mieścił się w oknie edytora z zachowaniem marginesu.
   - **Zoom to Selection:** Przybliża i centruje widok na aktualnie zaznaczonym elemencie HUD.
   - **Reset View:** Przywraca powiększenie 100% (`zoom = 1.0`) oraz `panX = 0, panY = 0`.
