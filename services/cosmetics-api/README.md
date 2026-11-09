# Cosmetics API Service — ZombekClient

## 1. Przeznaczenie i zakres modułu

`services/cosmetics-api` to moduł dedykowany dla zewnętrznej usługi backendowej zarządzającej systemem kosmetyków ZombekClient.

Odpowiada docelowo za:
- serwowanie peleryn (capes), skinów, skrzydeł, czapek świątecznych oraz unikalnych dodatków wizualnych powiązanych z maskotką Ząbek;
- synchronizację kosmetyków pomiędzy graczami korzystającymi z ZombekClient w czasie rzeczywistym;
- weryfikację uprawnień gracza do posiadanych przedmiotów kosmetycznych;
- integrację z CDN dla optymalizacji dostarczania tekstur.

## 2. Separacja architektoniczna

Moduł ten celowo znajduje się w katalogu `services/` i jest odseparowany od:
- kodu launchera (`apps/launcher`),
- kodu modyfikacji Minecraft (`apps/minecraft-client`),
- wspólnych bibliotek desktopowych (`packages/*`).

**Zasada projektowa:** Launcher ani Mod nie posiadają bezpośrednich powiązań bazodanowych ani logiki serwerowej. Komunikacja odbywa się wyłącznie przez REST API / WebSocket na podstawie zdefiniowanych kontraktów HTTP/JSON.

## 3. Planowane punkty końcowe (Etap przyszły)

- `GET /v1/cosmetics/user/:uuid` — pobieranie aktywnego zestawu kosmetyków gracza
- `GET /v1/cosmetics/textures/:hash` — pobieranie zbuforowanej tekstury (PNG)
- `POST /v1/cosmetics/equip` — zmiana aktywnego przedmiotu kosmetycznego (wymaga tokenu autoryzacji)
- `GET /v1/health` — monitorowanie stanu usługi

## 4. Status w Etapie 1

W Etapie 1 (Fundamenty) moduł pozostaje wyłącznie specyfikacją architektoniczną. Zgodnie z wytycznymi nie implementujemy jeszcze backendu, bazy danych ani serwera API.
