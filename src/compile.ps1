# compile.ps1
# PowerShell script to compile all Java files under 'src'

Write-Host "Cleaning old compiled files..."
if (Test-Path "../bin") { Remove-Item -Recurse -Force "../bin" }
New-Item -ItemType Directory -Force -Path "../bin" | Out-Null

$libDir = Join-Path (Split-Path $PSScriptRoot -Parent) "lib"
$jars = @()
if (Test-Path $libDir) {
    $jars = Get-ChildItem -Path $libDir -Filter *.jar | ForEach-Object { $_.FullName }
}
$classpath = if ($jars.Count -gt 0) { (@(".") + $jars) -join ";" } else { "." }

Write-Host "Compiling all Java files..."
$javaFiles = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }

# compile into bin
javac -d ../bin -cp $classpath $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful! Class files are in '../bin'."
} else {
    Write-Host "Compilation failed. Check the errors above."
}
