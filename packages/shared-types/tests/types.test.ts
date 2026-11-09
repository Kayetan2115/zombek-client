import { describe, it, expect } from 'vitest';
import { SUPPORTED_LOADERS, MinecraftLoaderType } from '../src/index';

describe('shared-types package', () => {
  it('should expose supported mod loaders including fabric, forge, neoforge, and quilt', () => {
    expect(SUPPORTED_LOADERS).toContain('fabric');
    expect(SUPPORTED_LOADERS).toContain('forge');
    expect(SUPPORTED_LOADERS).toContain('neoforge');
    expect(SUPPORTED_LOADERS).toContain('quilt');
    expect(SUPPORTED_LOADERS).toContain('vanilla');
    expect(SUPPORTED_LOADERS.length).toBe(5);
  });

  it('should have correct loader type assignment', () => {
    const loader: MinecraftLoaderType = 'fabric';
    expect(loader).toBe('fabric');
  });
});
