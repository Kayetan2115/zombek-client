import type {
  MrpackIndex,
  MrpackIndexFile,
  MrpackDependencies,
  MinecraftLoaderType,
} from '@zombek/shared-types';
import { MrpackPathValidator } from './MrpackPathValidator.js';

export interface MrpackManifestValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
  loaderType: MinecraftLoaderType;
  loaderVersion?: string;
  minecraftVersion: string;
  manifest?: MrpackIndex;
}

export class MrpackManifestValidator {
  public static readonly SUPPORTED_FORMAT_VERSION = 1;
  public static readonly SUPPORTED_GAME = 'minecraft';

  private static readonly KNOWN_DEPENDENCY_KEYS = new Set([
    'minecraft',
    'fabric-loader',
    'forge',
    'neoforge',
    'quilt-loader',
  ]);

  /**
   * Validates a parsed or raw modrinth.index.json against the official Modrinth formatVersion 1 specification.
   */
  public static validate(rawJson: unknown): MrpackManifestValidationResult {
    const errors: string[] = [];
    const warnings: string[] = [];

    if (!rawJson || typeof rawJson !== 'object') {
      return {
        valid: false,
        errors: ['Manifest modrinth.index.json nie jest poprawnym obiektem JSON.'],
        warnings: [],
        loaderType: 'vanilla',
        minecraftVersion: '',
      };
    }

    const index = rawJson as Partial<MrpackIndex>;

    // 1. Check formatVersion
    if (typeof index.formatVersion !== 'number') {
      errors.push("Brak wymaganego pola 'formatVersion' w manifeście.");
    } else if (index.formatVersion !== this.SUPPORTED_FORMAT_VERSION) {
      errors.push(
        `Nieobsługiwana wersja formatu mrpack: formatVersion=${index.formatVersion} (obsługiwana wersja: ${this.SUPPORTED_FORMAT_VERSION}).`
      );
    }

    // 2. Check game
    if (typeof index.game !== 'string') {
      errors.push("Brak wymaganego pola 'game' w manifeście.");
    } else if (index.game.toLowerCase() !== this.SUPPORTED_GAME) {
      errors.push(
        `Nieobsługiwana gra (game): '${index.game}'. ZombekClient obsługuje wyłącznie modpacki dla gry '${this.SUPPORTED_GAME}'.`
      );
    }

    // 3. Check name & versionId
    if (!index.name || typeof index.name !== 'string' || index.name.trim().length === 0) {
      errors.push("Pole 'name' w manifeście jest puste lub nieprawidłowe.");
    }

    if (!index.versionId || typeof index.versionId !== 'string' || index.versionId.trim().length === 0) {
      errors.push("Pole 'versionId' w manifeście jest puste lub nieprawidłowe.");
    }

    // 4. Check dependencies (Minecraft & Modloader)
    let loaderType: MinecraftLoaderType = 'vanilla';
    let loaderVersion: string | undefined = undefined;
    let minecraftVersion = '';

    if (!index.dependencies || typeof index.dependencies !== 'object') {
      errors.push("Brak wymaganego obiektu 'dependencies' w manifeście.");
    } else {
      const deps = index.dependencies as Record<string, string | undefined>;

      // Minecraft version is strictly required
      if (!deps.minecraft || typeof deps.minecraft !== 'string' || deps.minecraft.trim().length === 0) {
        errors.push("Brak wymaganej wersji Minecrafta ('dependencies.minecraft').");
      } else {
        minecraftVersion = deps.minecraft.trim();
      }

      // Detect loaders and check for conflicting combinations
      const detectedLoaders: Array<{ type: MinecraftLoaderType; key: string; version: string }> = [];

      if (deps['fabric-loader'] && typeof deps['fabric-loader'] === 'string') {
        detectedLoaders.push({ type: 'fabric', key: 'fabric-loader', version: deps['fabric-loader'].trim() });
      }
      if (deps['forge'] && typeof deps['forge'] === 'string') {
        detectedLoaders.push({ type: 'forge', key: 'forge', version: deps['forge'].trim() });
      }
      if (deps['neoforge'] && typeof deps['neoforge'] === 'string') {
        detectedLoaders.push({ type: 'neoforge', key: 'neoforge', version: deps['neoforge'].trim() });
      }
      if (deps['quilt-loader'] && typeof deps['quilt-loader'] === 'string') {
        detectedLoaders.push({ type: 'quilt', key: 'quilt-loader', version: deps['quilt-loader'].trim() });
      }

      if (detectedLoaders.length > 1) {
        const conflictNames = detectedLoaders.map((l) => `${l.key} (${l.version})`).join(', ');
        errors.push(`Konflikt loaderów: Wykryto sprzeczne deklaracje modloaderów w jednej paczce: ${conflictNames}.`);
      } else if (detectedLoaders.length === 1) {
        loaderType = detectedLoaders[0].type;
        loaderVersion = detectedLoaders[0].version;
      } else {
        loaderType = 'vanilla';
        warnings.push('Paczka nie deklaruje żadnego modloadera. Zostanie potraktowana jako Vanilla Minecraft.');
      }

      // Check for unknown dependency keys
      for (const [key, val] of Object.entries(deps)) {
        if (!this.KNOWN_DEPENDENCY_KEYS.has(key)) {
          warnings.push(`Nieznana zależność w manifeście: '${key}'='${val}'. Wymaga weryfikacji.`);
        }
      }
    }

    // 5. Check files array
    if (!Array.isArray(index.files)) {
      errors.push("Pole 'files' w manifeście musi być tablicą.");
    } else {
      const seenFilePaths = new Set<string>();

      for (let i = 0; i < index.files.length; i++) {
        const file = index.files[i] as Partial<MrpackIndexFile>;
        const prefix = `Plik #${i + 1}`;

        if (!file || typeof file !== 'object') {
          errors.push(`${prefix}: Wpis nie jest poprawnym obiektem.`);
          continue;
        }

        // Path check
        if (!file.path || typeof file.path !== 'string') {
          errors.push(`${prefix}: Brak wymaganej ścieżki ('path').`);
        } else {
          const pathVal = MrpackPathValidator.validateRelativePath(file.path);
          if (!pathVal.valid || !pathVal.normalized) {
            errors.push(`${prefix} [${file.path}]: Niepoprawna ścieżka (${pathVal.error}).`);
          } else {
            const lowerNorm = pathVal.normalized.toLowerCase();
            if (seenFilePaths.has(lowerNorm)) {
              errors.push(`${prefix} [${file.path}]: Zduplikowana ścieżka pliku w sekcji 'files'.`);
            }
            seenFilePaths.add(lowerNorm);
          }
        }

        // Hashes check (SHA-1 and SHA-512)
        if (!file.hashes || typeof file.hashes !== 'object') {
          errors.push(`${prefix}: Brak wymaganego obiektu 'hashes'.`);
        } else {
          const sha1 = file.hashes.sha1;
          const sha512 = file.hashes.sha512;

          if (!sha1 || typeof sha1 !== 'string' || !/^[a-fA-F0-9]{40}$/.test(sha1)) {
            errors.push(`${prefix} [${file.path || 'nieznany'}]: Nieprawidłowa lub brakująca suma SHA-1 (wymagane 40 znaków hex).`);
          }

          if (!sha512 || typeof sha512 !== 'string' || !/^[a-fA-F0-9]{128}$/.test(sha512)) {
            errors.push(`${prefix} [${file.path || 'nieznany'}]: Nieprawidłowa lub brakująca suma SHA-512 (wymagane 128 znaków hex).`);
          }
        }

        // File size check
        if (typeof file.fileSize !== 'number' || file.fileSize <= 0) {
          errors.push(`${prefix} [${file.path || 'nieznany'}]: Nieprawidłowy rozmiar pliku ('fileSize' musi być liczbą > 0).`);
        }

        // Downloads check
        if (!Array.isArray(file.downloads) || file.downloads.length === 0) {
          errors.push(`${prefix} [${file.path || 'nieznany'}]: Brak listy adresów URL do pobrania ('downloads').`);
        } else {
          for (const url of file.downloads) {
            if (typeof url !== 'string' || !url.startsWith('https://')) {
              errors.push(`${prefix} [${file.path || 'nieznany'}]: Adres URL pobierania musi używać protokołu HTTPS: '${url}'.`);
            }
          }
        }

        // Environment check if present
        if (file.env && typeof file.env === 'object') {
          const validEnvValues = new Set(['required', 'optional', 'unsupported']);
          if (file.env.client && !validEnvValues.has(file.env.client)) {
            errors.push(`${prefix} [${file.path || 'nieznany'}]: Nieznana wartość env.client: '${file.env.client}'.`);
          }
          if (file.env.server && !validEnvValues.has(file.env.server)) {
            errors.push(`${prefix} [${file.path || 'nieznany'}]: Nieznana wartość env.server: '${file.env.server}'.`);
          }
        }
      }
    }

    const isValid = errors.length === 0;

    return {
      valid: isValid,
      errors,
      warnings,
      loaderType,
      loaderVersion,
      minecraftVersion,
      manifest: isValid ? (index as MrpackIndex) : undefined,
    };
  }
}
