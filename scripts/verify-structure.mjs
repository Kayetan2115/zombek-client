import * as fs from 'node:fs';
import * as path from 'node:path';

console.log('--- Verifying ZombekClient Folder Structure ---');

const requiredPaths = [
  'apps/launcher/electron/main',
  'apps/launcher/electron/preload',
  'apps/launcher/electron/ipc',
  'apps/launcher/src/app',
  'apps/launcher/src/pages',
  'apps/launcher/src/components',
  'apps/launcher/src/features',
  'apps/launcher/src/hooks',
  'apps/launcher/src/stores',
  'apps/launcher/src/styles',
  'apps/launcher/src/assets',
  'apps/launcher/src/utils',
  'apps/launcher/tests',
  'apps/launcher/package.json',
  'apps/minecraft-client/fabric/src/main/java/pl/zombek/client/core',
  'apps/minecraft-client/fabric/src/main/java/pl/zombek/client/modules',
  'apps/minecraft-client/fabric/src/main/java/pl/zombek/client/hud',
  'apps/minecraft-client/fabric/src/main/java/pl/zombek/client/config',
  'apps/minecraft-client/fabric/src/main/java/pl/zombek/client/render',
  'apps/minecraft-client/fabric/src/main/java/pl/zombek/client/integration',
  'apps/minecraft-client/fabric/src/main/resources',
  'apps/minecraft-client/fabric/src/test',
  'apps/minecraft-client/fabric/build.gradle',
  'apps/minecraft-client/fabric/settings.gradle',
  'apps/minecraft-client/fabric/gradle.properties',
  'packages/shared-types',
  'packages/config-schema',
  'packages/contracts',
  'services/cosmetics-api/README.md',
  'docs/architecture.md',
  'docs/folder-structure.md',
  'docs/project-status.md',
  'docs/roadmap.md',
  'docs/decisions.md',
  'docs/testing.md',
  'scripts',
  'package.json',
  'pnpm-workspace.yaml',
  'tsconfig.json',
  '.gitignore',
  'README.md'
];

const projectRoot = process.cwd();
let missing = 0;

for (const p of requiredPaths) {
  const full = path.join(projectRoot, p);
  if (!fs.existsSync(full)) {
    console.error(`[MISSING] ${p}`);
    missing++;
  } else {
    console.log(`[EXISTS]  ${p}`);
  }
}

if (missing > 0) {
  console.error(`\nStructure verification failed: ${missing} missing paths.`);
  process.exit(1);
} else {
  console.log(`\nAll ${requiredPaths.length} required paths verified successfully!`);
  process.exit(0);
}
