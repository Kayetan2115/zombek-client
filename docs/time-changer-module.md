# Moduł Zmiany Czasu Świata (TimeChangerModule)

## 1. Cel i Zasada Działania
`TimeChangerModule` umożliwia lokalną, czysto wizualną modyfikację pory dnia w świecie gry Minecraft. Pozwala graczowi zablokować lub płynnie ustawić preferowany czas nieba (np. bezchmurny świt lub wieczne południe) bez wpływania na serwer.

### Kluczowa Zasada Separacji
- Prawdziwy czas świata serwera (`Level.getGameTime()` / `ClientWorld.getTime()`) pozostaje nienaruszony.
- Moduły informacyjne HUD, takie jak `ClockModule` i `DayCounterModule`, nadal odczytują rzeczywisty stan serwerowy.
- Nie dochodzi do zaburzenia cyklu snu w łóżku, wzrostu roślin czy mechanik regeneracji i spływu wody.

## 2. Presety Czasowe (TimeChangerPreset)
Moduł udostępnia predefiniowane, astronomicznie poprawne pozycje słońca i księżyca:
1. `VANILLA` — czas rzeczywisty pobierany bezpośrednio ze świata gry.
2. `SUNRISE` — wschód słońca (23000 ticków, kąt nieba $\approx 0.958$).
3. `MORNING` — poranek (1000 ticków, kąt nieba $\approx 0.042$).
4. `NOON` — samo południe (6000 ticków, kąt nieba $\approx 0.250$).
5. `AFTERNOON` — popołudnie (9000 ticków, kąt nieba $\approx 0.375$).
6. `SUNSET` — zachód słońca (12000 ticków, kąt nieba $\approx 0.500$).
7. `NIGHT` — noc (14000 ticków, kąt nieba $\approx 0.583$).
8. `MIDNIGHT` — północ (18000 ticków, kąt nieba $\approx 0.750$).
9. `CUSTOM` — dowolna wartość zdefiniowana przez użytkownika na osi 0 - 24000 ticków.

## 3. Cykliczna Interpolacja Kątowa (TimeTransitionManager)
Czas w Minecraft jest wartością modularną modulo 24000 ticków ($[0, 24000)$). Przejście między np. godziną 23000 a 1000 nie powinno przeskakiwać wstecz przez cały dzień, lecz płynnie pokonać 2000 ticków do przodu:
$$\Delta t = \min\left((t_2 - t_1) \pmod{24000}, (t_1 - t_2) \pmod{24000}\right)$$
`TimeTransitionManager` realizuje najkrótszą drogę na okręgu zegara dobowego z wykorzystaniem wygładzania Ease-In-Out.

## 4. Konfiguracja i Filtrowanie Wymiarów
- `preset` — wybór presetu czasowego.
- `custom_time` (0 - 24000 ticków, Domyślnie: `6000`) — czas dla trybu `CUSTOM`.
- `smooth_time_transition` (Domyślnie: `true`) — płynna animacja przy zmianie pory.
- `transition_speed` (100 - 5000 ms, Domyślnie: `600 ms`) — czas trwania tranzycji.
- `enable_in_overworld` (Domyślnie: `true`) — działanie w Overworld.
- `enable_in_nether` (Domyślnie: `false`) — działanie w Netherze (wymiar domyślnie pozbawiony słońca).
- `enable_in_end` (Domyślnie: `false`) — działanie w The End.
- `enable_in_custom_dimensions` (Domyślnie: `false`) — modyfikacje w wymiarach z modów.
- `fast_forward_cycle` (Domyślnie: `false`) — przyspieszony, kinowy cykl dobowy.
- `cycle_speed_multiplier` (0.1 - 20.0, Domyślnie: `1.0`) — mnożnik prędkości obrotu słońca.
