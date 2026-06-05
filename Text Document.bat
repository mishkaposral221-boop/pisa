@echo off
chcp 65001 >nul
setlocal

REM === Путь к проекту ===
cd /d "C:\Users\awepu\OneDrive\Desktop\Новая папка (5)"

echo === [1/3] Принудительная синхронизация с GitHub (origin/main) ===
git remote -v
echo.
git fetch origin
if errorlevel 1 (
    echo ОШИБКА: git fetch не удался. Проверь интернет/доступ.
    pause
    exit /b 1
)
git reset --hard origin/main
if errorlevel 1 (
    echo ОШИБКА: git reset не удался.
    pause
    exit /b 1
)
echo.

echo === [2/3] Последние коммиты (сверху должен быть ca82063) ===
git log --oneline -3
echo.
echo Проверь: сверху ca82063 = "blindness-aware critAchievable".
echo Если сверху 245797d - origin смотрит не туда (см. remote выше).
echo.

echo === [3/3] Запускаю Minecraft (клиент) ===
call gradlew.bat runClient

echo.
echo === Готово (или см. ошибки выше) ===
pause
endlocal