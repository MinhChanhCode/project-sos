$ErrorActionPreference = "Stop"

$node = "C:\Program Files\nodejs\node.exe"
$npmCli = "C:\Program Files\nodejs\node_modules\npm\bin\npm-cli.js"

if (-not (Test-Path $node) -or -not (Test-Path $npmCli)) {
    Write-Host "Node.js/NPM was not found in C:\Program Files\nodejs. Install Node.js 20+ first."
    exit 1
}

function Get-RestaurantLanIp {
    try {
        $ip = Get-NetIPAddress -AddressFamily IPv4 -ErrorAction Stop |
            Where-Object {
                $_.IPAddress -notlike "127.*" -and
                $_.IPAddress -notlike "169.254.*" -and
                $_.PrefixOrigin -ne "WellKnown"
            } |
            Sort-Object InterfaceMetric |
            Select-Object -First 1 -ExpandProperty IPAddress
        if ($ip) { return $ip }
    } catch {}

    $matches = ipconfig | Select-String "IPv4"
    foreach ($match in $matches) {
        $candidate = (($match.ToString() -split ":")[-1]).Trim()
        if ($candidate -and $candidate -notlike "127.*" -and $candidate -notlike "169.254.*") {
            return $candidate
        }
    }
    return $null
}

$lanIp = Get-RestaurantLanIp

if (-not $lanIp) {
    Write-Host "Could not detect a LAN IP address. Connect this computer to Wi-Fi/LAN first."
    exit 1
}

if (-not $env:NUXT_PUBLIC_SITE_URL) {
    $env:NUXT_PUBLIC_SITE_URL = "http://$lanIp`:3000"
}
if (-not $env:NUXT_PUBLIC_API_BASE) {
    $env:NUXT_PUBLIC_API_BASE = ""
}
if (-not $env:NUXT_PUBLIC_WS_BASE) {
    $env:NUXT_PUBLIC_WS_BASE = "/ws"
}
if (-not $env:SOS_API_TARGET) {
    $env:SOS_API_TARGET = "http://127.0.0.1:8080"
}

$fePort = netstat -ano | Select-String "LISTENING" | Select-String ":3000"
if ($fePort) {
    Write-Host "Frontend is already running."
    Write-Host "Open on this computer: http://127.0.0.1:3000"
    Write-Host "Open/scan from phones: $env:NUXT_PUBLIC_SITE_URL"
    exit 0
}

Write-Host "Starting frontend on http://127.0.0.1:3000"
Write-Host "Phone/QR URL: $env:NUXT_PUBLIC_SITE_URL"
Write-Host "Frontend will proxy API/WS to: $env:SOS_API_TARGET"
& $node $npmCli run dev
