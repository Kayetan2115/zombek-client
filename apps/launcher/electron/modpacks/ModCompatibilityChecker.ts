import type {
  UniversalModMetadata,
  MinecraftLoaderType,
  ModCompatibilityStatus,
} from '@zombek/shared-types';

export interface CompatibilityTarget {
  loader: MinecraftLoaderType;
  minecraftVersion: string;
  loaderVersion?: string;
}

export interface CompatibilityCheckResult {
  status: ModCompatibilityStatus;
  reason?: string;
}

export class ModCompatibilityChecker {
  /**
   * Checks if a mod's loader, minecraft version, and environment match the target instance.
   */
  public static checkCompatibility(
    mod: UniversalModMetadata,
    target: CompatibilityTarget
  ): CompatibilityCheckResult {
    // 1. Vanilla check: Vanilla cannot run JAR mods
    if (target.loader === 'vanilla') {
      return {
        status: 'Incompatible',
        reason: 'Wersja Vanilla nie obsługuje modów JAR. Utwórz instalację z loaderem Fabric, Forge, NeoForge lub Quilt.',
      };
    }

    // 2. Unknown metadata check
    if (mod.rawSource === 'unknown' || mod.loader === 'unknown') {
      return {
        status: 'Unknown Metadata',
        reason: 'Brak metadanych modloadera. Nie można jednoznacznie potwierdzić kompatybilności.',
      };
    }

    // 3. Loader compatibility
    const loaderCheck = this.checkLoaderMatch(mod.loader, target.loader);
    if (loaderCheck.status !== 'Compatible') {
      return loaderCheck;
    }

    // 4. Minecraft Version compatibility
    if (mod.minecraftVersionRequirement) {
      const matchesMc = this.matchesMinecraftVersion(
        mod.minecraftVersionRequirement,
        target.minecraftVersion
      );
      if (!matchesMc) {
        return {
          status: 'Incompatible',
          reason: `Wymagana wersja Minecraft: ${mod.minecraftVersionRequirement}, a bieżąca instalacja to ${target.minecraftVersion}.`,
        };
      }
    }

    // 5. Environment check
    if (mod.environment && mod.environment.toLowerCase() === 'server') {
      return {
        status: 'Incompatible',
        reason: 'Mod jest przeznaczony wyłącznie dla serwerów dedykowanych (Server-side only).',
      };
    }

    return {
      status: loaderCheck.status,
      reason: loaderCheck.reason || 'Mod jest w pełni kompatybilny z wybraną instalacją.',
    };
  }

  /**
   * Checks whether the mod's declared loader matches the target instance's loader.
   */
  private static checkLoaderMatch(
    modLoader: MinecraftLoaderType | 'quilt-or-fabric' | 'unknown',
    targetLoader: MinecraftLoaderType
  ): CompatibilityCheckResult {
    if (modLoader === targetLoader) {
      return { status: 'Compatible' };
    }

    // Dual compatibility: Fabric & Quilt
    if (modLoader === 'quilt-or-fabric') {
      if (targetLoader === 'fabric' || targetLoader === 'quilt') {
        return { status: 'Compatible' };
      }
      return {
        status: 'Incompatible',
        reason: `Mod wymaga Fabric lub Quilt, a instalacja używa ${targetLoader.toUpperCase()}.`,
      };
    }

    // Fabric mod on Quilt
    if (modLoader === 'fabric' && targetLoader === 'quilt') {
      return {
        status: 'Possibly Compatible',
        reason: 'Mod Fabric może działać na Quilt dzięki wstecznej kompatybilności QuiltMC.',
      };
    }

    // Quilt mod on Fabric
    if (modLoader === 'quilt' && targetLoader === 'fabric') {
      return {
        status: 'Incompatible',
        reason: 'Mod Quilt wymaga środowiska QuiltMC i nie działa bezpośrednio na czystym Fabric.',
      };
    }

    // Forge vs NeoForge
    if (modLoader === 'forge' && targetLoader === 'neoforge') {
      return {
        status: 'Incompatible',
        reason: 'Mod Forge nie jest kompatybilny ze środowiskiem NeoForge.',
      };
    }

    if (modLoader === 'neoforge' && targetLoader === 'forge') {
      return {
        status: 'Incompatible',
        reason: 'Mod NeoForge nie jest kompatybilny ze środowiskiem Forge.',
      };
    }

    return {
      status: 'Incompatible',
      reason: `Mod wymaga środowiska ${modLoader.toUpperCase()}, a instalacja używa ${targetLoader.toUpperCase()}.`,
    };
  }

