import React, { createContext, useContext, useState, useEffect, useMemo, ReactNode } from 'react';
import type {
  MinecraftLoaderType,
  MojangVersionManifest,
  EngineState,
  EngineProgress,
  InstanceRecord,
  AccountRecord,
  MicrosoftAuthProgress,
  FabricLoaderVersion,
  ForgeVersionInfo,
  NeoForgeVersionInfo,
  QuiltLoaderVersion,
  ModRecord,
  ModpackRecord,
  ModValidationResult,
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
  MrpackInspectionResult,
  MrpackImportOptions,
  MrpackImportResult,
  MrpackExportOptions,
  MrpackExportResult,
  MrpackImportProgress
} from '@zombek/shared-types';

export type TabType =
  | 'home'
  | 'play'
  | 'versions'
  | 'modpacks'
  | 'mods'
  | 'performance'
  | 'cosmetics'
  | 'accounts'
  | 'settings'
  | 'logs';

export type ModCategory = 'pvp' | 'hud' | 'visual' | 'survival' | 'other';

export interface ModSettings {
  scale: number;
  position: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right' | 'center';
  bgColor: string;
  textColor: string;
  pressedColor: string;
  border: boolean;
  shadow: boolean;
  opacity: number;
}

export interface ModItem {
  id: string;
  name: string;
  category: ModCategory;
  description: string;
  enabled: boolean;
  author: string;
  badge: 'UI PREVIEW' | 'DEMO' | 'STABLE';
  settings: ModSettings;
}

export interface Installation {
  id: string;
  name: string;
  minecraftVersion: string;
  loader: MinecraftLoaderType;
  loaderVersion: string;
  icon: string;
  created: string;
  lastPlayed: string;
  allocatedRam: number;
  customJvmArgs: string;
  assignedAccountId?: string;
}

export interface UserAccount {
  id: string;
  username: string;
  type: 'microsoft' | 'offline';
  avatar: string;
  uuid: string;
  lastUsed: string;
  hasEntitlement?: boolean;
  entitlements?: string[];
  sessionStatus?: 'valid' | 'expired' | 'requires_login' | 'unauthorized';
  isDefault?: boolean;
}

export interface CosmeticItem {
  id: string;
  name: string;
  type: 'skin' | 'cape' | 'accessory' | 'rank';
  previewColor: string;
  pattern: string;
  rarity: 'Common' | 'Rare' | 'Epic' | 'Legendary' | 'Festive';
  description: string;
}

export interface ModpackItem {
  id: string;
  name: string;
  minecraftVersion: string;
  loader: MinecraftLoaderType;
  modCount: number;
  status: 'Ready' | 'Update Available' | 'Custom';
  description: string;
}

export interface LogEntry {
  id: string;
  level: 'INFO' | 'WARNING' | 'ERROR' | 'DEBUG';
  timestamp: string;
  category: string;
  message: string;
}

export interface ToastMessage {
  id: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  message?: string;
}

export interface LauncherSettingsState {
  // Launcher
  language: 'pl' | 'en';
  startWithSystem: boolean;
  notifications: boolean;
  minimizeToTray: boolean;
  // Game
  minRamMb: number;
  maxRamMb: number;
  resolutionWidth: number;
  resolutionHeight: number;
  fullscreen: boolean;
  jvmArgs: string;
  gameDirectory: string;
  // Java
  autoDetectJava: boolean;
  customJavaPath: string;
  detectedJavaVersion: string;
  // Downloads
  concurrentDownloads: number;
  downloadSpeedLimitMb: number;
  diskCache: boolean;
  // Appearance
  theme: 'festive-dark' | 'midnight-pine' | 'pure-dark';
  snowfall: boolean;
  snowfallIntensity: 'light' | 'medium' | 'heavy';
  uiScale: number;
  reducedMotion: boolean;
}

interface LauncherContextType {
  // Navigation
  currentTab: TabType;
  setCurrentTab: (tab: TabType) => void;

  // Installations
  installations: Installation[];
  activeInstallation: Installation;
  setActiveInstallationId: (id: string) => void;
  addInstallation: (inst: Omit<Installation, 'id' | 'created' | 'lastPlayed'>) => Promise<void>;
  deleteInstallation: (id: string) => Promise<void>;
  setInstallationAccount: (installationId: string, accountId: string | undefined) => Promise<void>;

  // Accounts
  accounts: UserAccount[];
  activeAccount: UserAccount;
  setActiveAccountId: (id: string) => Promise<void>;
  addOfflineAccount: (username: string) => Promise<boolean>;
  loginMicrosoft: () => Promise<{ success: boolean; account?: AccountRecord; error?: string }>;
  refreshAccountSession: (accountId: string) => Promise<boolean>;
  removeAccount: (id: string) => Promise<boolean>;
  authProgress: MicrosoftAuthProgress | null;

  // Mods
  mods: ModItem[];
  toggleMod: (id: string) => void;
  updateModSettings: (id: string, settings: Partial<ModSettings>) => void;
  resetModSettings: (id: string) => void;

  // Real Mods (Etap 6A)
  realMods: ModRecord[];
  isLoadingMods: boolean;
  loadModsForInstance: (instanceId: string) => Promise<ModRecord[]>;
  validateModFile: (filePath: string, instanceId: string) => Promise<ModValidationResult>;
  installModFile: (filePath: string, instanceId: string, overwrite?: boolean) => Promise<{ success: boolean; mod?: ModRecord; error?: string }>;
  toggleRealMod: (instanceId: string, fileName: string, enabled: boolean) => Promise<boolean>;
  deleteRealMod: (instanceId: string, fileName: string) => Promise<boolean>;
  openModFileDialog: () => Promise<string[] | null>;
  openModsFolder: (instanceId: string) => Promise<void>;

  // Modpacks
  modpacks: ModpackItem[];
  addModpack: (pack: Omit<ModpackItem, 'id'>) => void;
  deleteModpack: (id: string) => void;
  duplicateModpack: (id: string) => void;

  // Real Modpacks (Etap 6A)
  realModpacks: ModpackRecord[];
  isLoadingModpacks: boolean;
  loadModpacks: () => Promise<ModpackRecord[]>;
  createRealModpack: (data: any) => Promise<ModpackRecord>;
  updateRealModpack: (id: string, updates: any) => Promise<ModpackRecord>;
  duplicateRealModpack: (id: string, newName?: string) => Promise<ModpackRecord>;
  deleteRealModpack: (id: string) => Promise<boolean>;
  applyModpackToInstance: (modpackId: string, instanceId: string) => Promise<boolean>;

  // Modrinth Integration (Etap 6B)
  searchModrinth: (params: ModrinthSearchParams) => Promise<ModrinthSearchResult>;
  getModrinthProject: (projectIdOrSlug: string) => Promise<ModrinthProject>;
  getModrinthProjectVersions: (
    projectIdOrSlug: string,
    loaders?: string[],
    gameVersions?: string[],
    includeChangelog?: boolean
  ) => Promise<ModrinthVersion[]>;
  getModrinthVersion: (versionId: string) => Promise<ModrinthVersion>;
  getModrinthCategories: () => Promise<ModrinthCategoryTag[]>;
  getModrinthLoaders: () => Promise<ModrinthLoaderTag[]>;
  getModrinthGameVersions: () => Promise<ModrinthGameVersionTag[]>;
  installModrinthVersion: (options: ModrinthInstallOptions) => Promise<ModrinthInstallResult>;

  // Modpack Auto-Updates, Dependency Resolver & Transactions (Etap 6C)
  transactionProgress: TransactionProgressInfo | null;
  checkModUpdates: (instanceId: string, forceRefresh?: boolean) => Promise<ModUpdateCheckResult>;
  resolveModDependencies: (instanceId: string, versionId: string) => Promise<ModDependencyResolutionPlan>;
  pinMod: (instanceId: string, fileName: string, pinned: boolean) => Promise<boolean>;
  setModUpdatePolicy: (instanceId: string, fileName: string, policy: ModUpdatePolicy) => Promise<boolean>;
  applyModTransaction: (options: ModTransactionApplyOptions) => Promise<ModTransactionResult>;
  getModBackups: (instanceId: string) => Promise<ModpackBackupInfo[]>;
  rollbackModBackup: (instanceId: string, backupId: string) => Promise<{ success: boolean; restoredCount?: number; error?: string }>;
  getModLockfile: (instanceId: string) => Promise<ModpackLockfile | null>;

  // Modrinth Modpack format (.mrpack) (Etap 6D)
  mrpackProgress: MrpackImportProgress | null;
  mrpackInspect: (archivePath: string) => Promise<MrpackInspectionResult>;
  mrpackImport: (options: MrpackImportOptions) => Promise<MrpackImportResult>;
  mrpackExport: (options: MrpackExportOptions) => Promise<MrpackExportResult>;
  mrpackOpenFileDialog: () => Promise<string | null>;
  mrpackOpenSaveDialog: (defaultFileName: string) => Promise<string | null>;

  // Cosmetics
  cosmetics: CosmeticItem[];
  equippedCosmetics: {
    skinId: string;
    capeId: string;
    accessoryId: string;
    rankId: string;
  };
  equipCosmetic: (type: 'skin' | 'cape' | 'accessory' | 'rank', id: string) => void;

  // Settings
  settings: LauncherSettingsState;
  updateSettings: (updates: Partial<LauncherSettingsState>) => void;

