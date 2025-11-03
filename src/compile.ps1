# compile.ps1
# PowerShell script to compile all Java files under 'src'

Write-Host "Cleaning old compiled files..."
if (Test-Path "../bin") { Remove-Item -Recurse -Force "../bin" }
New-Item -ItemType Directory -Force -Path "../bin" | Out-Null

Write-Host "Compiling all Java files..."
# find java
$javaFiles = Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName }

# compile into bin
javac -d ../bin -cp . $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful! Class files are in '../bin'."
} else {
    Write-Host "Compilation failed. Check the errors above."
}
