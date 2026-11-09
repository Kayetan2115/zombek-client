# Śledzenie Wejścia i Rejestracja Kliknięć (Input Tracking)

## 1. Architektura Śledzenia Klawiszy i Myszy

Śledzenie stanu klawiatury i myszy realizowane jest w sposób zunifikowany w pakiecie `pl.zombek.client.core.tracking`:
- `KeyTracker` — singleton przechowujący stany wciśnięcia klawiszy (W, A, S, D, SPACE, SHIFT, CTRL) oraz przycisków myszy.
- `ClickTracker` — singleton mierzący rzeczywiste kliknięcia z użyciem nanosekundowego zegara monotonicznego (`System.nanoTime()`).

---

## 2. Bezpieczeństwo GUI i Reset Stanów

Jednym z kluczowych wyzwań w klientach PvP jest unikanie „zaciętych klawiszy” (stuck keys) oraz zliczania kliknięć w elementach interfejsu (np. przyciskach w menu lub na czacie) jako kliknięć w grze (CPS).

W każdym adapterze loaderów zaimplementowano rygorystyczny warunek:
```java
if (client.screen == null && client.world != null && isWindowFocused) {
    // Aktywne śledzenie klawiszy poruszania się oraz kliknięć
} else {
    // Natychmiastowe czyszczenie stanów
    KeyTracker.getInstance().resetAll();
    wasLmbDown = false;
    wasRmbDown = false;
}
```

Dzięki temu:
1. Otwarcie menu ZombekClient (RIGHT SHIFT), czatu (T) lub ekwipunku (E) natychmiast wygasza stany wciśniętych klawiszy.
2. Kliknięcia wewnątrz GUI menu nigdy nie podbijają licznika CPS.
3. Utrata fokusu okna gry (Alt-Tab) nie powoduje pozostawienia wciśniętego stanu klawiszy.
