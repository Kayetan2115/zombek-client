import { ipcMain, BrowserWindow, dialog, shell } from 'electron';
import * as os from 'node:os';
import * as fs from 'node:fs';
import { IPC_CHANNELS } from '@zombek/contracts';
import { DEFAULT_LAUNCHER_CONFIG, validateLauncherConfig, type LauncherConfig } from '@zombek/config-schema';
import type {
  LauncherAppInfo,
  SystemDiagnostics,
  InstanceRecord,
  MrpackImportOptions,
  MrpackExportOptions
} from '@zombek/shared-types';
import { VersionManifestService } from '../engine/VersionManifestService.js';
import { InstallationRepository } from '../engine/InstallationRepository.js';
import { JavaRuntimeManager } from '../engine/JavaRuntimeManager.js';
import { GameLauncher } from '../engine/GameLauncher.js';
import { AccountRepository } from '../auth/AccountRepository.js';
import { MicrosoftAuthService } from '../auth/MicrosoftAuthService.js';
import { FabricMetaService } from '../loaders/FabricMetaService.js';
import { FabricLoaderAdapter } from '../loaders/FabricLoaderAdapter.js';
import { ForgeMetaService } from '../loaders/ForgeMetaService.js';
import { ForgeLoaderAdapter } from '../loaders/ForgeLoaderAdapter.js';
import { NeoForgeMetaService } from '../loaders/NeoForgeMetaService.js';
import { NeoForgeLoaderAdapter } from '../loaders/NeoForgeLoaderAdapter.js';
import { QuiltMetaService } from '../loaders/QuiltMetaService.js';
import { QuiltLoaderAdapter } from '../loaders/QuiltLoaderAdapter.js';
import { DownloadManager } from '../engine/DownloadManager.js';
import { ModRepository } from '../modpacks/ModRepository.js';
import { ModInstallationService } from '../modpacks/ModInstallationService.js';
import { ModpackRepository } from '../modpacks/ModpackRepository.js';
import { ModpackProfileManager } from '../modpacks/ModpackProfileManager.js';
import { ModrinthApiClient } from '../modrinth/ModrinthApiClient.js';
import { ModrinthInstallationManager } from '../modrinth/ModrinthInstallationManager.js';
import { ModUpdateManager } from '../modpacks/ModUpdateManager.js';
import { ModDependencyResolver } from '../modpacks/ModDependencyResolver.js';
import { ModpackLockfileManager } from '../modpacks/ModpackLockfileManager.js';
import { ModpackBackupManager } from '../modpacks/ModpackBackupManager.js';
import { ModpackTransactionManager } from '../modpacks/ModpackTransactionManager.js';
import { MrpackFormatManager } from '../modpacks/mrpack/MrpackFormatManager.js';
import { EnginePaths } from '../engine/paths.js';
import { HardwareDetectionService } from '../performance/HardwareDetectionService.js';
import { ZombekPerformanceService } from '../performance/ZombekPerformanceService.js';
import { MinecraftOptionsManager } from '../performance/MinecraftOptionsManager.js';
import { PERFORMANCE_PRESETS } from '../performance/PerformancePresetManifests.js';

let currentConfig: LauncherConfig = { ...DEFAULT_LAUNCHER_CONFIG };

