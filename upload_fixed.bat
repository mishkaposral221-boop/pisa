@echo off
chcp 65001 >nul
setlocal EnableExtensions

cd /d "%~dp0"

REM ====== НАСТРОЙКИ ======
set "REPO_URL=https://github.com/mishkaposral221-boop/pisa.git"
set "BRANCH=main"
set "GIT_NAME=gavnikpauk221-netizen"
set "GIT_EMAIL=ваш_email@example.com"
REM =======================

set "COMMIT_MSG=update %date% %time%"

echo.
echo === Заливка проекта на GitHub ===
echo Папка: %CD%
echo.

git --version >nul 2>&1
if errorlevel 1 (
    echo [ОШИБКА] Git не установлен: https://git-scm.com/download/win
    pause
    exit /b 1
)

REM Глобальные настройки можно задавать вне репозитория
git config --global core.longpaths true

REM .gitignore (создаём, если его нет)
if not exist ".gitignore" (
    > .gitignore  echo .gradle/
    >> .gitignore echo build/
    >> .gitignore echo out/
    >> .gitignore echo bin/
    >> .gitignore echo run/
    >> .gitignore echo .idea/
    >> .gitignore echo *.iml
    >> .gitignore echo .settings/
    >> .gitignore echo .classpath
    >> .gitignore echo .project
    >> .gitignore echo *.log
)

REM Сначала инициализируем репозиторий, ПОТОМ настраиваем user.name/email
if not exist ".git" (
    git init
    git branch -M %BRANCH%
)

git config user.name "%GIT_NAME%"
git config user.email "%GIT_EMAIL%"

REM Настройка remote origin
git remote get-url origin >nul 2>&1
if errorlevel 1 (
    git remote add origin %REPO_URL%
) else (
    git remote set-url origin %REPO_URL%
)

echo Добавляю файлы...
git add -A

echo Создаю коммит...
git commit -m "%COMMIT_MSG%"
if errorlevel 1 (
    echo [i] Нечего коммитить ^(нет изменений^) — продолжаю.
)

echo Заливаю на GitHub...
git push -u origin %BRANCH%
if not errorlevel 1 goto done

echo.
echo [!] Обычный пуш отклонён. Пробую подтянуть историю и слить...
git pull --rebase origin %BRANCH%
git push -u origin %BRANCH%
if not errorlevel 1 goto done

echo.
echo [!] Истории всё ещё расходятся. Делаю безопасный принудительный пуш...
git push --force-with-lease origin %BRANCH%
if not errorlevel 1 goto done

echo.
echo [ОШИБКА] Пуш не прошёл. Возможные причины:
echo   - Не выполнен вход в GitHub ^(при первом пуше открывается окно входа^).
echo   - Репозитория %REPO_URL% не существует или нет доступа.
pause
exit /b 1

:done
echo.
echo === Готово! Проект залит на GitHub ===
pause
endlocal
