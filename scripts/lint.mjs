import * as fs from 'node:fs';
import * as path from 'node:path';

console.log('--- Running ZombekClient Workspace Linter ---');

const projectRoot = process.cwd();
const filesToCheck = [
  'package.json',
  'pnpm-workspace.yaml',
  'tsconfig.json',
  'apps/launcher/package.json',
  'packages/shared-types/package.json',
  'packages/config-schema/package.json',
  'packages/contracts/package.json'
];

let hasErrors = false;

for (const relPath of filesToCheck) {
  const fullPath = path.join(projectRoot, relPath);
  if (!fs.existsSync(fullPath)) {
    console.error(`[ERROR] Missing required file: ${relPath}`);
    hasErrors = true;
    continue;
  }

  if (relPath.endsWith('.json')) {
    try {
      const content = fs.readFileSync(fullPath, 'utf-8');
      JSON.parse(content);
      console.log(`[OK] Valid JSON: ${relPath}`);
    } catch (err) {
      console.error(`[ERROR] Malformed JSON in ${relPath}: ${String(err)}`);
      hasErrors = true;
    }
  } else {
    console.log(`[OK] Verified existence: ${relPath}`);
  }
}

if (hasErrors) {
  console.error('\nLinting failed with errors.');
  process.exit(1);
} else {
  console.log('\nAll linter checks passed successfully!');
  process.exit(0);
}
