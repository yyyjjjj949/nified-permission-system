[CmdletBinding()]
param(
    [string]$JdkHome = $env:JAVA_HOME,
    [string]$MavenCache = 'D:\CodexData\cache\m2',
    [string]$NpmCache = 'D:\CodexData\cache\npm',
    [switch]$SkipFrontend,
    [switch]$Offline
)
$ErrorActionPreference = 'Stop'
$reviewRoot = Split-Path -Parent $PSScriptRoot
$reviewOldJava = $env:JAVA_HOME
$reviewOldMavenHome = $env:MAVEN_USER_HOME
try {
    if ($JdkHome) { $env:JAVA_HOME = $JdkHome }
    $reviewJava = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\java.exe' } else { (Get-Command java).Source }
    $reviewVersion = (& $reviewJava --version | Out-String)
    if ($reviewVersion -notmatch '(?m)^(openjdk|java) 21(?:[. ]|$)') { throw "构建要求 JDK 21。请用 -JdkHome 指定。当前：$reviewVersion" }
    $env:MAVEN_USER_HOME = Split-Path -Parent $MavenCache
    $reviewMaven = (Get-Command mvn.cmd -ErrorAction SilentlyContinue).Source
    if (-not $reviewMaven) { $reviewMaven = Join-Path $reviewRoot 'mvnw.cmd' }
    $reviewArgs = @('-B', "-Dmaven.repo.local=$MavenCache")
    if ($Offline) { $reviewArgs += '-o' }
    Push-Location $reviewRoot
    try {
        & $reviewMaven @reviewArgs -f 'third-party/clonefactory/pom.xml' install '-DskipTests'
        if ($LASTEXITCODE -ne 0) { throw 'CloneFactory 本地依赖构建失败。' }
        if (-not $SkipFrontend) {
            Push-Location (Join-Path $reviewRoot 'frontend')
            try {
                if (-not (Test-Path -LiteralPath 'node_modules')) {
                    & npm.cmd ci --cache $NpmCache --registry 'https://registry.npmjs.org'
                    if ($LASTEXITCODE -ne 0) { throw '前端依赖安装失败。' }
                }
                & npm.cmd run build
                if ($LASTEXITCODE -ne 0) { throw '前端构建失败。' }
            } finally { Pop-Location }
        }
        & $reviewMaven @reviewArgs clean verify
        if ($LASTEXITCODE -ne 0) { throw '后端构建或测试失败。' }
        Write-Host '构建与测试通过。报告：target/site/jacoco/index.html'
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $reviewOldJava
    $env:MAVEN_USER_HOME = $reviewOldMavenHome
}
