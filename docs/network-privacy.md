# Bezpieczeństwo Sieciowe i Ochrona Prywatności w Net Graph

## 1. Filozofia Non-Invasive Passive Diagnostics

Moduł **Net Graph** w ZombekClient został zaprojektowany z bezwzględnym poszanowaniem prywatności użytkownika oraz zasad Fair Play. Stanowi wyłącznie narzędzie obserwacyjne — **nie ingeruje w żaden sposób w komunikację sieciową**.

---

## 2. Rygorystyczne Zakazy Implementacyjne

W module Net Graph bezwzględnie wykluczono:
- **Packet Injection:** Brak wstrzykiwania sztucznych pakietów.
- **Packet Modification / Spoofing:** Brak manipulowania nagłówkami, payloadami lub sumami kontrolnymi.
- **Artificial Traffic / Flooding:** Brak zapytań pingujących i testów obciążeniowych (stress testing).
- **Payload Inspection / Logging:** Brak odczytywania, parsowania lub rejestrowania treści czatu, komend, haseł, tokenów autoryzacyjnych czy danych konta.
- **Telemetry / Remote Reporting:** Klient nie wysyła żadnych danych diagnostycznych ani metryk na zewnętrzne serwery.

---

## 3. Brak Pomiaru Utraty Pakietów (Packet Loss Limitation)

W protokole Minecraft opartym o TCP, utrata pakietów jest obsługiwana na poziomie warstwy transportowej systemu operacyjnego poprzez mechanizmy retransmisji. Standardowe API gry nie udostępnia bezpiecznych ani wiarygodnych statystyk o utraconych segmentach TCP.
Zgodnie z zasadą prawdomówności klienta:
- ZombekClient **nie szacuje** utraty pakietów na podstawie wysokiego pingu ani spadku TPS.
- Wartość `Packet Loss` nie jest fikcyjnie wyliczana — wyświetlane jest `N/A`.

---

## 4. Ochrona Prywatności przy Eksporcie Statystyk

Funkcje eksportu `exportToJson()` oraz `exportToText()` w `NetworkStatistics`:
- Zawierają wyłącznie zagregowane wskaźniki numeryczne (min/max/avg ping, jitter, wolumen bajtów i pakietów, czas sesji).
- **Nie zawierają** adresu IP serwera, nazwy hosta, portu, loginu gracza ani treści przesyłanych wiadomości.
- Eksport następuje wyłącznie na jawne żądanie użytkownika w panelu konfiguracyjnym.
