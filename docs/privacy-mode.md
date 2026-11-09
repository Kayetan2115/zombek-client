# Privacy Mode & Streamer Protection — ZombekClient (Etap 8B4)

## 1. Przeznaczenie i Rola Systemu

System ochrony prywatności i tryb streamera w ZombekClient chronią wrażliwe dane gracza przed przypadkowym ujawnieniem na transmisjach na żywo (Twitch, YouTube, Kick), nagraniach wideo oraz zrzutach ekranu.

Wrażliwe dane objęte ochroną:
1. **Adresy IP i domeny serwerów:** Prywatne adresy SMP znajomych, serwery z whitelisty, numery portów niestandardowych.
2. **Nazwy światów lokalnych (Singleplayer):** Nazwy folderów i światów gracza, które mogą zawierać dane osobowe lub poufne projekty.
3. **Status połączenia:** Unikanie ujawniania specyficznych routingów sieciowych.

Pliki źródłowe:
- Kontroler prywatności: [ServerAddressPrivacyController.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/serveraddress/ServerAddressPrivacyController.java)
- Tryby prywatności: [PrivacyMode.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/serveraddress/PrivacyMode.java)
- Menedżer aliasów: [ServerAliasManager.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/serveraddress/ServerAliasManager.java)

---

## 2. Tryby Prywatności (`PrivacyMode`)

| Tryb | Zachowanie dla Hosta | Zachowanie dla Portu | Zastosowanie |
| :--- | :--- | :--- | :--- |
| **`NORMAL`** | Wyświetla pełny host (np. `play.hypixel.net`) | Pokazuje port jeśli różny od 25565 | Serwery publiczne, zaufane środowisko |
| **`MASK_HOST` (Domyślny)** | Maskuje środek hosta (np. `pla*****.net`) | Ukrywa port | Domyślna bezpieczna ochrona |
| **`HIDE_ADDRESS`** | Zastępuje host napisem `[HIDDEN]` | Całkowicie ukrywa port | Pełna anonimizacja |
| **`HIDE_PORT`** | Pokazuje host, ale ukrywa port | Zawsze ukrywa port | Serwery z dedykowanymi portami |
| **`SHOW_ALIAS_ONLY`** | Pokazuje wyłącznie alias użytkownika | Całkowicie ukrywa port | Streamowanie ze stałymi aliasami |
| **`STREAMER_MODE`** | Zastępuje host napisem `[HIDDEN]` | Całkowicie ukrywa port i nazwę świata | Transmisje na żywo |

---

## 3. Lokalny Menedżer Aliasów (`ServerAliasManager`)

Gracz może przypisać każdemu serwerowi przyjazny alias lokalny (oraz niestandardowy kolor tekstu w formacie ARGB).
- **Całkowicie lokalne przechowywanie:** Aliasy zapisywane są wyłącznie w lokalnej konfiguracji klienta (`config/zombekclient/aliases.json`). Nigdy nie są przesyłane przez sieć ani raportowane serwerom.
- **Normalizacja adresów:** Klucze są znormalizowane (usunięcie końcówki `:25565`, małe litery), co gwarantuje jednoznaczne dopasowanie bez względu na wielkość liter w domenie.

---

## 4. Ochrona Świata Jednoosobowego (Singleplayer Protection)

W trybie Singleplayer:
- Moduł `ServerAddressModule` jednoznacznie prezentuje typ jako **Singleplayer**.
- W przeciwieństwie do błędnych implementacji w niektórych klientach zewnętrznych, ZombekClient **nigdy nie wyświetla fałszywych adresów IP (np. `127.0.0.1` czy `localhost`)** w grze jednoosobowej.
- Opcja `hide_world_name` (domyślnie włączona) zastępuje nazwę świata etykietą `[HIDDEN]` lub `Singleplayer World`.
