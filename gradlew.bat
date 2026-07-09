@echo off
setlocal
REM Text-only Gradle launcher used because this repository host rejects binary files.
REM Install Gradle 8.10.2 or set GRADLE_HOME before running this script.

if not "%GRADLE_HOME%"=="" if exist "%GRADLE_HOME%\bin\gradle.bat" (
  "%GRADLE_HOME%\bin\gradle.bat" %*
  exit /b %ERRORLEVEL%
)

where gradle >nul 2>nul
if %ERRORLEVEL%==0 (
  gradle %*
  exit /b %ERRORLEVEL%
)

echo Gradle 8.10.2 is required, but no Gradle executable was found. 1>&2
exit /b 127
