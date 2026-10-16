@echo off
setlocal
cd /d "%~dp0"
echo ======================================================================
echo           Starting SmartLibrary Java REST Backend (Port 8080)
echo ======================================================================

if not exist "backend\bin" mkdir "backend\bin"
if exist "backend\sources.txt" del "backend\sources.txt"

echo Collecting Java source files...
(for /r "backend\src\main\java" %%F in (*.java) do @echo "%%F") > "backend\sources.txt"

for %%A in ("backend\sources.txt") do if %%~zA EQU 0 (
    echo ERROR: No Java source files found under backend\src\main\java
    pause
    exit /b 1
)

echo Compiling Java Sources...
javac -d "backend\bin" @"backend\sources.txt"
if errorlevel 1 (
    echo.
    echo Compilation failed. Please send the complete error above.
    pause
    exit /b 1
)

echo.
echo Compilation succeeded. Starting server...
java -cp "backend\bin" com.smartlibrary.Server
pause
endlocal
