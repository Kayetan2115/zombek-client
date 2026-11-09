@echo off
setlocal

title ZombekClient - Full Build Suite
echo ======================================================================
echo                  ZOMBEKCLIENT - FULL BUILD SUITE
echo ======================================================================
echo.

set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%"

:: 0. Auto-pull updates from Git if remote configured
git remote get-url origin >nul 2>&1
if not errorlevel 1 (
    echo [GIT] Sprawdzanie i pobieranie najnowszych zmian z repozytorium...
    git branch --set-upstream-to=origin/main main >nul 2>&1
    git pull origin main --quiet >nul 2>&1
)

:: Sprawdzenie Node.js i pnpm
where pnpm >nul 2>&1
if errorlevel 1 (
    echo [INFO] Nie znaleziono pnpm w systemie. Proba automatycznej instalacji przez npm...
    where npm >nul 2>&1
    if not errorlevel 1 (
        call npm install -g pnpm
    ) else (
        echo [BLAD] Brak Node.js i pnpm w PATH.
        echo Pobierz i zainstaluj Node.js ze strony: https://nodejs.org/
        pause
        exit /b 1
    )
)

:: 1. Build launcher and packages
echo [1/6] Budowanie pakietow TypeScript i Launchera...
call pnpm run build
if errorlevel 1 (
    echo [BLAD] Budowanie launchera zakonczone niepowodzeniem.
    pause
    exit /b 1
)
echo [OK] Launcher i pakiety zbudowane pomyslnie.
echo.

:: 2. Build Core
echo [2/6] Budowanie modulu ZombekClient Core (Java)...
cd /d "%ROOT_DIR%apps\minecraft-client\core"
call gradlew.bat build
if errorlevel 1 (
    echo [BLAD] Budowanie ZombekClient Core zakonczone niepowodzeniem.
    pause
    exit /b 1
)
echo [OK] Core zbudowany pomyslnie.
echo.

:: 3. Build Fabric
echo [3/6] Budowanie modu Fabric (1.20.4)...
cd /d "%ROOT_DIR%apps\minecraft-client\fabric"
call gradlew.bat build
if errorlevel 1 (
    echo [BLAD] Budowanie modulu Fabric zakonczone niepowodzeniem.
    pause
    exit /b 1
)
echo [OK] Fabric mod zbudowany pomyslnie.
echo.

:: 4. Build Quilt
echo [4/6] Budowanie modu Quilt (1.20.4)...
cd /d "%ROOT_DIR%apps\minecraft-client\quilt"
call gradlew.bat build
if errorlevel 1 (
    echo [BLAD] Budowanie modulu Quilt zakonczone niepowodzeniem.
    pause
    exit /b 1
)
echo [OK] Quilt mod zbudowany pomyslnie.
echo.

:: 5. Build Forge
echo [5/6] Budowanie modu Forge (1.20.4)...
cd /d "%ROOT_DIR%apps\minecraft-client\forge"
call gradlew.bat build
if errorlevel 1 (
    echo [BLAD] Budowanie modulu Forge zakonczone niepowodzeniem.
    pause
    exit /b 1
)
echo [OK] Forge mod zbudowany pomyslnie.
echo.

:: 6. Build NeoForge
echo [6/6] Budowanie modu NeoForge (1.20.4)...
cd /d "%ROOT_DIR%apps\minecraft-client\neoforge"
call gradlew.bat build
if errorlevel 1 (
    echo [BLAD] Budowanie modulu NeoForge zakonczone niepowodzeniem.
    pause
    exit /b 1
)
echo [OK] NeoForge mod zbudowany pomyslnie.
echo.

cd /d "%ROOT_DIR%"
echo ======================================================================
echo                     ZESTAWIENIE ARTEFAKTOW JAR
echo ======================================================================
dir /b /s apps\minecraft-client\*\build\libs\zombekclient-*.jar
echo.
echo [SUKCES] Caly projekt zostal pomyslnie zbudowany i zweryfikowany!
echo ======================================================================

endlocal
