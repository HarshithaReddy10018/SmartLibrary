@echo off
echo ======================================================================
echo           Running SmartLibrary Algorithm Demonstration Suite
echo ======================================================================

if not exist "%~dp0backend\bin" mkdir "%~dp0backend\bin"

echo Compiling Java Sources...
javac -d "%~dp0backend\bin" "%~dp0backend\src\main\java\com\smartlibrary\algorithms\*.java"
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Running AlgorithmDemo...
java -cp "%~dp0backend\bin" com.smartlibrary.algorithms.AlgorithmDemo
pause
