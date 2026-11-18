@echo off
REM Starts the Dental Clinic application (needs Java 17+ and Maven in the PATH).
cd /d "%~dp0"
call mvn -q javafx:run
if errorlevel 1 pause
