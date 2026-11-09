# Kontroler Skalowania Elementów HUD (HudScaleController) — ZombekClient (Etap 9B)

## 1. Wprowadzenie i Założenia

Wielu graczy preferuje kompaktowe lub powiększone elementy interfejsu niezależnie od globalnej skali GUI Minecrafta (`guiScale`).
Moduł **HudScaleController** zarządza skalowaniem wizualnym komponentów HUD w ZombekClient.

Kluczowe założenia implementacji:
1. **Niezmienność wymiarów bazowych:** Zmiana skali (`scale`) nie modyfikuje fizycznych wartości `width` i `height` w deskryptorze. Wymiary efektywne na ekranie to:
   $$W_{effective} = \lfloor W_{base} \times \mathrm{scale} \rceil$$
   $$H_{effective} = \lfloor H_{base} \times \mathrm{scale} \rceil$$
2. **Zakres Skali:** Dopuszczalny przedział to `0.25` (25%) do `3.00` (300%) z precyzją do 2 miejsc po przecinku (krok 0.05 / 0.10).
3. **Zaokrąglanie i odporność na błędy zmiennoprzecinkowe:** Wszystkie wartości skali są zaokrąglane za pomocą `Math.round(scale * 100.0) / 100.0`, eliminując anomalie typu `1.0500000000000003`.
4. **Ochrona Zablokowanych Komponentów:** Jeśli `state.isLocked() == true`, wywołanie metody skalującej zwraca `false` i nie wprowadza zmian w stanie.

---

## 2. API Kontrolera

```java
public final class HudScaleController {
    public static final double MIN_SCALE = 0.25;
    public static final double MAX_SCALE = 3.00;
    public static final double DEFAULT_STEP = 0.05;

    public static boolean setScale(HudComponentState state, HudComponentDescriptor descriptor, double newScale);
    public static boolean scaleUp(HudComponentState state, HudComponentDescriptor descriptor);
    public static boolean scaleDown(HudComponentState state, HudComponentDescriptor descriptor);
    public static boolean resetScale(HudComponentState state, HudComponentDescriptor descriptor);
}
```

---

## 3. Integracja z Inspektorem i Renderowaniem

- W panelu bocznym (`HudEditorInspector`) skala jest prezentowana jako precyzyjne pole tekstowe z przyciskami `+` oraz `-`.
- Zmiana wartości w polu tekstowym i wciśnięcie klawisza `Enter` zatwierdza nową skalę.
- Wciśnięcie klawisza `Escape` przywraca poprzednią wartość sprzed edycji.
- Renderowanie elementu (`IHudRenderer`) wykorzystuje matrycę skalowania przed rysowaniem zawartości modułu, zachowując wycentrowanie lub przesunięcie zgodne z kotwicą.
