$ErrorActionPreference = "Stop"

$node = "C:\Program Files\nodejs\node.exe"
$frontendScript = Join-Path $PSScriptRoot "run-fe.ps1"
$tunnelScript = Join-Path $PSScriptRoot "scripts\public-tunnel.mjs"

if (-not (Test-Path $node)) {
    Write-Host "Node.js was not found in C:\Program Files\nodejs. Install Node.js first."
    exit 1
}

if (-not (Test-Path $tunnelScript)) {
    Write-Host "Tunnel script was not found: $tunnelScript"
    exit 1
}

$fePort = netstat -ano | Select-String "LISTENING" | Select-String ":3000"
if (-not $fePort) {
    Write-Host "Frontend is not running on port 3000. Starting it now..."
    if (-not (Test-Path $frontendScript)) {
        Write-Host "Frontend script was not found: $frontendScript"
        exit 1
    }

    Start-Process -FilePath powershell.exe `
        -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-File", "`"$frontendScript`"" `
        -WorkingDirectory $PSScriptRoot `
        -WindowStyle Minimized

    $started = $false
    for ($i = 1; $i -le 45; $i++) {
        Start-Sleep -Seconds 1
        $fePort = netstat -ano | Select-String "LISTENING" | Select-String ":3000"
        if ($fePort) {
            $started = $true
            break
        }
        Write-Host "Waiting for frontend on port 3000... ($i/45)"
    }

    if (-not $started) {
        Write-Host "Frontend did not start on port 3000."
        Write-Host "Please run this in another PowerShell and keep it open:"
        Write-Host "  cd D:\ProjectSOS\sos-fe"
        Write-Host "  .\run-fe.ps1"
        exit 1
    }
}

Write-Host "Opening public tunnel for frontend port 3000..."
Write-Host "Phones on 4G/5G will use the URL printed below."
& $node $tunnelScript
