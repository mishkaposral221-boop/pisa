import subprocess
import os

os.chdir(r"C:\Users\awepu\OneDrive\Desktop\Новая папка (5)")
result = subprocess.run([r"gradlew.bat", "compileClientJava"], capture_output=True, text=True)
print(result.stdout)
print(result.stderr)
print(f"Return code: {result.returncode}")
