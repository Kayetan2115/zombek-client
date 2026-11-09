import * as path from 'node:path';
import { JarArchiveReader } from './JarArchiveReader.js';
import { SimpleTomlParser } from './SimpleTomlParser.js';
import type {
  UniversalModMetadata,
  ModDependencyRequirement,
  MinecraftLoaderType,
} from '@zombek/shared-types';

export class ModMetadataReader {
  /**
   * Safely reads and parses metadata from a JAR file without loading any Java classes or executing code.
   */
  public static async readMetadata(jarPath: string): Promise<UniversalModMetadata> {
    const val = JarArchiveReader.validateFile(jarPath);
    if (!val.valid) {
      throw new Error(val.error);
    }

    // 1. Check for Quilt and Fabric
    const hasFabric = await JarArchiveReader.hasEntry(jarPath, 'fabric.mod.json');
    const hasQuilt = await JarArchiveReader.hasEntry(jarPath, 'quilt.mod.json');

    if (hasQuilt && hasFabric) {
      const quiltMeta = await this.parseQuiltModJson(jarPath);
      quiltMeta.loader = 'quilt-or-fabric';
      return quiltMeta;
    }

    if (hasQuilt) {
      return this.parseQuiltModJson(jarPath);
    }

    if (hasFabric) {
      return this.parseFabricModJson(jarPath);
    }

    // 2. Check for NeoForge
    const hasNeoForge = await JarArchiveReader.hasEntry(jarPath, 'META-INF/neoforge.mods.toml');
    if (hasNeoForge) {
      return this.parseNeoForgeModsToml(jarPath);
    }

    // 3. Check for Forge
    const hasForge = await JarArchiveReader.hasEntry(jarPath, 'META-INF/mods.toml');
    if (hasForge) {
      return this.parseForgeModsToml(jarPath);
    }

    // 4. Unknown metadata fallback
    return this.createUnknownMetadata(jarPath);
  }

  /**
   * Parses Fabric's fabric.mod.json.
   */
  private static async parseFabricModJson(jarPath: string): Promise<UniversalModMetadata> {
    const raw = await JarArchiveReader.readEntryText(jarPath, 'fabric.mod.json');
    if (!raw) {
      return this.createUnknownMetadata(jarPath);
    }

    try {
      const data = JSON.parse(raw);
      const id = String(data.id || path.basename(jarPath, '.jar'));
      const name = String(data.name || id);
      const version = String(data.version || '1.0.0');
      const description = String(data.description || '');

      let authors: string[] = [];
      if (Array.isArray(data.authors)) {
        authors = data.authors.map((a: any) => (typeof a === 'string' ? a : a?.name || String(a)));
      } else if (typeof data.authors === 'string') {
        authors = [data.authors];
      }

      const dependencies: ModDependencyRequirement[] = [];
      let minecraftVersionRequirement: string | undefined;
      let loaderVersionRequirement: string | undefined;

      // Depends
      if (data.depends && typeof data.depends === 'object') {
        for (const [depId, versionRange] of Object.entries(data.depends)) {
          const vStr = Array.isArray(versionRange) ? versionRange.join(' || ') : String(versionRange);
          if (depId === 'minecraft') {
            minecraftVersionRequirement = vStr;
          } else if (depId === 'fabricloader') {
            loaderVersionRequirement = vStr;
          }
          dependencies.push({
            id: depId,
            versionRange: vStr,
            kind: 'depends',
            mandatory: true,
          });
        }
      }

      // Recommends
      if (data.recommends && typeof data.recommends === 'object') {
        for (const [depId, versionRange] of Object.entries(data.recommends)) {
          dependencies.push({
            id: depId,
            versionRange: Array.isArray(versionRange) ? versionRange.join(' || ') : String(versionRange),
            kind: 'recommends',
            mandatory: false,
          });
        }
      }

      // Suggests
      if (data.suggests && typeof data.suggests === 'object') {
        for (const [depId, versionRange] of Object.entries(data.suggests)) {
          dependencies.push({
            id: depId,
            versionRange: Array.isArray(versionRange) ? versionRange.join(' || ') : String(versionRange),
            kind: 'suggests',
            mandatory: false,
          });
        }
      }

      // Conflicts / Breaks
      const conflicts = { ...(data.conflicts || {}), ...(data.breaks || {}) };
      for (const [depId, versionRange] of Object.entries(conflicts)) {
        dependencies.push({
          id: depId,
          versionRange: Array.isArray(versionRange) ? versionRange.join(' || ') : String(versionRange),
          kind: 'breaks',
          mandatory: false,
        });
      }

      return {
        id,
        name,
        version,
        description,
        authors,
        contact: data.contact,
        license: typeof data.license === 'string' ? data.license : undefined,
        environment: data.environment || '*',
        loader: 'fabric',
        minecraftVersionRequirement,
        loaderVersionRequirement,
        dependencies,
        rawSource: 'fabric.mod.json',
      };
    } catch {
      return this.createUnknownMetadata(jarPath);
    }
  }

