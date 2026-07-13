@echo off
setlocal enabledelayedexpansion
set GRADLE_VERSION=8.10.2
set DIST_NAME=gradle-%GRADLE_VERSION%-bin
set DIST_URL=https://services.gradle.org/distributions/%DIST_NAME%.zip
set BASE_DIR=%~dp0
set BOOTSTRAP_DIR=%BASE_DIR%.gradle\bootstrap
set GRADLE_HOME_LOCAL=%BOOTSTRAP_DIR%\%DIST_NAME%
set GRADLE_BIN=%GRADLE_HOME_LOCAL%\bin\gradle.bat
set ZIP_PATH=%BOOTSTRAP_DIR%\%DIST_NAME%.zip

if not "%GRADLE_HOME%"=="" if exist "%GRADLE_HOME%\bin\gradle.bat" (
  "%GRADLE_HOME%\bin\gradle.bat" %*
  exit /b %ERRORLEVEL%
)

if exist "%GRADLE_BIN%" (
  "%GRADLE_BIN%" %*
  exit /b %ERRORLEVEL%
)

if not exist "%BOOTSTRAP_DIR%" mkdir "%BOOTSTRAP_DIR%"
echo Gradle %GRADLE_VERSION% not found locally; downloading %DIST_URL% 1>&2
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%ZIP_PATH%'"
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%ZIP_PATH%' '%BOOTSTRAP_DIR%'"
if %ERRORLEVEL% neq 0 exit /b %ERRORLEVEL%
del "%ZIP_PATH%"

if not exist "%GRADLE_BIN%" (
  echo Downloaded Gradle but executable was not found at %GRADLE_BIN% 1>&2
  exit /b 1
)

"%GRADLE_BIN%" %*
exit /b %ERRORLEVEL%
