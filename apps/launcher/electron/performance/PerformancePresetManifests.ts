import type {
  MinecraftLoaderType,
  PerformanceProfileType,
  OptimizationModEntry,
  GraphicsPresetOptions
} from '@zombek/shared-types';

export interface PerformancePresetDefinition {
  profile: PerformanceProfileType;
  name: string;
  description: string;
  targetFpsCategory: string;
  graphics: GraphicsPresetOptions;
  includedModSlugs: string[];
}

export const OPTIMIZATION_MODS_CATALOG: OptimizationModEntry[] = [
  {
    slug: 'sodium',
    name: 'Sodium',
    description: 'Zaawansowany silnik renderowania zastępujący standardowy renderer Minecrafta. Zwiększa FPS nawet o 200-400%.',
    category: 'rendering',
    installed: false,
    active: false,
    supportedLoaders: ['fabric', 'quilt', 'neoforge'],
    recommendedFor: ['MAX_FPS', 'BALANCED', 'VISUAL_QUALITY'],
    conflictsWith: ['embeddium', 'rubidium', 'optifine']
  },
  {
    slug: 'embeddium',
    name: 'Embeddium',
    description: 'Oficjalny, stabilny port Sodium dla loaderów Forge i NeoForge.',
    category: 'rendering',
    installed: false,
    active: false,
    supportedLoaders: ['forge', 'neoforge'],
    recommendedFor: ['MAX_FPS', 'BALANCED', 'VISUAL_QUALITY'],
    conflictsWith: ['sodium', 'rubidium', 'optifine']
  },
  {
    slug: 'lithium',
    name: 'Lithium',
    description: 'Optymalizacja fizyki gry, ładowania chunków, sztucznej inteligencji mobów i obliczeń serwerowych bez zmian w rozgrywce.',
    category: 'engine',
    installed: false,
    active: false,
    supportedLoaders: ['fabric', 'quilt'],
    recommendedFor: ['MAX_FPS', 'BALANCED', 'VISUAL_QUALITY']
  },
  {
    slug: 'ferrite-core',
    name: 'FerriteCore',
    description: 'Redukcja zużycia pamięci RAM (Heap) o 30-50% poprzez optymalizację cache stanów bloków i modeli.',
    category: 'memory',
    installed: false,
    active: false,
    supportedLoaders: ['fabric', 'forge', 'neoforge', 'quilt'],
    recommendedFor: ['MAX_FPS', 'BALANCED', 'VISUAL_QUALITY']
  },
  {
    slug: 'entityculling',
    name: 'Entity Culling',
    description: 'Pomija renderowanie encji i skrzyń zasłoniętych przez ściany lub znajdujących się poza polem widzenia kamery.',
    category: 'culling',
    installed: false,
    active: false,
    supportedLoaders: ['fabric', 'forge', 'neoforge', 'quilt'],
    recommendedFor: ['MAX_FPS', 'BALANCED', 'VISUAL_QUALITY']
  },
  {
    slug: 'immediatelyfast',
    name: 'ImmediatelyFast',
    description: 'Optymalizuje renderowanie trybu bezpośredniego (HUD, napisy, ekrany ekwipunku, cząsteczki).',
    category: 'rendering',
    installed: false,
    active: false,
    supportedLoaders: ['fabric', 'forge', 'neoforge', 'quilt'],
    recommendedFor: ['MAX_FPS', 'BALANCED', 'VISUAL_QUALITY']
  },
  {
    slug: 'modernfix',
    name: 'ModernFix',
    description: 'Kompleksowe poprawki wycieków pamięci, optymalizacja czasu ładowania gry oraz struktury struktur.',
    category: 'engine',
    installed: false,
    active: false,
    supportedLoaders: ['fabric', 'forge', 'neoforge', 'quilt'],
    recommendedFor: ['MAX_FPS', 'BALANCED']
  },
  {
    slug: 'dynamic-fps',
    name: 'Dynamic FPS',
    description: 'Automatycznie obniża klatkaż gry, gdy okno Minecrafta jest zminimalizowane lub w tle, redukując obciążenie sprzętu.',
    category: 'rendering',
    installed: false,
    active: false,
    supportedLoaders: ['fabric', 'quilt', 'forge', 'neoforge'],
    recommendedFor: ['MAX_FPS', 'BALANCED']
  }
];

export const PERFORMANCE_PRESETS: Record<PerformanceProfileType, PerformancePresetDefinition> = {
  MAX_FPS: {
    profile: 'MAX_FPS',
    name: 'Maksymalny FPS',
    description: 'Agresywna optymalizacja grafiki i renderowania. Priorytet: maksymalna płynność i zero ścinek.',
    targetFpsCategory: 'Rywalizacyjny / Niski sprzęt',
    graphics: {
      renderDistance: 8,
      simulationDistance: 5,
      graphicsMode: 'fast',
      particles: 'minimal',
      clouds: false,
      entityDistance: 75,
      entityCulling: true,
      framerateLimit: 0,
      biomeBlend: 0,
      enableVsync: false
    },
    includedModSlugs: ['sodium', 'embeddium', 'lithium', 'ferrite-core', 'entityculling', 'immediatelyfast', 'modernfix', 'dynamic-fps']
  },
  BALANCED: {
    profile: 'BALANCED',
    name: 'Zrównoważony',
    description: 'Idealny kompromis między wysoką liczbą FPS a ładnym wyglądem świata i średnim zasięgiem widzenia.',
    targetFpsCategory: 'Codzienna gra / Uniwersalny',
    graphics: {
      renderDistance: 12,
      simulationDistance: 8,
      graphicsMode: 'fancy',
      particles: 'decreased',
      clouds: true,
      entityDistance: 100,
      entityCulling: true,
      framerateLimit: 144,
      biomeBlend: 2,
      enableVsync: false
    },
    includedModSlugs: ['sodium', 'embeddium', 'lithium', 'ferrite-core', 'entityculling', 'immediatelyfast', 'modernfix']
  },
  VISUAL_QUALITY: {
    profile: 'VISUAL_QUALITY',
    name: 'Wysoka Jakość',
    description: 'Zapewnia pełne efekty wizualne przy zachowaniu fundamentów optymalizacyjnych silnika gry.',
    targetFpsCategory: 'Mocne PC / Eksploracja',
    graphics: {
      renderDistance: 16,
      simulationDistance: 10,
      graphicsMode: 'fancy',
      particles: 'all',
      clouds: true,
      entityDistance: 120,
      entityCulling: true,
      framerateLimit: 0,
      biomeBlend: 5,
      enableVsync: false
    },
    includedModSlugs: ['sodium', 'embeddium', 'lithium', 'ferrite-core', 'entityculling', 'immediatelyfast']
  }
};

/**
 * Filter mods applicable for a specific loader.
 */
export function getAvailableModsForLoader(loader: MinecraftLoaderType): OptimizationModEntry[] {
  return OPTIMIZATION_MODS_CATALOG.filter(mod => {
    if (loader === 'forge' && mod.slug === 'sodium') return false;
    if (loader === 'fabric' && mod.slug === 'embeddium') return false;
    if (loader === 'quilt' && mod.slug === 'embeddium') return false;
    return mod.supportedLoaders.includes(loader);
  });
}
