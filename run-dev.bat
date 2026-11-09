@echo off
setlocal

title ZombekClient Launcher
echo ======================================================================
echo             ZOMBEKCLIENT - DESKTOP LAUNCHER (DEV MODE)
echo ======================================================================
echo.

cd /d "%~dp0"

:: 1. Check Node.js
where node >nul 2>&1
if errorlevel 1 (
    echo [BLAD] Node.js nie zostal znaleziony w sciezce PATH.
    echo Zainstaluj Node.js v20+ ze strony https://nodejs.org/
    echo.
    pause
    exit /b 1
)

:: 2. Check Java
where java >nul 2>&1
if errorlevel 1 (
    echo [OSTRZEZENIE] Java nie zostala znaleziona w PATH.
    echo Upewnij sie, ze Java 17+ jest zainstalowana do uruchamiania Minecrafta.
    echo.
) else (
    echo [INFO] Wykryto srodowisko Java w systemie:
    java -version
    echo.
)

:: 3. Build launcher if dist or dist-electron missing
if not exist "apps\launcher\dist\renderer\index.html" goto do_build
if not exist "apps\launcher\dist-electron\main\index.js" goto do_build
goto skip_build

:do_build
echo [INFO] Kompilacja aplikacji Launchera (renderer + electron)...
call pnpm --filter @zombek/launcher build
if errorlevel 1 (
    echo [BLAD] Budowanie aplikacji zakonczone niepowodzeniem.
    pause
    exit /b 1
)

:skip_build
echo [INFO] Uruchamianie ZombekClient Launcher w srodowisku Electron...
echo.

if exist "apps\launcher\node_modules\.bin\electron.cmd" (
    call "apps\launcher\node_modules\.bin\electron.cmd" apps\launcher
    goto end
)

if exist "node_modules\.bin\electron.cmd" (
    call "node_modules\.bin\electron.cmd" apps\launcher
    goto end
)

:: Fallback through pnpm
call pnpm --filter @zombek/launcher start

:end
if errorlevel 1 (
    echo.
    echo [INFO] Launcher zakonczyl dzialanie z kodem bledu.
    pause
)

endlocal
