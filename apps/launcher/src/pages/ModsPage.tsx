import React, { useState, useMemo } from 'react';
import { useLauncherContext, ModItem, ModCategory, ModSettings } from '../stores';
import { Button, Badge, Modal, SearchBar, Toggle, Slider, Dropdown } from '../components/common';
import {
  ModsIcon,
  GearIcon,
  SwordIcon,
  ShieldIcon,
  CompassIcon,
  SparkleIcon,
  CheckIcon,
  RefreshIcon,
  PlusIcon,
  TrashIcon,
  DownloadIcon,
  ExternalLinkIcon,
  CopyIcon,
  CloseIcon,
  GlobeIcon,
} from '../components/icons';
import { ModrinthCatalog, ModUpdatesPanel } from '../components/modrinth';
import type { ModRecord, ModValidationResult, ModCompatibilityStatus } from '@zombek/shared-types';

const CATEGORIES: { id: ModCategory | 'all'; label: string; icon?: React.ReactNode }[] = [
  { id: 'all', label: 'Wszystkie Moduły HUD' },
  { id: 'pvp', label: 'PvP / Walka', icon: <SwordIcon size={14} /> },
  { id: 'hud', label: 'HUD / Informacje', icon: <CompassIcon size={14} /> },
  { id: 'visual', label: 'Grafika / Wizualne', icon: <SparkleIcon size={14} /> },
  { id: 'survival', label: 'Survival / Świat', icon: <ShieldIcon size={14} /> },
  { id: 'other', label: 'Inne i Narzędzia' },
];

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