  // Logs
  logs: LogEntry[];
  addLog: (level: LogEntry['level'], category: string, message: string) => void;
  clearLogs: () => void;

  // Toasts
  toasts: ToastMessage[];
  addToast: (toast: Omit<ToastMessage, 'id'>) => void;
  removeToast: (id: string) => void;

  // Engine & Launch state
  isLaunching: boolean;
  isRunning: boolean;
  launchStatusText: string;
  engineState: EngineState;
  engineProgress: EngineProgress | null;
  mojangManifest: MojangVersionManifest | null;
  isDesktopReady: boolean;
  launchGame: (instanceId?: string) => Promise<void>;
  cancelLaunch: () => Promise<void>;
  simulatePlay: () => void;
  refreshVersionsManifest: (force?: boolean) => Promise<void>;
  detectSystemJava: () => Promise<void>;
  getFabricLoaderVersions: (gameVersion: string) => Promise<FabricLoaderVersion[]>;
  installFabricLoader: (instanceId: string, gameVersion: string, loaderVersion: string) => Promise<boolean>;
  getForgeVersions: (gameVersion: string) => Promise<ForgeVersionInfo[]>;
  installForgeLoader: (instanceId: string, gameVersion: string, forgeVersion: string) => Promise<boolean>;
  getNeoForgeVersions: (gameVersion: string) => Promise<NeoForgeVersionInfo[]>;
  installNeoForgeLoader: (instanceId: string, gameVersion: string, neoForgeVersion: string) => Promise<boolean>;
  getQuiltLoaderVersions: (gameVersion: string) => Promise<QuiltLoaderVersion[]>;
  installQuiltLoader: (instanceId: string, gameVersion: string, loaderVersion: string) => Promise<boolean>;
}

const DEFAULT_MOD_SETTINGS: ModSettings = {
  scale: 100,
  position: 'top-left',
  bgColor: '#141c2e',
  textColor: '#f8fafc',
  pressedColor: '#10b981',
  border: true,
  shadow: true,
  opacity: 85,
};

const INITIAL_INSTALLATIONS: Installation[] = [
  {
    id: 'inst-fabric-1',
    name: 'ZombekClient Fabric 1.20.4 (Zalecany)',
    minecraftVersion: '1.20.4',
    loader: 'fabric',
    loaderVersion: '0.16.10',
    icon: 'gem',
    created: '2026-11-01',
    lastPlayed: 'Dzisiaj, 16:30',
    allocatedRam: 4096,
    customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
  },
  {
    id: 'inst-quilt-1',
    name: 'ZombekClient Quilt 1.20.4',
    minecraftVersion: '1.20.4',
    loader: 'quilt',
    loaderVersion: '0.27.1-beta.1',
    icon: 'sparkles',
    created: '2026-11-01',
    lastPlayed: 'Wczoraj, 18:20',
    allocatedRam: 4096,
    customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
  },
  {
    id: 'inst-forge-1',
    name: 'ZombekClient Forge 1.20.4',
    minecraftVersion: '1.20.4',
    loader: 'forge',
    loaderVersion: '49.1.0',
    icon: 'shield',
    created: '2026-11-01',
    lastPlayed: 'Wczoraj, 19:15',
    allocatedRam: 4096,
    customJvmArgs: '-XX:+UseG1GC',
  },
  {
    id: 'inst-neoforge-1',
    name: 'ZombekClient NeoForge 1.20.4',
    minecraftVersion: '1.20.4',
    loader: 'neoforge',
    loaderVersion: '20.4.80-beta',
    icon: 'shield',
    created: '2026-11-01',
    lastPlayed: '2 dni temu',
    allocatedRam: 4096,
    customJvmArgs: '-XX:+UseG1GC',
  },
  {
    id: 'inst-vanilla-1',
    name: 'Vanilla 1.20.4 (Oficjalny)',
    minecraftVersion: '1.20.4',
    loader: 'vanilla',
    loaderVersion: '1.20.4',
    icon: 'sword',
    created: '2026-10-25',
    lastPlayed: '3 dni temu',
    allocatedRam: 4096,
    customJvmArgs: '-XX:+UseG1GC',
  },
  {
    id: 'inst-vanilla-2',
    name: 'Vanilla 1.21.1 (Nowe Wydanie)',
    minecraftVersion: '1.21.1',
    loader: 'vanilla',
    loaderVersion: '1.21.1',
    icon: 'tree',
    created: '2026-11-01',
    lastPlayed: 'Nigdy',
    allocatedRam: 4096,
    customJvmArgs: '-XX:+UseG1GC',
  },
  {
    id: 'inst-vanilla-3',
    name: 'Vanilla 1.8.9 (Klasyk PvP)',
    minecraftVersion: '1.8.9',
    loader: 'vanilla',
    loaderVersion: '1.8.9',
    icon: 'bed',
    created: '2026-10-20',
    lastPlayed: '4 dni temu',
    allocatedRam: 3072,
    customJvmArgs: '-Xmx3G -Xms2G',
  },
];

const INITIAL_ACCOUNTS: UserAccount[] = [
  {
    id: 'acc-offline-default',
    username: 'ZombekPlayer',
    type: 'offline',
    avatar: 'https://mc-heads.net/avatar/ZombekPlayer/100',
    uuid: '00000000-0000-3000-8000-000000000001',
    lastUsed: 'Aktywne teraz',
    hasEntitlement: false,
    sessionStatus: 'valid',
    isDefault: true,
  },
];

const INITIAL_MODS: ModItem[] = [
  {
    id: 'mod-keystrokes',
    name: 'Keystrokes HUD',
    category: 'hud',
    description: 'Wyświetla klawisze WASD, LPM/PPM, Spację oraz CPS w eleganckim, konfigurowalnym panelu.',
    enabled: true,
    author: 'Zombek Team',
    badge: 'UI PREVIEW',
    settings: { ...DEFAULT_MOD_SETTINGS, position: 'top-left' },
  },
  {
    id: 'mod-cps',
    name: 'CPS Counter',
    category: 'hud',
    description: 'Licznik kliknięć na sekundę dla lewego i prawego przycisku myszy z wykrywaniem double-clicków.',
    enabled: true,
    author: 'Zombek Team',
    badge: 'UI PREVIEW',
    settings: { ...DEFAULT_MOD_SETTINGS, position: 'top-left', scale: 90 },
  },
  {
    id: 'mod-fps',
    name: 'FPS Display',
    category: 'hud',
    description: 'Płynny wskaźnik klatek na sekundę z kolorowaniem w zależności od stabilności renderowania.',
    enabled: true,
    author: 'Zombek Team',
    badge: 'UI PREVIEW',
    settings: { ...DEFAULT_MOD_SETTINGS, position: 'top-right', scale: 85 },
  },
  {
    id: 'mod-armorhud',
    name: 'Armor HUD & Durability',
    category: 'hud',
    description: 'Wskaźnik stanu zbroi, trzymanych przedmiotów oraz ich punktów wytrzymałości w czasie rzeczywistym.',
    enabled: true,
    author: 'Zombek Team',
    badge: 'UI PREVIEW',
    settings: { ...DEFAULT_MOD_SETTINGS, position: 'bottom-right' },
  },
  {
    id: 'mod-crosshair',
    name: 'Custom Crosshair',
    category: 'visual',
    description: 'Zaawansowany edytor celownika: kropka, krzyżyk, dynamiczny odrzut, tęczowe kolory i obrysy.',
    enabled: false,
    author: 'Zombek Team',
    badge: 'UI PREVIEW',
    settings: { ...DEFAULT_MOD_SETTINGS, position: 'center' },
  },
  {
    id: 'mod-motionblur',
    name: 'Motion Blur',
    category: 'visual',
    description: 'Efekt rozmycia ruchu kamery zwiększający płynność animacji przy wysokich klatkach.',
    enabled: false,
    author: 'Zombek Team',
    badge: 'UI PREVIEW',
    settings: { ...DEFAULT_MOD_SETTINGS, opacity: 50 },
  },
  {
    id: 'mod-autogg',
    name: 'AutoGG & AutoTip',
    category: 'pvp',
    description: 'Automatycznie wysyła wiadomość "gg" po zakończeniu rozgrywki na serwerach Hypixel i innych.',
    enabled: true,
    author: 'Zombek Community',
    badge: 'UI PREVIEW',
    settings: { ...DEFAULT_MOD_SETTINGS },
  },
  {
    id: 'mod-togglesprint',
    name: 'ToggleSprint & Sneak',
    category: 'pvp',
    description: 'Wygodne przełączanie sprintu i skradania z konfigurowalnym wskaźnikiem statusu na ekranie.',
    enabled: true,
    author: 'Zombek Team',
    badge: 'UI PREVIEW',
    settings: { ...DEFAULT_MOD_SETTINGS, position: 'bottom-left' },
  },
];

const INITIAL_MODPACKS: ModpackItem[] = [
  {
    id: 'pack-pvp-winter',
    name: 'Zombek PvP Christmas Edition',
    minecraftVersion: '1.20.4',
    loader: 'fabric',
    modCount: 24,
    status: 'Ready',
    description: 'Świąteczna paczka turniejowa z pełnym zestawem modów optymalizacyjnych i HUD PvP.',
  },
  {
    id: 'pack-hypixel-legacy',
    name: 'Classic Hypixel 1.8.9 Bedwars',
    minecraftVersion: '1.8.9',
    loader: 'forge',
    modCount: 16,
    status: 'Ready',
    description: 'Legendarny zestaw modów pod potyczki Bedwars i SkyWars z anty-ghost blokami i płynną myszą.',
  },
];

