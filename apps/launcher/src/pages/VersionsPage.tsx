import React, { useState, useMemo, useEffect } from 'react';
import { useLauncherContext, Installation } from '../stores';
import { Button, Badge, Modal, SearchBar, Dropdown } from '../components/common';
import { PlusIcon, TrashIcon, CheckIcon, VersionsIcon, RefreshIcon, PlayIcon } from '../components/icons';
import type { MinecraftLoaderType, FabricLoaderVersion, ForgeVersionInfo, NeoForgeVersionInfo, QuiltLoaderVersion } from '@zombek/shared-types';

const FALLBACK_MINECRAFT_VERSIONS = [
  { value: '1.21.1', label: '1.21.1 (Najnowsze wydanie Release)' },
  { value: '1.20.4', label: '1.20.4 (Zalecana / Najpopularniejsza)' },
  { value: '1.20.2', label: '1.20.2 (Stabilna)' },
  { value: '1.19.4', label: '1.19.4 (Klasyczna)' },
  { value: '1.18.2', label: '1.18.2 (Caves & Cliffs)' },
  { value: '1.16.5', label: '1.16.5 (Nether Update)' },
  { value: '1.12.2', label: '1.12.2 (Legacy Modding)' },
  { value: '1.8.9', label: '1.8.9 (Klasyk PvP / Hypixel)' },
];

const LOADERS: { value: MinecraftLoaderType; label: string }[] = [
  { value: 'vanilla', label: 'Vanilla (Czysty oficjalny Minecraft)' },
  { value: 'fabric', label: 'Fabric (Zalecany modloader, wysoka wydajność)' },
  { value: 'forge', label: 'Forge (Oficjalny modloader Forge)' },
  { value: 'neoforge', label: 'NeoForge (Oficjalny modloader NeoForged)' },
  { value: 'quilt', label: 'Quilt (Oficjalny modloader QuiltMC)' },
];

