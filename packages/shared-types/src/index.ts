/**
 * ZombekClient - Shared Domain Types
 * Defines core contracts and platform types shared across the monorepo.
 */

export type PlatformType = 'windows' | 'linux' | 'darwin';

export type MinecraftLoaderType = 'fabric' | 'forge' | 'neoforge' | 'quilt' | 'vanilla';

export type LauncherStatus =
  | 'idle'
  | 'checking-updates'
  | 'ready'
  | 'downloading'
  | 'launching'
  | 'game-running'
  | 'error';

export type AccountType = 'microsoft' | 'offline';

export type AccountSessionStatus = 'valid' | 'expired' | 'requires_login' | 'unauthorized';

export interface PlayerSkin {
  id?: string;
  state?: 'ACTIVE';
  url: string;
  variant?: 'CLASSIC' | 'SLIM';
}

export interface PlayerCape {
  id?: string;
  state?: 'ACTIVE';
  url: string;
  alias?: string;
}

export interface AccountRecord {
  id: string;
  type: AccountType;
  username: string;
  uuid: string;
  avatar: string;
  hasEntitlement: boolean;
  entitlements?: string[];
  sessionStatus: AccountSessionStatus;
  lastUsed: string;
  created: string;
  xuid?: string;
  isDefault?: boolean;
}

export interface MicrosoftAuthProgress {
  step:
    | 'connecting'
    | 'waiting_browser'
    | 'xbox_auth'
    | 'minecraft_auth'
    | 'checking_entitlements'
    | 'fetching_profile'
    | 'saving'
    | 'completed'
    | 'failed';
  message: string;
  error?: string;
}

export interface UserProfile {
  id: string;
  username: string;
  accountType: AccountType;
  uuid?: string;
  avatarUrl?: string;
}

export interface MinecraftVersionInfo {
  id: string;
  name: string;
  releaseType: 'release' | 'snapshot';
  supportedLoaders: MinecraftLoaderType[];
  releaseDate?: string;
}

export interface MemoryAllocation {
  minMb: number;
  maxMb: number;
}

export interface SystemDiagnostics {
  platform: PlatformType;
  arch: string;
  nodeVersion: string;
  electronVersion: string;
  totalMemoryMb: number;
  freeMemoryMb: number;
}

export interface LauncherAppInfo {
  name: string;
  version: string;
  environment: 'development' | 'production' | 'test';
}

export const SUPPORTED_LOADERS: readonly MinecraftLoaderType[] = [
  'fabric',
  'forge',
  'neoforge',
  'quilt',
  'vanilla'
] as const;

export interface MojangVersionItem {
  id: string;
  type: 'release' | 'snapshot' | 'old_beta' | 'old_alpha';
  url: string;
  time: string;
  releaseTime: string;
  sha1?: string;
  complianceLevel?: number;
}

export interface MojangVersionManifest {
  latest: {
    release: string;
    snapshot: string;
  };
  versions: MojangVersionItem[];
  lastUpdated?: string;
}

export type EngineState =
  | 'idle'
  | 'preparing'
  | 'downloading'
  | 'verifying'
  | 'preparing-java'
  | 'launching'
  | 'running'
  | 'closing'
  | 'completed'
  | 'failed'
  | 'cancelled';

export interface EngineProgress {
  state: EngineState;
  currentFile?: string;
  completedFiles: number;
  totalFiles: number;
  downloadedBytes: number;
  totalBytes: number;
  percent: number;
  speedMb?: number;
  message?: string;
}

export interface EngineLogMessage {
  id: string;
  level: 'INFO' | 'WARNING' | 'ERROR' | 'DEBUG';
  timestamp: string;
  category: 'LAUNCHER' | 'DOWNLOADS' | 'JAVA' | 'MINECRAFT' | 'AUTH';
  message: string;
}

