# Runs all unit tests from the repository root.
$root = Split-Path $PSScriptRoot -Parent
$binDir = Join-Path $root "bin"
$binTestDir = Join-Path $root "bin-test"
$junitJar = Join-Path $root "lib\junit-platform-console-standalone-1.10.2.jar"

if (-not (Test-Path $binTestDir)) {
    Write-Host "Tests not compiled. Run compile-tests.ps1 first."
    exit 1
}

Push-Location $root
java -jar $junitJar --class-path "$binDir;$binTestDir" --scan-class-path
$exitCode = $LASTEXITCODE
Pop-Location
exit $exitCode
