$ErrorActionPreference = "Stop"

$bundledPython = Join-Path $env:USERPROFILE ".cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"
$pythonCommand = Get-Command python -ErrorAction SilentlyContinue

if ($pythonCommand) {
    $python = $pythonCommand.Source
} elseif (Test-Path $bundledPython) {
    $python = $bundledPython
} else {
    Write-Host "Python was not found. Install Python 3.11+ and tick 'Add python.exe to PATH'."
    exit 1
}

Write-Host "Using Python: $python"
& $python -m uvicorn main:app --host 127.0.0.1 --port 8000
