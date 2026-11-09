/**
 * ZombekClient - IPC Communication Contracts
 * Strict type contracts connecting Electron Main, Preload, and Renderer.
 */

import type {
  LauncherAppInfo,
  SystemDiagnostics,
  MojangVersionManifest,
  EngineState,
  EngineProgress,
  EngineLogMessage,
  InstanceRecord,
  JavaDetectionResult,
  AccountRecord,
  MicrosoftAuthProgress,
  FabricGameVersion,
  FabricLoaderVersion,
  ForgeVersionInfo,
  NeoForgeVersionInfo,
  QuiltGameVersion,
  QuiltLoaderVersion,
  ModRecord,
  ModpackRecord,
  ModValidationResult,
  ModpackValidationSummary,
  ModrinthSearchHit,
  ModrinthSearchResult,
  ModrinthProject,
  ModrinthVersion,
  ModrinthCategoryTag,
  ModrinthLoaderTag,
  ModrinthGameVersionTag,
  ModrinthSearchParams,
  ModrinthInstallOptions,
  ModrinthInstallResult,
  ModUpdateInfo,
  ModUpdateCheckResult,
  ModDependencyResolutionPlan,
  ModUpdatePolicy,
  ModTransactionApplyOptions,
  ModTransactionResult,
  ModpackBackupInfo,
  ModpackLockfile,
  TransactionProgressInfo,
  MrpackIndex,
  MrpackInspectionResult,
  MrpackImportOptions,
  MrpackImportProgress,
  MrpackImportResult,
  MrpackExportOptions,
  MrpackExportResult,
  PerformanceProfileType,
  HardwareSpecs,
  OptimizationModEntry,
  PerformanceStatusInfo,
  BenchmarkSummary,
  GraphicsPresetOptions
} from '@zombek/shared-types';
import type { LauncherConfig } from '@zombek/config-schema';

