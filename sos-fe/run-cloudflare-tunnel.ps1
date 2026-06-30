$ErrorActionPreference = "Stop"

$node = "C:\Program Files\nodejs\node.exe"
$npxCli = "C:\Program Files\nodejs\node_modules\npm\bin\npx-cli.js"
$frontendScript = Join-Path $PSScriptRoot "run-fe.ps1"
$urlFile = Join-Path $PSScriptRoot "public-qr-url.txt"
$logFile = Join-Path $PSScriptRoot "cloudflare-tunnel.log"
$errFile = Join-Path $PSScriptRoot "cloudflare-tunnel.err.log"

if (-not (Test-Path $node) -or -not (Test-Path $npxCli)) {
    Write-Host "Node.js/NPX was not found in C:\Program Files\nodejs. Install Node.js first."
    exit 1
}

function Test-Frontend {
    try {
        $res = Invoke-WebRequest -Uri "http://127.0.0.1:3000/health" -TimeoutSec 3
        return $res.StatusCode -eq 200
    } catch {
        return $false
    }
}

if (-not (Test-Frontend)) {
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
    for ($i = 1; $i -le 60; $i++) {
        Start-Sleep -Seconds 1
        if (Test-Frontend) {
            $started = $true
            break
        }
        Write-Host "Waiting for frontend on port 3000... ($i/60)"
    }

    if (-not $started) {
        Write-Host "Frontend did not start on port 3000."
        Write-Host "Run this in another PowerShell and keep it open:"
        Write-Host "  cd D:\ProjectSOS\sos-fe"
        Write-Host "  .\run-fe.ps1"
        exit 1
    }
}

Remove-Item -LiteralPath $urlFile -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $logFile -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $errFile -ErrorAction SilentlyContinue

Write-Host "Opening Cloudflare public tunnel for frontend port 3000..."
Write-Host "This can take 10-40 seconds the first time."

$process = Start-Process -FilePath $node `
    -ArgumentList "`"$npxCli`" --yes cloudflared tunnel --url http://127.0.0.1:3000" `
    -WorkingDirectory $PSScriptRoot `
    -RedirectStandardOutput $logFile `
    -RedirectStandardError $errFile `
    -WindowStyle Hidden `
    -PassThru

$publicUrl = ""
for ($i = 1; $i -le 90; $i++) {
    Start-Sleep -Seconds 1
    $combined = ""
    if (Test-Path $logFile) {
        $combined += Get-Content -Path $logFile -Raw -ErrorAction SilentlyContinue
    }
    if (Test-Path $errFile) {
        $combined += "`n"
        $combined += Get-Content -Path $errFile -Raw -ErrorAction SilentlyContinue
    }

    $match = [regex]::Match($combined, "https://[a-zA-Z0-9-]+\.trycloudflare\.com")
    if ($match.Success) {
        $publicUrl = $match.Value
        break
    }
    Write-Host "Waiting for Cloudflare URL... ($i/90)"
}

if (-not $publicUrl) {
    Write-Host "Could not get a Cloudflare public URL."
    Write-Host "Log:"
    if (Test-Path $logFile) { Get-Content $logFile -Tail 80 }
    if (Test-Path $errFile) { Get-Content $errFile -Tail 80 }
    Write-Host "Tunnel process id: $($process.Id)"
    exit 1
}

$publicUrl | Set-Content -Path $urlFile -Encoding UTF8

Write-Host ""
Write-Host "Public QR URL:"
Write-Host $publicUrl
Write-Host ""
Write-Host "Saved URL to:"
Write-Host $urlFile
Write-Host ""
Write-Host "Use this in Admin QR Manager, field 'Dia chi khach quet QR':"
Write-Host $publicUrl
Write-Host ""
Write-Host "Then print QR again. This one QR works for both Wi-Fi and 4G/5G."
Write-Host "Keep this PowerShell window open while customers are ordering."
Write-Host ""

Wait-Process -Id $process.Id
