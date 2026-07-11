# Downloads JUnit 5 standalone jar if missing.
$libDir = Join-Path $PSScriptRoot "..\lib"
$junitJar = Join-Path $libDir "junit-platform-console-standalone-1.10.2.jar"

if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Force -Path $libDir | Out-Null
}

if (-not (Test-Path $junitJar)) {
    Write-Host "Downloading JUnit 5 standalone jar..."
    $url = "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar"
    Invoke-WebRequest -Uri $url -OutFile $junitJar
}

Write-Host "JUnit jar ready at $junitJar"
