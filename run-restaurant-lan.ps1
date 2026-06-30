$ErrorActionPreference = "Stop"

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
    Write-Host "Could not detect a LAN IP address. Connect this computer to the restaurant Wi-Fi/LAN first."
    exit 1
}

Write-Host "Restaurant LAN address detected: $lanIp"
Write-Host ""
Write-Host "Run these in separate PowerShell windows and keep them open:"
Write-Host ""
Write-Host "1) AI"
Write-Host "   cd D:\ProjectSOS\sos-ai"
Write-Host "   .\run-ai.ps1"
Write-Host ""
Write-Host "2) API"
Write-Host "   cd D:\ProjectSOS\sos-api"
Write-Host "   .\run-api.ps1"
Write-Host ""
Write-Host "3) Frontend/QR"
Write-Host "   cd D:\ProjectSOS\sos-fe"
Write-Host "   .\run-fe.ps1"
Write-Host ""
Write-Host "Admin opens on this computer:"
Write-Host "   http://127.0.0.1:3000"
Write-Host ""
Write-Host "Phones scan/open:"
Write-Host "   http://$lanIp`:3000"
Write-Host ""
Write-Host "QR codes must use this base URL:"
Write-Host "   http://$lanIp`:3000"
Write-Host ""
Write-Host "If a phone cannot open it, allow Java/Node through Windows Defender Firewall for Private networks."