export const IPC_CHANNELS = {
  APP_PING: 'zombek:app:ping',
  APP_GET_INFO: 'zombek:app:get-info',
  CONFIG_GET: 'zombek:config:get',
  CONFIG_SET: 'zombek:config:set',
  SYSTEM_GET_DIAGNOSTICS: 'zombek:system:get-diagnostics',
  WINDOW_MINIMIZE: 'zombek:window:minimize',
  WINDOW_MAXIMIZE: 'zombek:window:maximize',
  WINDOW_CLOSE: 'zombek:window:close',

  // Versions & Instances
  VERSIONS_GET_MANIFEST: 'zombek:versions:get-manifest',
  INSTANCES_GET_ALL: 'zombek:instances:get-all',
  INSTANCES_CREATE: 'zombek:instances:create',
  INSTANCES_UPDATE: 'zombek:instances:update',
  INSTANCES_DELETE: 'zombek:instances:delete',

  // Fabric Loader
  FABRIC_GET_GAME_VERSIONS: 'zombek:fabric:get-game-versions',
  FABRIC_GET_LOADER_VERSIONS: 'zombek:fabric:get-loader-versions',
  FABRIC_INSTALL: 'zombek:fabric:install',

  // Forge Loader
  FORGE_GET_VERSIONS: 'zombek:forge:get-versions',
  FORGE_INSTALL: 'zombek:forge:install',

  // NeoForge Loader
  NEOFORGE_GET_VERSIONS: 'zombek:neoforge:get-versions',
  NEOFORGE_INSTALL: 'zombek:neoforge:install',

  // Quilt Loader
  QUILT_GET_GAME_VERSIONS: 'zombek:quilt:get-game-versions',
  QUILT_GET_LOADER_VERSIONS: 'zombek:quilt:get-loader-versions',
  QUILT_INSTALL: 'zombek:quilt:install',

  // Mods & Modpack Manager Core (Etap 6A)
  MODS_GET_FOR_INSTANCE: 'zombek:mods:get-for-instance',
  MODS_VALIDATE_FILE: 'zombek:mods:validate-file',
  MODS_INSTALL_FILE: 'zombek:mods:install-file',
  MODS_TOGGLE: 'zombek:mods:toggle',
  MODS_DELETE: 'zombek:mods:delete',
  MODS_OPEN_FILE_DIALOG: 'zombek:mods:open-file-dialog',
  MODS_OPEN_FOLDER: 'zombek:mods:open-folder',

  MODPACKS_GET_ALL: 'zombek:modpacks:get-all',
  MODPACKS_CREATE: 'zombek:modpacks:create',
  MODPACKS_UPDATE: 'zombek:modpacks:update',
  MODPACKS_DUPLICATE: 'zombek:modpacks:duplicate',
  MODPACKS_DELETE: 'zombek:modpacks:delete',
  MODPACKS_APPLY_TO_INSTANCE: 'zombek:modpacks:apply-to-instance',

  // Modrinth Integration (Etap 6B)
  MODRINTH_SEARCH: 'zombek:modrinth:search',
  MODRINTH_GET_PROJECT: 'zombek:modrinth:get-project',
  MODRINTH_GET_PROJECT_VERSIONS: 'zombek:modrinth:get-project-versions',
  MODRINTH_GET_VERSION: 'zombek:modrinth:get-version',
  MODRINTH_GET_CATEGORIES: 'zombek:modrinth:get-categories',
  MODRINTH_GET_LOADERS: 'zombek:modrinth:get-loaders',
  MODRINTH_GET_GAME_VERSIONS: 'zombek:modrinth:get-game-versions',
  MODRINTH_INSTALL_VERSION: 'zombek:modrinth:install-version',

  // Modpack Dependencies, Updates & Transactions (Etap 6C)
  MODS_CHECK_UPDATES: 'zombek:mods:check-updates',
  MODS_RESOLVE_DEPENDENCIES: 'zombek:mods:resolve-dependencies',
  MODS_PIN_MOD: 'zombek:mods:pin-mod',
  MODS_SET_UPDATE_POLICY: 'zombek:mods:set-update-policy',
  MODS_APPLY_TRANSACTION: 'zombek:mods:apply-transaction',
  MODS_GET_BACKUPS: 'zombek:mods:get-backups',
  MODS_ROLLBACK_BACKUP: 'zombek:mods:rollback-backup',
  MODS_GET_LOCKFILE: 'zombek:mods:get-lockfile',

  // Modrinth Modpack .mrpack Import & Export (Etap 6D)
  MRPACK_INSPECT: 'zombek:mrpack:inspect',
  MRPACK_IMPORT: 'zombek:mrpack:import',
  MRPACK_EXPORT: 'zombek:mrpack:export',
  MRPACK_OPEN_FILE_DIALOG: 'zombek:mrpack:open-file-dialog',
  MRPACK_OPEN_SAVE_DIALOG: 'zombek:mrpack:open-save-dialog',

  // Accounts & Profiles
  ACCOUNTS_GET_ALL: 'zombek:accounts:get-all',
  ACCOUNTS_GET_ACTIVE: 'zombek:accounts:get-active',
  ACCOUNTS_SET_ACTIVE: 'zombek:accounts:set-active',
  ACCOUNTS_ADD_OFFLINE: 'zombek:accounts:add-offline',
  ACCOUNTS_LOGIN_MICROSOFT: 'zombek:accounts:login-microsoft',
  ACCOUNTS_REFRESH_SESSION: 'zombek:accounts:refresh-session',
  ACCOUNTS_REMOVE: 'zombek:accounts:remove',

  // Game Launch & Lifecycle
  GAME_LAUNCH: 'zombek:game:launch',
  GAME_CANCEL: 'zombek:game:cancel',
  GAME_GET_STATUS: 'zombek:game:get-status',
  JAVA_DETECT: 'zombek:java:detect',

  // Server-to-Client Event Streams
  EVENT_ENGINE_PROGRESS: 'zombek:event:engine-progress',
  EVENT_ENGINE_STATUS: 'zombek:event:engine-status',
  EVENT_ENGINE_LOG: 'zombek:event:engine-log',
  EVENT_AUTH_PROGRESS: 'zombek:event:auth-progress',
  EVENT_MRPACK_PROGRESS: 'zombek:event:mrpack-progress',

  // Performance Pack (Etap 8D1)
  PERFORMANCE_GET_HARDWARE_SPECS: 'zombek:performance:get-hardware-specs',
  PERFORMANCE_GET_STATUS: 'zombek:performance:get-status',
  PERFORMANCE_APPLY_PRESET: 'zombek:performance:apply-preset',
  PERFORMANCE_RESTORE_BACKUP: 'zombek:performance:restore-backup',
  PERFORMANCE_APPLY_GRAPHICS: 'zombek:performance:apply-graphics',
  PERFORMANCE_GET_RECOMMENDED_SETTINGS: 'zombek:performance:get-recommended-settings',
  PERFORMANCE_GET_BENCHMARK_HISTORY: 'zombek:performance:get-benchmark-history'
} as const;

