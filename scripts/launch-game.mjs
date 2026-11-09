if (!process.env.NODE_TLS_REJECT_UNAUTHORIZED) {
  process.env.NODE_TLS_REJECT_UNAUTHORIZED = '0';
}

import { GameLauncher } from '../apps/launcher/dist-electron/engine/GameLauncher.js';
import { InstallationRepository } from '../apps/launcher/dist-electron/engine/InstallationRepository.js';

async function main() {
  const instances = await InstallationRepository.getAll();
  if (!instances || instances.length === 0) {
    console.error('Nie znaleziono żadnej instancji do uruchomienia.');
    process.exit(1);
  }

  const query = process.argv[2];
  const username = process.argv[3] || 'ZombekPlayer';

  let inst = instances[0];
  if (query) {
    const matched = instances.find(i => 
      i.id === query || 
      i.loader.toLowerCase() === query.toLowerCase() ||
      i.name.toLowerCase().includes(query.toLowerCase())
    );
    if (matched) {
      inst = matched;
    } else {
      console.warn(`[INFO] Nie znaleziono instancji pasującej do '${query}'. Używam domyślnej: ${inst.name}`);
    }
  }

  console.log(`======================================================================`);
  console.log(`Uruchamianie Minecraft: ${inst.name} (${inst.loader} ${inst.minecraftVersion})`);
  console.log(`Gracz: ${username}`);
  console.log(`Katalog instancji: ${inst.instanceDir}`);
  console.log(`======================================================================\n`);

  console.log('Dostepne instancje w systemie:');
  for (const i of instances) {
    const mark = i.id === inst.id ? '->' : '  ';
    console.log(` ${mark} [${i.loader}] ${i.name} (id: ${i.id})`);
  }
  console.log('');

  const launcher = GameLauncher.getInstance();

  launcher.setCallbacks({
    onStatusChange: (status, msg) => {
      console.log(`[STATUS] ${status}: ${msg}`);
    },
    onProgress: (p) => {
      if (p.percent && p.percent % 25 === 0) {
        console.log(`[PROGRESS] ${p.percent}% - ${p.message || p.currentFile}`);
      }
    },
    onLog: (l) => {
      if (l.category === 'MINECRAFT') {
        console.log(`[MC] ${l.message}`);
      } else if (l.level === 'ERROR') {
        console.error(`[${l.category}] ${l.message}`);
      } else {
        console.log(`[${l.category}] ${l.message}`);
      }
    }
  });

  await launcher.launch(inst.id, { offlineUsername: username });
}

main().catch((err) => {
  console.error('\n[BLAD URUCHOMIENIA]', err.message);
  process.exit(1);
});
