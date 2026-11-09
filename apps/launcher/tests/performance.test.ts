import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import * as fs from 'node:fs';
import * as path from 'node:path';
import * as os from 'node:os';
import { HardwareDetectionService } from '../electron/performance/HardwareDetectionService';
import { MinecraftOptionsManager } from '../electron/performance/MinecraftOptionsManager';
import {
  PERFORMANCE_PRESETS,
  getAvailableModsForLoader,
} from '../electron/performance/PerformancePresetManifests';

describe('ETAP 8D1: Zombek Performance Pack Unit Tests', () => {
  let tempDir: string;

  beforeEach(async () => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-perf-test-'));
  });

  afterEach(async () => {
    if (fs.existsSync(tempDir)) {
      fs.rmSync(tempDir, { recursive: true, force: true });
    }
  });

  describe('HardwareDetectionService', () => {
    it('should detect local hardware specs without throwing', async () => {
      const specs = await HardwareDetectionService.getHardwareSpecs();
      expect(specs).toBeDefined();
      expect(specs.cpuModel).toBeTruthy();
      expect(specs.cpuThreads).toBeGreaterThan(0);
      expect(specs.totalRamMb).toBeGreaterThan(1024);
      expect(specs.freeRamMb).toBeGreaterThan(0);
      expect(specs.recommendedRamMb).toBeGreaterThanOrEqual(2048);
      expect(specs.recommendedRamMb).toBeLessThanOrEqual(8192);
      expect(specs.osName).toBeTruthy();
    });

    it('should generate modern clean G1GC JVM flags without snake-oil arguments', () => {
      const args = HardwareDetectionService.getRecommendedG1GcArgs(4096);
      expect(args).toContain('-Xmx4096M');
      expect(args).toContain('-XX:+UseG1GC');
      expect(args).toContain('-XX:+ParallelRefProcEnabled');
      expect(args).toContain('-XX:MaxGCPauseMillis=200');
      expect(args).toContain('-XX:+AlwaysPreTouch');
      // Must not contain invalid or dangerous flags
      expect(args.some(a => a.includes('AggressiveOpts'))).toBe(false);
    });
  });

  describe('MinecraftOptionsManager', () => {
    it('should read, backup, apply graphics options and restore', () => {
      const optionsPath = path.join(tempDir, 'options.txt');
      fs.writeFileSync(
        optionsPath,
        'renderDistance:16\nsimulationDistance:12\ngraphicsMode:2\nparticles:0\nenableVsync:true\nsoundCategory_master:1.0\n',
        'utf-8'
      );

      // Verify read
      const initial = MinecraftOptionsManager.readOptionsFile(tempDir);
      expect(initial.get('renderDistance')).toBe('16');
      expect(initial.get('soundCategory_master')).toBe('1.0');

      // Apply MAX_FPS preset
      const applied = MinecraftOptionsManager.applyGraphicsSettings(tempDir, {
        renderDistance: 8,
        simulationDistance: 5,
        graphicsMode: 'fast',
        particles: 'minimal',
        clouds: false,
        enableVsync: false,
      });

      expect(applied).toBe(true);

      const modified = MinecraftOptionsManager.readOptionsFile(tempDir);
      expect(modified.get('renderDistance')).toBe('8');
      expect(modified.get('simulationDistance')).toBe('5');
      expect(modified.get('graphicsMode')).toBe('0'); // fast
      expect(modified.get('particles')).toBe('2'); // minimal
      expect(modified.get('enableVsync')).toBe('false');
      // Preserved non-graphics keys:
      expect(modified.get('soundCategory_master')).toBe('1.0');

      // Verify backup exists
      expect(MinecraftOptionsManager.hasOptionsBackup(tempDir)).toBe(true);

      // Restore backup
      const restored = MinecraftOptionsManager.restoreOptionsBackup(tempDir);
      expect(restored).toBe(true);

      const reverted = MinecraftOptionsManager.readOptionsFile(tempDir);
      expect(reverted.get('renderDistance')).toBe('16');
      expect(reverted.get('graphicsMode')).toBe('2');
      expect(MinecraftOptionsManager.hasOptionsBackup(tempDir)).toBe(false);
    });
  });

  describe('PerformancePresetManifests', () => {
    it('should correctly filter mods by loader and avoid conflicting engines', () => {
      const fabricMods = getAvailableModsForLoader('fabric');
      expect(fabricMods.some(m => m.slug === 'sodium')).toBe(true);
      expect(fabricMods.some(m => m.slug === 'embeddium')).toBe(false);
      expect(fabricMods.some(m => m.slug === 'lithium')).toBe(true);

      const forgeMods = getAvailableModsForLoader('forge');
      expect(forgeMods.some(m => m.slug === 'sodium')).toBe(false);
      expect(forgeMods.some(m => m.slug === 'embeddium')).toBe(true);
      expect(forgeMods.some(m => m.slug === 'lithium')).toBe(false); // lithium is not forge
      expect(forgeMods.some(m => m.slug === 'ferrite-core')).toBe(true);
    });

    it('should define accurate graphics configurations for all 3 profiles', () => {
      expect(PERFORMANCE_PRESETS.MAX_FPS.graphics.renderDistance).toBe(8);
      expect(PERFORMANCE_PRESETS.MAX_FPS.graphics.graphicsMode).toBe('fast');
      expect(PERFORMANCE_PRESETS.MAX_FPS.graphics.clouds).toBe(false);

      expect(PERFORMANCE_PRESETS.BALANCED.graphics.renderDistance).toBe(12);
      expect(PERFORMANCE_PRESETS.BALANCED.graphics.graphicsMode).toBe('fancy');

      expect(PERFORMANCE_PRESETS.VISUAL_QUALITY.graphics.renderDistance).toBe(16);
    });
  });
});
