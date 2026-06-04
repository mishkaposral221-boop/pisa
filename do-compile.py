#!/usr/bin/env python3
import subprocess
import os
import sys

os.chdir(r"C:\Users\awepu\OneDrive\Desktop\Новая папка (5)")
result = subprocess.run(
    [r".\gradlew.bat", "compileClientJava"],
    shell=True,
    capture_output=False,
    text=True
)
sys.exit(result.returncode)