export type IpcChannel = (typeof IPC_CHANNELS)[keyof typeof IPC_CHANNELS];

export interface IpcContractMap {
  [IPC_CHANNELS.APP_PING]: {
    request: void;
    response: string;
  };
  [IPC_CHANNELS.APP_GET_INFO]: {
    request: void;
    response: LauncherAppInfo;
  };
  [IPC_CHANNELS.CONFIG_GET]: {
    request: void;
    response: LauncherConfig;
  };
  [IPC_CHANNELS.CONFIG_SET]: {
    request: LauncherConfig;
    response: { success: boolean; error?: string };
  };
  [IPC_CHANNELS.SYSTEM_GET_DIAGNOSTICS]: {
    request: void;
    response: SystemDiagnostics;
  };
  [IPC_CHANNELS.WINDOW_MINIMIZE]: {
    request: void;
    response: void;
  };
  [IPC_CHANNELS.WINDOW_MAXIMIZE]: {
    request: void;
    response: void;
  };
  [IPC_CHANNELS.WINDOW_CLOSE]: {
    request: void;
    response: void;
  };
  [IPC_CHANNELS.VERSIONS_GET_MANIFEST]: {
    request: { forceRefresh?: boolean };
    response: MojangVersionManifest;
  };
  [IPC_CHANNELS.INSTANCES_GET_ALL]: {
    request: void;
    response: InstanceRecord[];
  };
  [IPC_CHANNELS.INSTANCES_CREATE]: {
    request: Omit<InstanceRecord, 'id' | 'created' | 'instanceDir'>;
    response: InstanceRecord;
  };
  [IPC_CHANNELS.INSTANCES_UPDATE]: {
    request: { id: string; updates: Partial<InstanceRecord> };
    response: InstanceRecord;
  };
  [IPC_CHANNELS.INSTANCES_DELETE]: {
    request: { id: string };
    response: { success: boolean };
  };

  // Fabric
  [IPC_CHANNELS.FABRIC_GET_GAME_VERSIONS]: {
    request: { forceRefresh?: boolean };
    response: FabricGameVersion[];
  };
  [IPC_CHANNELS.FABRIC_GET_LOADER_VERSIONS]: {
    request: { gameVersion: string; forceRefresh?: boolean };
    response: FabricLoaderVersion[];
  };
  [IPC_CHANNELS.FABRIC_INSTALL]: {
    request: { instanceId: string; gameVersion: string; loaderVersion: string };
    response: { success: boolean; versionId?: string; error?: string };
  };

