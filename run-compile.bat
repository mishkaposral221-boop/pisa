@echo off
chcp 65001 >nul
cd /d "%~dp0"
title Minecraft - runtime-visuals (Fabric)

echo ============================================
echo   Zapusk Minecraft (Fabric client) s modom
echo ============================================
echo.

REM --- Proverka Java 21 ---
where java >nul 2>nul
if errorlevel 1 (
    echo [!] Java ne naydena v PATH.
    echo     Proektu nuzhna Java 21 (JDK 21^). Ustanovi ee i poprobuy snova.
    echo.
    pause
    exit /b 1
)

echo [1/2] Sborka i podgotovka klienta...
echo.

REM --- Zapusk klienta cherez Gradle ---
echo [2/2] Zapuskayu Minecraft (runClient^)...
call gradlew.bat runClient --stacktrace

if errorlevel 1 (
    echo.
    echo !!! Zapusk zavershilsya s oshibkoy. Smotri log vyshe.
)

echo.
pause