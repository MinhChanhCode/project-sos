$ErrorActionPreference = "Stop"

$mysqlPort = netstat -ano | Select-String "LISTENING" | Select-String ":3306"
if (-not $mysqlPort) {
    Write-Host "MySQL is not listening on port 3306. Start MySQL first, then run this script again."
    exit 1
}

$apiPort = netstat -ano | Select-String "LISTENING" | Select-String ":8080"
if ($apiPort) {
    Write-Host "API is already running on http://127.0.0.1:8080"
    Write-Host "Health check: Invoke-RestMethod http://127.0.0.1:8080/health"
    Write-Host "Demo logins: admin/admin123, staff/staff123, kitchen/kitchen123"
    exit 0
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

if ($lanIp -and -not $env:APP_FRONTEND_URL) {
    $env:APP_FRONTEND_URL = "http://$lanIp`:3000"
}
if (-not $env:AI_SERVICE_URL) {
    $env:AI_SERVICE_URL = "http://127.0.0.1:8000"
}
if ($env:APP_FRONTEND_URL) {
    Write-Host "Phone frontend URL for backend QR records: $env:APP_FRONTEND_URL"
}

Write-Host "Starting SOS API on http://127.0.0.1:8080"
Write-Host "Demo logins: admin/admin123, staff/staff123, kitchen/kitchen123"
.\gradlew.bat --no-daemon bootRun
