import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import * as fs from 'node:fs';
import * as path from 'node:path';
import * as crypto from 'node:crypto';
import { ModrinthApiClient, ModrinthRateLimitError } from '../electron/modrinth/ModrinthApiClient';
import { ModrinthDownloader } from '../electron/modrinth/ModrinthDownloader';
import { ModrinthInstallationManager } from '../electron/modrinth/ModrinthInstallationManager';
import { EnginePaths } from '../electron/engine/paths';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import { ModRepository } from '../electron/modpacks/ModRepository';
import type {
  ModrinthVersion,
  ModrinthVersionFile,
  ModRecord,
} from '@zombek/shared-types';

describe('Modrinth Integration - ModrinthApiClient', () => {
  it('should format User-Agent according to Modrinth API policy', () => {
    const userAgent = ModrinthApiClient.getUserAgent();
    expect(userAgent).toContain('ZombekClient/');
    expect(userAgent).toContain('github.com');
  });

  it('should construct correct facet filter query arrays', () => {
    // 1. Basic facets (project_type:mod + client_side default)
    const facets1 = ModrinthApiClient.buildFacets();
    expect(facets1).toContainEqual(['project_type:mod']);
    expect(facets1).toContainEqual(['client_side:required', 'client_side:optional', 'client_side:unknown']);

    // 2. Facets with specific loader and game version
    const facets2 = ModrinthApiClient.buildFacets({
      loader: 'fabric',
      gameVersion: '1.20.4',
    });
    expect(facets2).toContainEqual(['project_type:mod']);
    expect(facets2).toContainEqual(['categories:fabric']);
    expect(facets2).toContainEqual(['versions:1.20.4']);

    // 3. Facets with category and environment
    const facets3 = ModrinthApiClient.buildFacets({
      category: 'optimization',
      environment: 'client',
    });
    expect(facets3).toContainEqual(['categories:optimization']);
    expect(facets3).toContainEqual(['client_side:required', 'client_side:optional']);
  });

  it('should enforce rate limit tracking and cooldown', () => {
    // Check initial state
    ModrinthApiClient.clearCache();
    expect(ModrinthApiClient.getRateLimitStatus().isLimited).toBe(false);

    // Simulate rate limit state
    (ModrinthApiClient as any).rateLimitRemaining = 0;
    (ModrinthApiClient as any).rateLimitResetTimeMs = Date.now() + 60000;

    const status = ModrinthApiClient.getRateLimitStatus();
    expect(status.remaining).toBe(0);
    expect(status.isLimited).toBe(true);

    // Reset status for further tests
    ModrinthApiClient.clearCache();
    expect(ModrinthApiClient.getRateLimitStatus().isLimited).toBe(false);
  });

  it('should properly instantiate ModrinthRateLimitError', () => {
    const err = new ModrinthRateLimitError(45);
    expect(err.name).toBe('ModrinthRateLimitError');
    expect(err.resetSeconds).toBe(45);
    expect(err.message).toContain('45s');
  });
});

describe('Modrinth Integration - ModrinthDownloader Security & Verification', () => {
  it('should allow valid Modrinth CDN domains and protocols', () => {
    expect(() => {
      ModrinthDownloader.assertSafeDownloadUrl('https://cdn.modrinth.com/data/AANobbMI/versions/1.0.0/mod.jar');
    }).not.toThrow();

    expect(() => {
      ModrinthDownloader.assertSafeDownloadUrl('https://edge.modrinth.com/data/something/file.jar');
    }).not.toThrow();
  });

  it('should reject non-HTTPS, foreign domains, or localhost URLs', () => {
    // Non-HTTPS
    expect(() => {
      ModrinthDownloader.assertSafeDownloadUrl('http://cdn.modrinth.com/data/mod.jar');
    }).toThrow(/niebezpieczny protokół|HTTPS/i);

    // Foreign domain
    expect(() => {
      ModrinthDownloader.assertSafeDownloadUrl('https://malicious-site.com/exploit.jar');
    }).toThrow(/niezaufanej domeny|Modrinth CDN/i);

    // Localhost
    expect(() => {
      ModrinthDownloader.assertSafeDownloadUrl('https://localhost:8080/mod.jar');
    }).toThrow(/niezaufanej domeny|Modrinth CDN|lokalnego/i);

    // File URI
    expect(() => {
      ModrinthDownloader.assertSafeDownloadUrl('file:///C:/Windows/System32/calc.exe');
    }).toThrow();
  });

  it('should verify correct SHA-512 file hash and reject corrupt files', async () => {
    const tempDir = path.join(EnginePaths.getRootDir(), 'tests_scratch');
    fs.mkdirSync(tempDir, { recursive: true });

    const sampleContent = Buffer.from('ZombekClient Modrinth Test Content 2026');
    const correctSha512 = crypto.createHash('sha512').update(sampleContent).digest('hex');
    const correctSha1 = crypto.createHash('sha1').update(sampleContent).digest('hex');

    const testFile = path.join(tempDir, 'sample_verify.jar');
    fs.writeFileSync(testFile, sampleContent);

    try {
      // 1. Verify with correct SHA-512
      const valid = await ModrinthDownloader.verifyFileHash(testFile, {
        sha512: correctSha512,
        sha1: correctSha1,
      });
      expect(valid).toBe(true);

      // 2. Verify with wrong SHA-512
      const invalid = await ModrinthDownloader.verifyFileHash(testFile, {
        sha512: 'ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff',
        sha1: correctSha1,
      });
      expect(invalid).toBe(false);
    } finally {
      if (fs.existsSync(testFile)) fs.unlinkSync(testFile);
    }
  });
});

