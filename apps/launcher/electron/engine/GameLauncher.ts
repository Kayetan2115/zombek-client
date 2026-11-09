import * as fs from 'node:fs';
import * as path from 'node:path';
import * as cp from 'node:child_process';
import { EnginePaths } from './paths.js';
import { DownloadManager } from './DownloadManager.js';
import { VersionManifestService } from './VersionManifestService.js';
import { InstallationRepository } from './InstallationRepository.js';
import { JavaRuntimeManager } from './JavaRuntimeManager.js';
import { GameInstaller } from './GameInstaller.js';
import { GameArgumentsBuilder } from './GameArgumentsBuilder.js';
import { CompositeSessionProvider } from './GameSessionProvider.js';
import { FabricLoaderAdapter } from '../loaders/FabricLoaderAdapter.js';
import { ForgeLoaderAdapter } from '../loaders/ForgeLoaderAdapter.js';
import { NeoForgeLoaderAdapter } from '../loaders/NeoForgeLoaderAdapter.js';
import { QuiltLoaderAdapter } from '../loaders/QuiltLoaderAdapter.js';
import { ClientReleaseService } from './ClientReleaseService.js';
import type { MojangVersionPackage } from './types.js';
import type { EngineState, EngineProgress, EngineLogMessage } from '@zombek/shared-types';

export interface GameLauncherCallbacks {
  onStatusChange?: (state: EngineState, message: string) => void;
  onProgress?: (progress: EngineProgress) => void;
  onLog?: (log: EngineLogMessage) => void;
}

export interface LaunchOptions {
  accountId?: string;
  offlineUsername?: string;
}

export class GameLauncher {
  private static instance: GameLauncher;

  private state: EngineState = 'idle';
  private currentProgress: EngineProgress | null = null;
  private currentProcess: cp.ChildProcess | null = null;
  private currentInstanceId: string | null = null;
  private downloadManager: DownloadManager | null = null;
  private sessionProvider: CompositeSessionProvider;
  private callbacks: GameLauncherCallbacks = {};
  private logStream: fs.WriteStream | null = null;

  public constructor(sessionProvider?: CompositeSessionProvider) {
    this.sessionProvider = sessionProvider || new CompositeSessionProvider();
  }

  public static getInstance(): GameLauncher {
    if (!GameLauncher.instance) {
      GameLauncher.instance = new GameLauncher();
    }
    return GameLauncher.instance;
  }

  public setCallbacks(callbacks: GameLauncherCallbacks): void {
    this.callbacks = callbacks;
  }

  public getStatus(): {
    state: EngineState;
    progress: EngineProgress | null;
    pid: number | null;
    currentInstanceId: string | null;
  } {
    return {
      state: this.state,
      progress: this.currentProgress,
      pid: this.currentProcess?.pid ?? null,
      currentInstanceId: this.currentInstanceId,
    };
  }

  private setState(state: EngineState, message: string): void {
    this.state = state;
    this.emitLog('LAUNCHER', 'INFO', message);
    this.callbacks.onStatusChange?.(state, message);
  }

  private redactMessage(message: string): string {
    // Redact tokens matching 32+ hex/alphanumeric characters following accessToken flags
    return message
      .replace(/(--accessToken\s+)[^\s]+/gi, '$1[REDACTED]')
      .replace(/(-accessToken\s+)[^\s]+/gi, '$1[REDACTED]');
  }

  private redactArguments(args: string[]): string[] {
    const redacted = [...args];
    for (let i = 0; i < redacted.length; i++) {
      if (redacted[i] === '--accessToken' || redacted[i] === '-accessToken') {
        if (i + 1 < redacted.length) {
          redacted[i + 1] = '[REDACTED]';
        }
      }
    }
    return redacted;
  }

