import type {
  ModConflictInfo,
  ModrinthVersion,
  ModrinthDependency,
  MinecraftLoaderType,
  ModRecord,
  UniversalModMetadata,
} from '@zombek/shared-types';
import { ModCompatibilityChecker } from './ModCompatibilityChecker.js';

export class ModConflictResolver {
  /**
   * Checks for conflicts between candidate versions and existing installed mods or instance environment.
   */
  public static detectConflicts(params: {
    candidateVersions: ModrinthVersion[];
    installedMods: ModRecord[];
    targetInstance: {
      loader: MinecraftLoaderType;
      minecraftVersion: string;
      loaderVersion?: string;
    };
    pinnedFileNames?: Set<string>;
  }): ModConflictInfo[] {
    const conflicts: ModConflictInfo[] = [];
    const { candidateVersions, installedMods, targetInstance, pinnedFileNames = new Set() } = params;

    // Map installed mods by project ID (if known) and mod ID
    const installedById = new Map<string, ModRecord>();
    for (const mod of installedMods) {
      if (mod.metadata.id) {
        installedById.set(mod.metadata.id.toLowerCase(), mod);
      }
      installedById.set(mod.fileName.toLowerCase(), mod);
    }

    for (const version of candidateVersions) {
      // 1. Check Loader compatibility
      const loaderMatch = this.isLoaderCompatible(version.loaders, targetInstance.loader);
      if (!loaderMatch) {
        conflicts.push({
          modA: version.name || version.version_number,
          modB: targetInstance.loader.toUpperCase(),
          conflictType: 'loader-mismatch',
          description: `Mod wymaga loadera [${version.loaders.join(', ')}], podczas gdy aktywna instalacja to [${targetInstance.loader.toUpperCase()}].`,
          suggestedResolution: `Zmień loader instalacji na ${version.loaders[0]} lub wybierz inne wydanie moda.`,
        });
      }

      // 2. Check Minecraft Version compatibility
      if (!version.game_versions.includes(targetInstance.minecraftVersion)) {
        conflicts.push({
          modA: version.name || version.version_number,
          modB: `Minecraft ${targetInstance.minecraftVersion}`,
          conflictType: 'mc-version-mismatch',
          description: `Mod jest przeznaczony dla wersji gry [${version.game_versions.join(', ')}], a Twoja instalacja to ${targetInstance.minecraftVersion}.`,
          suggestedResolution: `Wybierz wydanie moda wspierające wersję ${targetInstance.minecraftVersion}.`,
        });
      }

      // 3. Check declared incompatible dependencies
      for (const dep of version.dependencies || []) {
        if (dep.dependency_type === 'incompatible') {
          const conflictingTarget = dep.project_id || dep.file_name || 'nieznany mod';
          // Check if installed
          const matchInstalled = installedMods.find(
            (m) =>
              (dep.project_id && m.metadata.id.toLowerCase() === dep.project_id.toLowerCase()) ||
              (dep.file_name && m.fileName.toLowerCase() === dep.file_name.toLowerCase())
          );

          if (matchInstalled) {
            conflicts.push({
              modA: version.name || version.version_number,
              modB: matchInstalled.metadata.name || matchInstalled.fileName,
              conflictType: 'incompatible',
              description: `Mod zgłasza jawną niekompatybilność z zainstalowanym modem '${matchInstalled.metadata.name}'.`,
              suggestedResolution: `Usuń lub wyłącz mod '${matchInstalled.fileName}' przed instalacją tego wydania.`,
            });
          }
        }
      }

      // 4. Check for pinned version conflict
      const primaryFile = version.files.find((f) => f.primary) || version.files[0];
      if (primaryFile && pinnedFileNames.has(primaryFile.filename.toLowerCase())) {
        const existing = installedMods.find(
          (m) => m.fileName.toLowerCase() === primaryFile.filename.toLowerCase()
        );
        if (existing && existing.metadata.version !== version.version_number) {
          conflicts.push({
            modA: primaryFile.filename,
            modB: `Przypięta wersja (${existing.metadata.version})`,
            conflictType: 'pinned-version',
            description: `Plik '${primaryFile.filename}' jest przypięty w wersji ${existing.metadata.version}. Automatyczna aktualizacja do ${version.version_number} naruszyłaby przypięcie.`,
            suggestedResolution: `Odepnij wersję w panelu aktualizacji, jeśli chcesz zaktualizować ten mod.`,
          });
        }
      }
    }

    return conflicts;
  }

  /**
   * Evaluates if a list of loaders is compatible with an instance loader.
   */
  public static isLoaderCompatible(modLoaders: string[], instanceLoader: MinecraftLoaderType): boolean {
    const normalizedModLoaders = modLoaders.map((l) => l.toLowerCase());
    const normalizedInstance = instanceLoader.toLowerCase();

    if (normalizedInstance === 'vanilla') return false;
    if (normalizedModLoaders.includes(normalizedInstance)) return true;

    // Quilt backwards compatibility: Quilt supports Fabric mods
    if (normalizedInstance === 'quilt' && normalizedModLoaders.includes('fabric')) {
      return true;
    }

    return false;
  }
}