export interface InstanceRecord {
  id: string;
  name: string;
  minecraftVersion: string;
  loader: MinecraftLoaderType;
  loaderVersion?: string;
  resolvedVersionId?: string;
  status?: 'installed' | 'ready' | 'not_installed';
  created: string;
  lastPlayed?: string;
  allocatedRamMb: number;
  customJvmArgs?: string;
  resolutionWidth?: number;
  resolutionHeight?: number;
  fullscreen?: boolean;
  customJavaPath?: string;
  instanceDir: string;
  assignedAccountId?: string;
}

export interface JavaDetectionResult {
  valid: boolean;
  version?: string;
  majorVersion?: number;
  path: string;
  error?: string;
}

export interface FabricGameVersion {
  version: string;
  stable: boolean;
}

export interface FabricLoaderVersion {
  separator: string;
  build: number;
  maven: string;
  version: string;
  stable: boolean;
}

export interface FabricLoaderItem {
  loader: FabricLoaderVersion;
  intermediary?: {
    maven: string;
    version: string;
    stable: boolean;
  };
  launcherMeta?: {
    version: number;
    min_java_version?: number;
  };
}

export interface ForgeVersionInfo {
  version: string;          // Full version string e.g. "1.20.4-49.2.13"
  rawVersion: string;       // Forge build e.g. "49.2.13"
  minecraftVersion: string; // e.g. "1.20.4"
  isRecommended: boolean;
  isLatest: boolean;
  installerUrl: string;
  installerSha1?: string;
  installerSize?: number;
}

export interface ForgeProcessorEntry {
  sides?: ('client' | 'server')[];
  jar: string;
  classpath: string[];
  args: string[];
  outputs?: Record<string, string>;
}

export interface ForgeInstallProfile {
  spec?: number;
  profile?: string;
  version: string;
  minecraft?: string;
  serverJarPath?: string;
  path?: string;
  data?: Record<string, { client?: string; server?: string }>;
  processors?: ForgeProcessorEntry[];
  libraries?: any[];
  versionInfo?: any;
}

export interface NeoForgeVersionInfo {
  version: string;             // Display version, e.g. "20.4.237" or "1.20.1-47.1.106"
  rawVersion: string;          // Raw build string
  minecraftVersion: string;    // Compatible Minecraft version, e.g. "1.20.4"
  artifactCoordinates: string; // "net.neoforged:neoforge" or "net.neoforged:forge"
  installerUrl: string;        // Official Maven installer URL
  generation: 'modern' | 'transition';
  isRecommended?: boolean;
  isLatest?: boolean;
  releaseTime?: string;
}

export interface NeoForgeProcessorEntry {
  sides?: ('client' | 'server')[];
  jar: string;
  classpath: string[];
  args: string[];
  outputs?: Record<string, string>;
}

export interface NeoForgeInstallProfile {
  spec?: number;
  profile?: string;
  version: string;
  minecraft?: string;
  path?: string;
  data?: Record<string, { client?: string; server?: string }>;
  processors?: NeoForgeProcessorEntry[];
  libraries?: any[];
  versionInfo?: any;
}

export interface QuiltGameVersion {
  version: string;
  stable: boolean;
}

export interface QuiltLoaderVersion {
  separator: string;
  build: number;
  maven: string;
  version: string;
  stable?: boolean;
  file_size?: number;
  hashes?: {
    sha1?: string;
    sha256?: string;
    sha512?: string;
  };
}

export interface QuiltLoaderItem {
  loader: QuiltLoaderVersion;
  hashed?: {
    maven: string;
    version: string;
  };
  intermediary?: {
    maven: string;
    version: string;
  };
  launcherMeta?: {
    version: number;
    min_java_version?: number;
    libraries?: {
      client?: any[];
      common?: any[];
      server?: any[];
    };
    mainClass?: {
      client?: string;
      server?: string;
      serverLauncher?: string;
    };
  };
}

/**
 * Mod compatibility target classification for future Modpack Manager
 */
export type ModCompatibilityTarget =
  | 'quilt'
  | 'fabric'
  | 'fabric-and-quilt'
  | 'quilt-incompatible'
  | 'forge'
  | 'neoforge';

