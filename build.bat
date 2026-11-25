@echo off
REM Build script (Windows). Usage: build.bat  -> dist\ICS26012-Scheduling-Simulator.jar
setlocal
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
mkdir dist 2>nul
dir /s /b src\*.java > out\sources.txt
javac -encoding UTF-8 -d out @out\sources.txt || exit /b 1
echo Main-Class: scheduler.App> out\manifest.txt
jar cfm dist\ICS26012-Scheduling-Simulator.jar out\manifest.txt -C out scheduler || exit /b 1
echo Built dist\ICS26012-Scheduling-Simulator.jar  (run with: java -jar dist\ICS26012-Scheduling-Simulator.jar)
