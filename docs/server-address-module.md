# Server Address Module — ZombekClient (Etap 8B4)

## 1. Przeznaczenie i Rola Modułu

Moduł `ServerAddressModule` (`"server_address"`) w ZombekClient dostarcza uniwersalny, bezpieczny komponent HUD wyświetlający informacje o bieżącym połączeniu sieciowym lub lokalnym świecie gracza.

Moduł zarejestrowany jest w kategorii `ModuleCategory.HUD` („HUD / Informacyjne”), współpracuje z `HudComponentModel`, kontrolerem prywatności `ServerAddressPrivacyController`, menedżerem aliasów `ServerAliasManager` oraz systemem kolizji `HudPriorityResolver` (priorytet: **24**).

Pliki źródłowe:
- Moduł HUD: [ServerAddressModule.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/serveraddress/ServerAddressModule.java)
- Układy prezentacji: [ServerAddressLayout.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/hud/serveraddress/ServerAddressLayout.java)
- Kontroler prywatności: [ServerAddressPrivacyController.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/serveraddress/ServerAddressPrivacyController.java)
- Menedżer aliasów: [ServerAliasManager.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/serveraddress/ServerAliasManager.java)
- Tryby ochrony prywatności: [PrivacyMode.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/serveraddress/PrivacyMode.java)
- DTO połączenia: [ServerConnectionData.java](file:///c:/Users/Admin/Desktop/jules_session_17875811770345124279/apps/minecraft-client/core/src/main/java/pl/zombek/client/core/serveraddress/ServerConnectionData.java)

---

## 2. Architektura i Ochrona Prywatności

```mermaid
flowchart TD
    subgraph Connection State
        SP[Singleplayer World]
        MP[Multiplayer Server]
        LAN[LAN World]
    end

    subgraph Loader Providers
        FabProv[FabricServerAddressDataProvider]
        ForgeProv[ForgeServerAddressDataProvider]
        NeoProv[NeoForgeServerAddressDataProvider]
        QuiltProv[QuiltServerAddressDataProvider]
    end

    subgraph Privacy Engine
        AliasMgr[ServerAliasManager (Local Only)]
        PrivCtrl[ServerAddressPrivacyController]
    end

    subgraph Server Address HUD
        SAMod[ServerAddressModule (Priority: 24)]
        BuildLines[buildDisplayLines()]
        HudRen[HudComponentRenderer]
    end

    SP & MP & LAN --> FabProv & ForgeProv & NeoProv & QuiltProv
    FabProv & ForgeProv & NeoProv & QuiltProv --> PrivCtrl
    AliasMgr --> PrivCtrl
    PrivCtrl --> SAMod --> BuildLines --> HudRen
```

1. **Domyślna ochrona prywatności (Default OFF for Full Address):** Wyświetlanie pełnego adresu surowego jest **domyślnie wyłączone** (`show_server_address: false`), a domyślny tryb prywatności to `MASK_HOST`. Zapobiega to przypadkowemu ujawnieniu adresu prywatnego serwera lub IP na streamie czy zrzucie ekranu.
2. **Singleplayer vs Multiplayer:**
   - W świecie jednoosobowym moduł wyświetla napis **"Singleplayer"** oraz nazwę świata (lub `[HIDDEN]` gdy włączono ukrywanie). **NIGDY nie generuje fałszywych adresów IP (np. `127.0.0.1` czy `localhost`).**
   - Na serwerze wieloosobowym moduł wyświetla zamaskowany host (np. `pla*****.net`), niestandardowy alias nadany przez gracza, lub nazwę serwera z listy serwerów.
3. **Kopiowanie do schowka:** Moduł udostępnia funkcję `copyAddressToClipboard()`, która kopiuje rzeczywisty adres do schowka systemowego wyłącznie w wyniku świadomej akcji użytkownika (np. kliknięcia przycisku w menu konfiguracji).

---

## 3. Tryby Układu (`ServerAddressLayout`)

1. **`COMPACT` (Domyślny):** Etykieta `Server: <host/alias>` (np. `Server: pla****.net`).
2. **`DETAILED`:** Rozbicie na wiersze: Typ połączenia, Nazwa serwera, Adres/Alias, Port, Status, Wymiar.
3. **`MINIMAL`:** Sam host lub alias (np. `Hypixel`).
4. **`CONNECTION_INFO`:** Pełny panel diagnostyczny połączenia sieciowego.

---

## 4. Opcje Konfiguracyjne (Grupy Ustawień)

- **General (`group_general`):**
  - `show_connection_type`: Pokazuje typ połączenia (Singleplayer, Multiplayer, LAN, domyślnie `true`).
  - `show_server_address`: Pokazuje adres serwera (**domyślnie `false`** dla ochrony prywatności).
  - `show_port`: Pokazuje port serwera (domyślnie `false`).
  - `show_server_name`: Pokazuje nazwę serwera z listy serwerów (domyślnie `true`).
  - `show_connection_status`: Wskaźnik statusu (CONNECTED / DISCONNECTED, domyślnie `true`).
  - `show_dimension`: Pokazuje bieżący wymiar (domyślnie `false`).
- **Display (`group_display`):**
  - `layout`: Tryb układu (`COMPACT`, `DETAILED`, `MINIMAL`, `CONNECTION_INFO`).
  - `custom_alias_enabled`: Włączanie niestandardowych aliasów (domyślnie `true`).
  - `alias_priority`: Priorytet aliasu nad nazwą serwera (domyślnie `true`).
- **Privacy (`group_privacy`):**
  - `privacy_mode`: Poziom ochrony (`NORMAL`, `HIDE_ADDRESS`, `MASK_HOST`, `HIDE_PORT`, `SHOW_ALIAS_ONLY`, `STREAMER_MODE`, domyślnie `MASK_HOST`).
  - `streamer_mode`: Szybki przełącznik ochrony streamera (domyślnie `false`).
  - `hide_world_name`: Ukrywanie nazwy lokalnego świata w Singleplayer (domyślnie `true`).
- **Appearance & Layout:**
  - Standardowe opcje stylu, kolorów i pozycjonowania HUD ZombekClient.