/**
 * Mod Compatibility Statuses
 */
export type ModCompatibilityStatus =
  | 'Compatible'
  | 'Possibly Compatible'
  | 'Incompatible'
  | 'Missing Dependency'
  | 'Dependency Conflict'
  | 'Unknown Metadata'
  | 'Disabled'
  | 'Broken File';

export interface ModDependencyRequirement {
  id: string;
  versionRange?: string;
  kind: 'depends' | 'recommends' | 'suggests' | 'conflicts' | 'breaks';
  mandatory: boolean;
}

export interface UniversalModMetadata {
  id: string;
  name: string;
  version: string;
  description: string;
  authors: string[];
  contact?: Record<string, string>;
  license?: string;
  environment?: 'client' | 'server' | 'both' | 'universal' | '*';
  loader: MinecraftLoaderType | 'quilt-or-fabric' | 'unknown';
  minecraftVersionRequirement?: string;
  loaderVersionRequirement?: string;
  dependencies: ModDependencyRequirement[];
  rawSource: 'fabric.mod.json' | 'quilt.mod.json' | 'mods.toml' | 'neoforge.mods.toml' | 'unknown';
  iconPath?: string;
}

export interface ModRecord {
  fileName: string;
  filePath: string;
  fileSize: number;
  sha256: string;
  enabled: boolean;
  metadata: UniversalModMetadata;
  compatibilityStatus: ModCompatibilityStatus;
  compatibilityDetails?: string;
  missingDependencies?: string[];
  conflictingMods?: string[];
  dateAdded: string;
}

export interface ModpackRecord {
  id: string;
  name: string;
  description: string;
  minecraftVersion: string;
  loader: MinecraftLoaderType;
  loaderVersion?: string;
  created: string;
  lastModified: string;
  instanceId?: string;
  isDefault?: boolean;
  modsCount: number;
  compatibilityStatus: ModCompatibilityStatus;
  modFiles: string[];
}

export interface ModValidationResult {
  valid: boolean;
  fileName: string;
  sha256: string;
  fileSize: number;
  metadata: UniversalModMetadata;
  compatibilityStatus: ModCompatibilityStatus;
  compatibilityDetails: string;
  missingDependencies: string[];
  conflictingMods: string[];
  isDuplicate?: boolean;
  duplicateFileName?: string;
  error?: string;
}

export interface ModpackValidationSummary {
  valid: boolean;
  criticalErrors: string[];
  warnings: string[];
  totalActive: number;
  totalDisabled: number;
}

/**
 * Modrinth API Types (Etap 6B)
 */
export interface ModrinthSearchHit {
  project_id: string;
  project_type: 'mod' | 'modpack' | 'resourcepack' | 'shader';
  slug: string;
  author: string;
  title: string;
  description: string;
  categories: string[];
  display_categories: string[];
  versions: string[];
  downloads: number;
  follows: number;
  icon_url: string | null;
  date_created: string;
  date_modified: string;
  latest_version?: string;
  license: string;
  client_side: 'required' | 'optional' | 'unsupported' | 'unknown';
  server_side: 'required' | 'optional' | 'unsupported' | 'unknown';
  gallery: string[];
}

export interface ModrinthSearchResult {
  hits: ModrinthSearchHit[];
  offset: number;
  limit: number;
  total_hits: number;
}

export interface ModrinthGalleryImage {
  url: string;
  featured: boolean;
  title?: string;
  description?: string;
  created?: string;
}

export interface ModrinthLicense {
  id: string;
  name: string;
  url?: string | null;
}

export interface ModrinthProject {
  id: string;
  slug: string;
  project_type: string;
  team: string;
  author?: string;
  title: string;
  description: string;
  body: string;
  published: string;
  updated: string;
  approved?: string;
  status: string;
  downloads: number;
  followers: number;
  categories: string[];
  additional_categories?: string[];
  loaders: string[];
  versions: string[];
  icon_url: string | null;
  issues_url?: string | null;
  source_url?: string | null;
  wiki_url?: string | null;
  discord_url?: string | null;
  donation_urls?: Array<{ id: string; platform: string; url: string }>;
  gallery: ModrinthGalleryImage[];
  license: ModrinthLicense;
  client_side: string;
  server_side: string;
}