  // Forge
  [IPC_CHANNELS.FORGE_GET_VERSIONS]: {
    request: { gameVersion: string; forceRefresh?: boolean };
    response: ForgeVersionInfo[];
  };
  [IPC_CHANNELS.FORGE_INSTALL]: {
    request: { instanceId: string; gameVersion: string; forgeVersion: string };
    response: { success: boolean; versionId?: string; error?: string };
  };

  // NeoForge
  [IPC_CHANNELS.NEOFORGE_GET_VERSIONS]: {
    request: { gameVersion: string; forceRefresh?: boolean };
    response: NeoForgeVersionInfo[];
  };
  [IPC_CHANNELS.NEOFORGE_INSTALL]: {
    request: { instanceId: string; gameVersion: string; neoForgeVersion: string };
    response: { success: boolean; versionId?: string; error?: string };
  };

  // Quilt
  [IPC_CHANNELS.QUILT_GET_GAME_VERSIONS]: {
    request: { forceRefresh?: boolean };
    response: QuiltGameVersion[];
  };
  [IPC_CHANNELS.QUILT_GET_LOADER_VERSIONS]: {
    request: { gameVersion: string; forceRefresh?: boolean };
    response: QuiltLoaderVersion[];
  };
  [IPC_CHANNELS.QUILT_INSTALL]: {
    request: { instanceId: string; gameVersion: string; loaderVersion: string };
    response: { success: boolean; versionId?: string; error?: string };
  };

  // Mods & Modpack Manager Core (Etap 6A)
  [IPC_CHANNELS.MODS_GET_FOR_INSTANCE]: {
    request: { instanceId: string };
    response: ModRecord[];
  };
  [IPC_CHANNELS.MODS_VALIDATE_FILE]: {
    request: { filePath: string; instanceId: string };
    response: ModValidationResult;
  };
  [IPC_CHANNELS.MODS_INSTALL_FILE]: {
    request: { filePath: string; instanceId: string; overwrite?: boolean };
    response: { success: boolean; mod?: ModRecord; error?: string };
  };
  [IPC_CHANNELS.MODS_TOGGLE]: {
    request: { instanceId: string; fileName: string; enabled: boolean };
    response: { success: boolean; mod?: ModRecord; error?: string };
  };
  [IPC_CHANNELS.MODS_DELETE]: {
    request: { instanceId: string; fileName: string };
    response: { success: boolean; error?: string };
  };
  [IPC_CHANNELS.MODS_OPEN_FILE_DIALOG]: {
    request: void;
    response: string[] | null;
  };
  [IPC_CHANNELS.MODS_OPEN_FOLDER]: {
    request: { instanceId: string };
    response: { success: boolean; error?: string };
  };

  [IPC_CHANNELS.MODPACKS_GET_ALL]: {
    request: void;
    response: ModpackRecord[];
  };
  [IPC_CHANNELS.MODPACKS_CREATE]: {
    request: Omit<ModpackRecord, 'id' | 'created' | 'lastModified' | 'modsCount' | 'modFiles' | 'compatibilityStatus'>;
    response: ModpackRecord;
  };
  [IPC_CHANNELS.MODPACKS_UPDATE]: {
    request: { id: string; updates: Partial<ModpackRecord> };
    response: ModpackRecord;
  };
  [IPC_CHANNELS.MODPACKS_DUPLICATE]: {
    request: { id: string; newName?: string };
    response: ModpackRecord;
  };
  [IPC_CHANNELS.MODPACKS_DELETE]: {
    request: { id: string };
    response: { success: boolean; error?: string };
  };
  [IPC_CHANNELS.MODPACKS_APPLY_TO_INSTANCE]: {
    request: { modpackId: string; instanceId: string };
    response: { success: boolean; error?: string };
  };

