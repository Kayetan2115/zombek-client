import { describe, it, expect } from 'vitest';
import { formatBytesToMb } from '../src/utils/index';
import { ASSETS_INFO } from '../src/assets/index';

describe('launcher module foundation', () => {
  it('should verify utility functions', () => {
    expect(formatBytesToMb(1024 * 1024 * 512)).toBe(512);
  });

  it('should initialize asset metadata correctly', () => {
    expect(ASSETS_INFO.theme).toBe('festive-pixel-zombek');
    expect(ASSETS_INFO.status).toBe('active');
    expect(ASSETS_INFO.mascotName).toBe('Ząbek');
  });

  it('should verify design token dimensions', () => {
    expect(ASSETS_INFO.version).toBe('2.0.0');
  });
});
