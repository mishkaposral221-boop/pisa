@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo === Компиляция Phase D ===
call gradlew compileClientJava --stacktrace
pause
