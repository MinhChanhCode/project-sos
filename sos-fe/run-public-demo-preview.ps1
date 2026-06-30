param(
    [string]$ApiTarget = "http://127.0.0.1:8080",
    [switch]$SkipBuild
)

$ErrorActionPreference = "Stop"

$node = "C:\Program Files\nodejs\node.exe"
$npmCli = "C:\Program Files\nodejs\node_modules\npm\bin\npm-cli.js"
$npxCli = "C:\Program Files\nodejs\node_modules\npm\bin\npx-cli.js"
$urlFile = Join-Path $PSScriptRoot "public-qr-url.txt"
$logFile = Join-Path $PSScriptRoot "cloudflare-tunnel.log"
$errFile = Join-Path $PSScriptRoot "cloudflare-tunnel.err.log"

if (-not (Test-Path $node) -or -not (Test-Path $npmCli) -or -not (Test-Path $npxCli)) {
    Write-Host "Node.js/NPM/NPX was not found in C:\Program Files\nodejs. Install Node.js 20+ first."
    exit 1
}

$apiTarget = $ApiTarget.TrimEnd("/")
$env:NUXT_PUBLIC_API_BASE = ""
$env:NUXT_PUBLIC_WS_BASE = "/ws"
$env:SOS_API_TARGET = $apiTarget

function Test-UrlOk([string]$Url) {
    try {
        $res = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
        return $res.StatusCode -ge 200 -and $res.StatusCode -lt 500
    } catch {
        return $false
    }
}

if (-not (Test-UrlOk "$apiTarget/health")) {
    Write-Host "Backend is not ready at $apiTarget."
    Write-Host "Run this first and keep it open:"
    Write-Host "  cd D:\ProjectSOS\sos-api"
    Write-Host "  `$env:AI_SERVICE_URL='http://127.0.0.1:8000'"
    Write-Host "  .\gradlew.bat --no-daemon bootRun"
    exit 1
}

if (-not $SkipBuild) {
    Write-Host "Building frontend production bundle..."
    & $node $npmCli run build
}

$fePort = netstat -ano | Select-String "LISTENING" | Select-String ":3000"
if (-not $fePort) {
    Write-Host "Starting Nuxt preview on http://127.0.0.1:3000 ..."
    Start-Process -FilePath powershell.exe `
        -ArgumentList "-NoExit", "-ExecutionPolicy", "Bypass", "-Command", "`$env:NUXT_PUBLIC_API_BASE=''; `$env:NUXT_PUBLIC_WS_BASE='/ws'; `$env:SOS_API_TARGET='$apiTarget'; cd '$PSScriptRoot'; & '$node' '$npmCli' run preview -- --host 0.0.0.0 --port 3000" `
        -WorkingDirectory $PSScriptRoot `
        -WindowStyle Minimized
}

$started = $false
for ($i = 1; $i -le 45; $i++) {
    Start-Sleep -Seconds 1
    if (Test-UrlOk "http://127.0.0.1:3000/") {
        $started = $true
        break
    }
    Write-Host "Waiting for frontend preview on port 3000... ($i/45)"
}

if (-not $started) {
    Write-Host "Frontend preview did not start on port 3000."
    Write-Host "If another frontend is already running, close it and run this script again."
    exit 1
}

Remove-Item -LiteralPath $urlFile -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $logFile -ErrorAction SilentlyContinue
Remove-Item -LiteralPath $errFile -ErrorAction SilentlyContinue

Write-Host "Opening Cloudflare public tunnel for frontend port 3000..."
Write-Host "Keep this window open during the demo."

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
    Write-Host "Tunnel process id: $($process.Id)"
    if (Test-Path $logFile) { Get-Content $logFile -Tail 80 }
    if (Test-Path $errFile) { Get-Content $errFile -Tail 80 }
    exit 1
}

$env:NUXT_PUBLIC_SITE_URL = $publicUrl
$publicUrl | Set-Content -Path $urlFile -Encoding UTF8

Write-Host ""
Write-Host "Public demo URL:"
Write-Host $publicUrl
Write-Host ""
Write-Host "Customer QR format:"
Write-Host "$publicUrl/customer?tableId=TABLE_UUID"
Write-Host ""
Write-Host "Admin QR Manager will auto-read:"
Write-Host $urlFile
Write-Host ""
Write-Host "Keep this PowerShell window open. Closing it closes the public tunnel."

Wait-Process -Id $process.Id
