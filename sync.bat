@echo off
setlocal enabledelayedexpansion

title ZombekClient - Git Quick Sync
cd /d "%~dp0"

echo ======================================================================
echo                ZOMBEKCLIENT - SYNCHRONIZACJA Z GITEM
echo ======================================================================
echo.

git remote get-url origin >nul 2>&1
if errorlevel 1 (
    echo [BLAD] Brak skonfigurowanego repozytorium GitHub (origin).
    echo Uruchom najpierw: setup-github.bat
    echo.
    pause
    exit /b 1
)

for /f "tokens=*" %%b in ('git branch --show-current 2^>nul') do set CURRENT_BRANCH=%%b
if not defined CURRENT_BRANCH set CURRENT_BRANCH=main

echo [1/3] Pobieranie najnowszych zmian od ziomka...
git pull --rebase origin %CURRENT_BRANCH%
if errorlevel 1 (
    echo [OSTRZEZENIE] Nie udalo sie automatycznie polaczyc zmian. Sprawdz git status.
    pause
    exit /b 1
)
echo [OK] Pobrane!
echo.

echo [2/3] Sprawdzanie Twoich lokalnych zmian...
git status --porcelain > "%TEMP%\git_changes.tmp"
set HAS_CHANGES=0
for %%A in ("%TEMP%\git_changes.tmp") do if %%~zA gtr 0 set HAS_CHANGES=1
del "%TEMP%\git_changes.tmp" >nul 2>&1

if "%HAS_CHANGES%"=="1" (
    echo Znaleziono Twoje lokalne modyfikacje:
    git status -s
    echo.
    set /p MSG="Podaj krotki opis zmian (lub nacisnij ENTER by uzyc domyslnego): "
    if "!MSG!"=="" set MSG=Aktualizacja plikow projektu

    echo.
    echo [3/3] Wysylanie Twoich zmian do ziomka na GitHub...
    git add .
    git commit -m "!MSG!"
    git push origin %CURRENT_BRANCH%
    if errorlevel 1 (
        echo [BLAD] Nie udalo sie wyslac zmian do repozytorium.
        pause
        exit /b 1
    )
    echo [OK] Twoje zmiany zostaly pomyslnie wyslane na GitHub!
) else (
    echo [INFO] Brak nowych lokalnych zmian do wyslania. Wszystko jest aktualne!
)

echo.
echo ======================================================================
echo Synchronizacja zakonczona pomyslnie!
echo ======================================================================
timeout /t 5
