@echo off
echo ======================================================================
echo           Starting SmartLibrary React Frontend (Port 5173)
echo ======================================================================

cd /d "%~dp0frontend"
if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
)

echo Starting Vite development server...
call npm run dev
pause