  /**
   * Parses Quilt's quilt.mod.json.
   */
  private static async parseQuiltModJson(jarPath: string): Promise<UniversalModMetadata> {
    const raw = await JarArchiveReader.readEntryText(jarPath, 'quilt.mod.json');
    if (!raw) {
      return this.createUnknownMetadata(jarPath);
    }

    try {
      const data = JSON.parse(raw);
      const ql = data.quilt_loader || {};
      const meta = ql.metadata || {};

      const id = String(ql.id || data.id || path.basename(jarPath, '.jar'));
      const name = String(meta.name || ql.name || id);
      const version = String(ql.version || data.version || '1.0.0');
      const description = String(meta.description || ql.description || '');

      let authors: string[] = [];
      if (Array.isArray(meta.contributors)) {
        authors = meta.contributors.map((c: any) => (typeof c === 'string' ? c : c?.name || String(c)));
      } else if (meta.contributors && typeof meta.contributors === 'object') {
        authors = Object.keys(meta.contributors);
      } else if (Array.isArray(meta.authors)) {
        authors = meta.authors.map((a: any) => (typeof a === 'string' ? a : a?.name || String(a)));
      }

      const dependencies: ModDependencyRequirement[] = [];
      let minecraftVersionRequirement: string | undefined;
      let loaderVersionRequirement: string | undefined;

      // Depends
      const dependsList = ql.depends || data.depends || [];
      if (Array.isArray(dependsList)) {
        for (const item of dependsList) {
          if (typeof item === 'string') {
            dependencies.push({ id: item, kind: 'depends', mandatory: true });
          } else if (item && typeof item === 'object' && item.id) {
            const vStr = typeof item.versions === 'string' ? item.versions : undefined;
            if (item.id === 'minecraft') {
              minecraftVersionRequirement = vStr;
            } else if (item.id === 'quilt_loader') {
              loaderVersionRequirement = vStr;
            }
            dependencies.push({
              id: item.id,
              versionRange: vStr,
              kind: 'depends',
              mandatory: !item.optional,
            });
          }
        }
      }

      // Breaks
      const breaksList = ql.breaks || data.breaks || [];
      if (Array.isArray(breaksList)) {
        for (const item of breaksList) {
          const bId = typeof item === 'string' ? item : item?.id;
          if (bId) {
            dependencies.push({
              id: bId,
              versionRange: typeof item === 'object' ? item.versions : undefined,
              kind: 'breaks',
              mandatory: false,
            });
          }
        }
      }

      return {
        id,
        name,
        version,
        description,
        authors,
        contact: meta.contact,
        license: typeof meta.license === 'string' ? meta.license : undefined,
        environment: ql.environment || '*',
        loader: 'quilt',
        minecraftVersionRequirement,
        loaderVersionRequirement,
        dependencies,
        rawSource: 'quilt.mod.json',
      };
    } catch {
      return this.createUnknownMetadata(jarPath);
    }
  }