export function registerIpcHandlers(): void {
  const launcher = GameLauncher.getInstance();

  // Wire up event broadcasting for engine streams
  launcher.setCallbacks({
    onStatusChange: (state, message) => {
      for (const win of BrowserWindow.getAllWindows()) {
        if (!win.isDestroyed()) {
          win.webContents.send(IPC_CHANNELS.EVENT_ENGINE_STATUS, { state, message });
        }
      }
    },
    onProgress: (progress) => {
      for (const win of BrowserWindow.getAllWindows()) {
        if (!win.isDestroyed()) {
          win.webContents.send(IPC_CHANNELS.EVENT_ENGINE_PROGRESS, progress);
        }
      }
    },
    onLog: (log) => {
      for (const win of BrowserWindow.getAllWindows()) {
        if (!win.isDestroyed()) {
          win.webContents.send(IPC_CHANNELS.EVENT_ENGINE_LOG, log);
        }
      }
    },
  });

  // Wire up event broadcasting for auth flow progress
  MicrosoftAuthService.setProgressCallback((progress) => {
    for (const win of BrowserWindow.getAllWindows()) {
      if (!win.isDestroyed()) {
        win.webContents.send(IPC_CHANNELS.EVENT_AUTH_PROGRESS, progress);
      }
    }
  });

  // Application ping
  ipcMain.handle(IPC_CHANNELS.APP_PING, async () => {
    return 'pong';
  });

  // App information
  ipcMain.handle(IPC_CHANNELS.APP_GET_INFO, async (): Promise<LauncherAppInfo> => {
    return {
      name: 'ZombekClient Launcher',
      version: '0.1.0-alpha',
      environment: process.env.NODE_ENV === 'production' ? 'production' : 'development',
    };
  });

  // System Diagnostics
  ipcMain.handle(IPC_CHANNELS.SYSTEM_GET_DIAGNOSTICS, async (): Promise<SystemDiagnostics> => {
    const platform = process.platform === 'win32' ? 'windows' : process.platform === 'darwin' ? 'darwin' : 'linux';
    return {
      platform,
      arch: process.arch,
      nodeVersion: process.versions.node,
      electronVersion: process.versions.electron,
      totalMemoryMb: Math.round(os.totalmem() / 1024 / 1024),
      freeMemoryMb: Math.round(os.freemem() / 1024 / 1024),
    };
  });

  // Configuration
  ipcMain.handle(IPC_CHANNELS.CONFIG_GET, async (): Promise<LauncherConfig> => {
    return currentConfig;
  });

  ipcMain.handle(IPC_CHANNELS.CONFIG_SET, async (_event, newConfig: unknown) => {
    const validation = validateLauncherConfig(newConfig);
    if (!validation.success) {
      return { success: false, error: validation.errors.join('; ') };
    }
    currentConfig = validation.data;
    return { success: true };
  });

  // Window Controls
  ipcMain.handle(IPC_CHANNELS.WINDOW_MINIMIZE, async (event) => {
    const win = BrowserWindow.fromWebContents(event.sender);
    win?.minimize();
  });

  ipcMain.handle(IPC_CHANNELS.WINDOW_MAXIMIZE, async (event) => {
    const win = BrowserWindow.fromWebContents(event.sender);
    if (win) {
      if (win.isMaximized()) {
        win.unmaximize();
      } else {
        win.maximize();
      }
    }
  });

  ipcMain.handle(IPC_CHANNELS.WINDOW_CLOSE, async (event) => {
    const win = BrowserWindow.fromWebContents(event.sender);
    win?.close();
  });

  // Mojang Versions Manifest
  ipcMain.handle(
    IPC_CHANNELS.VERSIONS_GET_MANIFEST,
    async (_event, args?: { forceRefresh?: boolean }) => {
      return VersionManifestService.getManifest(args?.forceRefresh);
    }
  );

  // Instances Management
  ipcMain.handle(IPC_CHANNELS.INSTANCES_GET_ALL, async () => {
    return InstallationRepository.getAll();
  });

  ipcMain.handle(
    IPC_CHANNELS.INSTANCES_CREATE,
    async (_event, data: Omit<InstanceRecord, 'id' | 'created' | 'instanceDir'>) => {
      return InstallationRepository.create(data);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.INSTANCES_UPDATE,
    async (_event, args: { id: string; updates: Partial<InstanceRecord> }) => {
      return InstallationRepository.update(args.id, args.updates);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.INSTANCES_DELETE,
    async (_event, args: { id: string }) => {
      const success = await InstallationRepository.delete(args.id);
      return { success };
    }
  );

  // Fabric Loader APIs
  ipcMain.handle(
    IPC_CHANNELS.FABRIC_GET_GAME_VERSIONS,
    async (_event, args?: { forceRefresh?: boolean }) => {
      return FabricMetaService.getGameVersions(args?.forceRefresh);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.FABRIC_GET_LOADER_VERSIONS,
    async (_event, args: { gameVersion: string; forceRefresh?: boolean }) => {
      return FabricMetaService.getLoaderVersions(args.gameVersion, args?.forceRefresh);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.FABRIC_INSTALL,
    async (_event, args: { instanceId: string; gameVersion: string; loaderVersion: string }) => {
      try {
        const instance = await InstallationRepository.getById(args.instanceId);
        if (!instance) {
          throw new Error(`Instancja o ID '${args.instanceId}' nie istnieje.`);
        }
        const dm = new DownloadManager(8);
        const res = await FabricLoaderAdapter.getInstance().install(instance, args.loaderVersion, dm);
        return { success: true, versionId: res.versionId };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  // Forge Loader APIs
  ipcMain.handle(
    IPC_CHANNELS.FORGE_GET_VERSIONS,
    async (_event, args: { gameVersion: string; forceRefresh?: boolean }) => {
      return ForgeMetaService.getForgeVersionsForGame(args.gameVersion, args?.forceRefresh);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.FORGE_INSTALL,
    async (_event, args: { instanceId: string; gameVersion: string; forgeVersion: string }) => {
      try {
        const instance = await InstallationRepository.getById(args.instanceId);
        if (!instance) {
          throw new Error(`Instancja o ID '${args.instanceId}' nie istnieje.`);
        }
        const dm = new DownloadManager(8);
        const res = await ForgeLoaderAdapter.getInstance().install(instance, args.forgeVersion, dm);
        return { success: true, versionId: res.versionId };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  // NeoForge Loader APIs
  ipcMain.handle(
    IPC_CHANNELS.NEOFORGE_GET_VERSIONS,
    async (_event, args: { gameVersion: string; forceRefresh?: boolean }) => {
      return NeoForgeMetaService.getNeoForgeVersionsForGame(args.gameVersion, args?.forceRefresh);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.NEOFORGE_INSTALL,
    async (_event, args: { instanceId: string; gameVersion: string; neoForgeVersion: string }) => {
      try {
        const instance = await InstallationRepository.getById(args.instanceId);
        if (!instance) {
          throw new Error(`Instancja o ID '${args.instanceId}' nie istnieje.`);
        }
        const dm = new DownloadManager(8);
        const res = await NeoForgeLoaderAdapter.getInstance().install(instance, args.neoForgeVersion, dm);
        return { success: true, versionId: res.versionId };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  // Quilt Loader APIs
  ipcMain.handle(
    IPC_CHANNELS.QUILT_GET_GAME_VERSIONS,
    async (_event, args?: { forceRefresh?: boolean }) => {
      return QuiltMetaService.getGameVersions(args?.forceRefresh);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.QUILT_GET_LOADER_VERSIONS,
    async (_event, args: { gameVersion: string; forceRefresh?: boolean }) => {
      return QuiltMetaService.getLoaderVersions(args.gameVersion, args?.forceRefresh);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.QUILT_INSTALL,
    async (_event, args: { instanceId: string; gameVersion: string; loaderVersion: string }) => {
      try {
        const instance = await InstallationRepository.getById(args.instanceId);
        if (!instance) {
          throw new Error(`Instancja o ID '${args.instanceId}' nie istnieje.`);
        }
        const dm = new DownloadManager(8);
        const res = await QuiltLoaderAdapter.getInstance().install(instance, args.loaderVersion, dm);
        return { success: true, versionId: res.versionId };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  // Mods Management APIs (Etap 6A)
  ipcMain.handle(
    IPC_CHANNELS.MODS_GET_FOR_INSTANCE,
    async (_event, args: { instanceId: string }) => {
      return ModRepository.getModsForInstance(args.instanceId);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_VALIDATE_FILE,
    async (_event, args: { filePath: string; instanceId: string }) => {
      return ModInstallationService.validateCandidate(args.filePath, args.instanceId);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_INSTALL_FILE,
    async (_event, args: { filePath: string; instanceId: string; overwrite?: boolean }) => {
      return ModInstallationService.installMod(args.filePath, args.instanceId, args.overwrite);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_TOGGLE,
    async (_event, args: { instanceId: string; fileName: string; enabled: boolean }) => {
      try {
        const mod = await ModRepository.toggleMod(args.instanceId, args.fileName, args.enabled);
        return { success: true, mod };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_DELETE,
    async (_event, args: { instanceId: string; fileName: string }) => {
      try {
        const success = await ModRepository.deleteMod(args.instanceId, args.fileName);
        return { success };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  ipcMain.handle(IPC_CHANNELS.MODS_OPEN_FILE_DIALOG, async () => {
    const focusedWindow = BrowserWindow.getFocusedWindow();
    const result = await dialog.showOpenDialog(focusedWindow || undefined as any, {
      title: 'Wybierz mody Minecraft (.jar)',
      buttonLabel: 'Wybierz mody',
      filters: [{ name: 'Modyfikacje Minecraft (*.jar)', extensions: ['jar'] }],
      properties: ['openFile', 'multiSelections'],
    });

    if (result.canceled || !result.filePaths || result.filePaths.length === 0) {
      return null;
    }
    return result.filePaths;
  });

  ipcMain.handle(
    IPC_CHANNELS.MODS_OPEN_FOLDER,
    async (_event, args: { instanceId: string }) => {
      try {
        const folder = EnginePaths.getInstanceModsDir(args.instanceId);
        if (!fs.existsSync(folder)) {
          await fs.promises.mkdir(folder, { recursive: true });
        }
        await shell.openPath(folder);
        return { success: true };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  // Modpacks Management APIs (Etap 6A)
  ipcMain.handle(IPC_CHANNELS.MODPACKS_GET_ALL, async () => {
    return ModpackRepository.getAll();
  });

  ipcMain.handle(
    IPC_CHANNELS.MODPACKS_CREATE,
    async (_event, data: any) => {
      return ModpackRepository.create(data);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODPACKS_UPDATE,
    async (_event, args: { id: string; updates: any }) => {
      return ModpackRepository.update(args.id, args.updates);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODPACKS_DUPLICATE,
    async (_event, args: { id: string; newName?: string }) => {
      return ModpackProfileManager.duplicateModpack(args.id, args.newName);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODPACKS_DELETE,
    async (_event, args: { id: string }) => {
      try {
        const success = await ModpackRepository.delete(args.id);
        return { success };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODPACKS_APPLY_TO_INSTANCE,
    async (_event, args: { modpackId: string; instanceId: string }) => {
      try {
        const success = await ModpackProfileManager.applyModpackToInstance(args.modpackId, args.instanceId);
        return { success };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  // Modrinth API Handlers (Etap 6B)
  ipcMain.handle(IPC_CHANNELS.MODRINTH_SEARCH, async (_event, params: any) => {
    return ModrinthApiClient.searchProjects(params);
  });

  ipcMain.handle(IPC_CHANNELS.MODRINTH_GET_PROJECT, async (_event, args: { projectIdOrSlug: string }) => {
    return ModrinthApiClient.getProject(args.projectIdOrSlug);
  });

  ipcMain.handle(
    IPC_CHANNELS.MODRINTH_GET_PROJECT_VERSIONS,
    async (
      _event,
      args: { projectIdOrSlug: string; loaders?: string[]; gameVersions?: string[]; includeChangelog?: boolean }
    ) => {
      return ModrinthApiClient.getProjectVersions(args.projectIdOrSlug, {
        loaders: args.loaders,
        gameVersions: args.gameVersions,
        includeChangelog: args.includeChangelog,
      });
    }
  );

  ipcMain.handle(IPC_CHANNELS.MODRINTH_GET_VERSION, async (_event, args: { versionId: string }) => {
    return ModrinthApiClient.getVersion(args.versionId);
  });

  ipcMain.handle(IPC_CHANNELS.MODRINTH_GET_CATEGORIES, async () => {
    return ModrinthApiClient.getCategories();
  });

  ipcMain.handle(IPC_CHANNELS.MODRINTH_GET_LOADERS, async () => {
    return ModrinthApiClient.getLoaders();
  });

  ipcMain.handle(IPC_CHANNELS.MODRINTH_GET_GAME_VERSIONS, async () => {
    return ModrinthApiClient.getGameVersions();
  });

  ipcMain.handle(IPC_CHANNELS.MODRINTH_INSTALL_VERSION, async (_event, options: any) => {
    return ModrinthInstallationManager.installVersion(options);
  });

  // Modpack Auto-Updates, Dependency Resolver & Transactions (Etap 6C)
  ipcMain.handle(
    IPC_CHANNELS.MODS_CHECK_UPDATES,
    async (_event, args: { instanceId: string; forceRefresh?: boolean }) => {
      return ModUpdateManager.checkUpdates(args.instanceId, args.forceRefresh);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_RESOLVE_DEPENDENCIES,
    async (_event, args: { instanceId: string; versionId: string }) => {
      const instance = await InstallationRepository.getById(args.instanceId);
      if (!instance) throw new Error(`Instalacja '${args.instanceId}' nie istnieje.`);
      const installedMods = await ModRepository.getModsForInstance(args.instanceId);
      const targetVersion = await ModrinthApiClient.getVersion(args.versionId);
      return ModDependencyResolver.resolvePlan({
        targetVersion,
        installedMods,
        targetInstance: {
          loader: instance.loader,
          minecraftVersion: instance.minecraftVersion,
          loaderVersion: instance.loaderVersion,
          instanceId: args.instanceId,
        },
      });
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_PIN_MOD,
    async (_event, args: { instanceId: string; fileName: string; pinned: boolean }) => {
      try {
        await ModpackLockfileManager.setModPinned(args.instanceId, args.fileName, args.pinned);
        return { success: true };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_SET_UPDATE_POLICY,
    async (_event, args: { instanceId: string; fileName: string; policy: any }) => {
      try {
        await ModpackLockfileManager.setModUpdatePolicy(args.instanceId, args.fileName, args.policy);
        return { success: true };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  ipcMain.handle(IPC_CHANNELS.MODS_APPLY_TRANSACTION, async (_event, options: any) => {
    return ModpackTransactionManager.applyTransaction(options, (progress) => {
      for (const win of BrowserWindow.getAllWindows()) {
        if (!win.isDestroyed()) {
          win.webContents.send('zombek:transaction:progress', progress);
        }
      }
    });
  });

  ipcMain.handle(
    IPC_CHANNELS.MODS_GET_BACKUPS,
    async (_event, args: { instanceId: string }) => {
      return ModpackBackupManager.listBackups(args.instanceId);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_ROLLBACK_BACKUP,
    async (_event, args: { instanceId: string; backupId: string }) => {
      return ModpackBackupManager.rollbackBackup(args.instanceId, args.backupId);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MODS_GET_LOCKFILE,
    async (_event, args: { instanceId: string }) => {
      return ModpackLockfileManager.getLockfile(args.instanceId);
    }
  );

  // MRPACK Modpack format (Etap 6D)
  const mrpackManager = MrpackFormatManager.getInstance();

  ipcMain.handle(
    IPC_CHANNELS.MRPACK_INSPECT,
    async (_event, args: { archivePath: string }) => {
      return mrpackManager.inspect(args.archivePath);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MRPACK_IMPORT,
    async (_event, options: MrpackImportOptions) => {
      return mrpackManager.import(options, (progress) => {
        for (const win of BrowserWindow.getAllWindows()) {
          if (!win.isDestroyed()) {
            win.webContents.send(IPC_CHANNELS.EVENT_MRPACK_PROGRESS, progress);
          }
        }
      });
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.MRPACK_EXPORT,
    async (_event, options: MrpackExportOptions) => {
      return mrpackManager.export(options);
    }
  );

  ipcMain.handle(IPC_CHANNELS.MRPACK_OPEN_FILE_DIALOG, async () => {
    const result = await dialog.showOpenDialog({
      title: 'Wybierz paczkę Modrinth (.mrpack)',
      filters: [
        { name: 'Modrinth Modpack (*.mrpack)', extensions: ['mrpack'] },
        { name: 'Wszystkie pliki (*.*)', extensions: ['*'] },
      ],
      properties: ['openFile'],
    });
    if (result.canceled || result.filePaths.length === 0) {
      return null;
    }
    return result.filePaths[0];
  });

  ipcMain.handle(
    IPC_CHANNELS.MRPACK_OPEN_SAVE_DIALOG,
    async (_event, args: { defaultFileName: string }) => {
      const defaultName = args.defaultFileName.endsWith('.mrpack')
        ? args.defaultFileName
        : `${args.defaultFileName}.mrpack`;
      const result = await dialog.showSaveDialog({
        title: 'Zapisz paczkę Modrinth (.mrpack)',
        defaultPath: defaultName,
        filters: [
          { name: 'Modrinth Modpack (*.mrpack)', extensions: ['mrpack'] },
        ],
      });
      if (result.canceled || !result.filePath) {
        return null;
      }
      return result.filePath;
    }
  );

  // Accounts Management
  ipcMain.handle(IPC_CHANNELS.ACCOUNTS_GET_ALL, async () => {
    return AccountRepository.getAll();
  });

  ipcMain.handle(IPC_CHANNELS.ACCOUNTS_GET_ACTIVE, async () => {
    return AccountRepository.getActive();
  });

  ipcMain.handle(
    IPC_CHANNELS.ACCOUNTS_SET_ACTIVE,
    async (_event, args: { accountId: string }) => {
      const success = await AccountRepository.setActive(args.accountId);
      return { success };
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.ACCOUNTS_ADD_OFFLINE,
    async (_event, args: { username: string }) => {
      return AccountRepository.createOffline(args.username);
    }
  );

  ipcMain.handle(IPC_CHANNELS.ACCOUNTS_LOGIN_MICROSOFT, async () => {
    return MicrosoftAuthService.startLogin();
  });

  ipcMain.handle(
    IPC_CHANNELS.ACCOUNTS_REFRESH_SESSION,
    async (_event, args: { accountId: string }) => {
      try {
        const success = await MicrosoftAuthService.refreshAccountSession(args.accountId);
        return { success };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.ACCOUNTS_REMOVE,
    async (_event, args: { accountId: string }) => {
      const success = await AccountRepository.remove(args.accountId);
      return { success };
    }
  );

  // Game Launch Lifecycle
  ipcMain.handle(
    IPC_CHANNELS.GAME_LAUNCH,
    async (
      _event,
      args: { instanceId: string; accountId?: string; offlineUsername?: string }
    ) => {
      try {
        launcher
          .launch(args.instanceId, {
            accountId: args.accountId,
            offlineUsername: args.offlineUsername,
          })
          .catch((err) => {
            console.error('Błąd launchera podczas uruchamiania gry:', err);
          });
        return { success: true };
      } catch (err: any) {
        return { success: false, error: err.message };
      }
    }
  );

  ipcMain.handle(IPC_CHANNELS.GAME_CANCEL, async () => {
    await launcher.cancel();
    return { success: true };
  });

  ipcMain.handle(IPC_CHANNELS.GAME_GET_STATUS, async () => {
    const status = launcher.getStatus();
    return {
      state: status.state,
      isRunning: status.state === 'running',
    };
  });

  // Java Detection
  ipcMain.handle(
    IPC_CHANNELS.JAVA_DETECT,
    async (_event, args?: { customPath?: string }) => {
      return JavaRuntimeManager.detectJavaExecutable(args?.customPath);
    }
  );

  // Performance Pack Handlers (Etap 8D1)
  ipcMain.handle(IPC_CHANNELS.PERFORMANCE_GET_HARDWARE_SPECS, async () => {
    return HardwareDetectionService.getHardwareSpecs();
  });

  ipcMain.handle(
    IPC_CHANNELS.PERFORMANCE_GET_STATUS,
    async (_event, args: { instanceId: string }) => {
      return ZombekPerformanceService.getPerformanceStatus(args.instanceId);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.PERFORMANCE_APPLY_PRESET,
    async (_event, args: { instanceId: string; profile: any }) => {
      return ZombekPerformanceService.applyPerformancePreset(args.instanceId, args.profile);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.PERFORMANCE_RESTORE_BACKUP,
    async (_event, args: { instanceId: string }) => {
      return ZombekPerformanceService.restorePerformanceBackup(args.instanceId);
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.PERFORMANCE_APPLY_GRAPHICS,
    async (_event, args: { instanceId: string; options: any }) => {
      const inst = await InstallationRepository.getById(args.instanceId);
      if (!inst) return { success: false, error: 'Instancja nie istnieje' };
      const success = MinecraftOptionsManager.applyGraphicsSettings(inst.instanceDir, args.options);
      return { success };
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.PERFORMANCE_GET_RECOMMENDED_SETTINGS,
    async (_event, args: { instanceId: string; profile: any }) => {
      const prof = args.profile as keyof typeof PERFORMANCE_PRESETS;
      return PERFORMANCE_PRESETS[prof]?.graphics || PERFORMANCE_PRESETS.MAX_FPS.graphics;
    }
  );

  ipcMain.handle(
    IPC_CHANNELS.PERFORMANCE_GET_BENCHMARK_HISTORY,
    async (_event, args: { instanceId: string }) => {
      return ZombekPerformanceService.getBenchmarkHistory(args.instanceId);
    }
  );
}