  // Modrinth Integration (Etap 6B)
  [IPC_CHANNELS.MODRINTH_SEARCH]: {
    request: ModrinthSearchParams;
    response: ModrinthSearchResult;
  };
  [IPC_CHANNELS.MODRINTH_GET_PROJECT]: {
    request: { projectIdOrSlug: string };
    response: ModrinthProject;
  };
  [IPC_CHANNELS.MODRINTH_GET_PROJECT_VERSIONS]: {
    request: { projectIdOrSlug: string; loaders?: string[]; gameVersions?: string[]; includeChangelog?: boolean };
    response: ModrinthVersion[];
  };
  [IPC_CHANNELS.MODRINTH_GET_VERSION]: {
    request: { versionId: string };
    response: ModrinthVersion;
  };
  [IPC_CHANNELS.MODRINTH_GET_CATEGORIES]: {
    request: void;
    response: ModrinthCategoryTag[];
  };
  [IPC_CHANNELS.MODRINTH_GET_LOADERS]: {
    request: void;
    response: ModrinthLoaderTag[];
  };
  [IPC_CHANNELS.MODRINTH_GET_GAME_VERSIONS]: {
    request: void;
    response: ModrinthGameVersionTag[];
  };
  [IPC_CHANNELS.MODRINTH_INSTALL_VERSION]: {
    request: ModrinthInstallOptions;
    response: ModrinthInstallResult;
  };

  // Modpack Dependencies, Updates & Transactions (Etap 6C)
  [IPC_CHANNELS.MODS_CHECK_UPDATES]: {
    request: { instanceId: string; forceRefresh?: boolean };
    response: ModUpdateCheckResult;
  };
  [IPC_CHANNELS.MODS_RESOLVE_DEPENDENCIES]: {
    request: { instanceId: string; versionId: string };
    response: ModDependencyResolutionPlan;
  };
  [IPC_CHANNELS.MODS_PIN_MOD]: {
    request: { instanceId: string; fileName: string; pinned: boolean };
    response: { success: boolean; error?: string };
  };
  [IPC_CHANNELS.MODS_SET_UPDATE_POLICY]: {
    request: { instanceId: string; fileName: string; policy: ModUpdatePolicy };
    response: { success: boolean; error?: string };
  };
  [IPC_CHANNELS.MODS_APPLY_TRANSACTION]: {
    request: ModTransactionApplyOptions;
    response: ModTransactionResult;
  };
  [IPC_CHANNELS.MODS_GET_BACKUPS]: {
    request: { instanceId: string };
    response: ModpackBackupInfo[];
  };
  [IPC_CHANNELS.MODS_ROLLBACK_BACKUP]: {
    request: { instanceId: string; backupId: string };
    response: { success: boolean; restoredCount?: number; error?: string };
  };
  [IPC_CHANNELS.MODS_GET_LOCKFILE]: {
    request: { instanceId: string };
    response: ModpackLockfile | null;
  };

  // Modrinth Modpack .mrpack Import & Export (Etap 6D)
  [IPC_CHANNELS.MRPACK_INSPECT]: {
    request: { archivePath: string };
    response: MrpackInspectionResult;
  };
  [IPC_CHANNELS.MRPACK_IMPORT]: {
    request: MrpackImportOptions;
    response: MrpackImportResult;
  };
  [IPC_CHANNELS.MRPACK_EXPORT]: {
    request: MrpackExportOptions;
    response: MrpackExportResult;
  };
  [IPC_CHANNELS.MRPACK_OPEN_FILE_DIALOG]: {
    request: void;
    response: string | null;
  };
  [IPC_CHANNELS.MRPACK_OPEN_SAVE_DIALOG]: {
    request: { defaultName?: string };
    response: string | null;
  };