const INITIAL_COSMETICS: CosmeticItem[] = [
  {
    id: 'skin-zambek-sweater',
    name: 'Ząbek w Świątecznym Swetrze',
    type: 'skin',
    previewColor: '#10b981',
    pattern: 'Pixel-art Ząbek w czerwono-zielonym sweterku',
    rarity: 'Festive',
    description: 'Oficjalny świąteczny skin maskotki Ząbek dla użytkowników ZombekClient.',
  },
  {
    id: 'cape-zambek-santa',
    name: 'Peleryna Świąteczny Ząbek 2026',
    type: 'cape',
    previewColor: '#dc2626',
    pattern: 'Aksamitna czerwień z białym kożuszkiem i złotym logo',
    rarity: 'Legendary',
    description: 'Ekskluzywna animowana peleryna zimowa widoczna dla wszystkich graczy ZombekClient.',
  },
  {
    id: 'acc-santa-hat',
    name: 'Czapka Mikołaja 3D',
    type: 'accessory',
    previewColor: '#ef4444',
    pattern: 'Trójwymiarowa puszysta czapka z pomponem',
    rarity: 'Epic',
    description: 'Dodatek kosmetyczny 3D umieszczany nad głową Twojej postaci w grze.',
  },
  {
    id: 'rank-vip-festive',
    name: 'Ranga Winter VIP',
    type: 'rank',
    previewColor: '#fbbf24',
    pattern: 'Złoty lodowy prefiks [ZOMB-VIP] na liście graczy',
    rarity: 'Legendary',
    description: 'Wyróżniający prefiks czatu i świecący badge nad nickiem na serwerach partnerskich.',
  },
];

const INITIAL_LOGS: LogEntry[] = [
  {
    id: 'log-1',
    level: 'INFO',
    timestamp: '17:00:01',
    category: 'BOOT',
    message: 'Inicjalizacja środowiska ZombekClient Launcher v0.1.0-alpha...',
  },
  {
    id: 'log-2',
    level: 'INFO',
    timestamp: '17:00:02',
    category: 'ENGINE',
    message: 'Silnik Minecraft Java Edition gotowy do pracy.',
  },
];

const DEFAULT_SETTINGS: LauncherSettingsState = {
  language: 'pl',
  startWithSystem: false,
  notifications: true,
  minimizeToTray: true,
  minRamMb: 1024,
  maxRamMb: 4096,
  resolutionWidth: 1280,
  resolutionHeight: 720,
  fullscreen: false,
  jvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
  gameDirectory: '%APPDATA%\\.zombekclient',
  autoDetectJava: true,
  customJavaPath: '',
  detectedJavaVersion: 'Wykrywanie...',
  concurrentDownloads: 8,
  downloadSpeedLimitMb: 0,
  diskCache: true,
  theme: 'festive-dark',
  snowfall: true,
  snowfallIntensity: 'medium',
  uiScale: 100,
  reducedMotion: false,
};

const LauncherContext = createContext<LauncherContextType | undefined>(undefined);