  /**
   * Evaluates if a target Minecraft version satisfies a required version range.
   */
  public static matchesMinecraftVersion(requiredRange: string, gameVersion: string): boolean {
    if (!requiredRange || requiredRange === '*' || requiredRange === 'any') {
      return true;
    }

    const rangeStr = requiredRange.trim();

    // Check "||" disjunction
    if (rangeStr.includes('||')) {
      const options = rangeStr.split('||').map((s) => s.trim());
      return options.some((opt) => this.matchesMinecraftVersion(opt, gameVersion));
    }

    // Exact match
    if (rangeStr === gameVersion) return true;

    // Wildcard match (e.g. 1.20.x, 1.20.*)
    if (rangeStr.endsWith('.x') || rangeStr.endsWith('.*')) {
      const prefix = rangeStr.slice(0, -2);
      return gameVersion === prefix || gameVersion.startsWith(prefix + '.');
    }

    // Range: [min, max], [min, max), (min, max)
    if (
      (rangeStr.startsWith('[') || rangeStr.startsWith('(')) &&
      (rangeStr.endsWith(']') || rangeStr.endsWith(')'))
    ) {
      const minInclusive = rangeStr.startsWith('[');
      const maxInclusive = rangeStr.endsWith(']');
      const inner = rangeStr.substring(1, rangeStr.length - 1);
      const parts = inner.split(',').map((p) => p.trim());

      const minVer = parts[0] || '';
      const maxVer = parts[1] || '';

      if (minVer && minVer.length > 0) {
        const cmp = this.compareVersions(gameVersion, minVer);
        if (minInclusive ? cmp < 0 : cmp <= 0) return false;
      }
      if (maxVer && maxVer.length > 0) {
        const cmp = this.compareVersions(gameVersion, maxVer);
        if (maxInclusive ? cmp > 0 : cmp >= 0) return false;
      }
      return true;
    }

    // Comparison operators: >=, <=, >, <, ~, ^
    if (rangeStr.startsWith('>=')) {
      return this.compareVersions(gameVersion, rangeStr.substring(2).trim()) >= 0;
    }
    if (rangeStr.startsWith('<=')) {
      return this.compareVersions(gameVersion, rangeStr.substring(2).trim()) <= 0;
    }
    if (rangeStr.startsWith('>')) {
      return this.compareVersions(gameVersion, rangeStr.substring(1).trim()) > 0;
    }
    if (rangeStr.startsWith('<')) {
      return this.compareVersions(gameVersion, rangeStr.substring(1).trim()) < 0;
    }
    if (rangeStr.startsWith('~') || rangeStr.startsWith('^')) {
      const base = rangeStr.substring(1).trim();
      const parts = base.split('.');
      if (parts.length >= 2) {
        return (
          gameVersion.startsWith(parts[0] + '.' + parts[1]) &&
          this.compareVersions(gameVersion, base) >= 0
        );
      }
      return gameVersion.startsWith(parts[0]);
    }

    // Default prefix match (e.g. 1.20 matches 1.20.1, 1.20.4)
    return gameVersion === rangeStr || gameVersion.startsWith(rangeStr + '.');
  }

  /**
   * Compares two semantic version strings.
   * Returns: negative if a < b, 0 if a == b, positive if a > b.
   */
  public static compareVersions(a: string, b: string): number {
    const cleanA = a.replace(/[^0-9.]/g, '');
    const cleanB = b.replace(/[^0-9.]/g, '');

    const partsA = cleanA.split('.').map((p) => parseInt(p, 10) || 0);
    const partsB = cleanB.split('.').map((p) => parseInt(p, 10) || 0);

    const len = Math.max(partsA.length, partsB.length);
    for (let i = 0; i < len; i++) {
      const valA = partsA[i] || 0;
      const valB = partsB[i] || 0;
      if (valA !== valB) {
        return valA - valB;
      }
    }
    return 0;
  }
}