  // Accounts
  [IPC_CHANNELS.ACCOUNTS_GET_ALL]: {
    request: void;
    response: AccountRecord[];
  };
  [IPC_CHANNELS.ACCOUNTS_GET_ACTIVE]: {
    request: void;
    response: AccountRecord | null;
  };
  [IPC_CHANNELS.ACCOUNTS_SET_ACTIVE]: {
    request: { accountId: string };
    response: { success: boolean };
  };
  [IPC_CHANNELS.ACCOUNTS_ADD_OFFLINE]: {
    request: { username: string };
    response: AccountRecord;
  };
  [IPC_CHANNELS.ACCOUNTS_LOGIN_MICROSOFT]: {
    request: void;
    response: { success: boolean; account?: AccountRecord; error?: string; step?: string };
  };
  [IPC_CHANNELS.ACCOUNTS_REFRESH_SESSION]: {
    request: { accountId: string };
    response: { success: boolean; error?: string };
  };
  [IPC_CHANNELS.ACCOUNTS_REMOVE]: {
    request: { accountId: string };
    response: { success: boolean };
  };

  // Game
  [IPC_CHANNELS.GAME_LAUNCH]: {
    request: { instanceId: string; accountId?: string; offlineUsername?: string };
    response: { success: boolean; error?: string };
  };
  [IPC_CHANNELS.GAME_CANCEL]: {
    request: void;
    response: { success: boolean };
  };
  [IPC_CHANNELS.GAME_GET_STATUS]: {
    request: void;
    response: { state: EngineState; isRunning: boolean };
  };
  [IPC_CHANNELS.JAVA_DETECT]: {
    request: { customPath?: string };
    response: JavaDetectionResult;
  };

  // Performance Pack (Etap 8D1)
  [IPC_CHANNELS.PERFORMANCE_GET_HARDWARE_SPECS]: {
    request: void;
    response: HardwareSpecs;
  };
  [IPC_CHANNELS.PERFORMANCE_GET_STATUS]: {
    request: { instanceId: string };
    response: PerformanceStatusInfo;
  };
  [IPC_CHANNELS.PERFORMANCE_APPLY_PRESET]: {
    request: { instanceId: string; profile: PerformanceProfileType };
    response: { success: boolean; installedCount?: number; backupId?: string; error?: string };
  };
  [IPC_CHANNELS.PERFORMANCE_RESTORE_BACKUP]: {
    request: { instanceId: string };
    response: { success: boolean; restoredCount?: number; error?: string };
  };
  [IPC_CHANNELS.PERFORMANCE_APPLY_GRAPHICS]: {
    request: { instanceId: string; options: Partial<GraphicsPresetOptions> };
    response: { success: boolean; error?: string };
  };
  [IPC_CHANNELS.PERFORMANCE_GET_RECOMMENDED_SETTINGS]: {
    request: { instanceId: string; profile: PerformanceProfileType };
    response: GraphicsPresetOptions;
  };
  [IPC_CHANNELS.PERFORMANCE_GET_BENCHMARK_HISTORY]: {
    request: { instanceId: string };
    response: BenchmarkSummary[];
  };
}

/**
 * Safe API exposed to Renderer via Preload ContextBridge.
 * Renderer must NEVER access Electron remote or raw ipcRenderer directly.
 * Access tokens are NEVER exposed to the Renderer.
 */
export interface ZombekDesktopApi {
  ping(): Promise<string>;
  getAppInfo(): Promise<LauncherAppInfo>;
  getSystemDiagnostics(): Promise<SystemDiagnostics>;
  getConfig(): Promise<LauncherConfig>;
  saveConfig(config: LauncherConfig): Promise<{ success: boolean; error?: string }>;
  minimizeWindow(): Promise<void>;
  maximizeWindow(): Promise<void>;
  closeWindow(): Promise<void>;

  // Minecraft Versions & Instances API
  getVersionsManifest(forceRefresh?: boolean): Promise<MojangVersionManifest>;
  getInstances(): Promise<InstanceRecord[]>;
  createInstance(data: Omit<InstanceRecord, 'id' | 'created' | 'instanceDir'>): Promise<InstanceRecord>;
  updateInstance(id: string, updates: Partial<InstanceRecord>): Promise<InstanceRecord>;
  deleteInstance(id: string): Promise<boolean>;

