const { execSync } = require('child_process');
const path = require('path');

const dir = path.join('C:\\Users\\awepu\\OneDrive\\Desktop', 'Новая папка (5)');
process.chdir(dir);

try {
    const output = execSync('gradlew.bat compileClientJava', { encoding: 'utf-8', stdio: 'inherit' });
    console.log(output);
} catch (e) {
    console.error('Compilation failed:', e.message);
    process.exit(1);
}