  private emitLog(
    category: 'LAUNCHER' | 'DOWNLOADS' | 'JAVA' | 'MINECRAFT' | 'AUTH',
    level: 'INFO' | 'WARNING' | 'ERROR' | 'DEBUG',
    rawMessage: string
  ): void {
    const message = this.redactMessage(rawMessage);
    const timestamp = new Date().toTimeString().split(' ')[0];
    const id = `log-${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
    const logObj: EngineLogMessage = { id, timestamp, category, level, message };

    if (this.logStream && !this.logStream.destroyed) {
      this.logStream.write(`[${timestamp}] [${category}/${level}]: ${message}\n`);
    }

    this.callbacks.onLog?.(logObj);
  }

  public async cancel(): Promise<void> {
    this.emitLog('LAUNCHER', 'WARNING', 'Anulowanie operacji przez użytkownika...');

    if (this.downloadManager) {
      this.downloadManager.cancel();
      this.downloadManager = null;
    }

    if (this.currentProcess && !this.currentProcess.killed) {
      try {
        this.currentProcess.kill('SIGTERM');
      } catch {
        // ignore
      }
      this.currentProcess = null;
    }

    if (this.logStream) {
      this.logStream.end();
      this.logStream = null;
    }

    this.setState('cancelled', 'Operacja anulowana.');
    this.currentInstanceId = null;
  }

  public async launch(
    instanceId: string,
    options?: LaunchOptions | string
  ): Promise<void> {
    if (this.state === 'running' || this.state === 'launching' || this.state === 'downloading') {
      throw new Error('Inna instancja gry lub zadanie jest aktualnie w toku.');
    }

    EnginePaths.ensureAllBaseDirs();
    this.currentInstanceId = instanceId;

    try {
      // 1. Get instance config
      this.setState('preparing', `Przygotowywanie uruchomienia instancji: ${instanceId}`);
      const instance = await InstallationRepository.getById(instanceId);
      if (!instance) {
        throw new Error(`Instancja o ID '${instanceId}' nie została znaleziona.`);
      }

      const instanceDir = EnginePaths.getInstanceDir(instance.id);
      if (!fs.existsSync(instanceDir)) {
        fs.mkdirSync(instanceDir, { recursive: true });
      }

      // Ensure instance isolated directories (saves, config, mods, disabled-mods, resourcepacks, etc.)
      const subDirs = ['saves', 'resourcepacks', 'config', 'mods', 'disabled-mods', 'screenshots', 'natives', 'logs'];
      for (const sub of subDirs) {
        const targetDir = path.join(instanceDir, sub);
        if (!fs.existsSync(targetDir)) {
          fs.mkdirSync(targetDir, { recursive: true });
        }
      }

      // Prepare log file in instance folder
      const logsDir = path.join(instanceDir, 'logs');
      this.logStream = fs.createWriteStream(path.join(logsDir, 'latest.log'), { flags: 'w' });

      // 2. Fetch version package
      let versionPkg: MojangVersionPackage;
      if (instance.loader === 'fabric') {
        this.setState('preparing', `Pobieranie i weryfikacja profilu Fabric (${instance.minecraftVersion})...`);
        this.emitLog('LAUNCHER', 'INFO', `Inicjalizacja środowiska Fabric dla wersji ${instance.minecraftVersion}...`);
        const fabricAdapter = FabricLoaderAdapter.getInstance();
        versionPkg = await fabricAdapter.resolveProfile(instance.minecraftVersion, instance.loaderVersion);
        this.emitLog('LAUNCHER', 'INFO', `Załadowano profil Fabric: ${versionPkg.id} (główna klasa: ${versionPkg.mainClass})`);
      } else if (instance.loader === 'forge') {
        this.setState('preparing', `Weryfikacja i przygotowanie profilu Forge (${instance.minecraftVersion})...`);
        this.emitLog('LAUNCHER', 'INFO', `Inicjalizacja środowiska Forge dla wersji ${instance.minecraftVersion}...`);
        const forgeAdapter = ForgeLoaderAdapter.getInstance();
        versionPkg = await forgeAdapter.resolveProfile(instance.minecraftVersion, instance.loaderVersion);
        this.emitLog('LAUNCHER', 'INFO', `Załadowano profil Forge: ${versionPkg.id} (główna klasa: ${versionPkg.mainClass})`);
      } else if (instance.loader === 'neoforge') {
        this.setState('preparing', `Weryfikacja i przygotowanie profilu NeoForge (${instance.minecraftVersion})...`);
        this.emitLog('LAUNCHER', 'INFO', `Inicjalizacja środowiska NeoForge dla wersji ${instance.minecraftVersion}...`);
        const neoForgeAdapter = NeoForgeLoaderAdapter.getInstance();
        versionPkg = await neoForgeAdapter.resolveProfile(instance.minecraftVersion, instance.loaderVersion);
        this.emitLog('LAUNCHER', 'INFO', `Załadowano profil NeoForge: ${versionPkg.id} (główna klasa: ${versionPkg.mainClass})`);
      } else if (instance.loader === 'quilt') {
        this.setState('preparing', `Weryfikacja i przygotowanie profilu Quilt (${instance.minecraftVersion})...`);
        this.emitLog('LAUNCHER', 'INFO', `Inicjalizacja środowiska Quilt dla wersji ${instance.minecraftVersion}...`);
        const quiltAdapter = QuiltLoaderAdapter.getInstance();
        versionPkg = await quiltAdapter.resolveProfile(instance.minecraftVersion, instance.loaderVersion);
        this.emitLog('LAUNCHER', 'INFO', `Załadowano profil Quilt: ${versionPkg.id} (główna klasa: ${versionPkg.mainClass})`);
      } else {
        this.setState('preparing', `Pobieranie konfiguracji wersji Minecraft: ${instance.minecraftVersion}`);
        versionPkg = await VersionManifestService.getVersionPackage(instance.minecraftVersion);
      }

      // 3. Prepare Java Runtime
      this.setState('preparing-java', 'Weryfikacja środowiska Java...');
      let javaPath: string;

      if (instance.customJavaPath && fs.existsSync(instance.customJavaPath)) {
        this.emitLog('JAVA', 'INFO', `Użyto zdefiniowanej ścieżki Java: ${instance.customJavaPath}`);
        javaPath = instance.customJavaPath;
      } else {
        const requiredComponent = versionPkg.javaVersion?.component || 'java-runtime-gamma';
        this.emitLog('JAVA', 'INFO', `Wymagany komponent Java: ${requiredComponent}`);

        this.downloadManager = new DownloadManager(6);
        this.downloadManager.onProgress((p) => {
          this.currentProgress = p;
          this.callbacks.onProgress?.(p);
        });

        javaPath = await JavaRuntimeManager.prepareJavaRuntime(requiredComponent, this.downloadManager);
        this.emitLog('JAVA', 'INFO', `Środowisko Java gotowe: ${javaPath}`);
      }

      // 4. Download and install game files (JAR, libraries, assets, natives)
      this.setState('downloading', `Pobieranie plików gry Minecraft (${versionPkg.id})...`);
      this.downloadManager = new DownloadManager(8);
      this.downloadManager.onProgress((p) => {
        this.currentProgress = p;
        this.callbacks.onProgress?.(p);
      });

      await GameInstaller.installVersion(versionPkg, instance.id, this.downloadManager);
      this.downloadManager = null;

      // 4b. Automatically install/update ZombekClient Mod and dependencies for modded loaders
      if (instance.loader !== 'vanilla') {
        if (instance.loader === 'fabric' || instance.loader === 'quilt') {
          this.emitLog('LAUNCHER', 'INFO', `Weryfikacja biblioteki Fabric API dla loader'a ${instance.loader.toUpperCase()}...`);
          try {
            await FabricLoaderAdapter.getInstance().ensureFabricApiForInstance(instance.id, instance.minecraftVersion);
          } catch (err: any) {
            this.emitLog('LAUNCHER', 'WARNING', `Ostrzeżenie przy weryfikacji Fabric API: ${err.message}`);
          }
        }

