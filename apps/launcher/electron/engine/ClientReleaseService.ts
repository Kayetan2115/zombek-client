import * as fs from 'node:fs';
import * as path from 'node:path';
import { fileURLToPath } from 'node:url';
import type { MinecraftLoaderType, InstanceRecord } from '@zombek/shared-types';
import { InstallationRepository } from './InstallationRepository.js';
import { EnginePaths } from './paths.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export interface ClientReleaseItem {
  modId: string;
  clientVersion: string;
  minecraftVersion: string;
  loader: MinecraftLoaderType;
  loaderVersionRange: string;
  filename: string;
  relativePath: string;
  size: number;
  sha256: string;
  entrypoint: string;
  metadataFormat: string;
  environment: string;
}

export interface ClientReleasesManifest {
  $schema?: string;
  name: string;
  description: string;
  version: string;
  releases: ClientReleaseItem[];
}

export class ClientReleaseService {
  private static instance: ClientReleaseService;
  private manifestCache: ClientReleasesManifest | null = null;

  public static getInstance(): ClientReleaseService {
    if (!ClientReleaseService.instance) {
      ClientReleaseService.instance = new ClientReleaseService();
    }
    return ClientReleaseService.instance;
  }

  /**
   * Resolves the workspace root path of apps/minecraft-client
   */
  public getClientRootDir(): string {
    // Check multiple candidate locations (dev workspace, production bundled)
    const candidates = [
      path.resolve(process.cwd(), 'apps', 'minecraft-client'),
      path.resolve(EnginePaths.getBaseDir(), '..', '..', 'apps', 'minecraft-client'),
      path.resolve(__dirname, '..', '..', '..', 'minecraft-client'),
      path.resolve(__dirname, '..', '..', '..', '..', 'apps', 'minecraft-client'),
    ];

    for (const cand of candidates) {
      if (fs.existsSync(cand)) {
        return cand;
      }
    }

    return path.resolve(process.cwd(), 'apps', 'minecraft-client');
  }

  /**
   * Loads the client releases manifest from apps/minecraft-client/client-releases.json
   */
  public getManifest(): ClientReleasesManifest {
    if (this.manifestCache) {
      return this.manifestCache;
    }

    const manifestPath = path.join(this.getClientRootDir(), 'client-releases.json');
    if (fs.existsSync(manifestPath)) {
      try {
        const raw = fs.readFileSync(manifestPath, 'utf-8');
        this.manifestCache = JSON.parse(raw) as ClientReleasesManifest;
        return this.manifestCache;
      } catch (err) {
        // Fallback below
      }
    }

    // Default embedded catalog
    this.manifestCache = {
      name: 'ZombekClient In-Game Client',
      description: 'Oficjalne wydania modu klienta ZombekClient dla wspieranych loaderów',
      version: '0.1.0-alpha',
      releases: [
        {
          modId: 'zombekclient',
          clientVersion: '0.1.0-alpha',
          minecraftVersion: '1.20.4',
          loader: 'fabric',
          loaderVersionRange: '>=0.15.0',
          filename: 'zombekclient-fabric-0.1.0-alpha.jar',
          relativePath: 'fabric/build/libs/zombekclient-fabric-0.1.0-alpha.jar',
          size: 106420,
          sha256: 'a73c6dd249cc49983148ab7a583f717cf9436f0b5089c9a95687e6423dbb8005',
          entrypoint: 'pl.zombek.client.core.ZombekClientMod',
          metadataFormat: 'fabric.mod.json',
          environment: 'client',
        },
        {
          modId: 'zombekclient',
          clientVersion: '0.1.0-alpha',
          minecraftVersion: '1.20.4',
          loader: 'forge',
          loaderVersionRange: '[49.0,)',
          filename: 'zombekclient-forge-0.1.0-alpha.jar',
          relativePath: 'forge/build/libs/zombekclient-forge-0.1.0-alpha.jar',
          size: 584943,
          sha256: 'cfa164ab08699bfca3d4c2d8ddbd2a6ddf625799cac1783401807f5f93c86e8f',
          entrypoint: 'pl.zombek.client.forge.entrypoint.ZombekForgeMod',
          metadataFormat: 'META-INF/mods.toml',
          environment: 'client',
        },
        {
          modId: 'zombekclient',
          clientVersion: '0.1.0-alpha',
          minecraftVersion: '1.20.4',
          loader: 'neoforge',
          loaderVersionRange: '[20.4,)',
          filename: 'zombekclient-neoforge-0.1.0-alpha.jar',
          relativePath: 'neoforge/build/libs/zombekclient-neoforge-0.1.0-alpha.jar',
          size: 583599,
          sha256: 'c6967ae3646ff13ff41fb82b95786877afd632cd46186d65e1b3486d76c49337',
          entrypoint: 'pl.zombek.client.neoforge.entrypoint.ZombekNeoForgeMod',
          metadataFormat: 'META-INF/neoforge.mods.toml',
          environment: 'client',
        },
        {
          modId: 'zombekclient',
          clientVersion: '0.1.0-alpha',
          minecraftVersion: '1.20.4',
          loader: 'quilt',
          loaderVersionRange: '>=0.25.0',
          filename: 'zombekclient-quilt-0.1.0-alpha.jar',
          relativePath: 'quilt/build/libs/zombekclient-quilt-0.1.0-alpha.jar',
          size: 597440,
          sha256: 'ddb060846eba8785d21ceb481e61ca9266765e105d9b12b85744cb6052c9f4e5',
          entrypoint: 'pl.zombek.client.quilt.entrypoint.QuiltClientBootstrap',
          metadataFormat: 'quilt.mod.json',
          environment: 'client',
        },
      ],
    };

    return this.manifestCache;
  }