describe('Modrinth Integration - ModrinthInstallationManager Logic', () => {
  it('should select primary JAR file from version files list', () => {
    const versionWithPrimary: ModrinthVersion = {
      id: 'v1',
      project_id: 'p1',
      author_id: 'a1',
      name: 'Version 1',
      version_number: '1.0.0',
      dependencies: [],
      game_versions: ['1.20.4'],
      version_type: 'release',
      loaders: ['fabric'],
      date_published: '2026-01-01',
      downloads: 100,
      files: [
        {
          hashes: { sha512: 'aaa', sha1: 'bbb' },
          url: 'https://cdn.modrinth.com/data/sources.jar',
          filename: 'sources.jar',
          primary: false,
          size: 1000,
        },
        {
          hashes: { sha512: 'ccc', sha1: 'ddd' },
          url: 'https://cdn.modrinth.com/data/main.jar',
          filename: 'main.jar',
          primary: true,
          size: 5000,
        },
      ],
    };

    const selected = ModrinthInstallationManager.selectPrimaryFile(versionWithPrimary);
    expect(selected?.filename).toBe('main.jar');
    expect(selected?.primary).toBe(true);
  });

  it('should correctly evaluate loader compatibility including Quilt backwards compatibility', () => {
    // 1. Fabric mod on Fabric instance -> true
    expect(ModrinthInstallationManager.isLoaderCompatible(['fabric'], 'fabric')).toBe(true);

    // 2. Forge mod on Forge instance -> true
    expect(ModrinthInstallationManager.isLoaderCompatible(['forge'], 'forge')).toBe(true);

    // 3. NeoForge mod on NeoForge instance -> true
    expect(ModrinthInstallationManager.isLoaderCompatible(['neoforge'], 'neoforge')).toBe(true);

    // 4. Fabric mod on Quilt instance -> true (Quilt supports Fabric mods via QSL/Fabric API)
    expect(ModrinthInstallationManager.isLoaderCompatible(['fabric'], 'quilt')).toBe(true);

    // 5. Forge mod on Fabric instance -> false
    expect(ModrinthInstallationManager.isLoaderCompatible(['forge'], 'fabric')).toBe(false);

    // 6. Any mod on Vanilla instance -> false
    expect(ModrinthInstallationManager.isLoaderCompatible(['fabric', 'forge'], 'vanilla')).toBe(false);
  });

  it('should detect required missing dependencies for a mod version', async () => {
    const versionWithDeps: ModrinthVersion = {
      id: 'v2',
      project_id: 'sodium',
      author_id: 'jellysquid',
      name: 'Sodium 0.5.8',
      version_number: '0.5.8',
      dependencies: [
        {
          version_id: 'fab-api-1',
          project_id: 'fabric-api',
          file_name: null,
          dependency_type: 'required',
        },
        {
          version_id: 'iris-1',
          project_id: 'iris',
          file_name: null,
          dependency_type: 'optional',
        },
      ],
      game_versions: ['1.20.4'],
      version_type: 'release',
      loaders: ['fabric'],
      date_published: '2026-01-01',
      downloads: 50000,
      files: [
        {
          hashes: { sha512: '111', sha1: '222' },
          url: 'https://cdn.modrinth.com/data/sodium.jar',
          filename: 'sodium-0.5.8.jar',
          primary: true,
          size: 4000,
        },
      ],
    };

    // Case A: No mods installed -> fabric-api is missing
    const emptyExistingMods: ModRecord[] = [];
    const missingA = await (ModrinthInstallationManager as any).detectMissingDependencies(
      versionWithDeps,
      emptyExistingMods
    );
    expect(missingA).toContain('fabric-api');
    expect(missingA).not.toContain('iris'); // Optional deps should not trigger missing warning

    // Case B: fabric-api is installed -> no missing deps
    const existingWithFabricApi: ModRecord[] = [
      {
        fileName: 'fabric-api-0.91.0.jar',
        filePath: 'C:/fake/mods/fabric-api-0.91.0.jar',
        fileSize: 10000,
        sha256: 'abc123456789',
        dateAdded: '2026-01-01',
        enabled: true,
        compatibilityStatus: 'Compatible',
        metadata: {
          id: 'fabric-api',
          name: 'Fabric API',
          version: '0.91.0',
          description: 'Core API for Fabric mods',
          authors: ['FabricMC'],
          loader: 'fabric',
          dependencies: [],
          rawSource: 'fabric.mod.json',
        },
      },
    ];

    const missingB = await (ModrinthInstallationManager as any).detectMissingDependencies(
      versionWithDeps,
      existingWithFabricApi
    );
    expect(missingB.length).toBe(0);
  });
});

