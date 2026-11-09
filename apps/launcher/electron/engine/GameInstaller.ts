import * as fs from 'node:fs';
import * as path from 'node:path';
import { EnginePaths } from './paths.js';
import { DownloadManager } from './DownloadManager.js';
import { ZipExtractor } from './zip.js';
import type {
  MojangVersionPackage,
  MojangRule,
  DownloadTask,
  MojangAssetObjectsManifest,
  MojangLibrary
} from './types.js';

export class GameInstaller {
  public static isRuleAllowed(rules?: MojangRule[]): boolean {
    if (!rules || rules.length === 0) return true;

    let allowed = false;
    const currentOs = process.platform === 'win32' ? 'windows' : process.platform === 'darwin' ? 'osx' : 'linux';
    const currentArch = process.arch === 'ia32' ? 'x86' : process.arch;

    for (const rule of rules) {
      let matches = true;

      if (rule.os) {
        if (rule.os.name && rule.os.name !== currentOs) {
          matches = false;
        }
        if (rule.os.arch && rule.os.arch !== currentArch) {
          matches = false;
        }
      }

      if (rule.features) {
        // Feature flags (e.g. is_demo_user, has_custom_resolution)
        // Defaults to not matching in engine core unless specifically enabled
        matches = false;
      }

      if (matches) {
        allowed = rule.action === 'allow';
      }
    }

    return allowed;
  }

  public static getMavenPath(libName: string, classifier?: string, ext: string = 'jar'): string {
    const parts = libName.split(':');
    if (parts.length < 3) return libName;

    const group = parts[0].replace(/\./g, '/');
    const artifact = parts[1];
    const version = parts[2];
    const itemClassifier = classifier || (parts.length > 3 ? parts[3] : undefined);

    if (itemClassifier) {
      return `${group}/${artifact}/${version}/${artifact}-${version}-${itemClassifier}.${ext}`;
    }
    return `${group}/${artifact}/${version}/${artifact}-${version}.${ext}`;
  }

