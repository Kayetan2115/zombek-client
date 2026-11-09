import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import * as path from 'node:path';
import * as fs from 'node:fs';
import * as os from 'node:os';
import { ClientReleaseService } from '../electron/engine/ClientReleaseService';
import { InstallationRepository } from '../electron/engine/InstallationRepository';
import { EnginePaths } from '../electron/engine/paths';
import { ModMetadataReader } from '../electron/modpacks/ModMetadataReader';

describe('ClientReleaseService - Multi-loader In-Game Client Manager', () => {
  let tempDir: string;
  let service: ClientReleaseService;

  beforeEach(() => {
    tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'zombek-client-release-test-'));
    EnginePaths.setRootDir(tempDir);
    EnginePaths.ensureAllBaseDirs();

    service = ClientReleaseService.getInstance();
    service.clearCache();
  });

  afterEach(() => {
    try {
      fs.rmSync(tempDir, { recursive: true, force: true });
    } catch {
      // ignore
    }
    service.clearCache();
  });

  it('should load manifest with Fabric, Forge, NeoForge, and Quilt releases', () => {
    const releases = service.getAvailableReleases();
    expect(releases.length).toBeGreaterThanOrEqual(4);

    const fabricRelease = releases.find((r) => r.loader === 'fabric');
    expect(fabricRelease).toBeDefined();
    expect(fabricRelease?.minecraftVersion).toBe('1.20.4');
    expect(fabricRelease?.filename).toBe('zombekclient-fabric-0.1.0-alpha.jar');
    expect(fabricRelease?.metadataFormat).toBe('fabric.mod.json');

    const forgeRelease = releases.find((r) => r.loader === 'forge');
    expect(forgeRelease).toBeDefined();
    expect(forgeRelease?.minecraftVersion).toBe('1.20.4');
    expect(forgeRelease?.filename).toBe('zombekclient-forge-0.1.0-alpha.jar');
    expect(forgeRelease?.metadataFormat).toBe('META-INF/mods.toml');

    const neoForgeRelease = releases.find((r) => r.loader === 'neoforge');
    expect(neoForgeRelease).toBeDefined();
    expect(neoForgeRelease?.minecraftVersion).toBe('1.20.4');
    expect(neoForgeRelease?.filename).toBe('zombekclient-neoforge-0.1.0-alpha.jar');
    expect(neoForgeRelease?.metadataFormat).toBe('META-INF/neoforge.mods.toml');

    const quiltRelease = releases.find((r) => r.loader === 'quilt');
    expect(quiltRelease).toBeDefined();
    expect(quiltRelease?.minecraftVersion).toBe('1.20.4');
    expect(quiltRelease?.filename).toBe('zombekclient-quilt-0.1.0-alpha.jar');
    expect(quiltRelease?.metadataFormat).toBe('quilt.mod.json');
  });

  it('should match appropriate release for Forge 1.20.4 instance', () => {
    const release = service.findMatchingRelease('forge', '1.20.4');
    expect(release).not.toBeNull();
    expect(release?.loader).toBe('forge');
    expect(release?.filename).toBe('zombekclient-forge-0.1.0-alpha.jar');
    expect(release?.entrypoint).toBe('pl.zombek.client.forge.entrypoint.ZombekForgeMod');
  });

  it('should match appropriate release for Fabric 1.20.4 instance', () => {
    const release = service.findMatchingRelease('fabric', '1.20.4');
    expect(release).not.toBeNull();
    expect(release?.loader).toBe('fabric');
    expect(release?.filename).toBe('zombekclient-fabric-0.1.0-alpha.jar');
    expect(release?.entrypoint).toBe('pl.zombek.client.core.ZombekClientMod');
  });

  it('should match appropriate release for NeoForge 1.20.4 instance', () => {
    const release = service.findMatchingRelease('neoforge', '1.20.4');
    expect(release).not.toBeNull();
    expect(release?.loader).toBe('neoforge');
    expect(release?.filename).toBe('zombekclient-neoforge-0.1.0-alpha.jar');
    expect(release?.entrypoint).toBe('pl.zombek.client.neoforge.entrypoint.ZombekNeoForgeMod');
  });

  it('should match appropriate release for Quilt 1.20.4 instance', () => {
    const release = service.findMatchingRelease('quilt', '1.20.4');
    expect(release).not.toBeNull();
    expect(release?.loader).toBe('quilt');
    expect(release?.filename).toBe('zombekclient-quilt-0.1.0-alpha.jar');
    expect(release?.entrypoint).toBe('pl.zombek.client.quilt.entrypoint.QuiltClientBootstrap');
  });

  it('should reject vanilla instance when attempting client mod installation', async () => {
    const instance = await InstallationRepository.create({
      name: 'Vanilla Instance',
      minecraftVersion: '1.20.4',
      loader: 'vanilla',
      allocatedRamMb: 4096,
    });

    const result = await service.installClientModForInstance(instance.id);
    expect(result.success).toBe(false);
    expect(result.error).toContain('Vanilla');
  });

  it('should successfully install Forge client mod and detect its status', async () => {
    const forgeInstance = await InstallationRepository.create({
      name: 'Forge 1.20.4 Instance',
      minecraftVersion: '1.20.4',
      loader: 'forge',
      loaderVersion: '49.2.13',
      allocatedRamMb: 4096,
    });

    const installResult = await service.installClientModForInstance(forgeInstance.id);
    expect(installResult.success).toBe(true);
    expect(installResult.release?.filename).toBe('zombekclient-forge-0.1.0-alpha.jar');

    const status = await service.getClientModStatus(forgeInstance.id);
    expect(status.installed).toBe(true);
    expect(status.filename).toBe('zombekclient-forge-0.1.0-alpha.jar');
    expect(status.release?.loader).toBe('forge');

    const modsDir = path.join(forgeInstance.instanceDir, 'mods');
    const installedFile = path.join(modsDir, 'zombekclient-forge-0.1.0-alpha.jar');
    expect(fs.existsSync(installedFile)).toBe(true);
  });

  it('should successfully install NeoForge client mod and detect its status', async () => {
    const neoForgeInstance = await InstallationRepository.create({
      name: 'NeoForge 1.20.4 Instance',
      minecraftVersion: '1.20.4',
      loader: 'neoforge',
      loaderVersion: '20.4.237',
      allocatedRamMb: 4096,
    });

    const installResult = await service.installClientModForInstance(neoForgeInstance.id);
    expect(installResult.success).toBe(true);
    expect(installResult.release?.filename).toBe('zombekclient-neoforge-0.1.0-alpha.jar');

    const status = await service.getClientModStatus(neoForgeInstance.id);
    expect(status.installed).toBe(true);
    expect(status.filename).toBe('zombekclient-neoforge-0.1.0-alpha.jar');
    expect(status.release?.loader).toBe('neoforge');

    const modsDir = path.join(neoForgeInstance.instanceDir, 'mods');
    const installedFile = path.join(modsDir, 'zombekclient-neoforge-0.1.0-alpha.jar');
    expect(fs.existsSync(installedFile)).toBe(true);
  });

  it('should successfully install Quilt client mod and detect its status', async () => {
    const quiltInstance = await InstallationRepository.create({
      name: 'Quilt 1.20.4 Instance',
      minecraftVersion: '1.20.4',
      loader: 'quilt',
      loaderVersion: '0.27.1-beta.1',
      allocatedRamMb: 4096,
    });

    const installResult = await service.installClientModForInstance(quiltInstance.id);
    expect(installResult.success).toBe(true);
    expect(installResult.release?.filename).toBe('zombekclient-quilt-0.1.0-alpha.jar');

    const status = await service.getClientModStatus(quiltInstance.id);
    expect(status.installed).toBe(true);
    expect(status.filename).toBe('zombekclient-quilt-0.1.0-alpha.jar');
    expect(status.release?.loader).toBe('quilt');

    const modsDir = path.join(quiltInstance.instanceDir, 'mods');
    const installedFile = path.join(modsDir, 'zombekclient-quilt-0.1.0-alpha.jar');
    expect(fs.existsSync(installedFile)).toBe(true);
  });

  it('should parse compiled Forge jar metadata via ModMetadataReader', async () => {
    const forgeRelease = service.findMatchingRelease('forge', '1.20.4');
    expect(forgeRelease).not.toBeNull();
    const artifactPath = service.resolveArtifactPath(forgeRelease!);
    expect(fs.existsSync(artifactPath)).toBe(true);

    const meta = await ModMetadataReader.readMetadata(artifactPath);
    expect(meta.id).toBe('zombekclient');
    expect(meta.name).toBe('ZombekClient');
    expect(meta.loader).toBe('forge');
    expect(meta.rawSource).toBe('mods.toml');
    expect(meta.dependencies.some((d) => d.id === 'minecraft')).toBe(true);
    expect(meta.dependencies.some((d) => d.id === 'forge')).toBe(true);
  });

  it('should parse compiled NeoForge jar metadata via ModMetadataReader', async () => {
    const neoForgeRelease = service.findMatchingRelease('neoforge', '1.20.4');
    expect(neoForgeRelease).not.toBeNull();
    const artifactPath = service.resolveArtifactPath(neoForgeRelease!);
    expect(fs.existsSync(artifactPath)).toBe(true);

    const meta = await ModMetadataReader.readMetadata(artifactPath);
    expect(meta.id).toBe('zombekclient');
    expect(meta.name).toBe('ZombekClient');
    expect(meta.loader).toBe('neoforge');
    expect(meta.rawSource).toBe('neoforge.mods.toml');
    expect(meta.dependencies.some((d) => d.id === 'minecraft')).toBe(true);
    expect(meta.dependencies.some((d) => d.id === 'neoforge')).toBe(true);
  });

  it('should parse compiled Quilt jar metadata via ModMetadataReader', async () => {
    const quiltRelease = service.findMatchingRelease('quilt', '1.20.4');
    expect(quiltRelease).not.toBeNull();
    const artifactPath = service.resolveArtifactPath(quiltRelease!);
    expect(fs.existsSync(artifactPath)).toBe(true);

    const meta = await ModMetadataReader.readMetadata(artifactPath);
    expect(meta.id).toBe('zombekclient');
    expect(meta.name).toBe('ZombekClient');
    expect(meta.loader).toBe('quilt');
    expect(meta.rawSource).toBe('quilt.mod.json');
    expect(meta.dependencies.some((d) => d.id === 'minecraft')).toBe(true);
    expect(meta.dependencies.some((d) => d.id === 'quilt_loader')).toBe(true);
  });
});
