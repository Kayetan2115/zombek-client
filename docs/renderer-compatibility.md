# Kompatybilność z Silnikami Renderowania i Shaderami (Renderer Compatibility)

## 1. Zakres Kompatybilności
Etap 8C2 projektowany był ze szczególnym uwzględnieniem kompatybilności z popularnymi modami optymalizacyjnymi i graficznymi dla Minecraft 1.20.4:
- **Vanilla Renderer:** Domyślny potok renderowania Minecraft.
- **Sodium / Embeddium:** Nowoczesny silnik renderowania oparty o OpenGL 4.3 i własne pipeline'y vertexów.
- **Iris Shaders / Oculus:** Silnik obsługi zewnętrznych shaderów optycznych (OptiFine/BSL/Complementary format).
- **Indium:** Warstwa zgodności Fabric Rendering API dla Sodium.

## 2. Strategia Bezpieczeństwa Pipeline
Aby zapobiec konfliktom z Sodium oraz Iris:
1. **Lightmap Interception zamiast Overwrite:** Pełne rozjaśnienie (Full Bright) wstrzykuje się do metody `LightmapTextureManager.update`, modulując współczynnik gammy w locie. Nie modyfikuje obiektów buforów Sodium (`ChunkBuilder`) ani nie uszkadza struktury tabeli kolorów światła.
2. **Iris Shaders Detection & Fallback:** W przypadku aktywacji zewnętrznych shaderów ze zintegrowanymi modelami oświetlenia wolumetrycznego, `DynamicLightManager` oraz `FullBrightController` respektują flagi ochrony (`preserveVanillaLighting = true` lub detekcja aktywnego pakietu shaderów), delegując kalkulację oświetlenia do shadera zewnętrznego.
3. **Nieinwazyjny Time Changer:** Zmiana czasu przez modyfikację zwracanego `getSkyAngle` i `getTimeOfDay` w `World` jest w pełni respektowana przez renderer nieba Sodium/Iris bez wywoływania desynchronizacji ze słońcem shaderów.
4. **Izolacja Cząsteczek Pogody:** Limit cząsteczek deszczu i śniegu w `WeatherVisualController` działa w warstwie logicznej, co zapobiega przepełnieniu bufora cząsteczek w Sodium.
