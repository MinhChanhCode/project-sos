param(
    [Parameter(Mandatory = $true)]
    [string]$SiteUrl,

    [string]$ApiTarget = "http://127.0.0.1:8080"
)

$ErrorActionPreference = "Stop"

$site = $SiteUrl.TrimEnd("/")
$apiTarget = $ApiTarget.TrimEnd("/")

Write-Host "Public QR mode"
Write-Host ""
Write-Host "Frontend/QR public URL:"
Write-Host "   $site"
Write-Host ""
Write-Host "Local API target proxied by frontend:"
Write-Host "   $apiTarget"
Write-Host ""
Write-Host "Run API in one PowerShell window:"
Write-Host "   cd D:\ProjectSOS\sos-api"
Write-Host "   `$env:APP_FRONTEND_URL='$site'"
Write-Host "   .\run-api.ps1"
Write-Host ""
Write-Host "Run frontend in another PowerShell window:"
Write-Host "   cd D:\ProjectSOS\sos-fe"
Write-Host "   `$env:NUXT_PUBLIC_SITE_URL='$site'"
Write-Host "   `$env:NUXT_PUBLIC_API_BASE=''"
Write-Host "   `$env:NUXT_PUBLIC_WS_BASE='/ws'"
Write-Host "   `$env:SOS_API_TARGET='$apiTarget'"
Write-Host "   .\run-fe.ps1"
Write-Host ""
Write-Host "QR codes must contain:"
Write-Host "   $site/customer?tableId=..."
Write-Host ""
Write-Host "Expose only the frontend port 3000 with ngrok, Cloudflare Tunnel, or a public domain."