export interface ModrinthVersionFileHashes {
  sha512: string;
  sha1: string;
}

export interface ModrinthVersionFile {
  hashes: ModrinthVersionFileHashes;
  url: string;
  filename: string;
  primary: boolean;
  size: number;
  file_type?: string | null;
}

export interface ModrinthDependency {
  version_id: string | null;
  project_id: string | null;
  file_name: string | null;
  dependency_type: 'required' | 'optional' | 'incompatible' | 'embedded';
}

export interface ModrinthVersion {
  id: string;
  project_id: string;
  author_id: string;
  featured?: boolean;
  name: string;
  version_number: string;
  changelog?: string | null;
  dependencies: ModrinthDependency[];
  game_versions: string[];
  version_type: 'release' | 'beta' | 'alpha';
  loaders: string[];
  status?: string;
  requested_status?: string;
  date_published: string;
  downloads: number;
  files: ModrinthVersionFile[];
}

export interface ModrinthCategoryTag {
  icon: string;
  name: string;
  project_type: string;
  header: string;
}

export interface ModrinthLoaderTag {
  icon: string;
  name: string;
  supported_project_types: string[];
}

export interface ModrinthGameVersionTag {
  version: string;
  version_type: string;
  date: string;
  major: boolean;
}

export interface ModrinthSearchParams {
  query?: string;
  loader?: string;
  gameVersion?: string;
  category?: string;
  environment?: 'client' | 'server' | 'both';
  versionType?: 'release' | 'beta' | 'alpha';
  index?: 'relevance' | 'downloads' | 'follows' | 'newest' | 'updated';
  offset?: number;
  limit?: number;
}

export interface ModrinthInstallOptions {
  versionId: string;
  projectId?: string;
  instanceId: string;
  targetFileName?: string;
  overwrite?: boolean;
  overwriteDuplicate?: boolean;
}

export interface ModrinthInstallResult {
  success: boolean;
  fileName?: string;
  filePath?: string;
  modRecord?: ModRecord;
  missingDependencies?: string[];
  error?: string;
}

// ============================================================================
// MODPACK DEPENDENCY RESOLUTION & AUTO-UPDATES (ETAP 6C)
// ============================================================================

export type ModUpdatePolicy = 'stable-only' | 'stable-and-beta' | 'all-versions';

export type ModUpdateStatus =
  | 'available'
  | 'up-to-date'
  | 'pinned'
  | 'manual-action'
  | 'conflict';

export interface ModUpdateInfo {
  fileName: string;
  modName: string;
  currentVersion: string;
  projectId?: string;
  currentVersionId?: string;
  targetVersion: ModrinthVersion;
  updateType: 'release' | 'beta' | 'alpha';
  isPinned: boolean;
  updatePolicy: ModUpdatePolicy;
  sizeDiffBytes: number;
  currentSizeBytes: number;
  targetSizeBytes: number;
  publishedAt: string;
  changelog?: string | null;
  dependencies: ModrinthDependency[];
  status: ModUpdateStatus;
  statusReason?: string;
}

export interface ModDependencyPlanItem {
  projectId: string;
  version: ModrinthVersion;
  requiredBy: string[];
  dependencyType: 'required' | 'optional' | 'incompatible' | 'embedded';
  reason: string;
}

export interface ModDependencyRecommendation {
  projectId: string;
  versionId?: string | null;
  recommendedBy: string;
  title?: string;
}

export interface ModConflictInfo {
  modA: string;
  modB: string;
  conflictType:
    | 'incompatible'
    | 'version-mismatch'
    | 'pinned-version'
    | 'loader-mismatch'
    | 'mc-version-mismatch';
  description: string;
  suggestedResolution?: string;
}