  public static async installVersion(
    versionPkg: MojangVersionPackage,
    instanceId: string,
    downloadManager: DownloadManager
  ): Promise<{ clientJarPath: string; classpathLibraries: string[] }> {
    EnginePaths.ensureAllBaseDirs();
    const downloadTasks: DownloadTask[] = [];

    // 1. Client JAR (use inheritsFrom for modded versions inheriting from vanilla)
    const jarVersionId = versionPkg.inheritsFrom || versionPkg.id;
    const versionDir = path.join(EnginePaths.getVersionsDir(), jarVersionId);
    const clientJarPath = path.join(versionDir, `${jarVersionId}.jar`);

    if (versionPkg.downloads?.client) {
      downloadTasks.push({
        id: `client-jar-${jarVersionId}`,
        url: versionPkg.downloads.client.url,
        destination: clientJarPath,
        sha1: versionPkg.downloads.client.sha1,
        size: versionPkg.downloads.client.size,
        description: `Minecraft Client JAR (${jarVersionId})`
      });
    }

    // 2. Libraries & Natives
    const classpathLibraries: string[] = [];
    const nativesToExtract: Array<{ jarPath: string; exclude?: string[] }> = [];

    for (const lib of versionPkg.libraries || []) {
      if (!this.isRuleAllowed(lib.rules)) {
        continue;
      }

      // Standard library artifact
      if (lib.downloads?.artifact) {
        const relPath = lib.downloads.artifact.path || this.getMavenPath(lib.name);
        const dest = path.join(EnginePaths.getLibrariesDir(), relPath);

        classpathLibraries.push(dest);
        downloadTasks.push({
          id: `lib-${lib.name}`,
          url: lib.downloads.artifact.url,
          destination: dest,
          sha1: lib.downloads.artifact.sha1,
          size: lib.downloads.artifact.size,
          description: `Biblioteka: ${path.basename(relPath)}`
        });
      } else if (!lib.natives && lib.url) {
        // Maven repository direct fallback (Fabric loader, intermediaries, etc.)
        const relPath = this.getMavenPath(lib.name);
        const dest = path.join(EnginePaths.getLibrariesDir(), relPath);
        classpathLibraries.push(dest);
        const baseUrl = lib.url.endsWith('/') ? lib.url : `${lib.url}/`;
        downloadTasks.push({
          id: `lib-${lib.name}`,
          url: `${baseUrl}${relPath}`,
          destination: dest,
          sha1: lib.sha1,
          size: lib.size,
          description: `Biblioteka: ${path.basename(relPath)}`
        });
      }

      // Native classifier for current OS
      if (lib.natives) {
        const osKey = process.platform === 'win32' ? 'windows' : process.platform === 'darwin' ? 'osx' : 'linux';
        let rawClassifier = lib.natives[osKey];

        if (rawClassifier) {
          rawClassifier = rawClassifier.replace('${arch}', process.arch === 'x64' ? '64' : '32');
          const nativeArtifact = lib.downloads?.classifiers?.[rawClassifier];

          if (nativeArtifact) {
            const relPath = nativeArtifact.path || this.getMavenPath(lib.name, rawClassifier);
            const dest = path.join(EnginePaths.getLibrariesDir(), relPath);

            nativesToExtract.push({ jarPath: dest, exclude: lib.extract?.exclude });
            downloadTasks.push({
              id: `native-${lib.name}-${rawClassifier}`,
              url: nativeArtifact.url,
              destination: dest,
              sha1: nativeArtifact.sha1,
              size: nativeArtifact.size,
              description: `Natives (${rawClassifier}): ${path.basename(relPath)}`
            });
          }
        }
      }
    }

    // 3. Asset Index and Objects
    if (versionPkg.assetIndex) {
      const indexDest = path.join(EnginePaths.getAssetIndexesDir(), `${versionPkg.assetIndex.id}.json`);
      downloadTasks.push({
        id: `asset-index-${versionPkg.assetIndex.id}`,
        url: versionPkg.assetIndex.url,
        destination: indexDest,
        sha1: versionPkg.assetIndex.sha1,
        size: versionPkg.assetIndex.size,
        description: `Indeks Assetów (${versionPkg.assetIndex.id})`
      });
    }

    // 4. Logging Config
    if (versionPkg.logging?.client?.file) {
      const logFile = versionPkg.logging.client.file;
      const logDest = path.join(EnginePaths.getAssetsDir(), 'log_configs', `${versionPkg.id}.xml`);
      downloadTasks.push({
        id: `logging-config-${versionPkg.id}`,
        url: logFile.url,
        destination: logDest,
        sha1: logFile.sha1,
        size: logFile.size,
        description: `Konfiguracja logowania (${versionPkg.id})`
      });
    }

    // Download Phase 1: Client JAR, Libraries, Natives, Asset Index
    await downloadManager.downloadBatch(downloadTasks, `Pobieranie bibliotek i plików gry (${versionPkg.id})...`);

    // Download Phase 2: Read downloaded Asset Index and enqueue asset objects
    if (versionPkg.assetIndex) {
      const indexDest = path.join(EnginePaths.getAssetIndexesDir(), `${versionPkg.assetIndex.id}.json`);
      if (fs.existsSync(indexDest)) {
        try {
          const raw = await fs.promises.readFile(indexDest, 'utf-8');
          const indexJson = JSON.parse(raw) as MojangAssetObjectsManifest;
          const assetTasks: DownloadTask[] = [];

          for (const [name, obj] of Object.entries(indexJson.objects || {})) {
            const sub = obj.hash.substring(0, 2);
            const objDest = path.join(EnginePaths.getAssetObjectsDir(), sub, obj.hash);

            assetTasks.push({
              id: `asset-${obj.hash}`,
              url: `https://resources.download.minecraft.net/${sub}/${obj.hash}`,
              destination: objDest,
              sha1: obj.hash,
              size: obj.size,
              description: `Zasób: ${name}`
            });
          }

          if (assetTasks.length > 0) {
            await downloadManager.downloadBatch(assetTasks, `Pobieranie zasobów gry (Asset Objects)...`);
          }
        } catch (err: any) {
          // If asset parsing fails, log and continue
        }
      }
    }

    // 5. Extract Natives into instance natives directory
    const nativesDir = EnginePaths.getNativesDir(instanceId);
    if (!fs.existsSync(nativesDir)) {
      await fs.promises.mkdir(nativesDir, { recursive: true });
    }

    for (const native of nativesToExtract) {
      if (fs.existsSync(native.jarPath)) {
        await ZipExtractor.extractJarNatives(native.jarPath, nativesDir, native.exclude || ['META-INF']);
      }
    }

    return {
      clientJarPath,
      classpathLibraries
    };
  }
}
