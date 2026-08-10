#!/usr/bin/env bash
# compile.sh - universal build script for your project
# Run from the src/ directory.

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$(dirname "$0")"

echo "Cleaning old compiled files..."
rm -rf "$ROOT/bin"
mkdir -p "$ROOT/bin"

CP="."
if [ -d "$ROOT/lib" ]; then
  for jar in "$ROOT/lib"/*.jar; do
    [ -e "$jar" ] || continue
    CP="$CP:$jar"
  done
fi

echo "Compiling all Java files..."
find . -name "*.java" > sources.txt
javac -d "$ROOT/bin" -cp "$CP" @sources.txt
rm sources.txt

echo "Compilation successful! Class files are in '$ROOT/bin/'"
