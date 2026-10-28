@echo off
echo Поиск Java 17...
set JAVA_HOME=
set FOUND_JAVA17=false

for /f "tokens=*" %%i in ('dir /b /ad "C:\Program Files\Eclipse Adoptium\jdk-*17*" 2^>nul') do (
    set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\%%i"
    set "FOUND_JAVA17=true"
    goto :found
)

for /f "tokens=*" %%i in ('dir /b /ad "C:\Program Files\Java\jdk-*17*" 2^>nul') do (
    set "JAVA_HOME=C:\Program Files\Java\%%i"
    set "FOUND_JAVA17=true"
    goto :found
)

:not_found
echo Java 17 не найдена! Пожалуйста установите Java 17 с https://adoptium.net/
echo После установки запустите этот файл снова.
pause
exit /b 1

:found
if "%FOUND_JAVA17%"=="true" (
    echo Найдена Java 17: %JAVA_HOME%
    set PATH=%JAVA_HOME%\bin;%PATH%
    echo.
    echo Проверка версии Java:
    java -version
    echo.
    echo Сборка проекта...
    gradlew shadowJar
    echo.
    echo Готово! Плагин в build/libs/starfarm-mylittlefarm.jar
) else (
    echo Java 17 не найдена!
)
pause