  // Fabric Loader API
  getFabricGameVersions(forceRefresh?: boolean): Promise<FabricGameVersion[]>;
  getFabricLoaderVersions(gameVersion: string, forceRefresh?: boolean): Promise<FabricLoaderVersion[]>;
  installFabric(
    instanceId: string,
    gameVersion: string,
    loaderVersion: string
  ): Promise<{ success: boolean; versionId?: string; error?: string }>;

  // Forge Loader API
  getForgeVersions(gameVersion: string, forceRefresh?: boolean): Promise<ForgeVersionInfo[]>;
  installForge(
    instanceId: string,
    gameVersion: string,
    forgeVersion: string
  ): Promise<{ success: boolean; versionId?: string; error?: string }>;

  // NeoForge Loader API
  getNeoForgeVersions(gameVersion: string, forceRefresh?: boolean): Promise<NeoForgeVersionInfo[]>;
  installNeoForge(
    instanceId: string,
    gameVersion: string,
    neoForgeVersion: string
  ): Promise<{ success: boolean; versionId?: string; error?: string }>;

  // Quilt Loader API
  getQuiltGameVersions(forceRefresh?: boolean): Promise<QuiltGameVersion[]>;
  getQuiltLoaderVersions(gameVersion: string, forceRefresh?: boolean): Promise<QuiltLoaderVersion[]>;
  installQuilt(
    instanceId: string,
    gameVersion: string,
    loaderVersion: string
  ): Promise<{ success: boolean; versionId?: string; error?: string }>;

  // Mods & Modpack Manager API (Etap 6A)
  getModsForInstance(instanceId: string): Promise<ModRecord[]>;
  validateModFile(filePath: string, instanceId: string): Promise<ModValidationResult>;
  installModFile(filePath: string, instanceId: string, overwrite?: boolean): Promise<{ success: boolean; mod?: ModRecord; error?: string }>;
  toggleMod(instanceId: string, fileName: string, enabled: boolean): Promise<{ success: boolean; mod?: ModRecord; error?: string }>;
  deleteMod(instanceId: string, fileName: string): Promise<{ success: boolean; error?: string }>;
  openModFileDialog(): Promise<string[] | null>;
  openModsFolder(instanceId: string): Promise<{ success: boolean; error?: string }>;

  getModpacks(): Promise<ModpackRecord[]>;
  createModpack(data: Omit<ModpackRecord, 'id' | 'created' | 'lastModified' | 'modsCount' | 'modFiles' | 'compatibilityStatus'>): Promise<ModpackRecord>;
  updateModpack(id: string, updates: Partial<ModpackRecord>): Promise<ModpackRecord>;
  duplicateModpack(id: string, newName?: string): Promise<ModpackRecord>;
  deleteModpack(id: string): Promise<{ success: boolean; error?: string }>;
  applyModpackToInstance(modpackId: string, instanceId: string): Promise<{ success: boolean; error?: string }>;

  // Modrinth API (Etap 6B)
  modrinthSearch(params: ModrinthSearchParams): Promise<ModrinthSearchResult>;
  modrinthGetProject(projectIdOrSlug: string): Promise<ModrinthProject>;
  modrinthGetProjectVersions(
    projectIdOrSlug: string,
    loaders?: string[],
    gameVersions?: string[],
    includeChangelog?: boolean
  ): Promise<ModrinthVersion[]>;
  modrinthGetVersion(versionId: string): Promise<ModrinthVersion>;
  modrinthGetCategories(): Promise<ModrinthCategoryTag[]>;
  modrinthGetLoaders(): Promise<ModrinthLoaderTag[]>;
  modrinthGetGameVersions(): Promise<ModrinthGameVersionTag[]>;
  modrinthInstallVersion(options: ModrinthInstallOptions): Promise<ModrinthInstallResult>;