  /**
   * Parses NeoForge's META-INF/neoforge.mods.toml.
   */
  private static async parseNeoForgeModsToml(jarPath: string): Promise<UniversalModMetadata> {
    const raw = await JarArchiveReader.readEntryText(jarPath, 'META-INF/neoforge.mods.toml');
    if (!raw) {
      return this.createUnknownMetadata(jarPath);
    }

    try {
      const data = SimpleTomlParser.parse(raw);
      const modEntry = Array.isArray(data.mods) && data.mods.length > 0 ? data.mods[0] : {};

      const id = String(modEntry.modId || data.modId || path.basename(jarPath, '.jar'));
      const name = String(modEntry.displayName || modEntry.name || id);
      const version = String(modEntry.version || data.version || '1.0.0');
      const description = String(modEntry.description || '');

      let authors: string[] = [];
      if (modEntry.authors) {
        authors = Array.isArray(modEntry.authors)
          ? modEntry.authors
          : String(modEntry.authors).split(',').map((s) => s.trim());
      }

      const dependencies: ModDependencyRequirement[] = [];
      let minecraftVersionRequirement: string | undefined;
      let loaderVersionRequirement: string | undefined;

      // Extract dependencies from [[dependencies.<modId>]]
      const depsTable = data.dependencies || {};
      const modDepsList = depsTable[id] || Object.values(depsTable)[0] || [];

      if (Array.isArray(modDepsList)) {
        for (const dep of modDepsList) {
          if (!dep.modId) continue;
          const isMandatory = dep.type === 'required' || dep.mandatory === true;
          const isBreaks = dep.type === 'incompatible';

          if (dep.modId === 'minecraft') {
            minecraftVersionRequirement = dep.versionRange;
          } else if (dep.modId === 'neoforge') {
            loaderVersionRequirement = dep.versionRange;
          }

          dependencies.push({
            id: dep.modId,
            versionRange: dep.versionRange,
            kind: isBreaks ? 'breaks' : 'depends',
            mandatory: isMandatory,
          });
        }
      }

      return {
        id,
        name,
        version,
        description,
        authors,
        license: modEntry.license || data.license,
        environment: 'universal',
        loader: 'neoforge',
        minecraftVersionRequirement,
        loaderVersionRequirement,
        dependencies,
        rawSource: 'neoforge.mods.toml',
      };
    } catch {
      return this.createUnknownMetadata(jarPath);
    }
  }

  /**
   * Parses Forge's META-INF/mods.toml.
   */
  private static async parseForgeModsToml(jarPath: string): Promise<UniversalModMetadata> {
    const raw = await JarArchiveReader.readEntryText(jarPath, 'META-INF/mods.toml');
    if (!raw) {
      return this.createUnknownMetadata(jarPath);
    }

    try {
      const data = SimpleTomlParser.parse(raw);
      const modEntry = Array.isArray(data.mods) && data.mods.length > 0 ? data.mods[0] : {};

      const id = String(modEntry.modId || data.modId || path.basename(jarPath, '.jar'));
      const name = String(modEntry.displayName || modEntry.name || id);
      const version = String(modEntry.version || data.version || '1.0.0');
      const description = String(modEntry.description || '');

      let authors: string[] = [];
      if (modEntry.authors) {
        authors = Array.isArray(modEntry.authors)
          ? modEntry.authors
          : String(modEntry.authors).split(',').map((s) => s.trim());
      }

      const dependencies: ModDependencyRequirement[] = [];
      let minecraftVersionRequirement: string | undefined;
      let loaderVersionRequirement: string | undefined;

      const depsTable = data.dependencies || {};
      const modDepsList = depsTable[id] || Object.values(depsTable)[0] || [];

      if (Array.isArray(modDepsList)) {
        for (const dep of modDepsList) {
          if (!dep.modId) continue;
          const isMandatory = dep.mandatory === true || dep.type === 'required';

          if (dep.modId === 'minecraft') {
            minecraftVersionRequirement = dep.versionRange;
          } else if (dep.modId === 'forge') {
            loaderVersionRequirement = dep.versionRange;
          }

          dependencies.push({
            id: dep.modId,
            versionRange: dep.versionRange,
            kind: 'depends',
            mandatory: isMandatory,
          });
        }
      }

      return {
        id,
        name,
        version,
        description,
        authors,
        license: modEntry.license || data.license,
        environment: 'universal',
        loader: 'forge',
        minecraftVersionRequirement,
        loaderVersionRequirement,
        dependencies,
        rawSource: 'mods.toml',
      };
    } catch {
      return this.createUnknownMetadata(jarPath);
    }
  }

  /**
   * Fallback for JARs without recognized metadata.
   */
  public static createUnknownMetadata(jarPath: string): UniversalModMetadata {
    const baseName = path.basename(jarPath);
    const id = path.basename(jarPath, '.jar').toLowerCase().replace(/[^a-z0-9_-]/g, '_');

    return {
      id,
      name: baseName,
      version: 'unknown',
      description: 'Nie rozpoznano pliku metadanych modloadera (Unknown Metadata).',
      authors: [],
      loader: 'unknown',
      dependencies: [],
      rawSource: 'unknown',
    };
  }
}
