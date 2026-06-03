@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo === Компиляция Phase D + NoRender Nametags ===
call gradlew.bat compileClientJava --stacktrace 2>&1 | tee compile-nametags.log
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Компиляция завершена успешно!
    echo Запускаем клиент...
    call gradlew.bat runClient
) else (
    echo.
    echo [ERROR] Компиляция завершилась с ошибкой
)
pause
