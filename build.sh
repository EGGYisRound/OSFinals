#!/usr/bin/env bash
# Build script (Linux/macOS).  Usage:  ./build.sh        -> builds dist/ICS26012-Scheduling-Simulator.jar
#                                      ./build.sh test   -> compiles and runs the algorithm tests
set -euo pipefail
cd "$(dirname "$0")"

OUT=out
DIST=dist
JAR="$DIST/ICS26012-Scheduling-Simulator.jar"

rm -rf "$OUT"
mkdir -p "$OUT" "$DIST"
find src -name '*.java' > "$OUT/sources.txt"
javac -encoding UTF-8 -d "$OUT" @"$OUT/sources.txt"

if [ "${1:-}" = "test" ]; then
  mkdir -p "$OUT/test-classes"
  javac -encoding UTF-8 -cp "$OUT" -d "$OUT/test-classes" test/scheduler/AlgorithmTests.java
  java -cp "$OUT:$OUT/test-classes" scheduler.AlgorithmTests
  exit 0
fi

printf 'Main-Class: scheduler.App\n' > "$OUT/manifest.txt"
jar cfm "$JAR" "$OUT/manifest.txt" -C "$OUT" scheduler
echo "Built $JAR  (run with: java -jar $JAR)"
