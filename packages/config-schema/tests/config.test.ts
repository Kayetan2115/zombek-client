import { describe, it, expect } from 'vitest';
import { DEFAULT_LAUNCHER_CONFIG, validateLauncherConfig } from '../src/index';

describe('config-schema package', () => {
  it('should validate default launcher configuration successfully', () => {
    const result = validateLauncherConfig(DEFAULT_LAUNCHER_CONFIG);
    expect(result.success).toBe(true);
    if (result.success) {
      expect(result.data.memory.maxMb).toBeGreaterThanOrEqual(result.data.memory.minMb);
      expect(result.data.resolution.width).toBe(1280);
      expect(result.data.resolution.height).toBe(720);
    }
  });

  it('should reject invalid memory allocation', () => {
    const invalidConfig = {
      ...DEFAULT_LAUNCHER_CONFIG,
      memory: { minMb: 4096, maxMb: 1024 }
    };
    const result = validateLauncherConfig(invalidConfig);
    expect(result.success).toBe(false);
    if (!result.success) {
      expect(result.errors.length).toBeGreaterThan(0);
      expect(result.errors[0]).toContain('memory');
    }
  });

  it('should reject non-object values', () => {
    const result = validateLauncherConfig(null);
    expect(result.success).toBe(false);
  });
});