export const LauncherProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [currentTab, setCurrentTab] = useState<TabType>('home');
  const [installations, setInstallations] = useState<Installation[]>(INITIAL_INSTALLATIONS);
  const [activeInstallationId, setActiveInstallationId] = useState<string>(INITIAL_INSTALLATIONS[0].id);
  const [accounts, setAccounts] = useState<UserAccount[]>(INITIAL_ACCOUNTS);
  const [activeAccountId, setActiveAccountIdState] = useState<string>('acc-offline-default');
  const [authProgress, setAuthProgress] = useState<MicrosoftAuthProgress | null>(null);

  const [mods, setMods] = useState<ModItem[]>(INITIAL_MODS);
  const [modpacks, setModpacks] = useState<ModpackItem[]>(INITIAL_MODPACKS);
  const [realMods, setRealMods] = useState<ModRecord[]>([]);
  const [isLoadingMods, setIsLoadingMods] = useState<boolean>(false);
  const [realModpacks, setRealModpacks] = useState<ModpackRecord[]>([]);
  const [isLoadingModpacks, setIsLoadingModpacks] = useState<boolean>(false);
  const [transactionProgress, setTransactionProgress] = useState<TransactionProgressInfo | null>(null);
  const [mrpackProgress, setMrpackProgress] = useState<MrpackImportProgress | null>(null);
  const [cosmetics] = useState<CosmeticItem[]>(INITIAL_COSMETICS);
  const [equippedCosmetics, setEquippedCosmetics] = useState({
    skinId: 'skin-zambek-sweater',
    capeId: 'cape-zambek-santa',
    accessoryId: 'acc-santa-hat',
    rankId: 'rank-vip-festive',
  });
  const [settings, setSettings] = useState<LauncherSettingsState>(DEFAULT_SETTINGS);
  const [logs, setLogs] = useState<LogEntry[]>(INITIAL_LOGS);
  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  // Engine state
  const [isLaunching, setIsLaunching] = useState<boolean>(false);
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [launchStatusText, setLaunchStatusText] = useState<string>('Gotowy do gry');
  const [engineState, setEngineState] = useState<EngineState>('idle');
  const [engineProgress, setEngineProgress] = useState<EngineProgress | null>(null);
  const [mojangManifest, setMojangManifest] = useState<MojangVersionManifest | null>(null);
  const [isDesktopReady, setIsDesktopReady] = useState<boolean>(false);

  const activeInstallation = useMemo(() => {
    return installations.find((i) => i.id === activeInstallationId) || installations[0];
  }, [installations, activeInstallationId]);

  const activeAccount = useMemo(() => {
    return accounts.find((a) => a.id === activeAccountId) || accounts[0];
  }, [accounts, activeAccountId]);

  const addToast = (toast: Omit<ToastMessage, 'id'>) => {
    const id = `toast-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`;
    setToasts((prev) => [...prev, { ...toast, id }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4500);
  };

  const removeToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  const addLog = (level: LogEntry['level'], category: string, message: string) => {
    const now = new Date();
    const timeStr = now.toTimeString().split(' ')[0];
    const newLog: LogEntry = {
      id: `log-${Date.now()}-${Math.random().toString(36).substring(2, 5)}`,
      level,
      timestamp: timeStr,
      category,
      message,
    };
    setLogs((prev) => [...prev, newLog]);
  };

  const clearLogs = () => {
    setLogs([]);
    addToast({ type: 'info', title: 'Wyczyszczono listę logów' });
  };

  // Connect Electron IPC when desktop API is available
  useEffect(() => {
    if (typeof window === 'undefined' || !window.zombekDesktopApi) {
      return;
    }

    setIsDesktopReady(true);
    const api = window.zombekDesktopApi;

    // 1. Fetch Mojang manifest
    api
      .getVersionsManifest()
      .then((manifest) => {
        setMojangManifest(manifest);
        addLog(
          'INFO',
          'ENGINE',
          `Załadowano oficjalny manifest wersji Mojang: ${manifest.versions.length} wydań (najnowsze: ${manifest.latest.release})`
        );
      })
      .catch((err) => {
        addLog('WARNING', 'ENGINE', `Błąd pobierania manifestu Mojang: ${err.message}`);
      });

    // 2. Fetch stored instances
    api
      .getInstances()
      .then((instances) => {
        if (instances && instances.length > 0) {
          const mapped: Installation[] = instances.map((rec) => ({
            id: rec.id,
            name: rec.name,
            minecraftVersion: rec.minecraftVersion,
            loader: rec.loader,
            loaderVersion: rec.loaderVersion || '1.0.0',
            icon: rec.loader === 'fabric' ? 'gem' : rec.loader === 'forge' ? 'shield' : 'sword',
            created: rec.created ? rec.created.substring(0, 10) : 'Dzisiaj',
            lastPlayed: rec.lastPlayed ? new Date(rec.lastPlayed).toLocaleString('pl-PL') : 'Nigdy',
            allocatedRam: rec.allocatedRamMb || 4096,
            customJvmArgs: rec.customJvmArgs || '',
            assignedAccountId: rec.assignedAccountId,
          }));
          setInstallations(mapped);
          setActiveInstallationId(mapped[0].id);
          addLog('INFO', 'ENGINE', `Wczytano ${mapped.length} zainstalowanych profili z pamięci dyskowej.`);
        }
      })
      .catch((err) => {
        addLog('WARNING', 'ENGINE', `Błąd odczytu instancji: ${err.message}`);
      });

    // 3. Fetch accounts
    api
      .getAccounts()
      .then((accs) => {
        if (accs && accs.length > 0) {
          const mapped: UserAccount[] = accs.map((a) => ({
            id: a.id,
            username: a.username,
            type: a.type,
            avatar: a.avatar,
            uuid: a.uuid,
            lastUsed: a.lastUsed ? new Date(a.lastUsed).toLocaleString('pl-PL') : 'Aktywne teraz',
            hasEntitlement: a.hasEntitlement,
            entitlements: a.entitlements,
            sessionStatus: a.sessionStatus,
            isDefault: a.isDefault,
          }));
          setAccounts(mapped);

          const defaultAcc = mapped.find((a) => a.isDefault) || mapped[0];
          setActiveAccountIdState(defaultAcc.id);
          addLog('INFO', 'AUTH', `Wczytano ${mapped.length} kont graczy (aktywny: ${defaultAcc.username}).`);
        }
      })
      .catch((err) => {
        addLog('WARNING', 'AUTH', `Błąd wczytywania kont: ${err.message}`);
      });

    // 4. Detect Java
    api
      .detectJava()
      .then((res) => {
        if (res.valid && res.version) {
          setSettings((prev) => ({
            ...prev,
            detectedJavaVersion: `Java ${res.version} (${res.path})`,
          }));
          addLog('INFO', 'JAVA', `Wykryto środowisko systemowe Java: ${res.version}`);
        } else {
          setSettings((prev) => ({
            ...prev,
            detectedJavaVersion: 'Brak w PATH (zostanie pobrana automatycznie)',
          }));
        }
      })
      .catch(() => {});

    // 5. Load Modpacks (Etap 6A)
    api
      .getModpacks()
      .then((packs: ModpackRecord[]) => {
        setRealModpacks(packs);
        addLog('INFO', 'MODPACKS', `Wczytano ${packs.length} profili modpacków.`);
      })
      .catch((err: any) => {
        addLog('WARNING', 'MODPACKS', `Błąd wczytywania modpacków: ${err.message}`);
      });

    // 6. Listen to Engine events
    const unsubProgress = api.onEngineProgress((progress) => {
      setEngineProgress(progress);
      setLaunchStatusText(
        progress.currentFile
          ? `${progress.message || 'Pobieranie'} [${progress.percent}%]`
          : `Postęp: ${progress.percent}%`
      );
    });

    const unsubStatus = api.onEngineStatus((status) => {
      setEngineState(status.state);
      if (status.message) {
        setLaunchStatusText(status.message);
      }

      if (status.state === 'running') {
        setIsLaunching(false);
        setIsRunning(true);
        addToast({ type: 'success', title: 'Minecraft uruchomiony', message: status.message });
      } else if (status.state === 'completed') {
        setIsLaunching(false);
        setIsRunning(false);
        setEngineProgress(null);
        addToast({ type: 'info', title: 'Minecraft zakończony', message: status.message });
      } else if (status.state === 'failed') {
        setIsLaunching(false);
        setIsRunning(false);
        setEngineProgress(null);
        addToast({ type: 'error', title: 'Błąd silnika gry', message: status.message });
      } else if (status.state === 'cancelled') {
        setIsLaunching(false);
        setIsRunning(false);
        setEngineProgress(null);
        addToast({ type: 'warning', title: 'Anulowano', message: status.message });
      } else if (['preparing', 'downloading', 'verifying', 'preparing-java', 'launching'].includes(status.state)) {
        setIsLaunching(true);
        setIsRunning(false);
      }
    });

    const unsubLog = api.onEngineLog((log) => {
      addLog(log.level, log.category, log.message);
    });

    const unsubAuth = api.onAuthProgress((p) => {
      setAuthProgress(p);
      if (p.message) {
        addLog('INFO', 'AUTH', `[Microsoft OAuth] ${p.message}`);
      }
    });

    const unsubTx = api.onTransactionProgress?.((prog) => {
      setTransactionProgress(prog);
      if (prog.stage === 'completed' || prog.stage === 'failed') {
        setTimeout(() => setTransactionProgress(null), 3500);
      }
    });

    const unsubMrpack = api.onMrpackProgress?.((prog) => {
      setMrpackProgress(prog);
      if (prog.stage === 'completed' || prog.stage === 'failed') {
        setTimeout(() => setMrpackProgress(null), 3500);
      }
    });

    return () => {
      unsubProgress();
      unsubStatus();
      unsubLog();
      unsubAuth();
      unsubTx?.();
      unsubMrpack?.();
    };
  }, []);

  const refreshVersionsManifest = async (force: boolean = true) => {
    if (window.zombekDesktopApi) {
      addToast({ type: 'info', title: 'Odświeżanie', message: 'Pobieranie najnowszego manifestu Mojang...' });
      try {
        const manifest = await window.zombekDesktopApi.getVersionsManifest(force);
        setMojangManifest(manifest);
        addToast({ type: 'success', title: 'Zaktualizowano', message: `Wczytano ${manifest.versions.length} wersji gry.` });
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd manifestu', message: err.message });
      }
    }
  };

  const detectSystemJava = async () => {
    if (window.zombekDesktopApi) {
      try {
        const res = await window.zombekDesktopApi.detectJava(settings.customJavaPath || undefined);
        if (res.valid) {
          setSettings((prev) => ({
            ...prev,
            detectedJavaVersion: `Java ${res.version} (${res.path})`,
          }));
          addToast({ type: 'success', title: 'Wykryto Javę', message: `Wersja: ${res.version}` });
        } else {
          addToast({ type: 'warning', title: 'Brak Javy', message: res.error || 'Nie znaleziono środowiska Java.' });
        }
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd detekcji Java', message: err.message });
      }
    }
  };

  const getFabricLoaderVersions = async (gameVersion: string): Promise<FabricLoaderVersion[]> => {
    if (window.zombekDesktopApi) {
      try {
        return await window.zombekDesktopApi.getFabricLoaderVersions(gameVersion);
      } catch (err: any) {
        addLog('WARNING', 'FABRIC', `Błąd pobierania wersji loadera dla ${gameVersion}: ${err.message}`);
      }
    }
    return [
      { version: '0.16.10', stable: true, maven: 'net.fabricmc:fabric-loader:0.16.10', build: 10, separator: '.' },
      { version: '0.15.11', stable: true, maven: 'net.fabricmc:fabric-loader:0.15.11', build: 11, separator: '.' }
    ];
  };

  const installFabricLoader = async (
    instanceId: string,
    gameVersion: string,
    loaderVersion: string
  ): Promise<boolean> => {
    if (window.zombekDesktopApi) {
      try {
        addToast({
          type: 'info',
          title: 'Instalacja Fabric',
          message: `Konfigurowanie Fabric ${loaderVersion} dla profilu...`,
        });
        const res = await window.zombekDesktopApi.installFabric(instanceId, gameVersion, loaderVersion);
        if (res.success) {
          addToast({
            type: 'success',
            title: 'Fabric gotowy',
            message: `Pomyślnie skonfigurowano Fabric Loader ${loaderVersion}.`,
          });
          return true;
        } else {
          addToast({
            type: 'error',
            title: 'Błąd instalacji Fabric',
            message: res.error || 'Nieznany błąd podczas instalacji Fabric.',
          });
          return false;
        }
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd instalacji Fabric', message: err.message });
        return false;
      }
    }
    return true;
  };

  const getForgeVersions = async (gameVersion: string): Promise<ForgeVersionInfo[]> => {
    if (window.zombekDesktopApi) {
      try {
        return await window.zombekDesktopApi.getForgeVersions(gameVersion);
      } catch (err: any) {
        addLog('WARNING', 'FORGE', `Błąd pobierania wersji Forge dla ${gameVersion}: ${err.message}`);
      }
    }
    return [
      {
        version: `${gameVersion}-49.2.13`,
        rawVersion: '49.2.13',
        minecraftVersion: gameVersion,
        isRecommended: true,
        isLatest: true,
        installerUrl: `https://maven.minecraftforge.net/net/minecraftforge/forge/${gameVersion}-49.2.13/forge-${gameVersion}-49.2.13-installer.jar`,
      },
    ];
  };

  const installForgeLoader = async (
    instanceId: string,
    gameVersion: string,
    forgeVersion: string
  ): Promise<boolean> => {
    if (window.zombekDesktopApi) {
      try {
        addToast({
          type: 'info',
          title: 'Instalacja Forge',
          message: `Konfigurowanie Minecraft Forge ${forgeVersion}...`,
        });
        const res = await window.zombekDesktopApi.installForge(instanceId, gameVersion, forgeVersion);
        if (res.success) {
          addToast({
            type: 'success',
            title: 'Forge gotowy',
            message: `Pomyślnie zainstalowano Forge ${forgeVersion}.`,
          });
          return true;
        } else {
          addToast({
            type: 'error',
            title: 'Błąd instalacji Forge',
            message: res.error || 'Nieznany błąd podczas instalacji Forge.',
          });
          return false;
        }
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd instalacji Forge', message: err.message });
        return false;
      }
    }
    return true;
  };

  const getNeoForgeVersions = async (gameVersion: string): Promise<NeoForgeVersionInfo[]> => {
    if (window.zombekDesktopApi) {
      try {
        return await window.zombekDesktopApi.getNeoForgeVersions(gameVersion);
      } catch (err: any) {
        addLog('WARNING', 'NEOFORGE', `Błąd pobierania wersji NeoForge dla ${gameVersion}: ${err.message}`);
      }
    }
    return [
      {
        version: '20.4.237',
        rawVersion: '20.4.237',
        minecraftVersion: gameVersion,
        artifactCoordinates: 'net.neoforged:neoforge',
        generation: 'modern',
        isRecommended: true,
        isLatest: true,
        installerUrl: `https://maven.neoforged.net/releases/net/neoforged/neoforge/20.4.237/neoforge-20.4.237-installer.jar`,
      },
    ];
  };

  const installNeoForgeLoader = async (
    instanceId: string,
    gameVersion: string,
    neoForgeVersion: string
  ): Promise<boolean> => {
    if (window.zombekDesktopApi) {
      try {
        addToast({
          type: 'info',
          title: 'Instalacja NeoForge',
          message: `Konfigurowanie Minecraft NeoForge ${neoForgeVersion}...`,
        });
        const res = await window.zombekDesktopApi.installNeoForge(instanceId, gameVersion, neoForgeVersion);
        if (res.success) {
          addToast({
            type: 'success',
            title: 'NeoForge gotowy',
            message: `Pomyślnie zainstalowano NeoForge ${neoForgeVersion}.`,
          });
          return true;
        } else {
          addToast({
            type: 'error',
            title: 'Błąd instalacji NeoForge',
            message: res.error || 'Nieznany błąd podczas instalacji NeoForge.',
          });
          return false;
        }
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd instalacji NeoForge', message: err.message });
        return false;
      }
    }
    return true;
  };

  const getQuiltLoaderVersions = async (gameVersion: string): Promise<QuiltLoaderVersion[]> => {
    if (window.zombekDesktopApi) {
      try {
        return await window.zombekDesktopApi.getQuiltLoaderVersions(gameVersion);
      } catch (err: any) {
        addLog('WARNING', 'QUILT', `Błąd pobierania wersji Quilt Loader dla ${gameVersion}: ${err.message}`);
      }
    }
    return [
      {
        version: '0.27.1-beta.1',
        build: 1,
        maven: 'org.quiltmc:quilt-loader:0.27.1-beta.1',
        separator: '.',
        stable: true,
      },
    ];
  };

  const installQuiltLoader = async (
    instanceId: string,
    gameVersion: string,
    loaderVersion: string
  ): Promise<boolean> => {
    if (window.zombekDesktopApi) {
      try {
        addToast({
          type: 'info',
          title: 'Instalacja Quilt',
          message: `Konfigurowanie Minecraft Quilt Loader ${loaderVersion}...`,
        });
        const res = await window.zombekDesktopApi.installQuilt(instanceId, gameVersion, loaderVersion);
        if (res.success) {
          addToast({
            type: 'success',
            title: 'Quilt gotowy',
            message: `Pomyślnie zainstalowano Quilt ${loaderVersion}.`,
          });
          return true;
        } else {
          addToast({
            type: 'error',
            title: 'Błąd instalacji Quilt',
            message: res.error || 'Nieznany błąd podczas instalacji Quilt.',
          });
          return false;
        }
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd instalacji Quilt', message: err.message });
        return false;
      }
    }
    return true;
  };

  const addInstallation = async (inst: Omit<Installation, 'id' | 'created' | 'lastPlayed'>) => {
    if (window.zombekDesktopApi) {
      try {
        const created = await window.zombekDesktopApi.createInstance({
          name: inst.name,
          minecraftVersion: inst.minecraftVersion,
          loader: inst.loader,
          loaderVersion: inst.loaderVersion,
          allocatedRamMb: inst.allocatedRam,
          customJvmArgs: inst.customJvmArgs,
          resolutionWidth: settings.resolutionWidth,
          resolutionHeight: settings.resolutionHeight,
          fullscreen: settings.fullscreen,
          assignedAccountId: inst.assignedAccountId,
        });

        const newInst: Installation = {
          ...inst,
          id: created.id,
          created: created.created ? created.created.substring(0, 10) : 'Dzisiaj',
          lastPlayed: 'Nigdy',
        };

        setInstallations((prev) => [newInst, ...prev]);
        setActiveInstallationId(newInst.id);
        addLog('INFO', 'INSTANCES', `Utworzono profil instalacji w katalogu: ${newInst.name}`);

        // Prepare loader profile immediately if modded
        if (inst.loader === 'fabric') {
          await installFabricLoader(created.id, inst.minecraftVersion, inst.loaderVersion || '0.16.10');
        } else if (inst.loader === 'forge') {
          await installForgeLoader(created.id, inst.minecraftVersion, inst.loaderVersion || '49.1.0');
        } else if (inst.loader === 'neoforge') {
          await installNeoForgeLoader(created.id, inst.minecraftVersion, inst.loaderVersion || '20.4.80-beta');
        } else if (inst.loader === 'quilt') {
          await installQuiltLoader(created.id, inst.minecraftVersion, inst.loaderVersion || '0.27.1-beta.1');
        }

        addToast({
          type: 'success',
          title: 'Instalacja utworzona',
          message: `Dodano ${inst.name} (${inst.minecraftVersion})`,
        });
        return;
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd tworzenia profilu', message: err.message });
        return;
      }
    }

    const id = `inst-${Date.now()}`;
    const newInst: Installation = {
      ...inst,
      id,
      created: 'Dzisiaj',
      lastPlayed: 'Nigdy',
    };
    setInstallations((prev) => [newInst, ...prev]);
    setActiveInstallationId(id);
    addToast({
      type: 'success',
      title: 'Instalacja utworzona (UI)',
      message: `Pomyślnie dodano instalację ${inst.name}`,
    });
  };

  const setInstallationAccount = async (installationId: string, accountId: string | undefined) => {
    if (window.zombekDesktopApi) {
      try {
        await window.zombekDesktopApi.updateInstance(installationId, { assignedAccountId: accountId });
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd zapisu konta dla instalacji', message: err.message });
      }
    }

    setInstallations((prev) =>
      prev.map((inst) => {
        if (inst.id === installationId) {
          return { ...inst, assignedAccountId: accountId };
        }
        return inst;
      })
    );
  };

  const deleteInstallation = async (id: string) => {
    if (installations.length <= 1) {
      addToast({ type: 'warning', title: 'Nie można usunąć', message: 'Wymagana jest co najmniej jedna instalacja.' });
      return;
    }

    const target = installations.find((i) => i.id === id);
    if (window.zombekDesktopApi) {
      try {
        await window.zombekDesktopApi.deleteInstance(id);
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd usuwania', message: err.message });
        return;
      }
    }

    setInstallations((prev) => prev.filter((i) => i.id !== id));
    if (activeInstallationId === id) {
      const remaining = installations.filter((i) => i.id !== id);
      setActiveInstallationId(remaining[0].id);
    }
    addLog('INFO', 'INSTANCES', `Usunięto profil: "${target?.name}"`);
    addToast({ type: 'info', title: 'Instalacja usunięta', message: `Usunięto ${target?.name}` });
  };

  // Accounts Management
  const setActiveAccountId = async (id: string) => {
    if (window.zombekDesktopApi) {
      await window.zombekDesktopApi.setActiveAccount(id);
    }
    setActiveAccountIdState(id);
    const target = accounts.find((a) => a.id === id);
    if (target) {
      addLog('INFO', 'AUTH', `Aktywowano konto gracza: ${target.username} (${target.type})`);
      addToast({ type: 'info', title: 'Zmieniono aktywne konto', message: target.username });
    }
  };

  const addOfflineAccount = async (username: string): Promise<boolean> => {
    if (window.zombekDesktopApi) {
      try {
        const created = await window.zombekDesktopApi.addOfflineAccount(username);
        const mapped: UserAccount = {
          id: created.id,
          username: created.username,
          type: created.type,
          avatar: created.avatar,
          uuid: created.uuid,
          lastUsed: 'Aktywne teraz',
          hasEntitlement: false,
          sessionStatus: 'valid',
          isDefault: true,
        };
        setAccounts((prev) => [mapped, ...prev.filter((a) => a.id !== mapped.id)]);
        setActiveAccountIdState(mapped.id);
        addToast({ type: 'success', title: 'Dodano profil Offline', message: `Nick: ${created.username}` });
        return true;
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd dodawania profilu', message: err.message });
        return false;
      }
    }

    const id = `acc-offline-${Date.now()}`;
    const newAcc: UserAccount = {
      id,
      username,
      type: 'offline',
      avatar: `https://mc-heads.net/avatar/${username}/100`,
      uuid: 'offline-' + Math.random().toString(36).substring(2, 10),
      lastUsed: 'Aktywne teraz',
      hasEntitlement: false,
      sessionStatus: 'valid',
      isDefault: true,
    };
    setAccounts((prev) => [newAcc, ...prev]);
    setActiveAccountIdState(id);
    addToast({ type: 'success', title: 'Profil dodany (UI)', message: username });
    return true;
  };

  const loginMicrosoft = async (): Promise<{ success: boolean; account?: AccountRecord; error?: string }> => {
    if (!window.zombekDesktopApi) {
      addToast({ type: 'warning', title: 'Brak API', message: 'Logowanie Microsoft wymaga aplikacji Electron.' });
      return { success: false, error: 'Brak środowiska Electron' };
    }

    addToast({ type: 'info', title: 'Microsoft OAuth', message: 'Otwieranie przeglądarki systemowej...' });
    try {
      const res = await window.zombekDesktopApi.loginMicrosoft();
      if (res.success && res.account) {
        const created = res.account;
        const mapped: UserAccount = {
          id: created.id,
          username: created.username,
          type: created.type,
          avatar: created.avatar,
          uuid: created.uuid,
          lastUsed: 'Aktywne teraz',
          hasEntitlement: created.hasEntitlement,
          entitlements: created.entitlements,
          sessionStatus: created.sessionStatus,
          isDefault: true,
        };
        setAccounts((prev) => [mapped, ...prev.filter((a) => a.id !== mapped.id)]);
        setActiveAccountIdState(mapped.id);
        addToast({
          type: 'success',
          title: 'Zalogowano pomyślnie!',
          message: `Konto Microsoft: ${created.username}`,
        });
        return { success: true, account: created };
      } else {
        addToast({
          type: 'error',
          title: 'Błąd logowania Microsoft',
          message: res.error || 'Nie udało się ukończyć autoryzacji.',
        });
        return { success: false, error: res.error };
      }
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd logowania', message: err.message });
      return { success: false, error: err.message };
    }
  };

  const refreshAccountSession = async (accountId: string): Promise<boolean> => {
    if (!window.zombekDesktopApi) return false;

    addToast({ type: 'info', title: 'Odświeżanie sesji', message: 'Weryfikacja tokenów Microsoft/Xbox Live...' });
    try {
      const res = await window.zombekDesktopApi.refreshAccountSession(accountId);
      if (res.success) {
        setAccounts((prev) =>
          prev.map((a) => (a.id === accountId ? { ...a, sessionStatus: 'valid' } : a))
        );
        addToast({ type: 'success', title: 'Sesja odświeżona', message: 'Tokeny są aktualne i gotowe do gry.' });
        return true;
      } else {
        setAccounts((prev) =>
          prev.map((a) => (a.id === accountId ? { ...a, sessionStatus: 'requires_login' } : a))
        );
        addToast({ type: 'warning', title: 'Wymagane logowanie', message: res.error || 'Sesja wygasła.' });
        return false;
      }
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd odświeżania sesji', message: err.message });
      return false;
    }
  };

  const removeAccount = async (id: string): Promise<boolean> => {
    if (accounts.length <= 1) {
      addToast({ type: 'warning', title: 'Nie można usunąć', message: 'Musisz mieć co najmniej jedno konto w launcherze.' });
      return false;
    }

    const target = accounts.find((a) => a.id === id);
    if (window.zombekDesktopApi) {
      try {
        await window.zombekDesktopApi.removeAccount(id);
      } catch (err: any) {
        addToast({ type: 'error', title: 'Błąd usuwania konta', message: err.message });
        return false;
      }
    }

    setAccounts((prev) => prev.filter((a) => a.id !== id));
    if (activeAccountId === id) {
      const remaining = accounts.filter((a) => a.id !== id);
      setActiveAccountIdState(remaining[0].id);
    }
    addLog('INFO', 'AUTH', `Usunięto konto: "${target?.username}"`);
    addToast({ type: 'info', title: 'Konto usunięte', message: `Wylogowano ${target?.username}` });
    return true;
  };

  const launchGame = async (targetInstanceId?: string) => {
    const instId = targetInstanceId || activeInstallation.id;
    const currentInst = installations.find((i) => i.id === instId) || activeInstallation;
    const effectiveAccount =
      (currentInst.assignedAccountId
        ? accounts.find((a) => a.id === currentInst.assignedAccountId)
        : activeAccount) || activeAccount;

    if (window.zombekDesktopApi) {
      setIsLaunching(true);
      setLaunchStatusText('Przygotowywanie uruchomienia gry...');
      addLog(
        'INFO',
        'LAUNCHER',
        `[PLAY] Rozpoczynanie startu profilu: ${instId} (gracz: ${effectiveAccount.username}, typ: ${effectiveAccount.type})`
      );

      try {
        const res = await window.zombekDesktopApi.launchGame(instId, {
          accountId: effectiveAccount.id,
          offlineUsername: effectiveAccount.username,
        });
        if (!res.success && res.error) {
          setIsLaunching(false);
          addToast({ type: 'error', title: 'Błąd uruchamiania', message: res.error });
        }
      } catch (err: any) {
        setIsLaunching(false);
        addToast({ type: 'error', title: 'Błąd launchera', message: err.message });
      }
    } else {
      simulatePlay();
    }
  };

  const cancelLaunch = async () => {
    if (window.zombekDesktopApi) {
      await window.zombekDesktopApi.cancelGameLaunch();
    }
    setIsLaunching(false);
    setIsRunning(false);
    setEngineProgress(null);
    setLaunchStatusText('Przerwano przez użytkownika.');
  };

  const simulatePlay = () => {
    setIsLaunching(true);
    setLaunchStatusText('Weryfikacja środowiska (DEMO)...');
    addLog('INFO', 'LAUNCHER', `[PLAY] Żądanie uruchomienia dla profilu: ${activeInstallation.name}`);

    setTimeout(() => {
      setLaunchStatusText('Przygotowywanie plików (DEMO)...');
    }, 800);

    setTimeout(() => {
      setLaunchStatusText('Działasz w trybie aplikacji desktopowej Electron.');
      setIsLaunching(false);
      addToast({
        type: 'info',
        title: 'Komunikat Silnika',
        message: 'Uruchom aplikację przez Electron (pnpm run electron:dev), aby wystartować prawdziwego Minecrafta.',
      });
    }, 2000);
  };

  const toggleMod = (id: string) => {
    setMods((prev) =>
      prev.map((mod) => {
        if (mod.id === id) {
          const next = !mod.enabled;
          addLog('DEBUG', 'MODS', `Przełączono moduł ${mod.name}: ${next ? 'WŁĄCZONY' : 'WYŁĄCZONY'}`);
          addToast({
            type: next ? 'success' : 'info',
            title: mod.name,
            message: next ? 'Moduł został aktywowany (Preview)' : 'Moduł został wyłączony',
          });
          return { ...mod, enabled: next };
        }
        return mod;
      })
    );
  };

  const updateModSettings = (id: string, newSettings: Partial<ModSettings>) => {
    setMods((prev) =>
      prev.map((mod) => {
        if (mod.id === id) {
          return { ...mod, settings: { ...mod.settings, ...newSettings } };
        }
        return mod;
      })
    );
  };

  const resetModSettings = (id: string) => {
    setMods((prev) =>
      prev.map((mod) => {
        if (mod.id === id) {
          return { ...mod, settings: { ...DEFAULT_MOD_SETTINGS } };
        }
        return mod;
      })
    );
    addToast({ type: 'info', title: 'Zresetowano ustawienia modułu' });
  };

  const addModpack = (pack: Omit<ModpackItem, 'id'>) => {
    const id = `pack-${Date.now()}`;
    const newPack: ModpackItem = { ...pack, id };
    setModpacks((prev) => [newPack, ...prev]);
    addLog('INFO', 'MODPACKS', `Utworzono modpack: "${pack.name}"`);
    addToast({ type: 'success', title: 'Modpack utworzony', message: pack.name });
  };

  const deleteModpack = (id: string) => {
    const target = modpacks.find((p) => p.id === id);
    setModpacks((prev) => prev.filter((p) => p.id !== id));
    addToast({ type: 'info', title: 'Modpack usunięty', message: target?.name });
  };

  const duplicateModpack = (id: string) => {
    const target = modpacks.find((p) => p.id === id);
    if (!target) return;
    const copy: ModpackItem = {
      ...target,
      id: `pack-${Date.now()}`,
      name: `${target.name} (Kopia)`,
    };
    setModpacks((prev) => [copy, ...prev]);
    addToast({ type: 'success', title: 'Zduplikowano paczkę', message: copy.name });
  };

  const equipCosmetic = (type: 'skin' | 'cape' | 'accessory' | 'rank', id: string) => {
    setEquippedCosmetics((prev) => ({
      ...prev,
      [`${type}Id`]: id,
    }));
    const item = cosmetics.find((c) => c.id === id);
    addToast({
      type: 'success',
      title: 'Zmieniono kosmetyk',
      message: `Wyposażono: ${item?.name || id}`,
    });
    addLog('INFO', 'COSMETICS', `Wyposażono przedmiot kosmetyczny [${type}]: ${item?.name}`);
  };

  // Load real mods when active installation changes
  useEffect(() => {
    if (activeInstallationId && window.zombekDesktopApi) {
      loadModsForInstance(activeInstallationId).catch(() => {});
    }
  }, [activeInstallationId, isDesktopReady]);

  const loadModsForInstance = async (instanceId: string): Promise<ModRecord[]> => {
    if (!window.zombekDesktopApi) return [];
    setIsLoadingMods(true);
    try {
      const records = await window.zombekDesktopApi.getModsForInstance(instanceId);
      setRealMods(records);
      return records;
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd wczytywania modów', message: err.message });
      return [];
    } finally {
      setIsLoadingMods(false);
    }
  };

  const validateModFile = async (filePath: string, instanceId: string): Promise<ModValidationResult> => {
    if (!window.zombekDesktopApi) {
      return {
        valid: false,
        fileName: filePath,
        fileSize: 0,
        sha256: '',
        compatibilityStatus: 'Broken File',
        compatibilityDetails: 'Brak połączenia z silnikiem desktopowym',
        missingDependencies: [],
        conflictingMods: [],
        metadata: {
          id: 'unknown',
          name: filePath,
          version: '0.0.0',
          description: '',
          authors: [],
          loader: 'unknown',
          dependencies: [],
          rawSource: 'unknown',
        },
        error: 'Brak API desktopowego',
      };
    }
    return await window.zombekDesktopApi.validateModFile(filePath, instanceId);
  };

  const installModFile = async (
    filePath: string,
    instanceId: string,
    overwrite?: boolean
  ): Promise<{ success: boolean; mod?: ModRecord; error?: string }> => {
    if (!window.zombekDesktopApi) return { success: false, error: 'Desktop API niedostępne' };
    try {
      const res = await window.zombekDesktopApi.installModFile(filePath, instanceId, overwrite);
      if (res.success && res.mod) {
        addToast({
          type: 'success',
          title: 'Zainstalowano mod',
          message: `${res.mod.metadata.name} (${res.mod.metadata.version})`,
        });
        addLog('INFO', 'MODS', `Zainstalowano mod ${res.mod.metadata.name} (${res.mod.fileName}) dla instalacji ${instanceId}`);
        await loadModsForInstance(instanceId);
      } else {
        addToast({
          type: 'error',
          title: 'Nie udało się zainstalować moda',
          message: res.error || 'Nieznany błąd',
        });
      }
      return res;
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd instalacji moda', message: err.message });
      return { success: false, error: err.message };
    }
  };

  const toggleRealMod = async (instanceId: string, fileName: string, enabled: boolean): Promise<boolean> => {
    if (!window.zombekDesktopApi) return false;
    try {
      const res = await window.zombekDesktopApi.toggleMod(instanceId, fileName, enabled);
      if (res.success) {
        await loadModsForInstance(instanceId);
        addToast({
          type: 'info',
          title: enabled ? 'Mod włączony' : 'Mod wyłączony',
          message: fileName,
        });
      }
      return res.success;
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd zmiany stanu moda', message: err.message });
      return false;
    }
  };

  const deleteRealMod = async (instanceId: string, fileName: string): Promise<boolean> => {
    if (!window.zombekDesktopApi) return false;
    try {
      const res = await window.zombekDesktopApi.deleteMod(instanceId, fileName);
      if (res.success) {
        setRealMods((prev) => prev.filter((m) => m.fileName !== fileName));
        addToast({ type: 'warning', title: 'Usunięto mod', message: fileName });
        addLog('INFO', 'MODS', `Usunięto mod ${fileName} z instalacji ${instanceId}`);
      }
      return res.success;
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd usuwania moda', message: err.message });
      return false;
    }
  };

  const openModFileDialog = async (): Promise<string[] | null> => {
    if (!window.zombekDesktopApi) return null;
    return await window.zombekDesktopApi.openModFileDialog();
  };

  const openModsFolder = async (instanceId: string): Promise<void> => {
    if (!window.zombekDesktopApi) return;
    await window.zombekDesktopApi.openModsFolder(instanceId);
  };

  const loadModpacks = async (): Promise<ModpackRecord[]> => {
    if (!window.zombekDesktopApi) return [];
    setIsLoadingModpacks(true);
    try {
      const packs = await window.zombekDesktopApi.getModpacks();
      setRealModpacks(packs);
      return packs;
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd wczytywania paczek', message: err.message });
      return [];
    } finally {
      setIsLoadingModpacks(false);
    }
  };

  const createRealModpack = async (data: any): Promise<ModpackRecord> => {
    if (!window.zombekDesktopApi) throw new Error('Desktop API niedostępne');
    const created = await window.zombekDesktopApi.createModpack(data);
    await loadModpacks();
    addToast({ type: 'success', title: 'Utworzono modpack', message: created.name });
    addLog('INFO', 'MODPACKS', `Utworzono profil modpacka: ${created.name}`);
    return created;
  };

  const updateRealModpack = async (id: string, updates: any): Promise<ModpackRecord> => {
    if (!window.zombekDesktopApi) throw new Error('Desktop API niedostępne');
    const updated = await window.zombekDesktopApi.updateModpack(id, updates);
    await loadModpacks();
    addToast({ type: 'success', title: 'Zaktualizowano profil', message: updated.name });
    return updated;
  };

  const duplicateRealModpack = async (id: string, newName?: string): Promise<ModpackRecord> => {
    if (!window.zombekDesktopApi) throw new Error('Desktop API niedostępne');
    const dup = await window.zombekDesktopApi.duplicateModpack(id, newName);
    await loadModpacks();
    addToast({ type: 'success', title: 'Zduplikowano paczkę', message: dup.name });
    addLog('INFO', 'MODPACKS', `Zduplikowano profil paczki: ${dup.name}`);
    return dup;
  };

  const deleteRealModpack = async (id: string): Promise<boolean> => {
    if (!window.zombekDesktopApi) return false;
    const res = await window.zombekDesktopApi.deleteModpack(id);
    if (res.success) {
      setRealModpacks((prev) => prev.filter((p) => p.id !== id));
      addToast({ type: 'warning', title: 'Usunięto modpack' });
    }
    return res.success;
  };

  const applyModpackToInstance = async (modpackId: string, instanceId: string): Promise<boolean> => {
    if (!window.zombekDesktopApi) return false;
    try {
      const res = await window.zombekDesktopApi.applyModpackToInstance(modpackId, instanceId);
      if (res.success) {
        addToast({ type: 'success', title: 'Zastosowano paczkę w instalacji', message: `ID: ${modpackId}` });
        await loadModsForInstance(instanceId);
      }
      return res.success;
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd przypisywania paczki', message: err.message });
      return false;
    }
  };

  // Modrinth API Methods (Etap 6B)
  const searchModrinth = async (params: ModrinthSearchParams): Promise<ModrinthSearchResult> => {
    if (!window.zombekDesktopApi) {
      return { hits: [], offset: 0, limit: 20, total_hits: 0 };
    }
    return window.zombekDesktopApi.modrinthSearch(params);
  };

  const getModrinthProject = async (projectIdOrSlug: string): Promise<ModrinthProject> => {
    if (!window.zombekDesktopApi) throw new Error('Desktop API niedostępne');
    return window.zombekDesktopApi.modrinthGetProject(projectIdOrSlug);
  };

  const getModrinthProjectVersions = async (
    projectIdOrSlug: string,
    loaders?: string[],
    gameVersions?: string[],
    includeChangelog?: boolean
  ): Promise<ModrinthVersion[]> => {
    if (!window.zombekDesktopApi) return [];
    return window.zombekDesktopApi.modrinthGetProjectVersions(
      projectIdOrSlug,
      loaders,
      gameVersions,
      includeChangelog
    );
  };

  const getModrinthVersion = async (versionId: string): Promise<ModrinthVersion> => {
    if (!window.zombekDesktopApi) throw new Error('Desktop API niedostępne');
    return window.zombekDesktopApi.modrinthGetVersion(versionId);
  };

  const getModrinthCategories = async (): Promise<ModrinthCategoryTag[]> => {
    if (!window.zombekDesktopApi) return [];
    return window.zombekDesktopApi.modrinthGetCategories();
  };

  const getModrinthLoaders = async (): Promise<ModrinthLoaderTag[]> => {
    if (!window.zombekDesktopApi) return [];
    return window.zombekDesktopApi.modrinthGetLoaders();
  };

  const getModrinthGameVersions = async (): Promise<ModrinthGameVersionTag[]> => {
    if (!window.zombekDesktopApi) return [];
    return window.zombekDesktopApi.modrinthGetGameVersions();
  };

  const installModrinthVersion = async (
    options: ModrinthInstallOptions
  ): Promise<ModrinthInstallResult> => {
    if (!window.zombekDesktopApi) {
      return { success: false, error: 'Desktop API niedostępne' };
    }
    try {
      const res = await window.zombekDesktopApi.modrinthInstallVersion(options);
      if (res.success && res.modRecord) {
        addToast({
          type: 'success',
          title: 'Zainstalowano mod z Modrinth',
          message: `${res.modRecord.metadata.name} (${res.fileName})`,
        });
        addLog(
          'INFO',
          'MODRINTH',
          `Zainstalowano mod ${res.modRecord.metadata.name} (wersja ${res.modRecord.metadata.version}) z Modrinth do instalacji ${options.instanceId}`
        );
        await loadModsForInstance(options.instanceId);
      } else if (!res.success) {
        addToast({
          type: 'error',
          title: 'Błąd instalacji z Modrinth',
          message: res.error || 'Nieznany błąd',
        });
      }
      return res;
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Błąd pobierania moda z Modrinth',
        message: err.message,
      });
      return { success: false, error: err.message };
    }
  };

  const checkModUpdates = async (instanceId: string, forceRefresh = false): Promise<ModUpdateCheckResult> => {
    if (!window.zombekDesktopApi?.modsCheckUpdates) {
      return { updates: [], upToDateCount: 0, pinnedCount: 0, manualCount: 0, conflictsCount: 0 };
    }
    return window.zombekDesktopApi.modsCheckUpdates(instanceId, forceRefresh);
  };

  const resolveModDependencies = async (instanceId: string, versionId: string): Promise<ModDependencyResolutionPlan> => {
    if (!window.zombekDesktopApi?.modsResolveDependencies) {
      return { toInstall: [], alreadySatisfied: [], optionalRecommendations: [], conflicts: [], manualActionRequired: [], canProceed: false };
    }
    return window.zombekDesktopApi.modsResolveDependencies(instanceId, versionId);
  };

  const pinMod = async (instanceId: string, fileName: string, pinned: boolean): Promise<boolean> => {
    if (!window.zombekDesktopApi?.modsPinMod) return false;
    const res = await window.zombekDesktopApi.modsPinMod(instanceId, fileName, pinned);
    if (res.success) {
      addToast({
        type: 'success',
        title: pinned ? 'Przypięto wersję moda' : 'Odpięto wersję moda',
        message: `${fileName}: ${pinned ? 'Zablokowano automatyczną aktualizację' : 'Odblokowano aktualizacje'}`,
      });
      await loadModsForInstance(instanceId);
    }
    return Boolean(res.success);
  };

  const setModUpdatePolicy = async (instanceId: string, fileName: string, policy: ModUpdatePolicy): Promise<boolean> => {
    if (!window.zombekDesktopApi?.modsSetUpdatePolicy) return false;
    const res = await window.zombekDesktopApi.modsSetUpdatePolicy(instanceId, fileName, policy);
    return Boolean(res.success);
  };

  const applyModTransaction = async (options: ModTransactionApplyOptions): Promise<ModTransactionResult> => {
    if (!window.zombekDesktopApi?.modsApplyTransaction) {
      return { success: false, error: 'Desktop API niedostępne' };
    }
    const res = await window.zombekDesktopApi.modsApplyTransaction(options);
    if (res.success) {
      addToast({
        type: 'success',
        title: 'Zastosowano aktualizacje',
        message: `Pomyślnie zaktualizowano ${res.appliedCount || 0} modów.`,
      });
      await loadModsForInstance(options.instanceId);
    } else {
      addToast({
        type: 'error',
        title: res.rolledBack ? 'Wycofano zmiany (Rollback)' : 'Błąd aktualizacji',
        message: res.error || 'Operacja nie powiodła się',
      });
    }
    return res;
  };

  const getModBackups = async (instanceId: string): Promise<ModpackBackupInfo[]> => {
    if (!window.zombekDesktopApi?.modsGetBackups) return [];
    return window.zombekDesktopApi.modsGetBackups(instanceId);
  };

  const rollbackModBackup = async (
    instanceId: string,
    backupId: string
  ): Promise<{ success: boolean; restoredCount?: number; error?: string }> => {
    if (!window.zombekDesktopApi?.modsRollbackBackup) {
      return { success: false, error: 'Desktop API niedostępne' };
    }
    const res = await window.zombekDesktopApi.modsRollbackBackup(instanceId, backupId);
    if (res.success) {
      addToast({
        type: 'success',
        title: 'Przywrócono kopię zapasową',
        message: `Pomyślnie przywrócono ${res.restoredCount || 0} plików modów.`,
      });
      await loadModsForInstance(instanceId);
    } else {
      addToast({
        type: 'error',
        title: 'Błąd przywracania kopii',
        message: res.error || 'Nieznany błąd',
      });
    }
    return res;
  };

  const getModLockfile = async (instanceId: string): Promise<ModpackLockfile | null> => {
    if (!window.zombekDesktopApi?.modsGetLockfile) return null;
    return window.zombekDesktopApi.modsGetLockfile(instanceId);
  };

  const mrpackInspect = async (archivePath: string): Promise<MrpackInspectionResult> => {
    if (!window.zombekDesktopApi?.mrpackInspect) {
      return {
        valid: false,
        archivePath,
        loaderType: 'vanilla',
        minecraftVersion: '',
        totalFilesCount: 0,
        clientFilesCount: 0,
        optionalFilesCount: 0,
        serverOnlyFilesCount: 0,
        overridesCount: 0,
        clientOverridesCount: 0,
        serverOverridesCount: 0,
        totalDownloadSizeBytes: 0,
        files: [],
        overridesPreview: [],
        warnings: ['Desktop API niedostępne'],
        errors: [],
      };
    }
    return window.zombekDesktopApi.mrpackInspect(archivePath);
  };

  const mrpackImport = async (options: MrpackImportOptions): Promise<MrpackImportResult> => {
    if (!window.zombekDesktopApi?.mrpackImport) {
      return { success: false, error: 'Desktop API niedostępne' };
    }
    addToast({
      type: 'info',
      title: 'Importowanie modpacka .mrpack',
      message: 'Przygotowywanie instalacji...',
    });
    const res = await window.zombekDesktopApi.mrpackImport(options);
    if (res.success && res.instanceId) {
      addToast({
        type: 'success',
        title: 'Zaimportowano modpack!',
        message: `Instalacja "${res.instanceName || 'Modpack'}" jest gotowa do gry.`,
      });
      // Refresh installations list
      const updatedInstances = await window.zombekDesktopApi.getInstances();
      const mapped: Installation[] = updatedInstances.map((rec) => ({
        id: rec.id,
        name: rec.name,
        minecraftVersion: rec.minecraftVersion,
        loader: rec.loader,
        loaderVersion: rec.loaderVersion || '1.0.0',
        icon: rec.loader === 'fabric' ? 'gem' : rec.loader === 'forge' ? 'shield' : 'sword',
        created: rec.created ? rec.created.substring(0, 10) : 'Dzisiaj',
        lastPlayed: rec.lastPlayed ? new Date(rec.lastPlayed).toLocaleString('pl-PL') : 'Nigdy',
        allocatedRam: rec.allocatedRamMb || 4096,
        customJvmArgs: rec.customJvmArgs || '-XX:+UseG1GC',
        assignedAccountId: rec.assignedAccountId,
      }));
      setInstallations(mapped);
      setActiveInstallationId(res.instanceId);
      addLog('INFO', 'MRPACK', `Pomyślnie zaimportowano modpack ${res.instanceName || res.instanceId}`);
    } else {
      addToast({
        type: 'error',
        title: 'Błąd importu .mrpack',
        message: res.error || 'Nieznany błąd podczas importu',
      });
      addLog('ERROR', 'MRPACK', `Błąd importu paczki: ${res.error}`);
    }
    return res;
  };

  const mrpackExport = async (options: MrpackExportOptions): Promise<MrpackExportResult> => {
    if (!window.zombekDesktopApi?.mrpackExport) {
      return { success: false, error: 'Desktop API niedostępne' };
    }
    addToast({
      type: 'info',
      title: 'Eksportowanie modpacka .mrpack',
      message: 'Pakowanie plików paczki...',
    });
    const res = await window.zombekDesktopApi.mrpackExport(options);
    if (res.success && res.outputPath) {
      addToast({
        type: 'success',
        title: 'Wyeksportowano paczkę .mrpack!',
        message: `Zapisano ${res.filesCount} modów i ${res.overridesCount} plików overrides.`,
      });
      addLog('INFO', 'MRPACK', `Wyeksportowano modpack do: ${res.outputPath}`);
    } else {
      addToast({
        type: 'error',
        title: 'Błąd eksportu .mrpack',
        message: res.error || 'Nieznany błąd podczas eksportu',
      });
      addLog('ERROR', 'MRPACK', `Błąd eksportu paczki: ${res.error}`);
    }
    return res;
  };

  const mrpackOpenFileDialog = async (): Promise<string | null> => {
    if (!window.zombekDesktopApi?.mrpackOpenFileDialog) return null;
    return window.zombekDesktopApi.mrpackOpenFileDialog();
  };

  const mrpackOpenSaveDialog = async (defaultFileName: string): Promise<string | null> => {
    if (!window.zombekDesktopApi?.mrpackOpenSaveDialog) return null;
    return window.zombekDesktopApi.mrpackOpenSaveDialog(defaultFileName);
  };

  const updateSettings = (updates: Partial<LauncherSettingsState>) => {
    setSettings((prev) => ({ ...prev, ...updates }));
    addToast({ type: 'success', title: 'Ustawienia zaktualizowane' });
  };

  return (
    <LauncherContext.Provider
      value={{
        currentTab,
        setCurrentTab,
        installations,
        activeInstallation,
        setActiveInstallationId,
        addInstallation,
        deleteInstallation,
        setInstallationAccount,
        accounts,
        activeAccount,
        setActiveAccountId,
        addOfflineAccount,
        loginMicrosoft,
        refreshAccountSession,
        removeAccount,
        authProgress,
        mods,
        toggleMod,
        updateModSettings,
        resetModSettings,
        realMods,
        isLoadingMods,
        loadModsForInstance,
        validateModFile,
        installModFile,
        toggleRealMod,
        deleteRealMod,
        openModFileDialog,
        openModsFolder,
        modpacks,
        addModpack,
        deleteModpack,
        duplicateModpack,
        realModpacks,
        isLoadingModpacks,
        loadModpacks,
        createRealModpack,
        updateRealModpack,
        duplicateRealModpack,
        deleteRealModpack,
        applyModpackToInstance,
        cosmetics,
        equippedCosmetics,
        equipCosmetic,
        settings,
        updateSettings,
        logs,
        addLog,
        clearLogs,
        toasts,
        addToast,
        removeToast,
        isLaunching,
        isRunning,
        launchStatusText,
        engineState,
        engineProgress,
        mojangManifest,
        isDesktopReady,
        launchGame,
        cancelLaunch,
        simulatePlay,
        refreshVersionsManifest,
        detectSystemJava,
        getFabricLoaderVersions,
        installFabricLoader,
        getForgeVersions,
        installForgeLoader,
        getNeoForgeVersions,
        installNeoForgeLoader,
        getQuiltLoaderVersions,
        installQuiltLoader,
        searchModrinth,
        getModrinthProject,
        getModrinthProjectVersions,
        getModrinthVersion,
        getModrinthCategories,
        getModrinthLoaders,
        getModrinthGameVersions,
        installModrinthVersion,
        transactionProgress,
        checkModUpdates,
        resolveModDependencies,
        pinMod,
        setModUpdatePolicy,
        applyModTransaction,
        getModBackups,
        rollbackModBackup,
        getModLockfile,
        mrpackProgress,
        mrpackInspect,
        mrpackImport,
        mrpackExport,
        mrpackOpenFileDialog,
        mrpackOpenSaveDialog,
      }}
    >
      {children}
    </LauncherContext.Provider>
  );
};

export function useLauncherContext(): LauncherContextType {
  const context = useContext(LauncherContext);
  if (!context) {
    throw new Error('useLauncherContext must be used within a LauncherProvider');
  }
  return context;
}
