@echo off
setlocal enabledelayedexpansion

title ZombekClient - Konfiguracja GitHub
cd /d "%~dp0"

echo ======================================================================
echo                ZOMBEKCLIENT - PODLACZENIE DO GITHUB
echo ======================================================================
echo.
echo Wybierz jak chcesz podlaczyc projekt:
echo [1] Mam juz zalozone puste repozytorium na GitHub i chce wkleic link
echo [2] Chce zalogowac sie do GitHub przez terminal i stworzyc repo automatycznie
echo [3] Pokaz obecny status Gita
echo.
set /p CHOICE="Wybierz opcje [1, 2 lub 3]: "

if "%CHOICE%"=="1" goto manual_link
if "%CHOICE%"=="2" goto cli_link
if "%CHOICE%"=="3" goto status_git

echo Nieprawidlowy wybor.
pause
exit /b 1

:manual_link
echo.
echo Wklej link HTTPS do swojego repozytorium GitHub:
echo (np. https://github.com/TwojNick/ZombekClient.git)
echo.
set /p REPO_URL="Link: "
if "%REPO_URL%"=="" (
    echo [BLAD] Nie podano linku.
    pause
    exit /b 1
)

git remote remove origin >nul 2>&1
git remote add origin %REPO_URL%
echo.
echo [INFO] Wysylanie projektu na GitHub...
git branch -M main
git push -u origin main
if errorlevel 1 (
    echo.
    echo [BLAD] Nie udalo sie wyslac zmian. Upewnij sie, ze link jest poprawny
    echo i masz uprawnienia do repozytorium (zaloguj sie do Git/GitHub).
    pause
    exit /b 1
)
echo.
echo [SUKCES] Projekt zostal pomyslnie wrzucony na GitHub!
echo.
echo Pamietaj, aby dodac ziomka do repozytorium:
echo 1. Otworz repo na GitHubie
echo 2. Wejdz w Settings -^> Collaborators -^> Add people
echo 3. Wpisz jego nick lub email.
echo.
pause
exit /b 0

:cli_link
echo.
echo [KROK 1] Logowanie do GitHub...
call gh auth login
if errorlevel 1 (
    echo [BLAD] Logowanie przerwane lub nieudane.
    pause
    exit /b 1
)

echo.
set /p REPO_NAME="Podaj nazwe repozytorium [domyslnie: zombekclient]: "
if "%REPO_NAME%"=="" set REPO_NAME=zombekclient

echo.
set /p IS_PRIVATE="Czy repozytorium ma byc prywatne? (T/N) [domyslnie: T]: "
if /i "%IS_PRIVATE%"=="N" (
    set VISIBILITY=--public
) else (
    set VISIBILITY=--private
)

echo.
echo [KROK 2] Tworzenie repozytorium %REPO_NAME% na GitHub i wysylanie kodu...
call gh repo create %REPO_NAME% %VISIBILITY% --source=. --remote=origin --push
if errorlevel 1 (
    echo [BLAD] Nie udalo sie utworzyc repozytorium.
    pause
    exit /b 1
)

echo.
echo [SUKCES] Repozytorium zostalo stworzone i kod zostal wyslany!
echo.
set /p FRIEND_NAME="Chcesz od razu dodac ziomka jako wspoltworce? Wpisz jego nick GitHub (lub ENTER by pominac): "
if not "!FRIEND_NAME!"=="" (
    call gh repo collaborator add !FRIEND_NAME! --permission push
    echo Zaproszenie dla !FRIEND_NAME! zostalo wyslane!
)
echo.
pause
exit /b 0

:status_git
echo.
git status
git remote -v
pause
exit /b 0
