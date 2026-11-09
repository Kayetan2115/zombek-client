# Profile i Presety Animacji (Animation Profiles) — ZombekClient

## 1. Dostępne Presety Fabryczne

System 1.7 Visuals oferuje cztery główne profile animacji (`AnimationPreset`):

### 1.1. Preset `Modern`
- Pełna zgodność ze standardowym zachowaniem nowoczesnego Minecrafta (1.20.4).
- Wszystkie klasyczne animacje są wyłączone.
- Służy jako baza odniesienia dla graczy preferujących domyślne zachowanie gry.

### 1.2. Preset `Minecraft 1.8`
- Odtwarza charakterystykę animacji z wersji 1.8 (Bountiful Update):
  - **Blocking:** Styl `VANILLA_1_8` (delikatniejszy przechył miecza).
  - **Blockhit:** Styl `CLASSIC_1_8` z subtelniejszym ruchem ramienia.
  - **Swing:** Płynniejszy zamach z wersji 1.8.
  - **Sneak:** Przejście `SMOOTH_1_7` (szybkie wygładzone).
  - **Item Switching:** Prędkość 1.5x.
  - **Red Armor:** Wyłączony (w wersji 1.8 zbroja nie była barwiona na czerwono).

### 1.3. Preset `Minecraft 1.7`
- Autentyczne klasyczne zachowanie z Minecraft 1.7.10:
  - **Blocking:** Styl `VANILLA_1_7` (klasyczne ułożenie: $X=-0.14$, $Y=0.15$, $Z=-0.2$, rotacja: $-102^\circ$, $-59^\circ$, $79^\circ$).
  - **Blockhit:** Styl `CLASSIC_1_7` z pełnym ruchem ręki opartym o funkcje sinusoidalne.
  - **Swing:** Szeroki, błyskawiczny zamach 1.7.
  - **Sneak:** Styl `INSTANT_DOWN_SMOOTH_UP` (błyskawiczny skok w dół, płynne unoszenie).
  - **Item Switching:** Prędkość 2.0x (błyskawiczna zmiana).
  - **Red Armor:** Włączony (zbroja i ciało barwione na czerwono podczas ciosu).
  - **Health:** Włączony klasyczny obrys serc i miganie.

### 1.4. Profil `Custom`
- Pełna swoboda konfiguracji każdego suwaka, kąta i przełącznika przez użytkownika.
- Możliwość zapisu pod własną nazwą, powielania i resetowania.

---

## 2. Zarządzanie Profilami (`AnimationProfileManager`)

W klasie [AnimationProfileManager.java](file:///apps/minecraft-client/core/src/main/java/pl/zombek/client/core/modules/visual/oneseven/AnimationProfileManager.java) zaimplementowano pełen zestaw operacji:
- **`applyPreset(AnimationPreset preset)`:** natychmiastowe zaaplikowanie parametrów fabrycznych.
- **`saveCustomProfile(String name)`:** utrwalenie bieżącej konfiguracji pod unikalną nazwą.
- **`renameProfile(String oldName, String newName)`:** zmiana nazwy profilu.
- **`duplicateProfile(String sourceName, String newName)`:** klonowanie istniejącego profilu.
- **`resetProfile(String name)`:** przywrócenie profilu do wartości bazowych 1.7.
- **`exportProfile(String name)`:** eksport profilu do formatu JSON.
- **`importProfile(String jsonContent)`:** import profilu z pliku/schowka ze ścisłą walidacją.

---

## 3. Bezpieczeństwo i Walidacja Importu

Pliki profili podlegają restrykcyjnej walidacji:
1. Sprawdzanie poprawności schematu JSON (`schema_version <= 1`).
2. Odrzucanie wszelkich nieznanych lub podejrzanych struktur obiektowych.
3. Automatyczna deduplikacja nazw profili przy kolizji (np. `Profil (1)`).
4. Brak jakiejkolwiek możliwości wykonania obcego kodu lub deserializacji nieznanych klas Java.