export const VersionsPage: React.FC = () => {
  const {
    installations,
    activeInstallation,
    setActiveInstallationId,
    addInstallation,
    deleteInstallation,
    accounts,
    mojangManifest,
    refreshVersionsManifest,
    launchGame,
    isLaunching,
    addToast,
    getFabricLoaderVersions,
    getForgeVersions,
    getNeoForgeVersions,
    getQuiltLoaderVersions,
  } = useLauncherContext();

  const [search, setSearch] = useState('');
  const [showAddModal, setShowAddModal] = useState(false);
  const [selectedLoaderFilter, setSelectedLoaderFilter] = useState<string>('all');
  const [versionTypeFilter, setVersionTypeFilter] = useState<'all' | 'release' | 'snapshot'>('release');

  // Form state
  const [name, setName] = useState('');
  const [mcVersion, setMcVersion] = useState('1.20.4');
  const [loader, setLoader] = useState<MinecraftLoaderType>('vanilla');
  const [allocatedRam, setAllocatedRam] = useState(4096);
  const [selectedAccountId, setSelectedAccountId] = useState<string>('');

  // Fabric form state
  const [fabricLoaders, setFabricLoaders] = useState<FabricLoaderVersion[]>([]);
  const [selectedFabricLoader, setSelectedFabricLoader] = useState<string>('');
  const [isLoadingFabricLoaders, setIsLoadingFabricLoaders] = useState<boolean>(false);

  // Forge form state
  const [forgeVersions, setForgeVersions] = useState<ForgeVersionInfo[]>([]);
  const [selectedForgeVersion, setSelectedForgeVersion] = useState<string>('');
  const [isLoadingForgeVersions, setIsLoadingForgeVersions] = useState<boolean>(false);

  // NeoForge form state
  const [neoForgeVersions, setNeoForgeVersions] = useState<NeoForgeVersionInfo[]>([]);
  const [selectedNeoForgeVersion, setSelectedNeoForgeVersion] = useState<string>('');
  const [isLoadingNeoForgeVersions, setIsLoadingNeoForgeVersions] = useState<boolean>(false);

  // Quilt form state
  const [quiltLoaders, setQuiltLoaders] = useState<QuiltLoaderVersion[]>([]);
  const [selectedQuiltLoader, setSelectedQuiltLoader] = useState<string>('');
  const [isLoadingQuiltLoaders, setIsLoadingQuiltLoaders] = useState<boolean>(false);

  // Fetch Fabric loader versions when loader is fabric and mcVersion changes
  useEffect(() => {
    if (loader === 'fabric' && mcVersion) {
      setIsLoadingFabricLoaders(true);
      getFabricLoaderVersions(mcVersion)
        .then((loaders) => {
          setFabricLoaders(loaders);
          if (loaders.length > 0) {
            const stable = loaders.find((l) => l.stable);
            setSelectedFabricLoader(stable ? stable.version : loaders[0].version);
          }
        })
        .catch(() => {
          setFabricLoaders([]);
        })
        .finally(() => {
          setIsLoadingFabricLoaders(false);
        });
    }
  }, [loader, mcVersion]);

  // Fetch Forge versions when loader is forge and mcVersion changes
  useEffect(() => {
    if (loader === 'forge' && mcVersion) {
      setIsLoadingForgeVersions(true);
      getForgeVersions(mcVersion)
        .then((versions) => {
          setForgeVersions(versions);
          if (versions.length > 0) {
            const rec = versions.find((v) => v.isRecommended);
            setSelectedForgeVersion(rec ? rec.rawVersion : versions[0].rawVersion);
          }
        })
        .catch(() => {
          setForgeVersions([]);
        })
        .finally(() => {
          setIsLoadingForgeVersions(false);
        });
    }
  }, [loader, mcVersion]);

  // Fetch NeoForge versions when loader is neoforge and mcVersion changes
  useEffect(() => {
    if (loader === 'neoforge' && mcVersion) {
      setIsLoadingNeoForgeVersions(true);
      getNeoForgeVersions(mcVersion)
        .then((versions) => {
          setNeoForgeVersions(versions);
          if (versions.length > 0) {
            const rec = versions.find((v) => v.isRecommended);
            setSelectedNeoForgeVersion(rec ? rec.rawVersion : versions[0].rawVersion);
          }
        })
        .catch(() => {
          setNeoForgeVersions([]);
        })
        .finally(() => {
          setIsLoadingNeoForgeVersions(false);
        });
    }
  }, [loader, mcVersion]);

  // Fetch Quilt versions when loader is quilt and mcVersion changes
  useEffect(() => {
    if (loader === 'quilt' && mcVersion) {
      setIsLoadingQuiltLoaders(true);
      getQuiltLoaderVersions(mcVersion)
        .then((loaders) => {
          setQuiltLoaders(loaders);
          if (loaders.length > 0) {
            const stable = loaders.find((l) => l.stable);
            setSelectedQuiltLoader(stable ? stable.version : loaders[0].version);
          }
        })
        .catch(() => {
          setQuiltLoaders([]);
        })
        .finally(() => {
          setIsLoadingQuiltLoaders(false);
        });
    }
  }, [loader, mcVersion]);

  // Build available versions list from real Mojang manifest
  const availableVersionsOptions = useMemo(() => {
    if (!mojangManifest?.versions) {
      return FALLBACK_MINECRAFT_VERSIONS;
    }

    const filtered = mojangManifest.versions.filter((v) => {
      if (versionTypeFilter === 'all') return true;
      return v.type === versionTypeFilter;
    });

    // Take top 40 versions to keep dropdown responsive
    return filtered.slice(0, 40).map((v) => {
      const isLatest = v.id === mojangManifest.latest.release;
      const label = `${v.id} (${v.type.toUpperCase()}${isLatest ? ' • NAJNOWSZA' : ''})`;
      return { value: v.id, label };
    });
  }, [mojangManifest, versionTypeFilter]);

  const filteredInstallations = installations.filter((inst) => {
    const matchesSearch =
      inst.name.toLowerCase().includes(search.toLowerCase()) ||
      inst.minecraftVersion.includes(search);
    const matchesLoader = selectedLoaderFilter === 'all' || inst.loader === selectedLoaderFilter;
    return matchesSearch && matchesLoader;
  });

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    const finalLoaderVersion =
      loader === 'neoforge'
        ? selectedNeoForgeVersion || (neoForgeVersions[0]?.rawVersion ?? '20.4.80-beta')
        : loader === 'forge'
        ? selectedForgeVersion || (forgeVersions[0]?.rawVersion ?? '49.1.0')
        : loader === 'fabric'
        ? selectedFabricLoader || '0.16.10'
        : loader === 'quilt'
        ? selectedQuiltLoader || '0.27.1-beta.1'
        : mcVersion;

    const finalName =
      name.trim() ||
      `Minecraft ${mcVersion} (${
        loader === 'neoforge'
          ? `NeoForge ${finalLoaderVersion}`
          : loader === 'forge'
          ? `Forge ${finalLoaderVersion}`
          : loader === 'fabric'
          ? `Fabric ${finalLoaderVersion}`
          : loader === 'quilt'
          ? `Quilt ${finalLoaderVersion}`
          : 'Vanilla'
      })`;

    await addInstallation({
      name: finalName,
      minecraftVersion: mcVersion,
      loader,
      loaderVersion: finalLoaderVersion,
      icon: loader === 'quilt' ? 'sparkles' : loader === 'neoforge' ? 'shield' : loader === 'forge' ? 'shield' : loader === 'fabric' ? 'gem' : 'sword',
      allocatedRam,
      customJvmArgs: '-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200',
      assignedAccountId: selectedAccountId || undefined,
    });

    setName('');
    setShowAddModal(false);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Top action toolbar */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '16px',
          flexWrap: 'wrap',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
          <SearchBar value={search} onChange={setSearch} placeholder="Szukaj instalacji..." />

          {/* Loader filter pills */}
          <div style={{ display: 'flex', gap: '6px' }}>
            {['all', 'vanilla', 'fabric', 'forge', 'neoforge', 'quilt'].map((ld) => {
              const isSelected = selectedLoaderFilter === ld;
              return (
                <button
                  key={ld}
                  onClick={() => setSelectedLoaderFilter(ld)}
                  style={{
                    padding: '6px 12px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    background: isSelected ? 'var(--bg-surface-active)' : 'var(--bg-surface)',
                    border: `1px solid ${isSelected ? 'var(--frost-500)' : 'var(--border-subtle)'}`,
                    color: isSelected ? 'var(--frost-400)' : 'var(--text-secondary)',
                    textTransform: 'uppercase',
                  }}
                >
                  {ld === 'all' ? 'Wszystkie' : ld}
                </button>
              );
            })}
          </div>
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <Button
            variant="secondary"
            icon={<RefreshIcon size={14} />}
            onClick={() => refreshVersionsManifest(true)}
            title="Odśwież manifest z serwerów Mojang"
          >
            Odśwież manifest Mojang
          </Button>

          <Button
            variant="primary"
            icon={<PlusIcon size={16} />}
            onClick={() => setShowAddModal(true)}
          >
            Nowa Instalacja
          </Button>
        </div>
      </div>

      {/* Manifest Info Banner */}
      {mojangManifest && (
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '8px',
            padding: '10px 16px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            fontSize: '12px',
            color: 'var(--text-secondary)',
          }}
        >
          <div>
            Połączono z Mojang Piston Meta: Dostępnych <strong>{mojangManifest.versions.length}</strong> wersji gry.
            Najnowszy Release: <strong>{mojangManifest.latest.release}</strong> • Snapshot: <strong>{mojangManifest.latest.snapshot}</strong>
          </div>
          <Badge variant="success">Mojang API Online</Badge>
        </div>
      )}

      {/* Installations Table / Cards List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
        {filteredInstallations.map((inst) => {
          const isActive = inst.id === activeInstallation.id;
          const assignedAcc = accounts.find((a) => a.id === inst.assignedAccountId);
          return (
            <div
              key={inst.id}
              style={{
                background: isActive ? 'var(--bg-surface-active)' : 'var(--bg-card)',
                border: `1px solid ${isActive ? 'var(--pine-500)' : 'var(--border-subtle)'}`,
                borderRadius: '10px',
                padding: '16px 20px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '16px',
                flexWrap: 'wrap',
                transition: 'all 0.16s ease',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                <div
                  style={{
                    width: '44px',
                    height: '44px',
                    borderRadius: '8px',
                    background: isActive ? 'var(--pine-800)' : 'var(--bg-surface)',
                    border: `1px solid ${isActive ? 'var(--pine-400)' : 'var(--border-light)'}`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: isActive ? 'var(--pine-400)' : 'var(--text-muted)',
                  }}
                >
                  <VersionsIcon size={22} />
                </div>

                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <h4 style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff' }}>
                      {inst.name}
                    </h4>
                    {isActive && <Badge variant="success">AKTYWNY</Badge>}
                    <Badge variant={inst.loader as any}>
                      {inst.loader === 'fabric'
                        ? `FABRIC ${inst.loaderVersion ? `v${inst.loaderVersion}` : ''}`
                        : inst.loader === 'forge'
                        ? `FORGE ${inst.loaderVersion ? `v${inst.loaderVersion}` : ''}`
                        : inst.loader === 'neoforge'
                        ? `NEOFORGE ${inst.loaderVersion ? `v${inst.loaderVersion}` : ''}`
                        : inst.loader === 'quilt'
                        ? `QUILT ${inst.loaderVersion ? `v${inst.loaderVersion}` : ''}`
                        : inst.loader.toUpperCase()}
                    </Badge>
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '3px' }}>
                    Minecraft {inst.minecraftVersion}
                    {inst.loader === 'fabric' && inst.loaderVersion && ` • Fabric ${inst.loaderVersion}`}
                    {inst.loader === 'forge' && inst.loaderVersion && ` • Forge ${inst.loaderVersion}`}
                    {inst.loader === 'neoforge' && inst.loaderVersion && ` • NeoForge ${inst.loaderVersion}`}
                    {inst.loader === 'quilt' && inst.loaderVersion && ` • Quilt ${inst.loaderVersion}`}
                    {' • '}Konto:{' '}
                    <strong style={{ color: 'var(--text-secondary)' }}>
                      {assignedAcc ? assignedAcc.username : 'Domyślne aktywne'}
                    </strong>{' '}
                    • RAM: {inst.allocatedRam} MB • Ostatnia gra: {inst.lastPlayed}
                  </div>
                </div>
              </div>

              {/* Action Buttons */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Button
                  variant="play"
                  size="sm"
                  icon={<PlayIcon size={14} />}
                  onClick={() => {
                    setActiveInstallationId(inst.id);
                    launchGame(inst.id);
                  }}
                  disabled={isLaunching}
                >
                  Graj
                </Button>

                {!isActive && (
                  <Button
                    variant="secondary"
                    size="sm"
                    icon={<CheckIcon size={14} />}
                    onClick={() => {
                      setActiveInstallationId(inst.id);
                      addToast({
                        type: 'info',
                        title: 'Zmieniono profil',
                        message: `Aktywna instalacja: ${inst.name}`,
                      });
                    }}
                  >
                    Wybierz
                  </Button>
                )}

                <Button
                  variant="ghost"
                  size="sm"
                  icon={<TrashIcon size={14} />}
                  onClick={() => deleteInstallation(inst.id)}
                  title="Usuń instalację"
                >
                  Usuń
                </Button>
              </div>
            </div>
          );
        })}

        {filteredInstallations.length === 0 && (
          <div
            style={{
              padding: '40px',
              textAlign: 'center',
              color: 'var(--text-muted)',
              background: 'var(--bg-card)',
              borderRadius: '10px',
              border: '1px solid var(--border-subtle)',
            }}
          >
            Brak instalacji spełniających kryteria wyszukiwania.
          </div>
        )}
      </div>

      {/* Modal: Create Installation Form */}
      <Modal
        isOpen={showAddModal}
        onClose={() => setShowAddModal(false)}
        title="Utwórz nową instalację Minecraft"
        subtitle="Wybierz oficjalną wersję gry pobieraną bezpośrednio z serwerów Mojang."
        footer={
          <>
            <Button variant="secondary" onClick={() => setShowAddModal(false)}>
              Anuluj
            </Button>
            <Button variant="primary" onClick={handleCreate}>
              Utwórz instalację
            </Button>
          </>
        }
      >
        <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
              Nazwa instalacji
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="np. Minecraft Vanilla 1.20.4"
              style={{
                width: '100%',
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                borderRadius: '6px',
                padding: '8px 12px',
                color: '#ffffff',
                fontSize: '13px',
              }}
            />
          </div>

          {/* Filter Version Type */}
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Filtr wydań:</span>
            {(['release', 'snapshot', 'all'] as const).map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => setVersionTypeFilter(t)}
                style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '11px',
                  fontWeight: 600,
                  cursor: 'pointer',
                  background: versionTypeFilter === t ? 'var(--frost-500)' : 'var(--bg-surface)',
                  color: versionTypeFilter === t ? '#ffffff' : 'var(--text-muted)',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                {t.toUpperCase()}
              </button>
            ))}
          </div>

          <Dropdown
            label="Wersja Minecraft (z manifestu Mojang)"
            options={availableVersionsOptions}
            value={mcVersion}
            onChange={(val) => setMcVersion(val as string)}
          />

          <Dropdown
            label="Rodzaj modloadera"
            options={LOADERS}
            value={loader}
            onChange={(val) => setLoader(val as MinecraftLoaderType)}
          />

          {loader === 'fabric' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                  Wersja Fabric Loader (z oficjalnego Fabric Meta API)
                </label>
                <Badge variant="success">Fabric Meta API</Badge>
              </div>
              {isLoadingFabricLoaders ? (
                <div style={{ fontSize: '12px', color: 'var(--text-muted)', padding: '6px 0' }}>
                  Pobieranie kompatybilnych wersji Fabric Loader dla Minecraft {mcVersion}...
                </div>
              ) : fabricLoaders.length > 0 ? (
                <Dropdown
                  label=""
                  options={fabricLoaders.slice(0, 25).map((fl) => ({
                    value: fl.version,
                    label: `${fl.version} (${fl.stable ? 'STABILNA / ZALECANA' : 'SNAPSHOT / EKSPERYMENTALNA'})`
                  }))}
                  value={selectedFabricLoader}
                  onChange={(val) => setSelectedFabricLoader(val as string)}
                />
              ) : (
                <div style={{ fontSize: '12px', color: 'var(--crimson-400)', padding: '6px 0' }}>
                  Brak kompatybilnych wersji Fabric Loader dla Minecraft {mcVersion}.
                </div>
              )}
            </div>
          )}

          {loader === 'forge' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                  Wersja Minecraft Forge (z oficjalnego Forge Maven)
                </label>
                <Badge variant="forge">Forge Maven</Badge>
              </div>
              {isLoadingForgeVersions ? (
                <div style={{ fontSize: '12px', color: 'var(--text-muted)', padding: '6px 0' }}>
                  Pobieranie kompatybilnych wersji Forge dla Minecraft {mcVersion}...
                </div>
              ) : forgeVersions.length > 0 ? (
                <Dropdown
                  label=""
                  options={forgeVersions.slice(0, 30).map((fv) => ({
                    value: fv.rawVersion,
                    label: `${fv.rawVersion} ${
                      fv.isRecommended
                        ? '(ZALECANA / RECOMMENDED)'
                        : fv.isLatest
                        ? '(NAJNOWSZA / LATEST)'
                        : ''
                    }`,
                  }))}
                  value={selectedForgeVersion}
                  onChange={(val) => setSelectedForgeVersion(val as string)}
                />
              ) : (
                <div style={{ fontSize: '12px', color: 'var(--crimson-400)', padding: '6px 0' }}>
                  Brak kompatybilnych wersji Forge dla Minecraft {mcVersion}.
                </div>
              )}
            </div>
          )}

          {loader === 'neoforge' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                  Wersja NeoForge (z oficjalnego NeoForge Maven)
                </label>
                <Badge variant="neoforge">NeoForge Maven</Badge>
              </div>
              {isLoadingNeoForgeVersions ? (
                <div style={{ fontSize: '12px', color: 'var(--text-muted)', padding: '6px 0' }}>
                  Pobieranie kompatybilnych wersji NeoForge dla Minecraft {mcVersion}...
                </div>
              ) : neoForgeVersions.length > 0 ? (
                <Dropdown
                  label=""
                  options={neoForgeVersions.slice(0, 30).map((nv) => ({
                    value: nv.rawVersion,
                    label: `${nv.rawVersion} ${
                      nv.isRecommended
                        ? '(ZALECANA / RECOMMENDED)'
                        : nv.isLatest
                        ? '(NAJNOWSZA / LATEST)'
                        : ''
                    } • ${nv.generation === 'transition' ? 'Transition (1.20.1)' : 'Modern'}`,
                  }))}
                  value={selectedNeoForgeVersion}
                  onChange={(val) => setSelectedNeoForgeVersion(val as string)}
                />
              ) : (
                <div style={{ fontSize: '12px', color: 'var(--crimson-400)', padding: '6px 0' }}>
                  Brak kompatybilnych wersji NeoForge dla Minecraft {mcVersion}. NeoForge obsługuje wersje Minecraft 1.20.1 oraz nowsze (1.20.2+).
                </div>
              )}
            </div>
          )}

          {loader === 'quilt' && (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                  Wersja Quilt Loader (z oficjalnego Quilt Meta API v3)
                </label>
                <Badge variant="quilt">Quilt Meta v3</Badge>
              </div>
              {isLoadingQuiltLoaders ? (
                <div style={{ fontSize: '12px', color: 'var(--text-muted)', padding: '6px 0' }}>
                  Pobieranie kompatybilnych wersji Quilt Loader dla Minecraft {mcVersion}...
                </div>
              ) : quiltLoaders.length > 0 ? (
                <Dropdown
                  label=""
                  options={quiltLoaders.slice(0, 30).map((ql) => ({
                    value: ql.version,
                    label: `${ql.version} (${ql.stable ? 'STABILNA / ZALECANA' : 'BETA / WYDANIE'}), build ${ql.build}`
                  }))}
                  value={selectedQuiltLoader}
                  onChange={(val) => setSelectedQuiltLoader(val as string)}
                />
              ) : (
                <div style={{ fontSize: '12px', color: 'var(--crimson-400)', padding: '6px 0' }}>
                  Brak kompatybilnych wersji Quilt Loader dla Minecraft {mcVersion}.
                </div>
              )}
            </div>
          )}

          <Dropdown
            label="Przypisane konto gracza"
            options={[
              { value: '', label: 'Domyślne aktywne konto launchera' },
              ...accounts.map((a) => ({
                value: a.id,
                label: `${a.username} (${a.type === 'microsoft' ? 'Microsoft' : 'Offline'})`,
              })),
            ]}
            value={selectedAccountId}
            onChange={(val) => setSelectedAccountId(val as string)}
          />

          <div>
            <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
              Pamięć RAM dla profilu (MB)
            </label>
            <input
              type="number"
              min={1024}
              max={16384}
              step={512}
              value={allocatedRam}
              onChange={(e) => setAllocatedRam(parseInt(e.target.value, 10) || 4096)}
              style={{
                width: '100%',
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                borderRadius: '6px',
                padding: '8px 12px',
                color: '#ffffff',
                fontSize: '13px',
              }}
            />
          </div>

          <div
            style={{
              padding: '10px 12px',
              borderRadius: '6px',
              background: 'rgba(16, 185, 129, 0.1)',
              border: '1px solid rgba(16, 185, 129, 0.3)',
              fontSize: '12px',
              color: 'var(--pine-400)',
            }}
          >
            Silnik automatycznie pobierze pliki klienta, biblioteki i zasoby gry podczas pierwszego uruchomienia profilu.
          </div>
        </form>
      </Modal>
    </div>
  );
};
