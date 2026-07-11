# Compiles main sources and unit tests.
$root = Split-Path $PSScriptRoot -Parent
$srcDir = Join-Path $root "src"
$testDir = Join-Path $root "test"
$binDir = Join-Path $root "bin"
$binTestDir = Join-Path $root "bin-test"
$junitJar = Join-Path $root "lib\junit-platform-console-standalone-1.10.2.jar"

& (Join-Path $testDir "setup-junit.ps1")

Write-Host "Compiling main sources..."
Push-Location $srcDir
$srcFiles = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if (-not (Test-Path $binDir)) { New-Item -ItemType Directory -Force -Path $binDir | Out-Null }
javac -d $binDir -cp . $srcFiles
if ($LASTEXITCODE -ne 0) {
    Pop-Location
    exit $LASTEXITCODE
}
Pop-Location

Write-Host "Compiling tests..."
if (Test-Path $binTestDir) { Remove-Item -Recurse -Force $binTestDir }
New-Item -ItemType Directory -Force -Path $binTestDir | Out-Null

$testFiles = Get-ChildItem -Path $testDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d $binTestDir -cp "$binDir;$junitJar" $testFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Test compilation successful."
} else {
    Write-Host "Test compilation failed."
    exit $LASTEXITCODE
}
