import type {
  ModrinthVersion,
  ModrinthDependency,
  ModDependencyResolutionPlan,
  ModDependencyPlanItem,
  ModDependencyRecommendation,
  ModConflictInfo,
  ModRecord,
  MinecraftLoaderType,
} from '@zombek/shared-types';
import { ModrinthApiClient } from '../modrinth/ModrinthApiClient.js';
import { ModConflictResolver } from './ModConflictResolver.js';

export interface ResolveOptions {
  allowBeta?: boolean;
  allowAlpha?: boolean;
  pinnedFileNames?: Set<string>;
}

export class ModDependencyResolver {
  /**
   * Recursively resolves all required dependencies for a candidate Modrinth version.
   * Builds an installation plan and detects cycles, conflicts, embedded jars, and optional recommendations.
   */
  public static async resolvePlan(params: {
    targetVersion: ModrinthVersion;
    targetModName?: string;
    installedMods: ModRecord[];
    targetInstance: {
      loader: MinecraftLoaderType;
      minecraftVersion: string;
      loaderVersion?: string;
      instanceId: string;
    };
    options?: ResolveOptions;
  }): Promise<ModDependencyResolutionPlan> {
    const {
      targetVersion,
      targetModName = targetVersion.name || targetVersion.version_number,
      installedMods,
      targetInstance,
      options = {},
    } = params;

    const toInstallMap = new Map<string, ModDependencyPlanItem>();
    const alreadySatisfied: Array<{ projectId: string; satisfiedBy: string; isEmbedded?: boolean }> = [];
    const optionalRecommendations: ModDependencyRecommendation[] = [];
    const conflicts: ModConflictInfo[] = [];
    const manualActionRequired: Array<{ projectId: string; reason: string }> = [];

    const visitedProjects = new Set<string>();
    const visitedVersions = new Set<string>();

    if (targetVersion.project_id) {
      visitedProjects.add(targetVersion.project_id);
    }
    visitedVersions.add(targetVersion.id);

    // Build index of currently installed mod IDs and file names for instant check
    const installedProjectMap = new Map<string, ModRecord>();
    for (const mod of installedMods) {
      if (mod.metadata.id) {
        installedProjectMap.set(mod.metadata.id.toLowerCase(), mod);
      }
      installedProjectMap.set(mod.fileName.toLowerCase(), mod);
    }

    // Helper: Find if a dependency is already installed or embedded
    const findInstalled = (dep: ModrinthDependency): ModRecord | undefined => {
      if (dep.project_id && installedProjectMap.has(dep.project_id.toLowerCase())) {
        return installedProjectMap.get(dep.project_id.toLowerCase());
      }
      if (dep.file_name && installedProjectMap.has(dep.file_name.toLowerCase())) {
        return installedProjectMap.get(dep.file_name.toLowerCase());
      }
      return undefined;
    };

    // Recursive resolution function
    const resolveDependenciesRecursive = async (
      version: ModrinthVersion,
      parentName: string,
      depth: number = 0
    ): Promise<void> => {
      // Guard against excessive recursion depth
      if (depth > 15) {
        manualActionRequired.push({
          projectId: version.project_id,
          reason: `Przekroczono maksymalną głębokość drzewa zależności (15 poziomów) przy modzie ${version.name}.`,
        });
        return;
      }

      for (const dep of version.dependencies || []) {
        const depKey = dep.project_id || dep.version_id || dep.file_name || 'unknown';

        // 1. EMBEDDED DEPENDENCY:
        // Already bundled inside the JAR file (Jar-in-Jar). Never download duplicate copy!
        if (dep.dependency_type === 'embedded') {
          alreadySatisfied.push({
            projectId: depKey,
            satisfiedBy: `${version.name || parentName} (Osadzony wewnątrz pliku JAR)`,
            isEmbedded: true,
          });
          continue;
        }

        // 2. OPTIONAL DEPENDENCY:
        // User recommendation only. Do NOT download automatically.
        if (dep.dependency_type === 'optional') {
          if (!findInstalled(dep)) {
            optionalRecommendations.push({
              projectId: dep.project_id || depKey,
              versionId: dep.version_id,
              recommendedBy: parentName,
              title: dep.file_name || dep.project_id || undefined,
            });
          }
          continue;
        }

        // 3. INCOMPATIBLE DEPENDENCY:
        // Hard conflict declaration.
        if (dep.dependency_type === 'incompatible') {
          const conflictingInstalled = findInstalled(dep);
          if (conflictingInstalled) {
            conflicts.push({
              modA: parentName,
              modB: conflictingInstalled.metadata.name || conflictingInstalled.fileName,
              conflictType: 'incompatible',
              description: `Mod '${parentName}' deklaruje jawną niekompatybilność z zainstalowanym modem '${conflictingInstalled.metadata.name}'.`,
              suggestedResolution: `Wyłącz lub odinstaluj '${conflictingInstalled.fileName}' przed kontynuacją.`,
            });
          }
          continue;
        }

        // 4. REQUIRED DEPENDENCY:
        if (dep.dependency_type === 'required') {
          // Check if already installed in instance
          const existing = findInstalled(dep);
          if (existing) {
            alreadySatisfied.push({
              projectId: depKey,
              satisfiedBy: `${existing.metadata.name} (${existing.fileName})`,
              isEmbedded: false,
            });
            continue;
          }

          // Check if already planned in toInstallMap
          if (toInstallMap.has(depKey)) {
            const planned = toInstallMap.get(depKey)!;
            if (!planned.requiredBy.includes(parentName)) {
              planned.requiredBy.push(parentName);
            }
            continue;
          }

          // Circular dependency guard (e.g. A requires B, B requires A)
          if (dep.project_id && visitedProjects.has(dep.project_id)) {
            // Already visited in current chain - cycle recognized and grouped
            continue;
          }

          // Resolve required version from Modrinth API
          let resolvedVersion: ModrinthVersion | null = null;

          try {
            if (dep.version_id) {
              // Exact version specified
              resolvedVersion = await ModrinthApiClient.getVersion(dep.version_id);
            } else if (dep.project_id) {
              // Search compatible versions for active loader and MC version
              const loadersQuery =
                targetInstance.loader === 'quilt'
                  ? ['quilt', 'fabric']
                  : [targetInstance.loader.toLowerCase()];

              const versions = await ModrinthApiClient.getProjectVersions(dep.project_id, {
                loaders: loadersQuery,
                gameVersions: [targetInstance.minecraftVersion],
              });

              if (versions && versions.length > 0) {
                // Filter by preferred release type
                const releases = versions.filter((v) => v.version_type === 'release');
                const betas = versions.filter((v) => v.version_type === 'beta');

                if (releases.length > 0) {
                  resolvedVersion = releases[0];
                } else if (betas.length > 0 && (options.allowBeta || options.allowAlpha)) {
                  resolvedVersion = betas[0];
                } else if (versions.length > 0 && options.allowAlpha) {
                  resolvedVersion = versions[0];
                } else if (releases.length === 0 && betas.length > 0) {
                  // Only beta available: mark but pick best candidate
                  resolvedVersion = betas[0];
                } else {
                  resolvedVersion = versions[0];
                }
              }
            }
          } catch (err: any) {
            console.warn(`[DependencyResolver] Błąd pobierania zależności ${depKey}:`, err.message);
          }

          if (!resolvedVersion) {
            manualActionRequired.push({
              projectId: depKey,
              reason: `Nie udało się automatycznie odnaleźć kompatybilnej wersji zależności '${depKey}' dla ${targetInstance.loader.toUpperCase()} ${targetInstance.minecraftVersion}. Wymagana ręczna instalacja.`,
            });
            continue;
          }

          // Mark project as visited
          if (dep.project_id) visitedProjects.add(dep.project_id);
          visitedVersions.add(resolvedVersion.id);

          // Add to installation plan
          toInstallMap.set(depKey, {
            projectId: dep.project_id || resolvedVersion.project_id,
            version: resolvedVersion,
            requiredBy: [parentName],
            dependencyType: 'required',
            reason: `Wymagane przez '${parentName}'`,
          });

          // Recurse into this dependency's dependencies!
          await resolveDependenciesRecursive(
            resolvedVersion,
            resolvedVersion.name || resolvedVersion.version_number,
            depth + 1
          );
        }
      }
    };

    // Begin recursion starting with targetVersion
    await resolveDependenciesRecursive(targetVersion, targetModName, 0);

    // Run Conflict Resolution on all planned versions + target version
    const allCandidates = [targetVersion, ...Array.from(toInstallMap.values()).map((p) => p.version)];
    const detectedConflicts = ModConflictResolver.detectConflicts({
      candidateVersions: allCandidates,
      installedMods,
      targetInstance,
      pinnedFileNames: options.pinnedFileNames,
    });
    conflicts.push(...detectedConflicts);

    const toInstall = Array.from(toInstallMap.values());
    const canProceed = conflicts.length === 0 && manualActionRequired.length === 0;

    return {
      targetVersionId: targetVersion.id,
      targetModName,
      toInstall,
      alreadySatisfied,
      optionalRecommendations,
      conflicts,
      manualActionRequired,
      canProceed,
    };
  }
}