describe('Modrinth Integration - Live API Connectivity', () => {
  it('should successfully query real Modrinth search API', async () => {
    try {
      const result = await ModrinthApiClient.searchProjects({
        query: 'sodium',
        limit: 3,
        index: 'downloads',
      });

      expect(result).toBeDefined();
      expect(result.hits).toBeDefined();
      expect(Array.isArray(result.hits)).toBe(true);
      expect(result.hits.length).toBeGreaterThan(0);

      const firstHit = result.hits[0];
      expect(firstHit.title).toBeDefined();
      expect(firstHit.project_id).toBeDefined();
      expect(firstHit.downloads).toBeGreaterThan(0);
      expect(firstHit.categories).toBeDefined();
    } catch (err: any) {
      if (err?.message?.includes('fetch failed') || err?.code === 'UNABLE_TO_VERIFY_LEAF_SIGNATURE') {
        console.log('[Live Modrinth Search Test Skipped: External network or SSL interception active in test runner]');
        return;
      }
      throw err;
    }
  }, 15000);

  it('should fetch real Modrinth project and versions metadata', async () => {
    try {
      // Sodium is one of the most famous Modrinth projects (slug: sodium)
      const project = await ModrinthApiClient.getProject('sodium');
      expect(project).toBeDefined();
      expect(project.slug).toBe('sodium');
      expect(project.project_type).toBe('mod');
      expect(project.title).toContain('Sodium');
      expect(project.loaders).toContain('fabric');

      // Fetch versions for this project
      const versions = await ModrinthApiClient.getProjectVersions('sodium', {
        loaders: ['fabric'],
        gameVersions: ['1.20.4'],
      });
      expect(Array.isArray(versions)).toBe(true);
      if (versions.length > 0) {
        const v = versions[0];
        expect(v.files).toBeDefined();
        expect(v.files.length).toBeGreaterThan(0);
        expect(v.files[0].hashes.sha512).toBeDefined();
        expect(v.loaders).toContain('fabric');
      }
    } catch (err: any) {
      if (err?.message?.includes('fetch failed') || err?.code === 'UNABLE_TO_VERIFY_LEAF_SIGNATURE') {
        console.log('[Live Modrinth Project Test Skipped: External network or SSL interception active in test runner]');
        return;
      }
      throw err;
    }
  }, 15000);

  it('should fetch real categories and loaders tags from Modrinth API', async () => {
    try {
      const categories = await ModrinthApiClient.getCategories();
      expect(Array.isArray(categories)).toBe(true);
      expect(categories.length).toBeGreaterThan(0);
      const techCategory = categories.find((c) => c.name === 'technology' || c.name === 'optimization');
      expect(techCategory).toBeDefined();

      const loaders = await ModrinthApiClient.getLoaders();
      expect(Array.isArray(loaders)).toBe(true);
      const fabricLoader = loaders.find((l) => l.name === 'fabric');
      expect(fabricLoader).toBeDefined();
    } catch (err: any) {
      if (err?.message?.includes('fetch failed') || err?.code === 'UNABLE_TO_VERIFY_LEAF_SIGNATURE') {
        console.log('[Live Modrinth Tags Test Skipped: External network or SSL interception active in test runner]');
        return;
      }
      throw err;
    }
  }, 15000);
});
