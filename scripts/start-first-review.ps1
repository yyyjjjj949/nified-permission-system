[CmdletBinding()]
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [ValidateRange(1024,65535)][int]$Port = 18081,
    [ValidatePattern('^[A-Za-z0-9_.-]{1,64}$')][string]$AdminUsername = 'admin'
)
$ErrorActionPreference = 'Stop'
$reviewRoot = Split-Path -Parent $PSScriptRoot
$reviewJar = Join-Path $reviewRoot 'target\nified-permission-system-0.0.1-SNAPSHOT.jar'
if (-not (Test-Path -LiteralPath $reviewJar)) { throw '请先运行 scripts/build-first-review.ps1。' }
$reviewJava = if ($JdkHome) { Join-Path $JdkHome 'bin\java.exe' } else { (Get-Command java).Source }
$reviewVersion = (& $reviewJava --version | Out-String)
if ($reviewVersion -notmatch '(?m)^(openjdk|java) 21(?:[. ]|$)') { throw '启动要求 JDK 21，请用 -JdkHome 指定。' }
if (Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue) { throw "本地端口 $Port 已被占用，请更换 -Port。" }
$reviewOldUsername = $env:PERMISSION_BOOTSTRAP_USERNAME
$reviewOldPassword = $env:PERMISSION_BOOTSTRAP_PASSWORD
$reviewSecure = Read-Host "请设置本次内存演示的管理员 $AdminUsername 密码（至少8字符，大小写/数字/符号中至少三类）" -AsSecureString
$reviewPointer = [IntPtr]::Zero
try {
    $reviewPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($reviewSecure)
    $env:PERMISSION_BOOTSTRAP_USERNAME = $AdminUsername
    $env:PERMISSION_BOOTSTRAP_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($reviewPointer)
    Write-Host "页面：http://127.0.0.1:$Port/ui/index.html"
    Write-Host '本地演示使用内存库，停止后数据清空。保持终端打开，Ctrl+C停止。'
    Push-Location $reviewRoot
    try { & $reviewJava -jar $reviewJar "--server.port=$Port" } finally { Pop-Location }
} finally {
    if ($reviewPointer -ne [IntPtr]::Zero) { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($reviewPointer) }
    $reviewSecure.Dispose()
    $env:PERMISSION_BOOTSTRAP_USERNAME = $reviewOldUsername
    $env:PERMISSION_BOOTSTRAP_PASSWORD = $reviewOldPassword
}
