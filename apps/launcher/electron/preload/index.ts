import { contextBridge, ipcRenderer, type IpcRendererEvent } from 'electron';
import { IPC_CHANNELS, type ZombekDesktopApi } from '@zombek/contracts';
import type { LauncherConfig } from '@zombek/config-schema';
import type {
  InstanceRecord,
  EngineProgress,
  EngineState,
  EngineLogMessage,
  MicrosoftAuthProgress,
  MrpackInspectionResult,
  MrpackImportOptions,
  MrpackImportResult,
  MrpackExportOptions,
  MrpackExportResult,
  MrpackImportProgress
} from '@zombek/shared-types';

const api: ZombekDesktopApi = {
  ping: () => ipcRenderer.invoke(IPC_CHANNELS.APP_PING),
  getAppInfo: () => ipcRenderer.invoke(IPC_CHANNELS.APP_GET_INFO),
  getSystemDiagnostics: () => ipcRenderer.invoke(IPC_CHANNELS.SYSTEM_GET_DIAGNOSTICS),
  getConfig: () => ipcRenderer.invoke(IPC_CHANNELS.CONFIG_GET),
  saveConfig: (config: LauncherConfig) => ipcRenderer.invoke(IPC_CHANNELS.CONFIG_SET, config),
  minimizeWindow: () => ipcRenderer.invoke(IPC_CHANNELS.WINDOW_MINIMIZE),
  maximizeWindow: () => ipcRenderer.invoke(IPC_CHANNELS.WINDOW_MAXIMIZE),
  closeWindow: () => ipcRenderer.invoke(IPC_CHANNELS.WINDOW_CLOSE),

  // Real Minecraft Engine API
  getVersionsManifest: (forceRefresh?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.VERSIONS_GET_MANIFEST, { forceRefresh }),

  getInstances: () =>
    ipcRenderer.invoke(IPC_CHANNELS.INSTANCES_GET_ALL),

  createInstance: (data: Omit<InstanceRecord, 'id' | 'created' | 'instanceDir'>) =>
    ipcRenderer.invoke(IPC_CHANNELS.INSTANCES_CREATE, data),

  updateInstance: (id: string, updates: Partial<InstanceRecord>) =>
    ipcRenderer.invoke(IPC_CHANNELS.INSTANCES_UPDATE, { id, updates }),

  deleteInstance: async (id: string) => {
    const res = await ipcRenderer.invoke(IPC_CHANNELS.INSTANCES_DELETE, { id });
    return Boolean(res && res.success);
  },

  // Fabric Loader API
  getFabricGameVersions: (forceRefresh?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.FABRIC_GET_GAME_VERSIONS, { forceRefresh }),

  getFabricLoaderVersions: (gameVersion: string, forceRefresh?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.FABRIC_GET_LOADER_VERSIONS, { gameVersion, forceRefresh }),

  installFabric: (instanceId: string, gameVersion: string, loaderVersion: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.FABRIC_INSTALL, { instanceId, gameVersion, loaderVersion }),

  // Forge Loader API
  getForgeVersions: (gameVersion: string, forceRefresh?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.FORGE_GET_VERSIONS, { gameVersion, forceRefresh }),

  installForge: (instanceId: string, gameVersion: string, forgeVersion: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.FORGE_INSTALL, { instanceId, gameVersion, forgeVersion }),

  // NeoForge Loader API
  getNeoForgeVersions: (gameVersion: string, forceRefresh?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.NEOFORGE_GET_VERSIONS, { gameVersion, forceRefresh }),

  installNeoForge: (instanceId: string, gameVersion: string, neoForgeVersion: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.NEOFORGE_INSTALL, { instanceId, gameVersion, neoForgeVersion }),

  // Quilt Loader API
  getQuiltGameVersions: (forceRefresh?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.QUILT_GET_GAME_VERSIONS, { forceRefresh }),

  getQuiltLoaderVersions: (gameVersion: string, forceRefresh?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.QUILT_GET_LOADER_VERSIONS, { gameVersion, forceRefresh }),

  installQuilt: (instanceId: string, gameVersion: string, loaderVersion: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.QUILT_INSTALL, { instanceId, gameVersion, loaderVersion }),

  // Mods & Modpack Manager API (Etap 6A)
  getModsForInstance: (instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_GET_FOR_INSTANCE, { instanceId }),

  validateModFile: (filePath: string, instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_VALIDATE_FILE, { filePath, instanceId }),

  installModFile: (filePath: string, instanceId: string, overwrite?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_INSTALL_FILE, { filePath, instanceId, overwrite }),

  toggleMod: (instanceId: string, fileName: string, enabled: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_TOGGLE, { instanceId, fileName, enabled }),

  deleteMod: (instanceId: string, fileName: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_DELETE, { instanceId, fileName }),

  openModFileDialog: () =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_OPEN_FILE_DIALOG),

  openModsFolder: (instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_OPEN_FOLDER, { instanceId }),

  getModpacks: () =>
    ipcRenderer.invoke(IPC_CHANNELS.MODPACKS_GET_ALL),

  createModpack: (data: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODPACKS_CREATE, data),

  updateModpack: (id: string, updates: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODPACKS_UPDATE, { id, updates }),

  duplicateModpack: (id: string, newName?: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODPACKS_DUPLICATE, { id, newName }),

  deleteModpack: (id: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODPACKS_DELETE, { id }),

  applyModpackToInstance: (modpackId: string, instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODPACKS_APPLY_TO_INSTANCE, { modpackId, instanceId }),

  // Modrinth API (Etap 6B)
  modrinthSearch: (params: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODRINTH_SEARCH, params),

  modrinthGetProject: (projectIdOrSlug: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODRINTH_GET_PROJECT, { projectIdOrSlug }),

  modrinthGetProjectVersions: (
    projectIdOrSlug: string,
    loaders?: string[],
    gameVersions?: string[],
    includeChangelog?: boolean
  ) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODRINTH_GET_PROJECT_VERSIONS, {
      projectIdOrSlug,
      loaders,
      gameVersions,
      includeChangelog,
    }),

  modrinthGetVersion: (versionId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODRINTH_GET_VERSION, { versionId }),

  modrinthGetCategories: () =>
    ipcRenderer.invoke(IPC_CHANNELS.MODRINTH_GET_CATEGORIES),

  modrinthGetLoaders: () =>
    ipcRenderer.invoke(IPC_CHANNELS.MODRINTH_GET_LOADERS),

  modrinthGetGameVersions: () =>
    ipcRenderer.invoke(IPC_CHANNELS.MODRINTH_GET_GAME_VERSIONS),

  modrinthInstallVersion: (options: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODRINTH_INSTALL_VERSION, options),

  // Modpack Auto-Updates, Dependency Resolver & Transactions (Etap 6C)
  modsCheckUpdates: (instanceId: string, forceRefresh?: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_CHECK_UPDATES, { instanceId, forceRefresh }),

  modsResolveDependencies: (instanceId: string, versionId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_RESOLVE_DEPENDENCIES, { instanceId, versionId }),

  modsPinMod: (instanceId: string, fileName: string, pinned: boolean) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_PIN_MOD, { instanceId, fileName, pinned }),

  modsSetUpdatePolicy: (instanceId: string, fileName: string, policy: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_SET_UPDATE_POLICY, { instanceId, fileName, policy }),

  modsApplyTransaction: (options: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_APPLY_TRANSACTION, options),

  modsGetBackups: (instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_GET_BACKUPS, { instanceId }),

  modsRollbackBackup: (instanceId: string, backupId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_ROLLBACK_BACKUP, { instanceId, backupId }),

  modsGetLockfile: (instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MODS_GET_LOCKFILE, { instanceId }),

  // Accounts Management API
  getAccounts: () =>
    ipcRenderer.invoke(IPC_CHANNELS.ACCOUNTS_GET_ALL),

  getActiveAccount: () =>
    ipcRenderer.invoke(IPC_CHANNELS.ACCOUNTS_GET_ACTIVE),

  setActiveAccount: async (accountId: string) => {
    const res = await ipcRenderer.invoke(IPC_CHANNELS.ACCOUNTS_SET_ACTIVE, { accountId });
    return Boolean(res && res.success);
  },

  addOfflineAccount: (username: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.ACCOUNTS_ADD_OFFLINE, { username }),

  loginMicrosoft: () =>
    ipcRenderer.invoke(IPC_CHANNELS.ACCOUNTS_LOGIN_MICROSOFT),

  refreshAccountSession: (accountId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.ACCOUNTS_REFRESH_SESSION, { accountId }),

  removeAccount: async (accountId: string) => {
    const res = await ipcRenderer.invoke(IPC_CHANNELS.ACCOUNTS_REMOVE, { accountId });
    return Boolean(res && res.success);
  },

  // Game Engine Launch
  launchGame: (instanceId: string, options?: { accountId?: string; offlineUsername?: string } | string) => {
    const payload =
      typeof options === 'string'
        ? { instanceId, offlineUsername: options }
        : { instanceId, ...(options || {}) };
    return ipcRenderer.invoke(IPC_CHANNELS.GAME_LAUNCH, payload);
  },

  cancelGameLaunch: async () => {
    const res = await ipcRenderer.invoke(IPC_CHANNELS.GAME_CANCEL);
    return Boolean(res && res.success);
  },

  getGameStatus: () =>
    ipcRenderer.invoke(IPC_CHANNELS.GAME_GET_STATUS),

  detectJava: (customPath?: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.JAVA_DETECT, { customPath }),

  // Stream Event Listeners
  onEngineProgress: (callback: (progress: EngineProgress) => void) => {
    const handler = (_event: IpcRendererEvent, progress: EngineProgress) => callback(progress);
    ipcRenderer.on(IPC_CHANNELS.EVENT_ENGINE_PROGRESS, handler);
    return () => {
      ipcRenderer.removeListener(IPC_CHANNELS.EVENT_ENGINE_PROGRESS, handler);
    };
  },

  onEngineStatus: (callback: (status: { state: EngineState; message?: string }) => void) => {
    const handler = (_event: IpcRendererEvent, status: { state: EngineState; message?: string }) => callback(status);
    ipcRenderer.on(IPC_CHANNELS.EVENT_ENGINE_STATUS, handler);
    return () => {
      ipcRenderer.removeListener(IPC_CHANNELS.EVENT_ENGINE_STATUS, handler);
    };
  },

  onEngineLog: (callback: (log: EngineLogMessage) => void) => {
    const handler = (_event: IpcRendererEvent, log: EngineLogMessage) => callback(log);
    ipcRenderer.on(IPC_CHANNELS.EVENT_ENGINE_LOG, handler);
    return () => {
      ipcRenderer.removeListener(IPC_CHANNELS.EVENT_ENGINE_LOG, handler);
    };
  },

  onAuthProgress: (callback: (progress: MicrosoftAuthProgress) => void) => {
    const handler = (_event: IpcRendererEvent, progress: MicrosoftAuthProgress) => callback(progress);
    ipcRenderer.on(IPC_CHANNELS.EVENT_AUTH_PROGRESS, handler);
    return () => {
      ipcRenderer.removeListener(IPC_CHANNELS.EVENT_AUTH_PROGRESS, handler);
    };
  },

  onTransactionProgress: (callback: (progress: any) => void) => {
    const handler = (_event: IpcRendererEvent, progress: any) => callback(progress);
    ipcRenderer.on('zombek:transaction:progress', handler);
    return () => {
      ipcRenderer.removeListener('zombek:transaction:progress', handler);
    };
  },

  // Performance Pack API (Etap 8D1)
  getHardwareSpecs: () =>
    ipcRenderer.invoke(IPC_CHANNELS.PERFORMANCE_GET_HARDWARE_SPECS),

  getPerformanceStatus: (instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.PERFORMANCE_GET_STATUS, { instanceId }),

  applyPerformancePreset: (instanceId: string, profile: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.PERFORMANCE_APPLY_PRESET, { instanceId, profile }),

  restorePerformanceBackup: (instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.PERFORMANCE_RESTORE_BACKUP, { instanceId }),

  applyGraphicsOptions: (instanceId: string, options: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.PERFORMANCE_APPLY_GRAPHICS, { instanceId, options }),

  getRecommendedGraphicsOptions: (instanceId: string, profile: any) =>
    ipcRenderer.invoke(IPC_CHANNELS.PERFORMANCE_GET_RECOMMENDED_SETTINGS, { instanceId, profile }),

  getBenchmarkHistory: (instanceId: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.PERFORMANCE_GET_BENCHMARK_HISTORY, { instanceId }),

  // Modrinth MRPACK Modpack format (Etap 6D)
  mrpackInspect: (archivePath: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MRPACK_INSPECT, { archivePath }),

  mrpackImport: (options: MrpackImportOptions) =>
    ipcRenderer.invoke(IPC_CHANNELS.MRPACK_IMPORT, options),

  mrpackExport: (options: MrpackExportOptions) =>
    ipcRenderer.invoke(IPC_CHANNELS.MRPACK_EXPORT, options),

  mrpackOpenFileDialog: () =>
    ipcRenderer.invoke(IPC_CHANNELS.MRPACK_OPEN_FILE_DIALOG),

  mrpackOpenSaveDialog: (defaultFileName: string) =>
    ipcRenderer.invoke(IPC_CHANNELS.MRPACK_OPEN_SAVE_DIALOG, { defaultFileName }),

  onMrpackProgress: (callback: (progress: MrpackImportProgress) => void) => {
    const handler = (_event: IpcRendererEvent, progress: MrpackImportProgress) => callback(progress);
    ipcRenderer.on(IPC_CHANNELS.EVENT_MRPACK_PROGRESS, handler);
    return () => {
      ipcRenderer.removeListener(IPC_CHANNELS.EVENT_MRPACK_PROGRESS, handler);
    };
  }
};

contextBridge.exposeInMainWorld('zombekDesktopApi', api);
