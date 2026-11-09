/**
 * ZombekClient - Configuration Schemas and Defaults
 */

import type { MemoryAllocation } from '@zombek/shared-types';

export interface GameResolutionConfig {
  width: number;
  height: number;
  fullscreen: boolean;
}

export interface LauncherConfig {
  version: number;
  gameDirectory: string;
  javaPath?: string;
  memory: MemoryAllocation;
  jvmArgs: string[];
  resolution: GameResolutionConfig;
  autoUpdate: boolean;
  activeProfileId?: string;
}

export const DEFAULT_LAUNCHER_CONFIG: LauncherConfig = {
  version: 1,
  gameDirectory: '',
  memory: {
    minMb: 1024,
    maxMb: 4096
  },
  jvmArgs: [
    '-XX:+UseG1GC',
    '-XX:+ParallelRefProcEnabled',
    '-XX:MaxGCPauseMillis=200',
    '-XX:+UnlockExperimentalVMOptions',
    '-XX:+DisableExplicitGC'
  ],
  resolution: {
    width: 1280,
    height: 720,
    fullscreen: false
  },
  autoUpdate: true
};

export type ValidationResult<T> =
  | { success: true; data: T }
  | { success: false; errors: string[] };

/**
 * Validates a raw configuration object against LauncherConfig requirements.
 */
export function validateLauncherConfig(raw: unknown): ValidationResult<LauncherConfig> {
  if (typeof raw !== 'object' || raw === null) {
    return { success: false, errors: ['Configuration must be a non-null object'] };
  }

  const candidate = raw as Partial<LauncherConfig>;
  const errors: string[] = [];

  if (typeof candidate.version !== 'number' || candidate.version <= 0) {
    errors.push('version must be a positive number');
  }

  if (typeof candidate.gameDirectory !== 'string') {
    errors.push('gameDirectory must be a string');
  }

  if (
    !candidate.memory ||
    typeof candidate.memory.minMb !== 'number' ||
    typeof candidate.memory.maxMb !== 'number' ||
    candidate.memory.minMb <= 0 ||
    candidate.memory.maxMb < candidate.memory.minMb
  ) {
    errors.push('memory must define valid minMb and maxMb (maxMb >= minMb > 0)');
  }

  if (!Array.isArray(candidate.jvmArgs) || candidate.jvmArgs.some((arg) => typeof arg !== 'string')) {
    errors.push('jvmArgs must be an array of strings');
  }

  if (
    !candidate.resolution ||
    typeof candidate.resolution.width !== 'number' ||
    typeof candidate.resolution.height !== 'number' ||
    typeof candidate.resolution.fullscreen !== 'boolean' ||
    candidate.resolution.width < 640 ||
    candidate.resolution.height < 480
  ) {
    errors.push('resolution must define width >= 640, height >= 480, and boolean fullscreen');
  }

  if (typeof candidate.autoUpdate !== 'boolean') {
    errors.push('autoUpdate must be a boolean');
  }

  if (errors.length > 0) {
    return { success: false, errors };
  }

  return { success: true, data: candidate as LauncherConfig };
}
