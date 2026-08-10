# gcp-lib
A library for graph coloring heuristics

## Dependencies
JGraphT jars live in `lib/` (`jgrapht-core` plus its runtime deps).

## TO RUN
In src directory, `.\compile.ps1`  
This compiles every java file in the src directory (with `lib/` on the classpath).

`cd ..` to get into gcp-lib directory  
`java -cp "bin;lib/*" main <paramfilepath>`  
This line executes the main class and related class objects

On macOS/Linux use `:` instead of `;` in the classpath:  
`java -cp "bin:lib/*" main <paramfilepath>`
