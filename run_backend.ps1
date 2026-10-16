Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "          Starting SmartLibrary Java REST Backend (Port 8080)         " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$binPath = Join-Path $PSScriptRoot "backend\bin"
if (-not (Test-Path $binPath)) {
    New-Item -ItemType Directory -Path $binPath -Force | Out-Null
}

Write-Host "Compiling Java Sources..." -ForegroundColor Yellow
$javaSources = Get-ChildItem -Path (Join-Path $PSScriptRoot "backend\src\main\java\com\smartlibrary") -Recurse -Filter "*.java" | ForEach-Object { $_.FullName }
& javac -d $binPath $javaSources

if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed with exit code $LASTEXITCODE" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Starting Server on http://localhost:8080 ...`n" -ForegroundColor Green
& java -cp $binPath com.smartlibrary.Server