export interface ModDependencyResolutionPlan {
  targetVersionId?: string;
  targetModName?: string;
  toInstall: ModDependencyPlanItem[];
  alreadySatisfied: Array<{ projectId: string; satisfiedBy: string; isEmbedded?: boolean }>;
  optionalRecommendations: ModDependencyRecommendation[];
  conflicts: ModConflictInfo[];
  manualActionRequired: Array<{ projectId: string; reason: string }>;
  canProceed: boolean;
}

export interface ModpackLockfileMod {
  fileName: string;
  name: string;
  version: string;
  projectId?: string;
  versionId?: string;
  sha512?: string;
  sha1?: string;
  source: 'modrinth' | 'local';
  pinned: boolean;
  updatePolicy?: ModUpdatePolicy;
  enabled: boolean;
  dependencies: string[];
  installedAt: string;
}

export interface ModpackLockfile {
  schemaVersion: number;
  instanceId: string;
  gameVersion: string;
  loader: {
    id: MinecraftLoaderType;
    version?: string;
  };
  createdAt: string;
  updatedAt: string;
  mods: ModpackLockfileMod[];
}

export interface ModpackBackupInfo {
  id: string;
  instanceId: string;
  timestamp: string;
  description: string;
  filesCount: number;
  totalSizeBytes: number;
  modNames: string[];
  lockfileSnapshot?: ModpackLockfile;
}

export interface TransactionProgressInfo {
  stage:
    | 'checking-game'
    | 'resolving-deps'
    | 'downloading'
    | 'verifying'
    | 'backing-up'
    | 'applying'
    | 'updating-lockfile'
    | 'completed'
    | 'rolling-back'
    | 'failed';
  message: string;
  currentStep: number;
  totalSteps: number;
  downloadProgress?: {
    downloadedBytes: number;
    totalBytes: number;
    percent: number;
    speedMb: number;
    filename: string;
  };
}

export interface ModUpdateCheckResult {
  updates: ModUpdateInfo[];
  upToDateCount: number;
  pinnedCount: number;
  manualCount: number;
  conflictsCount: number;
}

export interface ModTransactionApplyOptions {
  instanceId: string;
  updates: Array<{ versionId: string; fileName?: string }>;
  createBackup?: boolean;
  backupDescription?: string;
}

export interface ModTransactionResult {
  success: boolean;
  appliedCount?: number;
  backupId?: string;
  error?: string;
  rolledBack?: boolean;
  rollbackError?: string;
}

// ============================================================================
// MODRINTH MODPACK (.mrpack) SPECIFICATION (ETAP 6D)
// ============================================================================

export interface MrpackIndexFileHashes {
  sha1: string;
  sha512: string;
}

export interface MrpackIndexFileEnv {
  client?: 'required' | 'optional' | 'unsupported';
  server?: 'required' | 'optional' | 'unsupported';
}

export interface MrpackIndexFile {
  path: string;
  hashes: MrpackIndexFileHashes;
  env?: MrpackIndexFileEnv;
  downloads: string[];
  fileSize: number;
}

export interface MrpackDependencies {
  minecraft: string;
  'fabric-loader'?: string;
  forge?: string;
  neoforge?: string;
  'quilt-loader'?: string;
  [key: string]: string | undefined;
}

export interface MrpackIndex {
  formatVersion: number;
  game: 'minecraft';
  versionId: string;
  name: string;
  summary?: string;
  files: MrpackIndexFile[];
  dependencies: MrpackDependencies;
}

export interface MrpackInspectionFileItem {
  path: string;
  fileName: string;
  fileSize: number;
  envClient: 'required' | 'optional' | 'unsupported';
  isOptional: boolean;
  isUnsupported: boolean;
  primaryDownloadUrl: string;
  sha512: string;
  sha1: string;
}

