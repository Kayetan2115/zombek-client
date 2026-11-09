# Architektura GlintRenderController i integracja Shiny Pots

## 1. Zasada działania kontrolera

Klasa [GlintRenderController](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/rendereffect/glint/GlintRenderController.java) odpowiada za wyliczanie koloru glintu podczas każdego wywołania renderowania przedmiotu.

Kolor jest wyliczany w czasie rzeczywistym i przekazywany bezpośrednio jako modyfikator koloru wierzchołków lub parametru shadera bez tworzenia nowych obiektów `RenderType`.

## 2. Hierarchia priorytetów (z modułem Shiny Pots)

Gdy renderowany przedmiot to mikstura lub strzała z miksturą, zachodzi rozstrzyganie priorytetów według ustawienia `shiny_pots_priority`:

```mermaid
flowchart TD
    Start["Wywołanie resolveGlintColor()"] --> LocCheck{"Czy lokalizacja jest włączona?"}
    LocCheck -- Nie --> Vanilla["Zwróć glint waniliowy lub 0"]
    LocCheck -- Tak --> PotCheck{"Czy to mikstura (Shiny Pots eligible)?"}
    
    PotCheck -- Tak --> PriCheck{"Jaki jest shinyPotsPriority?"}
    PriCheck -- PRESERVE_SHINY_POTS --> ShinyPots["Użyj koloru z ShinyPotsController"]
    PriCheck -- USE_POTION_SPECIFIC --> PotionCat["Użyj konfiguracji kategorii POTIONS"]
    PriCheck -- USE_GLOBAL / OVERRIDE --> CatCheck
    
    PotCheck -- Nie --> CatCheck{"Czy kategoria ma aktywny override?"}
    CatCheck -- Tak --> CatAnim["Oblicz animację dla kategorii"]
    CatCheck -- Nie --> GlobalAnim["Oblicz animację dla ustawień globalnych"]
```

## 3. Brak modyfikacji NBT

Moduł nigdy nie dodaje tagu `Enchantments` ani nie zmienia właściwości `ItemStack`. Wstrzykiwanie w kod gry odbywa się w warstwie renderera modeli przedmiotów i `ItemStack.hasGlint()`.
