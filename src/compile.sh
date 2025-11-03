#!/usr/bin/env bash
# compile.sh - universal build script for your project

echo "🧹 Cleaning old compiled files..."
rm -rf bin
mkdir -p bin

echo "🛠️ Compiling all Java files..."
find . -name "*.java" > sources.txt
javac -d bin @sources.txt
rm sources.txt

if [ $? -eq 0 ]; then
  echo "✅ Compilation successful! Class files are in 'bin/'"
else
  echo "❌ Compilation failed. Check the errors above."
  exit 1
fi