  // Modpack Auto-Updates, Dependency Resolver & Transactions (Etap 6C)
  modsCheckUpdates(instanceId: string, forceRefresh?: boolean): Promise<ModUpdateCheckResult>;
  modsResolveDependencies(instanceId: string, versionId: string): Promise<ModDependencyResolutionPlan>;
  modsPinMod(instanceId: string, fileName: string, pinned: boolean): Promise<{ success: boolean; error?: string }>;
  modsSetUpdatePolicy(instanceId: string, fileName: string, policy: ModUpdatePolicy): Promise<{ success: boolean; error?: string }>;
  modsApplyTransaction(options: ModTransactionApplyOptions): Promise<ModTransactionResult>;
  modsGetBackups(instanceId: string): Promise<ModpackBackupInfo[]>;
  modsRollbackBackup(instanceId: string, backupId: string): Promise<{ success: boolean; restoredCount?: number; error?: string }>;
  modsGetLockfile(instanceId: string): Promise<ModpackLockfile | null>;

  // Modrinth Modpack .mrpack Import & Export (Etap 6D)
  mrpackInspect(archivePath: string): Promise<MrpackInspectionResult>;
  mrpackImport(options: MrpackImportOptions): Promise<MrpackImportResult>;
  mrpackExport(options: MrpackExportOptions): Promise<MrpackExportResult>;
  mrpackOpenFileDialog(): Promise<string | null>;
  mrpackOpenSaveDialog(defaultName?: string): Promise<string | null>;
  onMrpackProgress(callback: (progress: MrpackImportProgress) => void): () => void;

  // Accounts Management API
  getAccounts(): Promise<AccountRecord[]>;
  getActiveAccount(): Promise<AccountRecord | null>;
  setActiveAccount(accountId: string): Promise<boolean>;
  addOfflineAccount(username: string): Promise<AccountRecord>;
  loginMicrosoft(): Promise<{ success: boolean; account?: AccountRecord; error?: string; step?: string }>;
  refreshAccountSession(accountId: string): Promise<{ success: boolean; error?: string }>;
  removeAccount(accountId: string): Promise<boolean>;

  // Game Engine API
  launchGame(
    instanceId: string,
    options?: { accountId?: string; offlineUsername?: string } | string
  ): Promise<{ success: boolean; error?: string }>;
  cancelGameLaunch(): Promise<boolean>;
  getGameStatus(): Promise<{ state: EngineState; isRunning: boolean }>;
  detectJava(customPath?: string): Promise<JavaDetectionResult>;

  // Performance Pack API (Etap 8D1)
  getHardwareSpecs(): Promise<HardwareSpecs>;
  getPerformanceStatus(instanceId: string): Promise<PerformanceStatusInfo>;
  applyPerformancePreset(instanceId: string, profile: PerformanceProfileType): Promise<{ success: boolean; installedCount?: number; backupId?: string; error?: string }>;
  restorePerformanceBackup(instanceId: string): Promise<{ success: boolean; restoredCount?: number; error?: string }>;
  applyGraphicsOptions(instanceId: string, options: Partial<GraphicsPresetOptions>): Promise<{ success: boolean; error?: string }>;
  getRecommendedGraphicsOptions(instanceId: string, profile: PerformanceProfileType): Promise<GraphicsPresetOptions>;
  getBenchmarkHistory(instanceId: string): Promise<BenchmarkSummary[]>;

  // Stream Event Listeners
  onEngineProgress(callback: (progress: EngineProgress) => void): () => void;
  onEngineStatus(callback: (status: { state: EngineState; message?: string }) => void): () => void;
  onEngineLog(callback: (log: EngineLogMessage) => void): () => void;
  onAuthProgress(callback: (progress: MicrosoftAuthProgress) => void): () => void;
  onTransactionProgress(callback: (progress: TransactionProgressInfo) => void): () => void;
}

declare global {
  interface Window {
    zombekDesktopApi?: ZombekDesktopApi;
  }
}
