@echo off
chcp 65001 >nul
setlocal

cd /d "%~dp0"

REM ====== НАСТРОЙКИ ======
set "REPO_URL=https://github.com/gavnikpauk221-netizen/popa.git"
set "BRANCH=main"
set "COMMIT_MSG=Initial commit"
set "GIT_NAME=gavnikpauk221-netizen"
set "GIT_EMAIL=ваш_email@example.com"
REM =======================

echo.
echo === Заливка проекта на GitHub ===
echo Папка: %CD%
echo.

git --version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Git не установлен: https://git-scm.com/download/win
    pause & exit /b 1
)

REM Поддержка длинных путей (от прав может потребовать --global вместо --system)
git config --global core.longpaths true

REM Личность git (для этого репозитория)
git config user.name "%GIT_NAME%"
git config user.email "%GIT_EMAIL%"

REM Создаём .gitignore, чтобы не заливать кэш и сборку
if not exist ".gitignore" (
    echo Создаю .gitignore...
    (
        echo .gradle/
        echo build/
        echo out/
        echo bin/
        echo run/
        echo .idea/
        echo *.iml
        echo .settings/
        echo .classpath
        echo .project
        echo *.log
    ) > .gitignore
)

if not exist ".git" (
    git init
    git branch -M %BRANCH%
)

git remote get-url origin >nul 2>&1
if errorlevel 1 ( git remote add origin %REPO_URL% ) else ( git remote set-url origin %REPO_URL% )

echo Добавляю файлы...
git add -A

echo Создаю коммит...
git commit -m "%COMMIT_MSG%"

echo Заливаю на GitHub...
git push -u origin %BRANCH%
if errorlevel 1 (
    echo.
    echo [ОШИБКА] Пуш не прошёл. Проверьте вход в GitHub и что коммит создан.
    pause & exit /b 1
)

echo.
echo === Готово! ===
pause
endlocal