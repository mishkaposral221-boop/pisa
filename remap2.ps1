$mappingFile = "C:\Users\awepu\.gradle\caches\fabric-loom\1.21.11\loom.mappings.1_21_11.layered+hash.2198-v2\mappings.tiny"
$baseDir = "C:\Users\awepu\OneDrive\Desktop"
$folderName = [string]::new([char[]]@(0x041D, 0x043E, 0x0432, 0x0430, 0x044F, 0x0020, 0x043F, 0x0430, 0x043F, 0x043A, 0x0430, 0x0020, 0x0028, 0x0035, 0x0029))
$srcDir = Join-Path $baseDir "$folderName\decompiled"
$outDir = Join-Path $baseDir "$folderName\src\client\java"
$utf8NoBom = New-Object System.Text.UTF8Encoding $false

$classMap = @{}
$methodReplace = @{}
$fieldReplace = @{}

Write-Host "Reading mappings..."
$lines = [System.IO.File]::ReadAllLines($mappingFile, [System.Text.Encoding]::UTF8)
$currentClassInter = $null

for ($i = 1; $i -lt $lines.Count; $i++) {
    $line = $lines[$i].TrimStart()
    $parts = $line.Split("`t")
    if ($parts.Count -lt 2) { continue }
    
    if ($parts[0] -eq "c" -and $parts.Count -ge 4) {
        $interPath = $parts[2]
        $namedPath = $parts[3]
        if ($interPath.StartsWith("net/minecraft/")) {
            $classMap[$interPath] = $namedPath
            $currentClassInter = $interPath
        }
    } elseif ($parts[0] -eq "m" -and $parts.Count -ge 5) {
        $interMethod = $parts[3]
        $namedMethod = $parts[4]
        if ($interMethod.StartsWith("method_") -and $interMethod -ne $namedMethod -and $namedMethod -ne "") {
            $methodReplace[$interMethod] = $namedMethod
        }
    } elseif ($parts[0] -eq "f" -and $parts.Count -ge 5) {
        $interField = $parts[3]
        $namedField = $parts[4]
        if ($interField.StartsWith("field_") -and $interField -ne $namedField -and $namedField -ne "") {
            $fieldReplace[$interField] = $namedField
        }
    }
}

Write-Host "Classes: $($classMap.Count), Methods: $($methodReplace.Count), Fields: $($fieldReplace.Count)"

$importReplace = @{}
$shortClassReplace = @{}
foreach ($kv in $classMap.GetEnumerator()) {
    $interJava = $kv.Key.Replace("/", ".")
    $namedJava = $kv.Value.Replace("/", ".")
    if ($interJava -ne $namedJava) { $importReplace[$interJava] = $namedJava }
    $interShort = $kv.Key.Split("/")[-1]
    $namedShort = $kv.Value.Split("/")[-1]
    if ($interShort.StartsWith("class_") -and $interShort -ne $namedShort) {
        $shortClassReplace[$interShort] = $namedShort
    }
}

$methodKeys = $methodReplace.Keys | Sort-Object { $_.Length } -Descending
$fieldKeys = $fieldReplace.Keys | Sort-Object { $_.Length } -Descending
$shortClassKeys = $shortClassReplace.Keys | Sort-Object { $_.Length } -Descending

$files = Get-ChildItem -LiteralPath $srcDir -Recurse -Filter "*.java"
foreach ($file in $files) {
    $relPath = $file.FullName.Substring($srcDir.Length)
    $outPath = Join-Path $outDir $relPath
    
    # Read as raw bytes, strip BOM if present
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        $bytes = $bytes[3..($bytes.Length-1)]
    }
    $content = [System.Text.Encoding]::UTF8.GetString($bytes)
    
    # Remove CFR header
    if ($content.StartsWith("/*")) {
        $idx = $content.IndexOf("*/")
        if ($idx -gt 0) { $content = $content.Substring($idx + 2).TrimStart() }
    }
    
    # Replace imports
    foreach ($kv in $importReplace.GetEnumerator()) {
        $content = $content.Replace("import $($kv.Key);", "import $($kv.Value);")
    }
    
    # Replace short class names
    foreach ($key in $shortClassKeys) {
        $content = $content.Replace($key, $shortClassReplace[$key])
    }
    
    # Replace method names
    foreach ($key in $methodKeys) {
        $content = [regex]::Replace($content, "\b$key\b", $methodReplace[$key])
    }
    
    # Replace field names
    foreach ($key in $fieldKeys) {
        $content = [regex]::Replace($content, "\b$key\b", $fieldReplace[$key])
    }
    
    # Cleanup
    $content = $content -replace '@Environment\(value=EnvType\.CLIENT\)\s*\r?\n', ''
    $content = $content -replace 'import net\.fabricmc\.api\.EnvType;\s*\r?\n', ''
    $content = $content -replace 'import net\.fabricmc\.api\.Environment;\s*\r?\n', ''
    $content = $content -replace 'import java\.util\.Objects;\s*\r?\n', ''
    $content = $content -replace 'Objects\.requireNonNull\([^)]+\);\s*\r?\n', ''
    
    $outDir2 = Split-Path $outPath -Parent
    if (!(Test-Path -LiteralPath $outDir2)) { New-Item -ItemType Directory -Path $outDir2 -Force | Out-Null }
    [System.IO.File]::WriteAllText($outPath, $content, $utf8NoBom)
    Write-Host "Remapped: $relPath"
}

Write-Host "Done! Files: $($files.Count)"
