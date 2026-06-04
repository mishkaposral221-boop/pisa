@echo off
cd /d "C:\Users\awepu\OneDrive\Desktop\Новая папка (5)"
gradlew.bat compileClientJava 2>&1 > compile-output.txt
echo --- COMPILE RESULT ---
type compile-output.txt | findstr /E "BUILD|ERROR|error|ERROR"
