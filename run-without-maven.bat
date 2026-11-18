@echo off
REM Compiles and starts the application WITHOUT Maven.
REM Requirements: JDK 17+, the JavaFX SDK, and the two jars described in lib\README.txt.
setlocal
cd /d "%~dp0"

REM >>> EDIT THIS LINE: the "lib" folder of your JavaFX SDK <<<
set "JAVAFX_LIB=C:\Users\ISSER TECH\Desktop\javafx-sdk-25.0.1\lib"

if not exist "%JAVAFX_LIB%\javafx.controls.jar" (
    echo JavaFX SDK not found in "%JAVAFX_LIB%". Edit JAVAFX_LIB in this file.
    pause & exit /b 1
)
if not exist "lib\mysql-connector-j*.jar" (
    echo Missing lib\mysql-connector-j-*.jar - see lib\README.txt
    pause & exit /b 1
)
if not exist "lib\jbcrypt*.jar" (
    echo Missing lib\jbcrypt-*.jar - see lib\README.txt
    pause & exit /b 1
)

if exist out rmdir /s /q out
mkdir out

javac -encoding UTF-8 -d out --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -cp "lib\*" ^
  src\main\java\com\dentalclinic\*.java ^
  src\main\java\com\dentalclinic\model\*.java ^
  src\main\java\com\dentalclinic\dao\*.java ^
  src\main\java\com\dentalclinic\service\*.java ^
  src\main\java\com\dentalclinic\utils\*.java ^
  src\main\java\com\dentalclinic\ui\*.java
if errorlevel 1 (
    echo Compilation failed.
    pause & exit /b 1
)

copy /y src\main\resources\* out\ >nul
java --module-path "%JAVAFX_LIB%" --add-modules javafx.controls -cp "out;lib\*" com.dentalclinic.Main
if errorlevel 1 pause
