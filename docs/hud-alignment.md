# System Wyrównywania i Marginesów Bezpiecznych (Alignment & Safe Area) — ZombekClient (Etap 9B)

## 1. Wprowadzenie i Cel Modułu

Precyzyjne rozmieszczenie elementów interfejsu względem osi ekranu oraz zachowanie marginesów ochronnych to kluczowe funkcje profesjonalnego edytora HUD.
W Etapie 9B wprowadzono dedykowane narzędzia:
- **`HudAlignmentManager`** — wykonuje operacje szybkiego wyrównywania zaznaczonego komponentu do krawędzi lub osi symetrii ekranu.
- **`HudSafeAreaManager`** — wizualizuje marginesy bezpieczeństwa (np. chroniące przed obcięciem interfejsu na monitorach o nietypowych proporcjach).

---

## 2. Operacje Wyrównywania (HudAlignmentManager)

Wszystkie operacje wyrównania uwzględniają efektywne wymiary komponentu ($W_{eff} = W \times \mathrm{scale}$, $H_{eff} = H \times \mathrm{scale}$) oraz logiczną kotwicę (`HudAnchor`):

| Narzędzie | Obliczona Pozycja Ekranowa | Odwrotna Transformacja Kotwicy |
| :--- | :--- | :--- |
| **Align Left** | $X = 0$ (lub safe margin) | `anchor.screenXToOffsetX(0, W_{eff}, screenW)` |
| **Align Right** | $X = screenW - W_{eff}$ | `anchor.screenXToOffsetX(screenW - W_{eff}, W_{eff}, screenW)` |
| **Align Top** | $Y = 0$ (lub safe margin) | `anchor.screenYToOffsetY(0, H_{eff}, screenH)` |
| **Align Bottom** | $Y = screenH - H_{eff}$ | `anchor.screenYToOffsetY(screenH - H_{eff}, H_{eff}, screenH)` |
| **Center Horizontal** | $X = \frac{screenW - W_{eff}}{2}$ | `anchor.screenXToOffsetX(X, W_{eff}, screenW)` |
| **Center Vertical** | $Y = \frac{screenH - H_{eff}}{2}$ | `anchor.screenYToOffsetY(Y, H_{eff}, screenH)` |
| **Center All** | $X = \frac{screenW - W_{eff}}{2}, Y = \frac{screenH - H_{eff}}{2}$ | Obie osie jednocześnie |

---

## 3. Menedżer Marginesów Bezpiecznych (HudSafeAreaManager)

W grach wieloosobowych i przy dynamicznym skalowaniu okna (np. zmiana rozmiaru okna gry) elementy umieszczone zbyt blisko krawędzi mogą zlewać się z obramowaniem lub paskami stanu.
`HudSafeAreaManager` udostępnia:
- **Wizualizację Safe Margin:** Przerywana ramka pomocnicza w kolorze turkusu (`0x8000E5FF`) oddalona o 8–16 px od krawędzi ekranu gry.
- **Przełącznik w Pasku Narzędzi:** Możliwość włączenia/wyłączenia widoczności Safe Area jednym kliknięciem.
- **Wymiary Graniczne:** Zapobiega przypadkowemu wysunięciu komponentu poza obszar gry ($0 \le X \le screenW - W_{eff}$).