export interface MrpackInspectionResult {
  valid: boolean;
  archivePath: string;
  index?: MrpackIndex;
  loaderType: MinecraftLoaderType;
  loaderVersion?: string;
  minecraftVersion: string;
  totalFilesCount: number;
  clientFilesCount: number;
  optionalFilesCount: number;
  serverOnlyFilesCount: number;
  overridesCount: number;
  clientOverridesCount: number;
  serverOverridesCount: number;
  totalDownloadSizeBytes: number;
  files: MrpackInspectionFileItem[];
  overridesPreview: string[];
  warnings: string[];
  errors: string[];
}

export interface MrpackImportOptions {
  archivePath: string;
  instanceName?: string;
  selectedOptionalPaths?: string[];
  targetInstanceId?: string;
}

export interface MrpackImportProgress {
  stage:
    | 'reading'
    | 'validating'
    | 'dependencies'
    | 'installing-engine'
    | 'downloading-files'
    | 'verifying-hashes'
    | 'applying-overrides'
    | 'generating-lockfile'
    | 'completed'
    | 'failed'
    | 'rollback';
  message: string;
  percent: number;
  currentFile?: string;
  completedFiles?: number;
  totalFiles?: number;
}

export interface MrpackImportResult {
  success: boolean;
  instanceId?: string;
  instanceName?: string;
  installedFilesCount?: number;
  appliedOverridesCount?: number;
  error?: string;
}

export interface MrpackExportOptions {
  instanceId: string;
  outputFilePath: string;
  name: string;
  summary?: string;
  versionId?: string;
  includeCustomJars?: boolean;
  selectedOverrides?: {
    config?: boolean;
    optionsTxt?: boolean;
    resourcepacks?: boolean;
    shaderpacks?: boolean;
    defaultconfigs?: boolean;
    scripts?: boolean;
  };
}

export interface MrpackExportResult {
  success: boolean;
  outputPath?: string;
  filesCount?: number;
  overridesCount?: number;
  totalSizeBytes?: number;
  customJarsCount?: number;
  warningMessages?: string[];
  error?: string;
}

// -------------------------------------------------------------
// ETAP 8D1: ZOMBEK PERFORMANCE PACK DOMAIN TYPES
// -------------------------------------------------------------

export type PerformanceProfileType = 'MAX_FPS' | 'BALANCED' | 'VISUAL_QUALITY';

export interface HardwareSpecs {
  cpuModel: string;
  cpuCores: number;
  cpuThreads: number;
  cpuSpeedMhz?: number;
  gpuModel: string;
  gpuDriverVersion?: string;
  totalRamMb: number;
  freeRamMb: number;
  recommendedRamMb: number;
  osName: string;
  osArch: string;
  javaVersion: string;
  javaPath: string;
}

export interface OptimizationModEntry {
  slug: string;
  name: string;
  description: string;
  category: 'rendering' | 'memory' | 'culling' | 'engine' | 'chunk';
  installed: boolean;
  active: boolean;
  version?: string;
  supportedLoaders: MinecraftLoaderType[];
  recommendedFor: PerformanceProfileType[];
  conflictsWith?: string[];
}

export interface PerformanceStatusInfo {
  currentProfile: PerformanceProfileType | 'CUSTOM';
  activePresetMods: OptimizationModEntry[];
  hardwareSpecs: HardwareSpecs;
  recommendedJvmArgs: string[];
  optionsApplied: boolean;
  hasBackup: boolean;
  lastBackupDate?: string;
}

export interface BenchmarkSummary {
  profileName: string;
  durationSeconds: number;
  sampleCount: number;
  avgFps: number;
  minFps: number;
  maxFps: number;
  onePercentLowFps: number;
  avgFrameTimeMs: number;
  peakFrameTimeMs: number;
  avgMemoryMb: number;
  peakMemoryMb: number;
  worldLoadTimeMs: number;
  completedAt: string;
}

export interface GraphicsPresetOptions {
  renderDistance: number;
  simulationDistance: number;
  graphicsMode: 'fast' | 'fancy' | 'fabulous';
  particles: 'minimal' | 'decreased' | 'all';
  clouds: boolean;
  entityDistance: number;
  entityCulling: boolean;
  framerateLimit: number;
  biomeBlend: number;
  enableVsync: boolean;
}

