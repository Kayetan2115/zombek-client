# Detekcja serwera Hypixel — HypixelServerDetector

## 1. Cel i zasady działania

Komponent `HypixelServerDetector` (`pl.zombek.client.core.hypixel.HypixelServerDetector`) odpowiada za bezbłędne, pasywne i natychmiastowe rozpoznawanie, czy gracz jest aktualnie połączony z oficjalną siecią serwerów **Hypixel Network**, czy znajduje się w świecie jednoosobowym (Singleplayer), czy też na innym serwerze multiplayer.

### Nienaruszalne reguły bezpieczeństwa i dyskrecji:
- **Zero zapytań DNS:** Detektor nie wykonuje żadnych zewnętrznych zapytań resolvera DNS w tle.
- **Zero pakietów i pingów:** Detektor nie wysyła żadnych komend tekstowych, pakietów handshake ani pingów ICMP/TCP w celu weryfikacji serwera.
- **Pasywna inspekcja danych połączenia:** Wykorzystywany jest wyłącznie ciąg znaków adresu hosta przekazany przez silnik Minecrafta (`ServerData.address` lub odpowiednik w adapterze platformowym).

---

## 2. Stany detekcji (`HypixelServerStatus`)

Detektor klasyfikuje połączenie do jednego z 5 stanów wyliczeniowych:

```java
public enum HypixelServerStatus {
    HYPIXEL_DETECTED,        // Wykryto oficjalny serwer Hypixel Network
    CUSTOM_HYPIXEL_PROFILE,  // Wymuszenie profilu Hypixel (np. tryb FORCE_ON)
    OTHER_SERVER,            // Wykryto inny serwer multiplayer
    UNKNOWN_SERVER,          // Brak danych o serwerze / stan nieustalony
    SINGLEPLAYER             // Gracz znajduje się w trybie jednoosobowym
}
```

---

## 3. Wzorce rozpoznawania domen

Detektor analizuje adres hosta przy użyciu metody `normalizeAddress()` (usunięcie portów, spacji i konwersja na małe litery):

- `hypixel.net`
- `mc.hypixel.net`
- `*.hypixel.net` (dowolna subdomena kończąca się na `.hypixel.net`, np. `alpha.hypixel.net`, `test.hypixel.net`)
- Opcjonalne adresy IP oficjalnych klastrów Hypixel, o ile zostaną jawnie zdefiniowane.

Jeśli adres pasuje do jednego z wzorców, detektor ustawia stan `HYPIXEL_DETECTED`.

---

## 4. Tryby pracy detektora (`DetectionMode`)

Użytkownik w menu `Right Shift` -> `Hypixel Mods` -> `Wykrywanie serwera` może wybrać jeden z 3 trybów:

1. **`AUTO` (Domyślny):**
   - Pełna automatyzacja. Moduł włącza optymalizacje Hypixel Mods wyłącznie po połączeniu z Hypixelem. Po przejściu na inny serwer lub do trybu Singleplayer funkcje Hypixel są bezpiecznie wygaszane.
2. **`FORCE_ON`:**
   - Wymuszenie profilu Hypixel (`CUSTOM_HYPIXEL_PROFILE`). Przydatne dla graczy łączących się przez niestandardowe proxy, VPN lub tunel lokalny, gdzie nazwa hosta nie kończy się na `hypixel.net`.
3. **`FORCE_OFF`:**
   - Całkowite wyłączenie modułu Hypixel Mods bez względu na podłączony serwer. Zapewnia zerowy wpływ na rozgrywkę.

---

## 5. Obsługa rozłączeń i ponownych połączeń

Podczas zdarzeń `onDisconnect()` lub zmiany serwera:
1. Stan detektora jest natychmiast resetowany do `UNKNOWN_SERVER` lub `SINGLEPLAYER`.
2. Aktywne toasty powiadomień oraz karty sugestii socjalnych są czyszczone z pamięci.
3. Polityka bezpieczeństwa `HypixelSafetyController` zostaje zsynchronizowana z nowym stanem.
4. Pamięć podręczna autouzupełniania komend i filtrów czatu zachowuje konfigurację użytkownika, lecz wstrzymuje interceptory do czasu ponownego wykrycia serwera.

Wszystkie scenariusze detekcji zostały zweryfikowane w teście jednostkowym `HypixelServerDetectorTest.java` (6 przypadków testowych, 100% zaliczone).
