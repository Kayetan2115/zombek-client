# Architektura Bezpieczeństwa Formatu MRPACK — ZombekClient

Niniejszy dokument przedstawia całościową analizę zagrożeń oraz mechanizmy obronne (Defense-in-Depth) zaimplementowane w obsłudze formatu **Modrinth Modpack** (`.mrpack`) w launcherze ZombekClient.

---

## 1. Wstęp i Model Zagrożeń

Pliki `.mrpack` pobierane są z internetu lub otrzymywane od innych użytkowników, co klasyfikuje je jako dane z niezaufanego źródła. Do głównych wektorów ataków związanych z formatem ZIP i archiwami modpacków należą:
1. **Zip Slip (Path Traversal):** Złośliwie spreparowane nazwy wpisów w archiwum zawierające sekwencje `..` mające na celu nadpisanie krytycznych plików systemowych (np. w `C:\Windows` lub katalogach autostartu).
2. **Zip Bomb (Decompression Bomb):** Niewielkie archiwum (np. 10 MB), które podczas rozpakowywania rozrasta się do setek gigabajtów, powodując wyczerpanie pamięci RAM lub zapełnienie dysku (Denial of Service).
3. **Symlink Traversal Attacks:** Utworzenie w archiwum dowiązania symbolicznego wskazującego na poufny plik systemowy, a następnie odczyt lub nadpisanie tego pliku.
4. **Server-Side Request Forgery (SSRF) & Złośliwe CDN:** Podawanie w manifeście adresów URL wskazujących na lokalną sieć użytkownika (`http://192.168.1.1`, `http://localhost`) w celu skanowania portów lub pobieranie plików przez niezabezpieczone protokoły HTTP.
5. **Przechwycenie i Podmiana Plików (Tampering):** Próba podmiany pliku `.jar` na złośliwe oprogramowanie (np. trojan kradnący dane przeglądarek).
6. **Wyciek Prywatnych Danych Gracza (Data Leakage):** Niezamierzone dołączenie prywatnych tokenów autoryzacyjnych Microsoftu, zapisów światów gracza czy logów czatu do eksportowanego archiwum modpacka.

---

## 2. Mechanizmy Obronne (Defense-in-Depth)

### 2.1. Wielopoziomowy Walidator Ścieżek (`MrpackPathValidator`)
Moduł `MrpackPathValidator` stosuje rygorystyczne reguły bezpieczeństwa dla każdego pliku:

```
Ścieżka wejściowa
      │
      ├─ 1. Sprawdzenie Null Byte: relPath.includes('\0') ➔ ODRZUCENIE
      ├─ 2. Konwersja separatorów: zamiana '\\' na '/'
      ├─ 3. Sprawdzenie ścieżki bezwzględnej: startsWith('/') ➔ ODRZUCENIE
      ├─ 4. Sprawdzenie liter dysków Windows: /^[a-zA-Z]:/ ➔ ODRZUCENIE
      ├─ 5. Sprawdzenie ścieżek sieciowych UNC: // lub \\ ➔ ODRZUCENIE
      ├─ 6. Podział na segmenty:
      │     ├─ Czy segment === '..' ➔ ODRZUCENIE (Zip Slip)
      │     ├─ Czy nazwa to urządzenie Windows (CON, NUL, AUX, COM1-9, LPT1-9) ➔ ODRZUCENIE
      │     └─ Czy zawiera znaki zakazane (< > : " | ? *) ➔ ODRZUCENIE
      ├─ 7. Normalizacja Posix: path.posix.normalize(...)
      │     └─ Czy normalized === '..' lub startsWith('../') ➔ ODRZUCENIE
      └─ 8. assertSafePath: Weryfikacja, czy cel mieści się fizycznie w baseDir
```

### 2.2. Twarde Limity Zasobów (Ochrona przed Zip Bomb)
W module `MrpackArchiveReader` zdefiniowano sztywne limity chroniące przed atakami DoS i wyczerpaniem pamięci:

| Parametr bezpieczeństwa | Wartość limitu | Działanie przy przekroczeniu |
| :--- | :---: | :--- |
| `MAX_ARCHIVE_SIZE_BYTES` | **500 MB** | Natychmiastowe przerwanie inspekcji z błędem walidacji. |
| `MAX_UNCOMPRESSED_TOTAL_BYTES` | **2 GB** | Zatrzymanie dekompresji przy przekroczeniu sumy bajtów wpisów. |
| `MAX_SINGLE_ENTRY_BYTES` | **250 MB** | Odrzucenie wpisu przekraczającego dopuszczalną wielkość. |
| `MAX_MANIFEST_SIZE_BYTES` | **5 MB** | Odrzucenie spreparowanego, gigantycznego `modrinth.index.json`. |
| `MAX_ENTRIES_COUNT` | **20 000** | Ochrona przed archiwami z milionami mikroskopijnych plików. |

