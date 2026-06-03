@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo === Обновляю проект и запускаю клиент ===

REM Если правки вы вносите вручную — этот pull не нужен.
REM git pull

echo Запускаю Minecraft (клиент)...
call gradlew runClient

pause