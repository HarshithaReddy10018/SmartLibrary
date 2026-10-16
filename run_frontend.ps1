Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          Starting SmartLibrary React Frontend (Port 5173)            " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$frontendPath = Join-Path $PSScriptRoot "frontend"
Set-Location $frontendPath

if (-not (Test-Path (Join-Path $frontendPath "node_modules"))) {
    Write-Host "Installing dependencies in frontend/ ..." -ForegroundColor Yellow
    & npm install
}

Write-Host "Starting Vite development server...`n" -ForegroundColor Green
& npm run dev
