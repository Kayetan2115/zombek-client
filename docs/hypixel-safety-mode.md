# Tryb bezpieczeństwa i Fair Play — HypixelSafetyController

## 1. Filozofia bezpieczeństwa

Serwer **Hypixel** stosuje zaawansowane systemy anticheat (Watchdog) oraz rygorystyczny regulamin dotyczący dozwolonych modyfikacji klienta (Rules & Policies: Allowed Modifications). Naruszenie tych zasad grozi trwałą blokadą konta (Ban).

Aby uchronić użytkowników ZombekClient przed jakimkolwiek ryzykiem, zaimplementowano scentralizowany moduł nadzorczy **`HypixelSafetyController`** (`pl.zombek.client.core.hypixel.HypixelSafetyController`).

> [!IMPORTANT]
> **Zasada domyślnego rygoru:**
> Każda funkcja, która nie kwalifikuje się jednoznacznie jako czysta modyfikacja wizualna lub lokalny element UI, jest **domyślnie blokowana** w profilu `STRICT_SAFETY`.

---

## 2. Profile bezpieczeństwa (`SafetyPreset`)

W module dostępne są 3 profile bezpieczeństwa:

1. **`STRICT_SAFETY` (Domyślny dla Hypixel Network):**
   - Pozwala wyłącznie na akcje z kategorii `COSMETIC`, `LOCAL_UI`, `LOCAL_NOTIFICATION` oraz `LOCAL_AUTOCOMPLETE`.
   - Zezwala na lokalne formatowanie czatu, filtry spamu, podpowiedzi ze słownika lokalnego oraz estetyczne powiadomienia HUD.
   - **Bezwzględnie blokuje** `GAMEPLAY_AUTOMATION`, `NETWORK_MODIFICATION` oraz `UNKNOWN`.
2. **`CUSTOM` (Tryb zaawansowany):**
   - Pozwala graczowi na ręczne dostosowanie poszczególnych uprawnień.
   - Wymaga potwierdzenia świadomości ryzyka przez użytkownika.
3. **`OTHER_SERVERS` (Dla serwerów prywatnych / innych sieci):**
   - Używany, gdy gracz nie znajduje się na Hypixelu. Odblokowuje specyficzne funkcje lokalne bez rygoru Watchdoga.

---

## 3. Kategorie operacji (`SafetyCategory`)

Każda operacja zgłaszana do `HypixelSafetyController.isActionAllowed(category)` podlega weryfikacji na podstawie poniższej kategoryzacji:

| Kategoria operacji | Przeznaczenie | Status w `STRICT_SAFETY` |
| :--- | :--- | :---: |
| `COSMETIC` | Zmiana kolorów tekstu, skracanie prefiksów kanałów, animacja powiadomień. | **DOZWOLONE** |
| `LOCAL_UI` | Renderowanie kart HUD, tablica sugestii GG/GL, przycisk kopiowania do schowka. | **DOZWOLONE** |
| `LOCAL_NOTIFICATION` | Płynne powiadomienia toast informujące o zaproszeniach do drużyny lub gildii. | **DOZWOLONE** |
| `LOCAL_AUTOCOMPLETE` | Lokalny słownik komend Hypixela bez wysyłania pakietów sieciowych. | **DOZWOLONE** |
| `GAMEPLAY_AUTOMATION` | Automatyczne wysyłanie wiadomości na czat, makra komend, auto-tip. | **ZABLOKOWANE** |
| `NETWORK_MODIFICATION` | Zmiana kolejności pakietów, modyfikacja handshake, sztuczne pakiety ping. | **ZABLOKOWANE** |
| `UNKNOWN` | Nieznane operacje nieposiadające jednoznacznej klasyfikacji. | **ZABLOKOWANE** |

---

## 4. Wyjaśnienia blokad w języku polskim (`getBlockReason`)

Gdy użytkownik podejmie próbę włączenia lub wykonania operacji niedozwolonej na Hypixelu, kontroler generuje precyzyjne, czytelne uzasadnienie w języku polskim, np.:

- Dla `GAMEPLAY_AUTOMATION`:
  > *"Automatyzacja rozgrywki i czatu (np. AutoGG/AutoTip) jest surowo zabroniona na Hypixelu w celu ochrony konta przed banem anticheat Watchdog."*
- Dla `NETWORK_MODIFICATION`:
  > *"Modyfikacja pakietów sieciowych narusza zasady Fair Play sieci Hypixel."*
- Dla `UNKNOWN`:
  > *"Nieznana operacja została zablokowana przez politykę Strict Safety w celu zapewnienia pełnego bezpieczeństwa konta."*

Wszystkie scenariusze weryfikacji bezpieczeństwa zostały przetestowane w `HypixelSafetyControllerTest.java` (5 testów, 100% zaliczone).