### 2.3. Blokada Dowiązań Symbolicznych (Symlink Traversal)
System ZombekClient w module `MrpackArchiveReader` analizuje nagłówki wpisów w Central Directory:
- Odczytywany jest nagłówek `externalFileAttributes` (przesunięcie +38 w nagłówku Central Directory).
- W systemach uniksowych bity typu pliku znajdują się w starszych 16 bitach (`externalFileAttributes >> 16`).
- Maska typu pliku dla dowiązania symbolicznego wynosi `0o120000` (`S_IFLNK`).
- Jeśli `(attrs >> 16) & 0o170000 === 0o120000`, wpis zostaje oznaczony jako `isSymlink: true`, a parser rzuca wyjątek bezpieczeństwa:
  > *"Wykryto niebezpieczny wpis dowiązania symbolicznego (symlink). Operacja została zablokowana."*

### 2.4. Bezpieczeństwo Sieciowe i Biała Lista Hostów
Moduł `MrpackImporter` weryfikuje każdy adres URL przed nawiązaniem połączenia:
1. **Wymuszenie protokołu HTTPS:** Wszelkie adresy `http://`, `ftp://` lub protokoły lokalne (`file://`) są bezwzględnie blokowane.
2. **Restrykcyjna biała lista domen CDN:**
   - `cdn.modrinth.com`
   - `edge.modrinth.com`
   - `github.com` (oficjalne wydania modów powiązane z Modrinth)
   - `raw.githubusercontent.com`
   - `gitlab.com`
3. **Blokada adresów lokalnych i prywatnych (Ochrona SSRF):**
   - Blokowane są adresy: `localhost`, `127.0.0.1`, `::1`.
   - Blokowane są prywatne zakresy IPv4: `10.0.0.0/8`, `192.168.0.0/16`, `172.16.0.0/12`, `169.254.0.0/16` (link-local).
4. **Limit przekierowań HTTP:** Maksymalnie 3 przekierowania 3xx; każde przekierowanie jest ponownie sprawdzane pod kątem białej listy domen.

### 2.5. Podwójna Weryfikacja Integralności Kryptograficznej
Każdy pobrany plik modyfikacji `.jar`:
- Zostaje pobrany do izolowanego folderu tymczasowego staging (`temp-mrpack/import-<timestamp>/`).
- Następuje kalkulacja sumy kontrolnej **SHA-512** (128 znaków szesnastkowych) oraz **SHA-1** (40 znaków szesnastkowych).
- Następuje porównanie z sumą zadeklarowaną w manifeście `modrinth.index.json`.
- Jeśli choć jeden bajt różni się od deklaracji:
  - Plik zostaje natychmiast usunięty z dysku.
  - Wyzwalany jest automatyczny `rollback` całej operacji.
  - Pliki gry w docelowej instalacji pozostają w 100% nienaruszone.

### 2.6. Ochrona Prywatności Gracza przy Eksporcie
Moduł `MrpackOverrideManager` podczas przygotowywania archiwum eksportu aktywnie eliminuje wszelkie pliki mogące zawierać poufne dane gracza:
- **Konta i Sesje:** `launcher_accounts.json`, `.session`, `usercache.json`, `usernamecache.json`.
- **Światy:** Katalog `saves/` jest całkowicie pomijany.
- **Logi:** `logs/`, `latest.log`, `crash-reports/` — eliminacja wycieków wiadomości z czatu i adresów serwerów.
- **Zrzuty ekranu:** Katalog `screenshots/` jest wykluczony.
- **Narzędzia systemowe:** `natives/`, `runtimes/`, `backups/`, `.cache/`.

---

## 3. Podsumowanie Zabezpieczeń

| Wektor ataku | Moduł obronny | Skuteczność |
| :--- | :--- | :---: |
| Zip Slip (`..`, `C:`, `\\unc`) | `MrpackPathValidator` | 100% zablokowane |
| Zastrzeżone urządzenia Windows (`CON`, `NUL`) | `MrpackPathValidator` | 100% zablokowane |
| Zip Bomb (> 500MB / > 2GB dekompresji) | `MrpackArchiveReader` | 100% zablokowane |
| Symlink Traversal (atrybuty 0o120000) | `MrpackArchiveReader` | 100% zablokowane |
| SSRF i skanowanie sieci lokalnej | `MrpackImporter.downloadSingleUrl` | 100% zablokowane |
| Podmiana pliku JAR (MitM / Fake CDN) | Weryfikacja SHA-512 & SHA-1 | 100% zablokowane |
| Wyciek haseł, tokenów i światów gracza | `MrpackOverrideManager.collectOverridesForExport` | 100% zablokowane |