        this.emitLog('LAUNCHER', 'INFO', `Weryfikacja modu ZombekClient dla loader'a ${instance.loader.toUpperCase()}...`);
        try {
          const deployRes = await ClientReleaseService.getInstance().installClientModForInstance(instance.id);
          if (deployRes.success) {
            this.emitLog('LAUNCHER', 'INFO', `Zainstalowano mod klienta ZombekClient: ${deployRes.release?.filename}`);
          } else {
            this.emitLog('LAUNCHER', 'WARNING', `Ostrzeżenie przy instalacji modu klienta: ${deployRes.error}`);
          }
        } catch (err: any) {
          this.emitLog('LAUNCHER', 'WARNING', `Nie udało się automatycznie wdrożyć modu klienta: ${err.message}`);
        }
      }

      // 5. Resolve player session
      this.setState('preparing', 'Przygotowywanie sesji gracza...');
      const targetAccountId =
        instance.assignedAccountId || (typeof options === 'object' ? options?.accountId : undefined);
      const offlineUsername = typeof options === 'string' ? options : options?.offlineUsername;

      const session = await this.sessionProvider.getSessionForLaunch({
        accountId: targetAccountId,
        offlineUsername,
      });

      if (session.userType === 'msa') {
        this.emitLog('AUTH', 'INFO', `Sesja Microsoft Premium gotowa: ${session.username} (${session.uuid})`);
      } else {
        this.emitLog('AUTH', 'INFO', `Sesja lokalna Offline gotowa: ${session.username} (${session.uuid})`);
      }

      // 6. Build arguments
      const nativesDir = EnginePaths.getNativesDir(instance.id);
      const allocatedRam = instance.allocatedRamMb || 4096;
      const minRam = Math.max(512, Math.round(allocatedRam / 2));

      const args = GameArgumentsBuilder.buildArguments({
        versionPackage: versionPkg,
        instanceDir,
        assetsDir: EnginePaths.getAssetsDir(),
        librariesDir: EnginePaths.getLibrariesDir(),
        versionsDir: EnginePaths.getVersionsDir(),
        nativesDir,
        session,
        minRamMb: minRam,
        maxRamMb: allocatedRam,
        customJvmArgs: instance.customJvmArgs ? instance.customJvmArgs.split(' ').filter(Boolean) : [],
        windowWidth: instance.resolutionWidth || 1280,
        windowHeight: instance.resolutionHeight || 720,
        fullscreen: instance.fullscreen || false,
      });

      const sanitizedArgs = this.redactArguments(args);
      this.emitLog(
        'LAUNCHER',
        'DEBUG',
        `Polecenie startowe: ${javaPath} ${sanitizedArgs.slice(0, 5).join(' ')} ... (${sanitizedArgs.length} argumentów)`
      );

      // 7. Spawn Minecraft process
      this.setState('launching', 'Uruchamianie procesu Minecraft...');
      const child = cp.spawn(javaPath, args, {
        cwd: instanceDir,
        stdio: ['ignore', 'pipe', 'pipe'],
      });

      this.currentProcess = child;

      child.on('spawn', () => {
        this.setState('running', `Minecraft uruchomiony pomyślnie (PID: ${child.pid}).`);
      });

      // Stream stdout
      let stdoutBuffer = '';
      child.stdout?.on('data', (data: Buffer) => {
        stdoutBuffer += data.toString('utf-8');
        const lines = stdoutBuffer.split(/\r?\n/);
        stdoutBuffer = lines.pop() || '';
        for (const line of lines) {
          if (line.trim()) {
            this.emitLog('MINECRAFT', 'INFO', line);
          }
        }
      });

      // Stream stderr
      let stderrBuffer = '';
      child.stderr?.on('data', (data: Buffer) => {
        stderrBuffer += data.toString('utf-8');
        const lines = stderrBuffer.split(/\r?\n/);
        stderrBuffer = lines.pop() || '';
        for (const line of lines) {
          if (line.trim()) {
            this.emitLog('MINECRAFT', 'WARNING', line);
          }
        }
      });

      child.on('error', (err) => {
        this.emitLog('LAUNCHER', 'ERROR', `Błąd procesu Minecraft: ${err.message}`);
        this.setState('failed', `Nie udało się uruchomić procesu: ${err.message}`);
        this.cleanupProcess();
      });

      child.on('close', (code) => {
        if (stdoutBuffer.trim()) this.emitLog('MINECRAFT', 'INFO', stdoutBuffer.trim());
        if (stderrBuffer.trim()) this.emitLog('MINECRAFT', 'WARNING', stderrBuffer.trim());

        const isSuccess = code === 0;
        this.emitLog('LAUNCHER', isSuccess ? 'INFO' : 'WARNING', `Proces Minecraft zakończył się kodem: ${code}`);
        this.setState(
          isSuccess ? 'completed' : 'failed',
          isSuccess ? 'Gra została zamknięta poprawnie.' : `Gra została zamknięta z kodem błędu (${code}).`
        );
        this.cleanupProcess();
      });

      // Update instance lastPlayed
      await InstallationRepository.update(instance.id, { lastPlayed: new Date().toISOString() });

    } catch (err: any) {
      this.emitLog('LAUNCHER', 'ERROR', `Błąd podczas uruchamiania gry: ${err.message}`);
      this.setState('failed', err.message);
      this.cleanupProcess();
      throw err;
    }
  }

  private cleanupProcess(): void {
    this.currentProcess = null;
    this.currentProgress = null;
    if (this.logStream) {
      this.logStream.end();
      this.logStream = null;
    }
  }
}
