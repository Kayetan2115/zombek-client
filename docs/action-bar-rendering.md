# Action Bar Rendering

Dokumentacja renderowania komunikatów Action Bar w **ZombekClient** (`CustomTitleRenderer`).

## 1. Założenia Architektoniczne

Komunikaty Action Bar (wysyłane np. przez `/title @p actionbar ...` lub wskaźniki cooldownów przedmiotów) są wyświetlane bezpośrednio nad paskiem podręcznym gracza (Hotbar).
W architekturze ZombekClient:
- Action Bar posiada w pełni **niezależny cykl życia** (`ActiveTitleState.abAgeTicks`, `abStayTicks`) od Title i Subtitle.
- Action Bar **nigdy nie trafia do historii czatu** i nie zaśmieca bufora wiadomości.
- Niezależny przełącznik w ustawieniach (`enable_action_bar`) pozwala użytkownikowi zdecydować, czy chce personalizować Action Bar niezależnie od tytułów głównych.

## 2. Warstwa Prezentacji

`CustomTitleRenderer.renderActionBar(...)` wykonuje następujące operacje:
- Wyliczenie pozycji Y (`action_bar_pos_y`): domyślnie nad paskiem ekwipunku z uwzględnieniem paska poziomu doświadczenia i paska głodu/serc.
- Skalowanie tekstu (`action_bar_scale`).
- Opcjonalne zaokrąglone tło panelu (`action_bar_background`) chroniące czytelność tekstu na jasnych tłach świata Minecraft.
- Cień tekstu oraz płynne zanikanie (Fade Out).

## 3. Integracja z Loaderami

- **Fabric / Quilt**: `InGameHudMixin.setOverlayMessage` przechwytuje komunikat, zeruje vanilla `overlayRemaining = 0` i przekazuje do `CustomTitleRenderer.getInstance().renderActionBar`.
- **Forge / NeoForge**: `RenderGuiOverlayEvent.Pre` anuluje `VanillaGuiOverlay.RECORD_OVERLAY` i deleguje renderowanie do `CustomTitleRenderer`.
