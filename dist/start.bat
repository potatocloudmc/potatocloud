@echo off
title potatocloud
cd /d "%~dp0"

set "JAVA=java"
if defined JAVA_HOME set "JAVA=%JAVA_HOME%\bin\java"

"%JAVA%" -version >nul 2>&1 || (
    echo Java was not found. Please install Java 25 ^(for example from https://adoptium.net^).
    pause
    exit /b 1
)

"%JAVA%" -Xms256M -Xmx1G -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar potatocloud.jar
pause
