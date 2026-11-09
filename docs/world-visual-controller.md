# Koordynator Środowiska Wizualnego Świata (ZombekWorldVisualController)

## 1. Koncepcja i Rola Koordynatora
`ZombekWorldVisualController` stanowi centralny punkt synchronizujący wszystkie efekty wizualne świata wprowadzone w Etapie 8C2. Zapewnia ścisłą separację pomiędzy **Prawdziwym Stanem Świata (World State)** a **Wizualnym Stanem Nadpisania (Visual Override State)**.

## 2. Deterministyczny Potok Renderowania (Priority Pipeline)
Przetwarzanie efektów w każdej klatce renderowania przebiega w ściśle określonej, deterministycznej kolejności:

```mermaid
flowchart LR
    A[Stan Waniliowy Świata] --> B[1. Weather Visual Override]
    B --> C[2. Time Visual Override]
    C --> D[3. Dynamic Lighting Computation]
    D --> E[4. Full Bright Lightmap Post-Processing]
    E --> F[Wyrenderowana Klatka Gry]
```

1. **Vanilla World State:** Odczyt pierwotnych danych ze świata gry (ticki, pozycja gracza, warunki pogodowe, oświetlenie bloków).
2. **Weather Visual Override:** Obliczenie gradientu deszczu i burzy z uwzględnieniem biomu oraz płynnych przejść czasowych.
3. **Time Visual Override:** Obliczenie kąta słońca na niebie (`visualSkyAngle`) oraz wizualnego czasu (`visualTimeOfDay`) na podstawie wybranego presetu lub czasu niestandardowego.
4. **Dynamic Lighting:** Zbiórka kandydatów światła (trzymane przedmioty, leżący loot, płonące moby), sortowanie dystansowe i kalkulacja natężenia lokalnego oświetlenia.
5. **Full Bright Post-processing:** Korekta gamma i luminancji w shaderze lightmapy z kompensacją nasycenia barw i zachowaniem subtelnych cieni.

## 3. Cykl Życia i Reset (Lifecycle Management)
- `onClientTick(x, y, z)` — aktualizacja stanu animacji interpolacyjnych Full Bright, przejść czasu i pogody oraz zbiórka źródeł dynamicznego światła.
- `onWorldChange()` — automatyczne, natychmiastowe czyszczenie źródeł światła i reset interpolacji przy zmianie wymiaru lub rozłączeniu z serwerem.
- `shutdown()` — bezpieczne zwolnienie zasobów i przywrócenie domyślnych wartości renderowania podczas zamykania gry.
- `createSnapshot()` — generowanie niemutowalnego rekordu `WorldVisualOverrideState` do celów diagnostycznych, testów jednostkowych oraz wglądu w stan silnika.