  public getAvailableReleases(): ClientReleaseItem[] {
    return this.getManifest().releases;
  }

  /**
   * Finds the appropriate client release for the specified loader and Minecraft version
   */
  public findMatchingRelease(
    loader: MinecraftLoaderType,
    minecraftVersion: string
  ): ClientReleaseItem | null {
    const releases = this.getAvailableReleases();
    const normalizedLoader = loader.toLowerCase();

    return (
      releases.find(
        (r) =>
          r.loader.toLowerCase() === normalizedLoader &&
          r.minecraftVersion === minecraftVersion
      ) || null
    );
  }

  /**
   * Resolves the absolute path to the compiled jar file
   */
  public resolveArtifactPath(release: ClientReleaseItem): string {
    return path.join(this.getClientRootDir(), release.relativePath);
  }

  /**
   * Installs the matching ZombekClient JAR for the target instance
   */
  public async installClientModForInstance(
    instanceId: string
  ): Promise<{ success: boolean; release?: ClientReleaseItem; error?: string }> {
    const instance = await InstallationRepository.getById(instanceId);

    if (!instance) {
      return { success: false, error: `Instancja ${instanceId} nie istnieje.` };
    }

    if (instance.loader === 'vanilla') {
      return {
        success: false,
        error: 'Instalacja Vanilla nie obsługuje modułów ani modów Minecraft. Wybierz Fabric lub Forge.',
      };
    }

    const release = this.findMatchingRelease(instance.loader, instance.minecraftVersion);
    if (!release) {
      return {
        success: false,
        error: `Brak kompatybilnego wydania ZombekClient dla loadera ${instance.loader} i Minecraft ${instance.minecraftVersion}.`,
      };
    }

    const artifactPath = this.resolveArtifactPath(release);
    if (!fs.existsSync(artifactPath)) {
      return {
        success: false,
        error: `Plik artefaktu klienta nie istnieje: ${artifactPath}. Zbuduj projekt Gradle najpierw.`,
      };
    }

    const modsDir = path.join(instance.instanceDir, 'mods');
    if (!fs.existsSync(modsDir)) {
      fs.mkdirSync(modsDir, { recursive: true });
    }

    // Clean up any existing zombekclient jars in instance mods/
    const existingFiles = fs.readdirSync(modsDir);
    for (const f of existingFiles) {
      if (f.toLowerCase().startsWith('zombekclient-') && f.toLowerCase().endsWith('.jar')) {
        try {
          fs.unlinkSync(path.join(modsDir, f));
        } catch {
          // ignore
        }
      }
    }

    // Copy the correct JAR
    const targetJarPath = path.join(modsDir, release.filename);
    fs.copyFileSync(artifactPath, targetJarPath);

    return { success: true, release };
  }

  /**
   * Checks whether ZombekClient is installed in the given instance
   */
  public async getClientModStatus(instanceId: string): Promise<{
    installed: boolean;
    release?: ClientReleaseItem;
    filename?: string;
  }> {
    const instance = await InstallationRepository.getById(instanceId);
    if (!instance) {
      return { installed: false };
    }

    const modsDir = path.join(instance.instanceDir, 'mods');
    if (!fs.existsSync(modsDir)) {
      return { installed: false };
    }

    const files = fs.readdirSync(modsDir);
    const zombekJar = files.find(
      (f) => f.toLowerCase().startsWith('zombekclient-') && f.toLowerCase().endsWith('.jar')
    );

    if (!zombekJar) {
      return { installed: false };
    }

    const matchingRelease = this.getAvailableReleases().find(
      (r) => r.filename.toLowerCase() === zombekJar.toLowerCase()
    );

    return {
      installed: true,
      release: matchingRelease,
      filename: zombekJar,
    };
  }

  public clearCache(): void {
    this.manifestCache = null;
  }
}
