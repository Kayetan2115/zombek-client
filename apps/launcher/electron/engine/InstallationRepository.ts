import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from './paths.js';
import type { InstanceRecord } from '@zombek/shared-types';

export class InstallationRepository {
  public static async getAll(): Promise<InstanceRecord[]> {
    EnginePaths.ensureAllBaseDirs();
    const instancesDir = EnginePaths.getInstancesDir();
    const entries = await fs.promises.readdir(instancesDir, { withFileTypes: true });

    const results: InstanceRecord[] = [];
    for (const entry of entries) {
      if (entry.isDirectory()) {
        const jsonPath = path.join(instancesDir, entry.name, 'instance.json');
        if (fs.existsSync(jsonPath)) {
          try {
            const raw = await fs.promises.readFile(jsonPath, 'utf-8');
            const data = JSON.parse(raw) as InstanceRecord;
            if (!data.loaderVersion) {
              if (data.loader === 'fabric') data.loaderVersion = '0.16.10';
              else if (data.loader === 'forge') data.loaderVersion = '49.1.0';
              else if (data.loader === 'neoforge') data.loaderVersion = '20.4.80-beta';
              else if (data.loader === 'quilt') data.loaderVersion = '0.27.1-beta.1';
              else data.loaderVersion = data.minecraftVersion;
            }
            results.push(data);
          } catch {
            // Ignore broken instance json
          }
        }
      }
    }

    if (results.length === 0) {
      // Seed default instances for all supported loaders and vanilla
      const defaultProfiles: Array<Omit<InstanceRecord, 'id' | 'created' | 'instanceDir'>> = [
        {
          name: 'ZombekClient Fabric 1.20.4 (Zalecany)',
          minecraftVersion: '1.20.4',
          loader: 'fabric',
          loaderVersion: '0.16.10',
          allocatedRamMb: 4096,
          customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
          resolutionWidth: 1280,
          resolutionHeight: 720,
          fullscreen: false
        },
        {
          name: 'ZombekClient Quilt 1.20.4',
          minecraftVersion: '1.20.4',
          loader: 'quilt',
          loaderVersion: '0.27.1-beta.1',
          allocatedRamMb: 4096,
          customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
          resolutionWidth: 1280,
          resolutionHeight: 720,
          fullscreen: false
        },
        {
          name: 'ZombekClient Forge 1.20.4',
          minecraftVersion: '1.20.4',
          loader: 'forge',
          loaderVersion: '49.1.0',
          allocatedRamMb: 4096,
          customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
          resolutionWidth: 1280,
          resolutionHeight: 720,
          fullscreen: false
        },
        {
          name: 'ZombekClient NeoForge 1.20.4',
          minecraftVersion: '1.20.4',
          loader: 'neoforge',
          loaderVersion: '20.4.80-beta',
          allocatedRamMb: 4096,
          customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
          resolutionWidth: 1280,
          resolutionHeight: 720,
          fullscreen: false
        },
        {
          name: 'Vanilla 1.20.4 (Oficjalny)',
          minecraftVersion: '1.20.4',
          loader: 'vanilla',
          loaderVersion: '1.20.4',
          allocatedRamMb: 4096,
          customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
          resolutionWidth: 1280,
          resolutionHeight: 720,
          fullscreen: false
        }
      ];

      for (const p of defaultProfiles) {
        const inst = await this.create(p);
        results.push(inst);
      }
    }

    return results;
  }

  public static async getById(id: string): Promise<InstanceRecord | null> {
    EnginePaths.assertSafeFilename(id);
    const jsonPath = path.join(EnginePaths.getInstanceDir(id), 'instance.json');
    if (!fs.existsSync(jsonPath)) return null;

    try {
      const raw = await fs.promises.readFile(jsonPath, 'utf-8');
      return JSON.parse(raw) as InstanceRecord;
    } catch {
      return null;
    }
  }

  public static async create(data: Omit<InstanceRecord, 'id' | 'created' | 'instanceDir'>): Promise<InstanceRecord> {
    EnginePaths.ensureAllBaseDirs();

    const id = `inst-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
    const instanceDir = EnginePaths.getInstanceDir(id);

    // Create instance directories
    await fs.promises.mkdir(instanceDir, { recursive: true });
    await fs.promises.mkdir(path.join(instanceDir, 'saves'), { recursive: true });
    await fs.promises.mkdir(path.join(instanceDir, 'resourcepacks'), { recursive: true });
    await fs.promises.mkdir(path.join(instanceDir, 'config'), { recursive: true });
    await fs.promises.mkdir(path.join(instanceDir, 'mods'), { recursive: true });
    await fs.promises.mkdir(path.join(instanceDir, 'screenshots'), { recursive: true });
    await fs.promises.mkdir(path.join(instanceDir, 'natives'), { recursive: true });

    const record: InstanceRecord = {
      ...data,
      id,
      created: new Date().toISOString(),
      instanceDir
    };

    const jsonPath = path.join(instanceDir, 'instance.json');
    await fs.promises.writeFile(jsonPath, JSON.stringify(record, null, 2), 'utf-8');

    return record;
  }

  public static async update(id: string, updates: Partial<InstanceRecord>): Promise<InstanceRecord> {
    const existing = await this.getById(id);
    if (!existing) {
      throw new Error(`Instalacja o ID ${id} nie istnieje.`);
    }

    const updated: InstanceRecord = {
      ...existing,
      ...updates,
      id: existing.id,
      instanceDir: existing.instanceDir // prevent changing directory path
    };

    const jsonPath = path.join(existing.instanceDir, 'instance.json');
    await fs.promises.writeFile(jsonPath, JSON.stringify(updated, null, 2), 'utf-8');

    return updated;
  }

  public static async delete(id: string): Promise<boolean> {
    EnginePaths.assertSafeFilename(id);
    const instanceDir = EnginePaths.getInstanceDir(id);

    if (!EnginePaths.isSafePath(EnginePaths.getInstancesDir(), instanceDir)) {
      throw new Error(`Niebezpieczna ścieżka do usunięcia: ${instanceDir}`);
    }

    if (fs.existsSync(instanceDir)) {
      await fs.promises.rm(instanceDir, { recursive: true, force: true });
      return true;
    }
    return false;
  }
}
