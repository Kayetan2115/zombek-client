import type {
  UniversalModMetadata,
  ModCompatibilityStatus,
  MinecraftLoaderType,
} from '@zombek/shared-types';
import { ModCompatibilityChecker } from './ModCompatibilityChecker.js';

export interface DependencyAnalysisResult {
  status: ModCompatibilityStatus;
  missingDependencies: string[];
  conflictingMods: string[];
  details?: string;
}

export class ModDependencyAnalyzer {
  /**
   * Analyzes dependencies and conflicts of a mod against currently installed/active mods.
   */
  public static analyzeDependencies(
    mod: UniversalModMetadata,
    installedMods: UniversalModMetadata[],
    targetInstance: { loader: MinecraftLoaderType; minecraftVersion: string; loaderVersion?: string }
  ): DependencyAnalysisResult {
    // 1. Initial compatibility check
    const baseCheck = ModCompatibilityChecker.checkCompatibility(mod, targetInstance);
    if (baseCheck.status === 'Incompatible' || baseCheck.status === 'Broken File') {
      return {
        status: baseCheck.status,
        missingDependencies: [],
        conflictingMods: [],
        details: baseCheck.reason,
      };
    }

    const missingDependencies: string[] = [];
    const conflictingMods: string[] = [];

    // Map installed mod IDs for fast lookup
    const installedIds = new Set<string>();
    const installedMap = new Map<string, UniversalModMetadata>();

    for (const inst of installedMods) {
      if (inst.id && inst.id !== mod.id) {
        installedIds.add(inst.id.toLowerCase());
        installedMap.set(inst.id.toLowerCase(), inst);
      }
    }

    // 2. Evaluate dependencies declared by the mod
    for (const dep of mod.dependencies || []) {
      const depId = dep.id.toLowerCase();

      // System dependencies (minecraft, loaders, java)
      if (depId === 'minecraft') {
        if (
          dep.versionRange &&
          !ModCompatibilityChecker.matchesMinecraftVersion(dep.versionRange, targetInstance.minecraftVersion)
        ) {
          missingDependencies.push(`Minecraft (${dep.versionRange})`);
        }
        continue;
      }

      if (depId === 'fabricloader' || depId === 'fabric-loader') {
        if (targetInstance.loader !== 'fabric' && targetInstance.loader !== 'quilt') {
          missingDependencies.push(`Fabric Loader (${dep.versionRange || 'dowolna'})`);
        }
        continue;
      }

      if (depId === 'quilt_loader' || depId === 'quilt-loader') {
        if (targetInstance.loader !== 'quilt') {
          missingDependencies.push(`Quilt Loader (${dep.versionRange || 'dowolna'})`);
        }
        continue;
      }

      if (depId === 'forge') {
        if (targetInstance.loader !== 'forge') {
          missingDependencies.push(`Forge (${dep.versionRange || 'dowolna'})`);
        }
        continue;
      }

      if (depId === 'neoforge') {
        if (targetInstance.loader !== 'neoforge') {
          missingDependencies.push(`NeoForge (${dep.versionRange || 'dowolna'})`);
        }
        continue;
      }

      if (depId === 'java') {
        continue; // Java runtime is verified at launcher launch level
      }

      // Check conflicts / breaks
      if (dep.kind === 'breaks' || dep.kind === 'conflicts') {
        if (installedIds.has(depId)) {
          const conflicting = installedMap.get(depId);
          conflictingMods.push(conflicting ? `${conflicting.name} (${conflicting.id})` : depId);
        }
        continue;
      }

      // Check mandatory dependencies
      if (dep.mandatory) {
        if (!installedIds.has(depId)) {
          const formatted = dep.versionRange ? `${dep.id} (${dep.versionRange})` : dep.id;
          missingDependencies.push(formatted);
        } else if (dep.versionRange) {
          const installed = installedMap.get(depId);
          if (
            installed &&
            installed.version !== 'unknown' &&
            !ModCompatibilityChecker.matchesMinecraftVersion(dep.versionRange, installed.version)
          ) {
            missingDependencies.push(`${dep.id} (wymagana wersja: ${dep.versionRange}, zainstalowana: ${installed.version})`);
          }
        }
      }
    }

    // 3. Also check if any currently installed mod declares a conflict/break against this mod
    for (const inst of installedMods) {
      if (inst.id === mod.id) continue;
      for (const instDep of inst.dependencies || []) {
        if (instDep.kind === 'breaks' || instDep.kind === 'conflicts') {
          if (instDep.id.toLowerCase() === mod.id.toLowerCase()) {
            conflictingMods.push(`${inst.name} (${inst.id}) zgłasza konflikt z tym modem`);
          }
        }
      }
    }

    // 4. Synthesize final status
    if (conflictingMods.length > 0) {
      return {
        status: 'Dependency Conflict',
        missingDependencies,
        conflictingMods,
        details: `Wykryto konflikt z modami: ${conflictingMods.join(', ')}`,
      };
    }

    if (missingDependencies.length > 0) {
      return {
        status: 'Missing Dependency',
        missingDependencies,
        conflictingMods,
        details: `Brakujące wymagane zależności: ${missingDependencies.join(', ')}`,
      };
    }

    return {
      status: baseCheck.status,
      missingDependencies: [],
      conflictingMods: [],
      details: baseCheck.reason,
    };
  }
}
