import * as fs from 'node:fs';
import * as path from 'node:path';
import type { GraphicsPresetOptions } from '@zombek/shared-types';

export class MinecraftOptionsManager {
  /**
   * Reads and parses key-value pairs from options.txt.
   */
  public static readOptionsFile(instanceDir: string): Map<string, string> {
    const optionsPath = path.join(instanceDir, 'options.txt');
    const map = new Map<string, string>();

    if (!fs.existsSync(optionsPath)) {
      return map;
    }

    try {
      const content = fs.readFileSync(optionsPath, 'utf-8');
      const lines = content.split(/\r?\n/);
      for (const line of lines) {
        const trimmed = line.trim();
        if (!trimmed || trimmed.startsWith('#')) continue;
        const colonIdx = trimmed.indexOf(':');
        if (colonIdx > 0) {
          const key = trimmed.slice(0, colonIdx).trim();
          const val = trimmed.slice(colonIdx + 1).trim();
          map.set(key, val);
        }
      }
    } catch (err) {
      console.error(`[MinecraftOptionsManager] Failed to read ${optionsPath}:`, err);
    }

    return map;
  }

  /**
   * Backs up existing options.txt and sodium-options.json.
   */
  public static createOptionsBackup(instanceDir: string): boolean {
    try {
      const optionsPath = path.join(instanceDir, 'options.txt');
      const backupPath = path.join(instanceDir, 'options.txt.zombek_bak');
      if (fs.existsSync(optionsPath) && !fs.existsSync(backupPath)) {
        fs.copyFileSync(optionsPath, backupPath);
      }

      const sodiumPath = path.join(instanceDir, 'config', 'sodium-options.json');
      const sodiumBackup = path.join(instanceDir, 'config', 'sodium-options.json.zombek_bak');
      if (fs.existsSync(sodiumPath) && !fs.existsSync(sodiumBackup)) {
        fs.copyFileSync(sodiumPath, sodiumBackup);
      }

      return true;
    } catch (err) {
      console.error('[MinecraftOptionsManager] Error creating options backup:', err);
      return false;
    }
  }

  /**
   * Restores options.txt and sodium-options.json from backup if present.
   */
  public static restoreOptionsBackup(instanceDir: string): boolean {
    let restored = false;
    try {
      const optionsPath = path.join(instanceDir, 'options.txt');
      const backupPath = path.join(instanceDir, 'options.txt.zombek_bak');
      if (fs.existsSync(backupPath)) {
        fs.copyFileSync(backupPath, optionsPath);
        fs.unlinkSync(backupPath);
        restored = true;
      }

      const sodiumPath = path.join(instanceDir, 'config', 'sodium-options.json');
      const sodiumBackup = path.join(instanceDir, 'config', 'sodium-options.json.zombek_bak');
      if (fs.existsSync(sodiumBackup)) {
        fs.copyFileSync(sodiumBackup, sodiumPath);
        fs.unlinkSync(sodiumBackup);
        restored = true;
      }
    } catch (err) {
      console.error('[MinecraftOptionsManager] Error restoring options backup:', err);
    }
    return restored;
  }

  /**
   * Applies graphics options into options.txt and sodium config, preserving other user settings.
   */
  public static applyGraphicsSettings(instanceDir: string, preset: Partial<GraphicsPresetOptions>): boolean {
    try {
      this.createOptionsBackup(instanceDir);

      const optionsPath = path.join(instanceDir, 'options.txt');
      const map = this.readOptionsFile(instanceDir);

      if (preset.renderDistance !== undefined) {
        map.set('renderDistance', String(preset.renderDistance));
      }
      if (preset.simulationDistance !== undefined) {
        map.set('simulationDistance', String(preset.simulationDistance));
      }
      if (preset.graphicsMode !== undefined) {
        // 0 = fast, 1 = fancy, 2 = fabulous
        const val = preset.graphicsMode === 'fast' ? '0' : preset.graphicsMode === 'fabulous' ? '2' : '1';
        map.set('graphicsMode', val);
      }
      if (preset.particles !== undefined) {
        // 0 = all, 1 = decreased, 2 = minimal
        const val = preset.particles === 'minimal' ? '2' : preset.particles === 'decreased' ? '1' : '0';
        map.set('particles', val);
      }
      if (preset.clouds !== undefined) {
        map.set('renderClouds', preset.clouds ? 'true' : 'false');
      }
      if (preset.entityDistance !== undefined) {
        map.set('entityDistanceScaling', (preset.entityDistance / 100).toFixed(2));
      }
      if (preset.framerateLimit !== undefined) {
        map.set('maxFps', String(preset.framerateLimit));
      }
      if (preset.biomeBlend !== undefined) {
        map.set('biomeBlendRadius', String(preset.biomeBlend));
      }
      if (preset.enableVsync !== undefined) {
        map.set('enableVsync', preset.enableVsync ? 'true' : 'false');
      }

      // Re-serialize options.txt
      const serialized = Array.from(map.entries())
        .map(([k, v]) => `${k}:${v}`)
        .join('\n') + '\n';

      fs.writeFileSync(optionsPath, serialized, 'utf-8');

      // Update sodium-options.json if directory exists
      const configDir = path.join(instanceDir, 'config');
      if (!fs.existsSync(configDir)) {
        fs.mkdirSync(configDir, { recursive: true });
      }
      const sodiumPath = path.join(configDir, 'sodium-options.json');
      let sodiumConfig: any = {};
      if (fs.existsSync(sodiumPath)) {
        try {
          sodiumConfig = JSON.parse(fs.readFileSync(sodiumPath, 'utf-8'));
        } catch {
          sodiumConfig = {};
        }
      }

      // Optimize Sodium properties according to preset
      sodiumConfig.quality = sodiumConfig.quality || {};
      sodiumConfig.performance = sodiumConfig.performance || {};
      sodiumConfig.notifications = sodiumConfig.notifications || {};

      if (preset.graphicsMode === 'fast') {
        sodiumConfig.quality.weather_quality = 'FAST';
        sodiumConfig.quality.leaves_quality = 'FAST';
        sodiumConfig.performance.chunk_builder_threads = 0; // auto
        sodiumConfig.performance.always_defer_chunk_updates = true;
      } else {
        sodiumConfig.quality.weather_quality = 'FANCY';
        sodiumConfig.quality.leaves_quality = 'FANCY';
      }

      fs.writeFileSync(sodiumPath, JSON.stringify(sodiumConfig, null, 2), 'utf-8');
      return true;
    } catch (err) {
      console.error('[MinecraftOptionsManager] Error applying graphics options:', err);
      return false;
    }
  }

  public static hasOptionsBackup(instanceDir: string): boolean {
    const backupPath = path.join(instanceDir, 'options.txt.zombek_bak');
    return fs.existsSync(backupPath);
  }
}