export const ModsPage: React.FC = () => {
  const {
    // Installation
    installations,
    activeInstallation,
    setActiveInstallationId,
    // Real Mods (Etap 6A)
    realMods,
    isLoadingMods,
    loadModsForInstance,
    validateModFile,
    installModFile,
    toggleRealMod,
    deleteRealMod,
    openModFileDialog,
    openModsFolder,
    // HUD Preview Mods
    mods,
    toggleMod,
    updateModSettings,
    resetModSettings,
    addToast,
  } = useLauncherContext();

  // Page view mode: Real JAR files vs Modrinth Catalog vs Auto Updates vs HUD Preview Overlays
  const [activeTab, setActiveTab] = useState<'real' | 'modrinth' | 'updates' | 'hud'>('real');

  // Real Mods State
  const [searchReal, setSearchReal] = useState('');
  const [realFilter, setRealFilter] = useState<'all' | 'enabled' | 'disabled' | 'compatible' | 'issues'>('all');
  const [realSortBy, setRealSortBy] = useState<'name' | 'date' | 'size' | 'status'>('name');

  // Modals for Real Mods
  const [inspectMod, setInspectMod] = useState<ModRecord | null>(null);
  const [modToDelete, setModToDelete] = useState<ModRecord | null>(null);
  const [showAddModal, setShowAddModal] = useState(false);

  // Add Mod Form State
  const [selectedFilePath, setSelectedFilePath] = useState('');
  const [isValidating, setIsValidating] = useState(false);
  const [validationResult, setValidationResult] = useState<ModValidationResult | null>(null);
  const [overwriteDuplicate, setOverwriteDuplicate] = useState(false);
  const [isInstalling, setIsInstalling] = useState(false);

  // HUD Preview State
  const [activeCategory, setActiveCategory] = useState<ModCategory | 'all'>('all');
  const [searchHud, setSearchHud] = useState('');
  const [sortByHud, setSortByHud] = useState<'name' | 'enabled'>('name');
  const [editingMod, setEditingMod] = useState<ModItem | null>(null);

  // Filtered & Sorted Real Mods
  const filteredRealMods = useMemo(() => {
    return realMods
      .filter((m) => {
        const query = searchReal.toLowerCase().trim();
        const matchesQuery =
          !query ||
          m.metadata.name.toLowerCase().includes(query) ||
          m.metadata.id.toLowerCase().includes(query) ||
          m.fileName.toLowerCase().includes(query) ||
          m.metadata.description.toLowerCase().includes(query) ||
          m.metadata.authors.some((a) => a.toLowerCase().includes(query));

        if (!matchesQuery) return false;

        if (realFilter === 'enabled') return m.enabled;
        if (realFilter === 'disabled') return !m.enabled;
        if (realFilter === 'compatible') return m.compatibilityStatus === 'Compatible';
        if (realFilter === 'issues') {
          return [
            'Incompatible',
            'Missing Dependency',
            'Dependency Conflict',
            'Broken File',
            'Possibly Compatible',
            'Unknown Metadata',
          ].includes(m.compatibilityStatus);
        }
        return true;
      })
      .sort((a, b) => {
        if (realSortBy === 'name') {
          return a.metadata.name.localeCompare(b.metadata.name);
        }
        if (realSortBy === 'date') {
          return new Date(b.dateAdded).getTime() - new Date(a.dateAdded).getTime();
        }
        if (realSortBy === 'size') {
          return b.fileSize - a.fileSize;
        }
        if (realSortBy === 'status') {
          return a.compatibilityStatus.localeCompare(b.compatibilityStatus);
        }
        return 0;
      });
  }, [realMods, searchReal, realFilter, realSortBy]);

  const activeModsCount = realMods.filter((m) => m.enabled).length;
  const disabledModsCount = realMods.filter((m) => !m.enabled).length;

  // Filtered HUD Mods
  const filteredHudMods = mods
    .filter((mod) => {
      const matchesCat = activeCategory === 'all' || mod.category === activeCategory;
      const matchesSearch =
        mod.name.toLowerCase().includes(searchHud.toLowerCase()) ||
        mod.description.toLowerCase().includes(searchHud.toLowerCase());
      return matchesCat && matchesSearch;
    })
    .sort((a, b) => {
      if (sortByHud === 'enabled') {
        if (a.enabled === b.enabled) return a.name.localeCompare(b.name);
        return a.enabled ? -1 : 1;
      }
      return a.name.localeCompare(b.name);
    });

  // Pick file handler for Add Mod
  const handlePickFile = async () => {
    const paths = await openModFileDialog();
    if (paths && paths.length > 0) {
      const chosen = paths[0];
      setSelectedFilePath(chosen);
      await triggerValidation(chosen);
    }
  };

  const triggerValidation = async (path: string) => {
    if (!path.trim()) return;
    setIsValidating(true);
    try {
      const result = await validateModFile(path.trim(), activeInstallation.id);
      setValidationResult(result);
      if (result.isDuplicate) {
        setOverwriteDuplicate(true);
      }
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd walidacji', message: err.message });
    } finally {
      setIsValidating(false);
    }
  };

  const handleInstallMod = async () => {
    if (!selectedFilePath) return;
    setIsInstalling(true);
    try {
      const res = await installModFile(selectedFilePath, activeInstallation.id, overwriteDuplicate);
      if (res.success) {
        setShowAddModal(false);
        setSelectedFilePath('');
        setValidationResult(null);
        setOverwriteDuplicate(false);
      }
    } finally {
      setIsInstalling(false);
    }
  };

  const getStatusBadge = (status: ModCompatibilityStatus) => {
    switch (status) {
      case 'Compatible':
        return <Badge variant="success">Kompatybilny</Badge>;
      case 'Possibly Compatible':
        return <Badge variant="gold">Prawdopodobnie działa</Badge>;
      case 'Missing Dependency':
        return <Badge variant="danger">Brak zależności</Badge>;
      case 'Dependency Conflict':
        return <Badge variant="danger">Konflikt modów</Badge>;
      case 'Incompatible':
        return <Badge variant="danger">Niekompatybilny</Badge>;
      case 'Broken File':
        return <Badge variant="danger">Uszkodzony JAR</Badge>;
      case 'Disabled':
        return <Badge variant="neutral">Wyłączony</Badge>;
      case 'Unknown Metadata':
      default:
        return <Badge variant="neutral">Brak metadanych</Badge>;
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* View Switcher: Real JAR Mods vs HUD Overlay Preview */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '12px',
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '12px',
          padding: '12px 18px',
        }}
      >
        <div style={{ display: 'flex', gap: '8px' }}>
          <button
            onClick={() => setActiveTab('real')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '8px',
              fontWeight: 700,
              fontSize: '13px',
              cursor: 'pointer',
              background: activeTab === 'real' ? 'var(--frost-500)' : 'transparent',
              color: activeTab === 'real' ? '#ffffff' : 'var(--text-secondary)',
              border: `1px solid ${activeTab === 'real' ? 'var(--frost-400)' : 'transparent'}`,
              transition: 'all 0.16s ease',
            }}
          >
            <ModsIcon size={16} />
            <span>Pliki Modów (.jar)</span>
            <span
              style={{
                fontSize: '11px',
                padding: '2px 7px',
                borderRadius: '10px',
                background: activeTab === 'real' ? 'rgba(0,0,0,0.25)' : 'var(--bg-surface)',
                color: '#ffffff',
              }}
            >
              {realMods.length}
            </span>
          </button>

          <button
            onClick={() => setActiveTab('modrinth')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '8px',
              fontWeight: 700,
              fontSize: '13px',
              cursor: 'pointer',
              background: activeTab === 'modrinth' ? 'var(--frost-500)' : 'transparent',
              color: activeTab === 'modrinth' ? '#ffffff' : 'var(--text-secondary)',
              border: `1px solid ${activeTab === 'modrinth' ? 'var(--frost-400)' : 'transparent'}`,
              transition: 'all 0.16s ease',
            }}
          >
            <GlobeIcon size={16} />
            <span>Katalog Modrinth (Online)</span>
            <Badge variant="success">API v2</Badge>
          </button>

          <button
            onClick={() => setActiveTab('updates')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '8px',
              fontWeight: 700,
              fontSize: '13px',
              cursor: 'pointer',
              background: activeTab === 'updates' ? 'var(--frost-500)' : 'transparent',
              color: activeTab === 'updates' ? '#ffffff' : 'var(--text-secondary)',
              border: `1px solid ${activeTab === 'updates' ? 'var(--frost-400)' : 'transparent'}`,
              transition: 'all 0.16s ease',
            }}
          >
            <RefreshIcon size={16} />
            <span>Aktualizacje i Zależności</span>
            <Badge variant="fabric">AUTO</Badge>
          </button>

          <button
            onClick={() => setActiveTab('hud')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '8px 16px',
              borderRadius: '8px',
              fontWeight: 700,
              fontSize: '13px',
              cursor: 'pointer',
              background: activeTab === 'hud' ? 'var(--pine-500)' : 'transparent',
              color: activeTab === 'hud' ? '#ffffff' : 'var(--text-secondary)',
              border: `1px solid ${activeTab === 'hud' ? 'var(--pine-400)' : 'transparent'}`,
              transition: 'all 0.16s ease',
            }}
          >
            <CompassIcon size={16} />
            <span>Nakładki HUD i Moduły PvP (Lunar Preview)</span>
            <Badge variant="preview">UI PREVIEW</Badge>
          </button>
        </div>

        {/* Quick Instance Switcher */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Aktywna instalacja:</span>
          <select
            value={activeInstallation.id}
            onChange={(e) => setActiveInstallationId(e.target.value)}
            style={{
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-light)',
              color: '#ffffff',
              borderRadius: '8px',
              padding: '6px 12px',
              fontSize: '12.5px',
              fontWeight: 600,
              outline: 'none',
              cursor: 'pointer',
            }}
          >
            {installations.map((inst) => (
              <option key={inst.id} value={inst.id}>
                {inst.name} ({inst.loader.toUpperCase()} {inst.minecraftVersion})
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* TAB 1: REAL MODS MANAGER (.JAR)                                           */}
      {/* ========================================================================= */}
      {activeTab === 'real' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {/* Active Instance Context Header */}
          <div
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-light)',
              borderRadius: '12px',
              padding: '18px 22px',
              display: 'flex',
              flexDirection: 'column',
              gap: '14px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <h2 style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff' }}>
                    Mody Instalacji: {activeInstallation.name}
                  </h2>
                  <Badge variant={activeInstallation.loader as any}>
                    {activeInstallation.loader.toUpperCase()} {activeInstallation.minecraftVersion}
                  </Badge>
                  {activeInstallation.loader === 'vanilla' && (
                    <Badge variant="danger">Vanilla (Brak modów w silniku)</Badge>
                  )}
                </div>
                <p style={{ fontSize: '12.5px', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Zarządzaj lokalnymi plikami modów <code>.jar</code>. Aktywne pliki są ładowane z katalogu{' '}
                  <code>mods/</code>, a wyłączone przechowywane w <code>disabled-mods/</code>.
                </p>
              </div>

              {/* Action Buttons */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Button
                  variant="secondary"
                  size="sm"
                  icon={<RefreshIcon size={14} />}
                  onClick={() => loadModsForInstance(activeInstallation.id)}
                  title="Odśwież listę modów z dysku"
                >
                  Odśwież
                </Button>

                <Button
                  variant="secondary"
                  size="sm"
                  icon={<ExternalLinkIcon size={14} />}
                  onClick={() => openModsFolder(activeInstallation.id)}
                  title="Otwórz folder mods w Eksploratorze plików"
                >
                  Otwórz Folder Mods
                </Button>

                <Button
                  variant="primary"
                  size="sm"
                  icon={<PlusIcon size={15} />}
                  onClick={() => {
                    setSelectedFilePath('');
                    setValidationResult(null);
                    setShowAddModal(true);
                  }}
                >
                  Dodaj Mod (.jar)
                </Button>
              </div>
            </div>

            {/* Vanilla notice if user is on Vanilla instance */}
            {activeInstallation.loader === 'vanilla' && (
              <div
                style={{
                  background: 'rgba(239, 68, 68, 0.1)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                  borderRadius: '8px',
                  padding: '12px 16px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px',
                }}
              >
                <div style={{ color: '#f87171', fontWeight: 700, fontSize: '13px' }}>
                  Uwaga: Wybrana instalacja używa silnika Vanilla Minecraft!
                </div>
                <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                  Vanilla Minecraft domyślnie nie wczytuje modyfikacji z folderu <code>mods/</code>. Aby gra uruchamiała
                  mody, utwórz nową instalację lub zmień loader na Fabric, Forge, NeoForge lub Quilt.
                </div>
              </div>
            )}

            {/* Real Stats & Filter Controls */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: '12px',
                borderTop: '1px solid var(--border-subtle)',
                paddingTop: '12px',
              }}
            >
              <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                <button
                  onClick={() => setRealFilter('all')}
                  style={{
                    padding: '6px 12px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    background: realFilter === 'all' ? 'var(--bg-surface-active)' : 'transparent',
                    border: `1px solid ${realFilter === 'all' ? 'var(--frost-500)' : 'var(--border-subtle)'}`,
                    color: realFilter === 'all' ? 'var(--frost-400)' : 'var(--text-secondary)',
                  }}
                >
                  Wszystkie ({realMods.length})
                </button>

                <button
                  onClick={() => setRealFilter('enabled')}
                  style={{
                    padding: '6px 12px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    background: realFilter === 'enabled' ? 'rgba(16, 185, 129, 0.15)' : 'transparent',
                    border: `1px solid ${realFilter === 'enabled' ? 'var(--pine-500)' : 'var(--border-subtle)'}`,
                    color: realFilter === 'enabled' ? '#34d399' : 'var(--text-secondary)',
                  }}
                >
                  Włączone ({activeModsCount})
                </button>

                <button
                  onClick={() => setRealFilter('disabled')}
                  style={{
                    padding: '6px 12px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    background: realFilter === 'disabled' ? 'var(--bg-surface-active)' : 'transparent',
                    border: `1px solid ${realFilter === 'disabled' ? 'var(--border-light)' : 'var(--border-subtle)'}`,
                    color: realFilter === 'disabled' ? 'var(--text-primary)' : 'var(--text-secondary)',
                  }}
                >
                  Wyłączone ({disabledModsCount})
                </button>

                <button
                  onClick={() => setRealFilter('compatible')}
                  style={{
                    padding: '6px 12px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    background: realFilter === 'compatible' ? 'rgba(56, 189, 248, 0.15)' : 'transparent',
                    border: `1px solid ${realFilter === 'compatible' ? 'var(--frost-500)' : 'var(--border-subtle)'}`,
                    color: realFilter === 'compatible' ? '#38bdf8' : 'var(--text-secondary)',
                  }}
                >
                  Kompatybilne ({realMods.filter((m) => m.compatibilityStatus === 'Compatible').length})
                </button>

                <button
                  onClick={() => setRealFilter('issues')}
                  style={{
                    padding: '6px 12px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    background: realFilter === 'issues' ? 'rgba(239, 68, 68, 0.15)' : 'transparent',
                    border: `1px solid ${realFilter === 'issues' ? '#ef4444' : 'var(--border-subtle)'}`,
                    color: realFilter === 'issues' ? '#f87171' : 'var(--text-secondary)',
                  }}
                >
                  Z uwagami ({realMods.filter((m) => m.compatibilityStatus !== 'Compatible').length})
                </button>
              </div>

              {/* Search & Sort */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <SearchBar
                  value={searchReal}
                  onChange={setSearchReal}
                  placeholder="Filtruj mody po nazwie, ID, autorze..."
                />

                <select
                  value={realSortBy}
                  onChange={(e) => setRealSortBy(e.target.value as any)}
                  style={{
                    padding: '6px 12px',
                    borderRadius: '6px',
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-light)',
                    color: 'var(--text-secondary)',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    outline: 'none',
                  }}
                >
                  <option value="name">Sortuj: Nazwa (A-Z)</option>
                  <option value="date">Sortuj: Data dodania</option>
                  <option value="size">Sortuj: Rozmiar pliku</option>
                  <option value="status">Sortuj: Status kompatybilności</option>
                </select>
              </div>
            </div>
          </div>

          {/* Loading Indicator */}
          {isLoadingMods && (
            <div
              style={{
                padding: '40px',
                textAlign: 'center',
                color: 'var(--text-muted)',
                fontSize: '14px',
                background: 'var(--bg-card)',
                borderRadius: '12px',
                border: '1px solid var(--border-light)',
              }}
            >
              Wczytywanie i analiza plików JAR w instalacji...
            </div>
          )}

          {/* Empty State */}
          {!isLoadingMods && filteredRealMods.length === 0 && (
            <div
              style={{
                background: 'var(--bg-card)',
                border: '1px dashed var(--border-light)',
                borderRadius: '12px',
                padding: '48px 24px',
                textAlign: 'center',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '12px',
              }}
            >
              <div
                style={{
                  width: '48px',
                  height: '48px',
                  borderRadius: '50%',
                  background: 'var(--bg-surface)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--frost-400)',
                }}
              >
                <ModsIcon size={24} />
              </div>
              <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>
                {realMods.length === 0 ? 'Brak modów w tej instalacji' : 'Brak modów spełniających kryteria wyszukiwania'}
              </h3>
              <p style={{ fontSize: '13px', color: 'var(--text-muted)', maxWidth: '440px' }}>
                {realMods.length === 0
                  ? 'Katalog mods/ jest pusty. Kliknij poniższy przycisk, aby wybrać plik .jar ze swojego dysku.'
                  : 'Zmień frazę wyszukiwania lub zresetuj filtry statusu.'}
              </p>
              {realMods.length === 0 && (
                <Button
                  variant="primary"
                  icon={<PlusIcon size={15} />}
                  onClick={() => {
                    setSelectedFilePath('');
                    setValidationResult(null);
                    setShowAddModal(true);
                  }}
                  style={{ marginTop: '8px' }}
                >
                  Dodaj swój pierwszy mod (.jar)
                </Button>
              )}
            </div>
          )}

          {/* Real Mod Cards Grid */}
          {!isLoadingMods && filteredRealMods.length > 0 && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '16px' }}>
              {filteredRealMods.map((mod) => (
                <div
                  key={mod.fileName}
                  style={{
                    background: 'var(--bg-card)',
                    border: `1px solid ${mod.enabled ? 'var(--border-light)' : 'var(--border-subtle)'}`,
                    borderRadius: '12px',
                    padding: '18px',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    gap: '14px',
                    opacity: mod.enabled ? 1 : 0.65,
                    transition: 'all 0.18s ease',
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.borderColor = mod.enabled ? 'var(--frost-500)' : 'var(--border-light)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = mod.enabled ? 'var(--border-light)' : 'var(--border-subtle)';
                  }}
                >
                  <div>
                    {/* Top row: Mod Title, Loader Badge, Status & Toggle */}
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '10px' }}>
                      <div style={{ flex: 1, minWidth: 0 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                          <h3
                            style={{
                              fontSize: '15px',
                              fontWeight: 700,
                              color: '#ffffff',
                              whiteSpace: 'nowrap',
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              maxWidth: '220px',
                            }}
                            title={mod.metadata.name}
                          >
                            {mod.metadata.name}
                          </h3>
                          <Badge variant={mod.metadata.loader === 'unknown' ? 'neutral' : (mod.metadata.loader as any)}>
                            {mod.metadata.loader.toUpperCase()}
                          </Badge>
                          {getStatusBadge(mod.compatibilityStatus)}
                        </div>

                        <div style={{ display: 'flex', gap: '8px', marginTop: '4px', fontSize: '11.5px', color: 'var(--text-muted)' }}>
                          <span>ID: <code>{mod.metadata.id}</code></span>
                          <span>•</span>
                          <span>v{mod.metadata.version}</span>
                        </div>
                      </div>

                      {/* Enable / Disable switch */}
                      <Toggle
                        checked={mod.enabled}
                        onChange={() => toggleRealMod(activeInstallation.id, mod.fileName, !mod.enabled)}
                      />
                    </div>

                    {/* Description */}
                    <p
                      style={{
                        fontSize: '12px',
                        color: 'var(--text-secondary)',
                        marginTop: '10px',
                        lineHeight: 1.45,
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden',
                      }}
                      title={mod.metadata.description}
                    >
                      {mod.metadata.description || 'Brak opisu w metadanych moda.'}
                    </p>

                    {/* Issues Warning Box */}
                    {mod.compatibilityDetails && mod.compatibilityStatus !== 'Compatible' && (
                      <div
                        style={{
                          marginTop: '10px',
                          padding: '8px 10px',
                          borderRadius: '6px',
                          background: 'rgba(239, 68, 68, 0.1)',
                          border: '1px solid rgba(239, 68, 68, 0.25)',
                          fontSize: '11px',
                          color: '#f87171',
                          lineHeight: 1.35,
                        }}
                      >
                        {mod.compatibilityDetails}
                      </div>
                    )}

                    {/* Missing Dependencies Box */}
                    {mod.missingDependencies && mod.missingDependencies.length > 0 && (
                      <div
                        style={{
                          marginTop: '6px',
                          padding: '6px 10px',
                          borderRadius: '6px',
                          background: 'rgba(245, 158, 11, 0.1)',
                          border: '1px solid rgba(245, 158, 11, 0.25)',
                          fontSize: '11px',
                          color: '#fbbf24',
                        }}
                      >
                        Brakujące zależności: <strong>{mod.missingDependencies.join(', ')}</strong>
                      </div>
                    )}
                  </div>

                  {/* Bottom Card Controls */}
                  <div
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      paddingTop: '12px',
                      borderTop: '1px solid var(--border-subtle)',
                      fontSize: '11px',
                      color: 'var(--text-dim)',
                    }}
                  >
                    <div>
                      <span>{mod.fileName}</span>
                      <span style={{ margin: '0 4px' }}>•</span>
                      <span>{formatBytes(mod.fileSize)}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <Button
                        variant="secondary"
                        size="sm"
                        onClick={() => setInspectMod(mod)}
                        title="Zobacz pełne metadane, hash SHA-256 i zależności"
                      >
                        Szczegóły
                      </Button>

                      <Button
                        variant="danger"
                        size="sm"
                        icon={<TrashIcon size={13} />}
                        onClick={() => setModToDelete(mod)}
                        title="Usuń plik z dysku"
                      >
                        Usuń
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ========================================================================= */}
      {/* TAB 2: MODRINTH ONLINE CATALOG                                            */}
      {/* ========================================================================= */}
      {activeTab === 'modrinth' && <ModrinthCatalog />}

      {/* ========================================================================= */}
      {/* TAB 3: MOD UPDATES, DEPENDENCY RESOLVER & ROLLBACK                         */}
      {/* ========================================================================= */}
      {activeTab === 'updates' && <ModUpdatesPanel />}

      {/* ========================================================================= */}
      {/* TAB 3: LUNAR CLIENT STYLE HUD OVERLAYS & PVP MODULES                      */}
      {/* ========================================================================= */}
      {activeTab === 'hud' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '22px' }}>
          {/* Top Header & Category Filters */}
          <div
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border-light)',
              borderRadius: '12px',
              padding: '18px 22px',
              display: 'flex',
              flexDirection: 'column',
              gap: '16px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <h2 style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff' }}>
                    Katalog Modułów i Nakładek HUD
                  </h2>
                  <Badge variant="festive">STYL LUNAR CLIENT</Badge>
                  <Badge variant="preview">UI PREVIEW</Badge>
                </div>
                <p style={{ fontSize: '12.5px', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Dostosuj wygląd i pozycję nakładek HUD (Keystrokes, CPS, FPS, Armor HUD).
                </p>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <SearchBar value={searchHud} onChange={setSearchHud} placeholder="Szukaj modów HUD (np. CPS)..." />

                <button
                  onClick={() => setSortByHud(sortByHud === 'name' ? 'enabled' : 'name')}
                  style={{
                    padding: '7px 12px',
                    borderRadius: '6px',
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-light)',
                    color: 'var(--text-secondary)',
                    fontSize: '12px',
                    fontWeight: 600,
                    cursor: 'pointer',
                  }}
                  title="Zmień tryb sortowania"
                >
                  Sortuj: {sortByHud === 'name' ? 'Nazwa (A-Z)' : 'Aktywne pierwsze'}
                </button>
              </div>
            </div>

            {/* Category Navigation Tabs */}
            <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' }}>
              {CATEGORIES.map((cat) => {
                const isActive = activeCategory === cat.id;
                return (
                  <button
                    key={cat.id}
                    onClick={() => setActiveCategory(cat.id)}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      padding: '7px 14px',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontSize: '12.5px',
                      fontWeight: 600,
                      transition: 'all 0.16s ease',
                      background: isActive ? 'var(--bg-surface-active)' : 'transparent',
                      border: `1px solid ${isActive ? 'var(--frost-500)' : 'transparent'}`,
                      color: isActive ? 'var(--frost-400)' : 'var(--text-secondary)',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {cat.icon}
                    <span>{cat.label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Modules Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px' }}>
            {filteredHudMods.map((mod) => (
              <div
                key={mod.id}
                style={{
                  background: 'var(--bg-card)',
                  border: `1px solid ${mod.enabled ? 'var(--pine-500)' : 'var(--border-subtle)'}`,
                  borderRadius: '12px',
                  padding: '18px',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  gap: '14px',
                  boxShadow: mod.enabled ? '0 4px 16px rgba(16, 185, 129, 0.12)' : 'none',
                  transition: 'all 0.18s ease',
                }}
              >
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '10px' }}>
                    <div>
                      <h3 style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff' }}>{mod.name}</h3>
                      <div style={{ display: 'flex', gap: '6px', marginTop: '4px' }}>
                        <Badge variant={mod.category === 'pvp' ? 'danger' : mod.category === 'hud' ? 'fabric' : 'neutral'}>
                          {mod.category}
                        </Badge>
                        <Badge variant="preview">{mod.badge}</Badge>
                      </div>
                    </div>

                    <Toggle checked={mod.enabled} onChange={() => toggleMod(mod.id)} />
                  </div>

                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '10px', lineHeight: 1.45 }}>
                    {mod.description}
                  </p>
                </div>

                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    paddingTop: '12px',
                    borderTop: '1px solid var(--border-subtle)',
                  }}
                >
                  <span style={{ fontSize: '11px', color: 'var(--text-dim)' }}>Autor: {mod.author}</span>

                  <Button
                    variant="secondary"
                    size="sm"
                    icon={<GearIcon size={14} />}
                    onClick={() => setEditingMod(mod)}
                  >
                    Ustawienia
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ========================================================================= */}
      {/* MODAL: DODAJ MOD (.JAR)                                                   */}
      {/* ========================================================================= */}
      {showAddModal && (
        <Modal
          isOpen={showAddModal}
          onClose={() => {
            setShowAddModal(false);
            setValidationResult(null);
            setSelectedFilePath('');
          }}
          title="Dodaj Mod (.jar) do Instalacji"
          subtitle={`Instalacja docelowa: ${activeInstallation.name} (${activeInstallation.loader.toUpperCase()} ${activeInstallation.minecraftVersion})`}
          maxWidth="640px"
          footer={
            <>
              <Button
                variant="ghost"
                onClick={() => {
                  setShowAddModal(false);
                  setValidationResult(null);
                  setSelectedFilePath('');
                }}
              >
                Anuluj
              </Button>

              <Button
                variant="primary"
                disabled={!selectedFilePath || !validationResult?.valid || isInstalling}
                onClick={handleInstallMod}
              >
                {isInstalling ? 'Instalowanie...' : 'Zainstaluj Mod'}
              </Button>
            </>
          }
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            {/* File Path Picker Section */}
            <div>
              <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Wybierz plik moda .jar z dysku komputera:
              </label>

              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="text"
                  placeholder="C:\Pobrane\sodium-fabric-0.5.8.jar"
                  value={selectedFilePath}
                  onChange={(e) => {
                    setSelectedFilePath(e.target.value);
                    triggerValidation(e.target.value);
                  }}
                  style={{
                    flex: 1,
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-light)',
                    borderRadius: '8px',
                    padding: '8px 12px',
                    color: '#ffffff',
                    fontSize: '13px',
                    outline: 'none',
                  }}
                />

                <Button variant="secondary" onClick={handlePickFile}>
                  Wybierz plik...
                </Button>
              </div>
            </div>

            {/* Validation in progress */}
            {isValidating && (
              <div style={{ fontSize: '12px', color: 'var(--frost-400)', padding: '10px 0' }}>
                Trwa bezpieczna analiza archiwum JAR, sumy kontrolnej SHA-256 oraz metadanych...
              </div>
            )}

            {/* Live Inspection / Validation Result */}
            {validationResult && !isValidating && (
              <div
                style={{
                  background: 'var(--bg-surface)',
                  border: `1px solid ${validationResult.valid ? 'var(--border-light)' : 'rgba(239, 68, 68, 0.4)'}`,
                  borderRadius: '10px',
                  padding: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '12px',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontSize: '14px', fontWeight: 700, color: '#ffffff' }}>
                      {validationResult.metadata.name}
                    </span>
                    <Badge variant={validationResult.metadata.loader === 'unknown' ? 'neutral' : (validationResult.metadata.loader as any)}>
                      {validationResult.metadata.loader.toUpperCase()}
                    </Badge>
                  </div>
                  {getStatusBadge(validationResult.compatibilityStatus)}
                </div>

                <div style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                  <div><strong>ID:</strong> <code>{validationResult.metadata.id}</code></div>
                  <div><strong>Wersja:</strong> {validationResult.metadata.version}</div>
                  <div><strong>Rozmiar:</strong> {formatBytes(validationResult.fileSize)}</div>
                  <div><strong>Źródło:</strong> {validationResult.metadata.rawSource}</div>
                </div>

                <div style={{ fontSize: '11px', color: 'var(--text-dim)' }}>
                  <strong>SHA-256:</strong> <code>{validationResult.sha256}</code>
                </div>

                {/* Compatibility explanation */}
                <div
                  style={{
                    padding: '10px',
                    borderRadius: '6px',
                    fontSize: '12px',
                    background:
                      validationResult.compatibilityStatus === 'Compatible'
                        ? 'rgba(16, 185, 129, 0.1)'
                        : 'rgba(245, 158, 11, 0.1)',
                    border:
                      validationResult.compatibilityStatus === 'Compatible'
                        ? '1px solid rgba(16, 185, 129, 0.25)'
                        : '1px solid rgba(245, 158, 11, 0.25)',
                    color:
                      validationResult.compatibilityStatus === 'Compatible'
                        ? '#34d399'
                        : '#fbbf24',
                  }}
                >
                  {validationResult.compatibilityDetails || 'Mod jest w pełni zgodny z tą instalacją.'}
                </div>

                {/* Missing Dependencies Warning */}
                {validationResult.missingDependencies.length > 0 && (
                  <div style={{ fontSize: '12px', color: '#f87171' }}>
                    Brakujące zależności w instalacji: <strong>{validationResult.missingDependencies.join(', ')}</strong>
                  </div>
                )}

                {/* Duplicate file warning */}
                {validationResult.isDuplicate && (
                  <div
                    style={{
                      background: 'rgba(239, 68, 68, 0.1)',
                      border: '1px solid rgba(239, 68, 68, 0.3)',
                      borderRadius: '6px',
                      padding: '10px',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '6px',
                    }}
                  >
                    <span style={{ fontSize: '12px', color: '#f87171', fontWeight: 600 }}>
                      Plik o takiej samej nazwie lub zawartości ({validationResult.duplicateFileName}) już istnieje w instalacji!
                    </span>
                    <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px', color: '#ffffff', cursor: 'pointer' }}>
                      <input
                        type="checkbox"
                        checked={overwriteDuplicate}
                        onChange={(e) => setOverwriteDuplicate(e.target.checked)}
                      />
                      <span>Nadpisz istniejący plik moda</span>
                    </label>
                  </div>
                )}

                {/* Errors if any */}
                {validationResult.error && (
                  <div style={{ fontSize: '12px', color: '#f87171' }}>
                    Błąd: {validationResult.error}
                  </div>
                )}
              </div>
            )}
          </div>
        </Modal>
      )}

      {/* ========================================================================= */}
      {/* MODAL: SZCZEGÓŁY MODA                                                     */}
      {/* ========================================================================= */}
      {inspectMod && (
        <Modal
          isOpen={!!inspectMod}
          onClose={() => setInspectMod(null)}
          title={`Szczegóły: ${inspectMod.metadata.name}`}
          subtitle={`Plik: ${inspectMod.fileName}`}
          maxWidth="640px"
          footer={
            <Button variant="primary" onClick={() => setInspectMod(null)}>
              Zamknij
            </Button>
          }
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>
                  {inspectMod.metadata.name}
                </span>
                <Badge variant={inspectMod.metadata.loader === 'unknown' ? 'neutral' : (inspectMod.metadata.loader as any)}>
                  {inspectMod.metadata.loader.toUpperCase()}
                </Badge>
              </div>
              {getStatusBadge(inspectMod.compatibilityStatus)}
            </div>

            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              {inspectMod.metadata.description || 'Brak opisu w metadanych moda.'}
            </p>

            <div
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                borderRadius: '8px',
                padding: '14px',
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '10px',
                fontSize: '12px',
              }}
            >
              <div><strong style={{ color: 'var(--text-muted)' }}>ID:</strong> <code>{inspectMod.metadata.id}</code></div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Wersja:</strong> {inspectMod.metadata.version}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Autorzy:</strong> {inspectMod.metadata.authors.join(', ') || 'Nieznany'}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Licencja:</strong> {inspectMod.metadata.license || 'Nieokreślona'}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Środowisko:</strong> {inspectMod.metadata.environment || 'both'}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Wersja MC:</strong> {inspectMod.metadata.minecraftVersionRequirement || '*'}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Rozmiar:</strong> {formatBytes(inspectMod.fileSize)}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Data dodania:</strong> {inspectMod.dateAdded.substring(0, 10)}</div>
            </div>

            {/* Declared Dependencies */}
            {inspectMod.metadata.dependencies.length > 0 && (
              <div>
                <h4 style={{ fontSize: '13px', fontWeight: 700, color: '#ffffff', marginBottom: '8px' }}>
                  Zadeklarowane Zależności ({inspectMod.metadata.dependencies.length})
                </h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  {inspectMod.metadata.dependencies.map((dep, idx) => (
                    <div
                      key={idx}
                      style={{
                        padding: '6px 10px',
                        background: 'var(--bg-surface)',
                        borderRadius: '6px',
                        fontSize: '11.5px',
                        display: 'flex',
                        justifyContent: 'space-between',
                      }}
                    >
                      <span>
                        <code>{dep.id}</code> {dep.versionRange && `(${dep.versionRange})`}
                      </span>
                      <span style={{ color: dep.mandatory ? '#f87171' : 'var(--text-muted)' }}>
                        {dep.kind} {dep.mandatory ? '[wymagany]' : '[opcjonalny]'}
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* SHA-256 and Physical Path */}
            <div
              style={{
                background: 'var(--bg-surface)',
                borderRadius: '8px',
                padding: '12px',
                display: 'flex',
                flexDirection: 'column',
                gap: '8px',
                fontSize: '11px',
              }}
            >
              <div>
                <strong style={{ color: 'var(--text-muted)' }}>Ścieżka na dysku:</strong>
                <div style={{ wordBreak: 'break-all', marginTop: '2px', color: '#ffffff' }}>
                  {inspectMod.filePath}
                </div>
              </div>

              <div>
                <strong style={{ color: 'var(--text-muted)' }}>Suma kontrolna SHA-256:</strong>
                <div
                  style={{
                    wordBreak: 'break-all',
                    marginTop: '2px',
                    fontFamily: 'monospace',
                    color: 'var(--frost-400)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                  }}
                >
                  <span>{inspectMod.sha256}</span>
                  <button
                    onClick={() => {
                      navigator.clipboard.writeText(inspectMod.sha256);
                      addToast({ type: 'info', title: 'Skopiowano SHA-256 do schowka' });
                    }}
                    style={{
                      background: 'transparent',
                      border: 'none',
                      color: 'var(--text-muted)',
                      cursor: 'pointer',
                      padding: '2px 4px',
                    }}
                    title="Kopiuj hash"
                  >
                    <CopyIcon size={14} />
                  </button>
                </div>
              </div>
            </div>
          </div>
        </Modal>
      )}

      {/* ========================================================================= */}
      {/* MODAL: USUŃ MOD POTWIERDZENIE                                             */}
      {/* ========================================================================= */}
      {modToDelete && (
        <Modal
          isOpen={!!modToDelete}
          onClose={() => setModToDelete(null)}
          title="Potwierdzenie usunięcia moda"
          subtitle="Ta operacja trwale usunie plik moda z dysku komputera."
          maxWidth="480px"
          footer={
            <>
              <Button variant="ghost" onClick={() => setModToDelete(null)}>
                Anuluj
              </Button>
              <Button
                variant="danger"
                onClick={async () => {
                  const toDelete = modToDelete;
                  setModToDelete(null);
                  await deleteRealMod(activeInstallation.id, toDelete.fileName);
                }}
              >
                Usuń plik z dysku
              </Button>
            </>
          }
        >
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <p>
              Czy na pewno chcesz usunąć mod <strong>{modToDelete.metadata.name}</strong>?
            </p>
            <div
              style={{
                background: 'var(--bg-surface)',
                padding: '10px',
                borderRadius: '6px',
                fontSize: '12px',
              }}
            >
              <div><strong>Nazwa pliku:</strong> {modToDelete.fileName}</div>
              <div><strong>Rozmiar:</strong> {formatBytes(modToDelete.fileSize)}</div>
              <div><strong>Katalog:</strong> {modToDelete.enabled ? 'mods/' : 'disabled-mods/'}</div>
            </div>
            <p style={{ color: '#f87171', fontSize: '12px' }}>
              Plik zostanie bezpowrotnie usunięty z instalacji {activeInstallation.name}.
            </p>
          </div>
        </Modal>
      )}

      {/* ========================================================================= */}
      {/* MODAL: HUD PREVIEW SETTINGS (Lunar Style)                                 */}
      {/* ========================================================================= */}
      {editingMod && (
        <Modal
          isOpen={!!editingMod}
          onClose={() => setEditingMod(null)}
          title={`Ustawienia Modułu: ${editingMod.name}`}
          subtitle="Dostosuj skalę, pozycję ekranową, kolory i przezroczystość nakładki HUD."
          maxWidth="680px"
          footer={
            <>
              <Button
                variant="ghost"
                icon={<RefreshIcon size={14} />}
                onClick={() => {
                  resetModSettings(editingMod.id);
                  const updated = mods.find((m) => m.id === editingMod.id);
                  if (updated) setEditingMod({ ...updated });
                }}
              >
                Przywróć Domyślne
              </Button>

              <Button
                variant="primary"
                onClick={() => {
                  addToast({
                    type: 'success',
                    title: 'Zapisano ustawienia modułu',
                    message: `Parametry dla ${editingMod.name} zaktualizowane.`,
                  });
                  setEditingMod(null);
                }}
              >
                Zapisz Ustawienia
              </Button>
            </>
          }
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {/* Live Interactive HUD Preview Box */}
            <div
              style={{
                background: '#070a10',
                border: '1px solid var(--border-light)',
                borderRadius: '8px',
                padding: '24px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '160px',
                position: 'relative',
                overflow: 'hidden',
              }}
            >
              <div
                style={{
                  position: 'absolute',
                  top: '8px',
                  left: '12px',
                  fontSize: '11px',
                  fontWeight: 700,
                  color: 'var(--text-muted)',
                  textTransform: 'uppercase',
                }}
              >
                Podgląd na żywo w grze (HUD Simulator)
              </div>

              <div
                style={{
                  transform: `scale(${editingMod.settings.scale / 100})`,
                  transformOrigin: 'center',
                  opacity: editingMod.settings.opacity / 100,
                  transition: 'all 0.15s ease',
                }}
              >
                {editingMod.id === 'mod-keystrokes' ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', alignItems: 'center' }}>
                    <div
                      style={{
                        width: '42px',
                        height: '42px',
                        background: editingMod.settings.bgColor,
                        border: editingMod.settings.border ? '1px solid #334155' : 'none',
                        boxShadow: editingMod.settings.shadow ? '0 4px 10px rgba(0,0,0,0.5)' : 'none',
                        color: editingMod.settings.textColor,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontWeight: 800,
                        fontSize: '14px',
                        borderRadius: '4px',
                      }}
                    >
                      W
                    </div>
                    <div style={{ display: 'flex', gap: '4px' }}>
                      {['A', 'S', 'D'].map((key) => (
                        <div
                          key={key}
                          style={{
                            width: '42px',
                            height: '42px',
                            background: key === 'S' ? editingMod.settings.pressedColor : editingMod.settings.bgColor,
                            border: editingMod.settings.border ? '1px solid #334155' : 'none',
                            boxShadow: editingMod.settings.shadow ? '0 4px 10px rgba(0,0,0,0.5)' : 'none',
                            color: key === 'S' ? '#ffffff' : editingMod.settings.textColor,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontWeight: 800,
                            fontSize: '14px',
                            borderRadius: '4px',
                          }}
                        >
                          {key}
                        </div>
                      ))}
                    </div>
                  </div>
                ) : (
                  <div
                    style={{
                      background: editingMod.settings.bgColor,
                      border: editingMod.settings.border ? '1px solid #334155' : 'none',
                      boxShadow: editingMod.settings.shadow ? '0 4px 10px rgba(0,0,0,0.5)' : 'none',
                      color: editingMod.settings.textColor,
                      padding: '8px 16px',
                      borderRadius: '6px',
                      fontWeight: 700,
                      fontSize: '14px',
                    }}
                  >
                    {editingMod.name}: 144 FPS
                  </div>
                )}
              </div>
            </div>

            {/* Slider Controls */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <Slider
                label="Skala nakładki (%)"
                value={editingMod.settings.scale}
                min={50}
                max={200}
                step={5}
                onChange={(val) => updateModSettings(editingMod.id, { scale: val })}
              />

              <Slider
                label="Przezroczystość (%)"
                value={editingMod.settings.opacity}
                min={20}
                max={100}
                step={5}
                onChange={(val) => updateModSettings(editingMod.id, { opacity: val })}
              />
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
