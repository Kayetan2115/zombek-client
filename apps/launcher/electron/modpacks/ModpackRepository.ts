import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from '../engine/paths.js';
import type { ModpackRecord } from '@zombek/shared-types';

export class ModpackRepository {
  private static cachedModpacks: ModpackRecord[] | null = null;

  public static getStoragePath(): string {
    return EnginePaths.getModpacksFilePath();
  }

  public static clearMemoryCache(): void {
    this.cachedModpacks = null;
  }

  public static async getAll(): Promise<ModpackRecord[]> {
    if (this.cachedModpacks) {
      return this.cachedModpacks;
    }

    const filePath = this.getStoragePath();
    if (!fs.existsSync(filePath)) {
      const initial = this.getInitialModpacks();
      await this.save(initial);
      this.cachedModpacks = initial;
      return initial;
    }

    try {
      const raw = await fs.promises.readFile(filePath, 'utf-8');
      const list = JSON.parse(raw) as ModpackRecord[];
      this.cachedModpacks = list;
      return list;
    } catch {
      const initial = this.getInitialModpacks();
      await this.save(initial);
      this.cachedModpacks = initial;
      return initial;
    }
  }

  public static async getById(id: string): Promise<ModpackRecord | null> {
    const list = await this.getAll();
    return list.find((p) => p.id === id) || null;
  }

  public static async create(
    data: Omit<
      ModpackRecord,
      'id' | 'created' | 'lastModified' | 'modsCount' | 'modFiles' | 'compatibilityStatus'
    >
  ): Promise<ModpackRecord> {
    const list = await this.getAll();
    const now = new Date().toISOString().substring(0, 10);
    const id = `modpack-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`;

    const newPack: ModpackRecord = {
      id,
      name: data.name,
      description: data.description || '',
      minecraftVersion: data.minecraftVersion,
      loader: data.loader,
      loaderVersion: data.loaderVersion,
      created: now,
      lastModified: now,
      instanceId: data.instanceId,
      isDefault: data.isDefault ?? false,
      modsCount: 0,
      modFiles: [],
      compatibilityStatus: 'Compatible',
    };

    const updated = [newPack, ...list];
    await this.save(updated);
    this.cachedModpacks = updated;
    return newPack;
  }

  public static async update(
    id: string,
    updates: Partial<ModpackRecord>
  ): Promise<ModpackRecord> {
    const list = await this.getAll();
    const idx = list.findIndex((p) => p.id === id);
    if (idx === -1) {
      throw new Error(`Modpack o ID '${id}' nie istnieje.`);
    }

    const updated: ModpackRecord = {
      ...list[idx],
      ...updates,
      id, // protect id
      lastModified: new Date().toISOString().substring(0, 10),
    };

    list[idx] = updated;
    await this.save(list);
    this.cachedModpacks = list;
    return updated;
  }

  public static async delete(id: string): Promise<boolean> {
    const list = await this.getAll();
    const filtered = list.filter((p) => p.id !== id);
    if (filtered.length === list.length) {
      return false;
    }

    await this.save(filtered);
    this.cachedModpacks = filtered;
    return true;
  }

  public static async duplicate(id: string, newName?: string): Promise<ModpackRecord> {
    const original = await this.getById(id);
    if (!original) {
      throw new Error(`Modpack o ID '${id}' nie istnieje.`);
    }

    const name = newName || `${original.name} (Kopia)`;
    const duplicated = await this.create({
      name,
      description: original.description,
      minecraftVersion: original.minecraftVersion,
      loader: original.loader,
      loaderVersion: original.loaderVersion,
      isDefault: false,
    });

    // Copy mod file lists
    return this.update(duplicated.id, {
      modFiles: [...original.modFiles],
      modsCount: original.modsCount,
      compatibilityStatus: original.compatibilityStatus,
    });
  }

  private static async save(list: ModpackRecord[]): Promise<void> {
    EnginePaths.ensureAllBaseDirs();
    const filePath = this.getStoragePath();
    const dir = path.dirname(filePath);
    if (!fs.existsSync(dir)) {
      await fs.promises.mkdir(dir, { recursive: true });
    }
    await fs.promises.writeFile(filePath, JSON.stringify(list, null, 2), 'utf-8');
  }

  private static getInitialModpacks(): ModpackRecord[] {
    const today = new Date().toISOString().substring(0, 10);
    return [
      {
        id: 'pack-pvp-fps',
        name: 'Zombek PvP & FPS Boost',
        description: 'Wysoka wydajność, optymalizacja pamięci RAM i moduły PvP.',
        minecraftVersion: '1.20.4',
        loader: 'fabric',
        loaderVersion: '0.16.10',
        created: today,
        lastModified: today,
        modsCount: 0,
        modFiles: [],
        compatibilityStatus: 'Compatible',
        isDefault: true,
      },
      {
        id: 'pack-survival-plus',
        name: 'Survival Plus',
        description: 'Rozszerzony świat, nowe mechaniki i zaawansowany crafting.',
        minecraftVersion: '1.20.4',
        loader: 'forge',
        loaderVersion: '49.2.13',
        created: today,
        lastModified: today,
        modsCount: 0,
        modFiles: [],
        compatibilityStatus: 'Compatible',
        isDefault: false,
      },
      {
        id: 'pack-vanilla-enhanced',
        name: 'Vanilla Enhanced',
        description: 'Delikatne usprawnienia interfejsu i dźwięków z zachowaniem klimatu Vanilla.',
        minecraftVersion: '1.20.4',
        loader: 'quilt',
        loaderVersion: '0.27.1-beta.1',
        created: today,
        lastModified: today,
        modsCount: 0,
        modFiles: [],
        compatibilityStatus: 'Compatible',
        isDefault: false,
      },
    ];
  }
}
