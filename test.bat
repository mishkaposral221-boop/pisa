@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo === Obnovlyayu proekt i zapuskayu klient ===

echo [1/2] git pull...
git pull
if errorlevel 1 (
    echo.
    echo !!! git pull zavershilsya s oshibkoy. Smotri soobshenie vyshe.
    echo     Zapusk klienta vse ravno prodolzhitsya cherez 3 sek...
    timeout /t 3 >nul
)

echo [2/2] Zapuskayu Minecraft (klient)...
call gradlew runClient

pause
