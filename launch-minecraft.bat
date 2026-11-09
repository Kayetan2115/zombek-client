@echo off
setlocal

title ZombekClient - Minecraft 1.20.4
cd /d "%~dp0"

echo ======================================================================
echo                  ZOMBEKCLIENT - MINECRAFT 1.20.4
echo ======================================================================
echo.

where node >nul 2>&1
if errorlevel 1 (
    echo [BLAD] Wymagane jest srodowisko Node.js w PATH.
    pause
    exit /b 1
)

node scripts\launch-game.mjs %*
if errorlevel 1 (
    echo.
    echo [INFO] Gra zostala zamknieta lub wystapil blad.
    pause
)

endlocal
