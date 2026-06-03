@echo off
chcp 65001 >nul
setlocal

cd /d "%~dp0"

REM ====== НАСТРОЙКИ ======
set "REPO_URL=https://github.com/mishkaposral221-boop/pisa.git"
set "BRANCH=main"
set "GIT_NAME=gavnikpauk221-netizen"
set "GIT_EMAIL=ваш_email@example.com"
REM =======================

REM Сообщение коммита с датой и временем
set "COMMIT_MSG=update %date% %time%"

echo.
echo === Заливка проекта на GitHub ===
echo Папка: %CD%
echo.

git --version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Git не установлен: https://git-scm.com/download/win
    pause & exit /b 1
)

git config --global core.longpaths true
git config user.name "%GIT_NAME%"
git config user.email "%GIT_EMAIL%"

if not exist ".gitignore" (
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
if not errorlevel 1 goto done

echo.
echo [!] Обычный пуш отклонён, истории разошлись. Делаю принудительный пуш...
git push --force origin %BRANCH%
if not errorlevel 1 goto done

echo.
echo [ОШИБКА] Пуш не прошёл. Проверьте вход в GitHub.
pause
exit /b 1

:done
echo.
echo === Готово! ===
pause
endlocal