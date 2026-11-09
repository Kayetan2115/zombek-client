@echo off
setlocal enabledelayedexpansion

title ZombekClient - Git Auto-Sync Monitor
cd /d "%~dp0"

echo ======================================================================
echo           ZOMBEKCLIENT - AUTOMATYCZNY SYNCHRONIZATOR GIT
echo ======================================================================
echo Ten skrypt dziala w tle i co 30 sekund sprawdza, czy ziomek
echo wrzucil nowe zmiany na GitHub. Jesli tak - pobiera je automatycznie!
echo.
echo Mozesz zminimalizowac to okno podczas pracy/gry.
echo ======================================================================
echo.

:loop
git remote get-url origin >nul 2>&1
if errorlevel 1 (
    echo [%TIME:~0,8%] Brak podpietego repozytorium origin. Czekam na skonfigurowanie...
    timeout /t 30 /nobreak >nul
    goto loop
)

:: Sprawdz aktualny branch (zwykle main)
for /f "tokens=*" %%b in ('git branch --show-current 2^>nul') do set CURRENT_BRANCH=%%b
if not defined CURRENT_BRANCH set CURRENT_BRANCH=main

:: Pobierz informacje o zmianach ze zdalnego serwera bez laczenia plikow
git fetch origin %CURRENT_BRANCH% >nul 2>&1
if errorlevel 1 (
    goto wait
)

:: Sprawdz ile commitow jestesmy do tylu wzgledem serwera
set BEHIND=0
for /f %%c in ('git rev-list HEAD..origin/%CURRENT_BRANCH% --count 2^>nul') do set BEHIND=%%c

if not "%BEHIND%"=="0" (
    echo [%TIME:~0,8%] Wykryto %BEHIND% nowych zmian od ziomka! Pobieranie...
    git pull --rebase origin %CURRENT_BRANCH%
    if errorlevel 1 (
        echo [%TIME:~0,8%] [UWAGA] Wystapil konflikt zmian lokalnych. Uzyj 'git status' lub 'sync.bat'.
    ) else (
        echo [%TIME:~0,8%] [SUKCES] Zaktualizowano projekt do najnowszej wersji!
    )
    echo.
)

:wait
timeout /t 30 /nobreak >nul
goto loop
