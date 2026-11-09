@echo off
setlocal

title ZombekClient - Fabric 1.20.4 Client (ZombekClient Mod)
cd /d "%~dp0"

echo ======================================================================
echo           ZOMBEKCLIENT - FABRIC 1.20.4 (ZOMBEKCLIENT MOD)
echo ======================================================================
echo.

cd /d "%~dp0apps\minecraft-client\fabric"
call gradlew.bat runClient

if errorlevel 1 (
    echo.
    echo [INFO] Gra zostala zamknieta lub wystapil blad.
    pause
)

endlocal
