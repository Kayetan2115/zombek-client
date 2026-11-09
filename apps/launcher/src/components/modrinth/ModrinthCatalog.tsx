import React, { useState, useEffect, useRef, useMemo } from 'react';
import type {
  ModrinthSearchHit,
  ModrinthProject,
  ModrinthCategoryTag,
  ModrinthLoaderTag,
  ModrinthGameVersionTag,
} from '@zombek/shared-types';
import { useLauncherContext } from '../../stores';
import { Button, Badge, SearchBar } from '../common';
import {
  SearchIcon,
  RefreshIcon,
  DownloadIcon,
  HeartIcon,
  CheckIcon,
  WarningIcon,
  ExternalLinkIcon,
  FilterIcon,
  ModsIcon,
} from '../icons';
import { ModrinthProjectModal } from './ModrinthProjectModal';

function formatNumber(num: number): string {
  if (num >= 1_000_000) {
    return `${(num / 1_000_000).toFixed(1)}M`;
  }
  if (num >= 1_000) {
    return `${(num / 1_000).toFixed(1)}K`;
  }
  return num.toString();
}

const COMMON_MC_VERSIONS = [
  '1.21.1',
  '1.21',
  '1.20.6',
  '1.20.4',
  '1.20.2',
  '1.20.1',
  '1.19.4',
  '1.19.2',
  '1.18.2',
  '1.16.5',
];

export const ModrinthCatalog: React.FC = () => {
  const {
    activeInstallation,
    realMods,
    searchModrinth,
    getModrinthProject,
    getModrinthCategories,
    getModrinthGameVersions,
    installModrinthVersion,
    getModrinthProjectVersions,
    addToast,
    addLog,
  } = useLauncherContext();

  // Search & Filter State
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [selectedLoader, setSelectedLoader] = useState<string>(activeInstallation.loader.toLowerCase());
  const [selectedVersion, setSelectedVersion] = useState<string>(activeInstallation.minecraftVersion);
  const [selectedCategory, setSelectedCategory] = useState<string>('');
  const [selectedEnvironment, setSelectedEnvironment] = useState<'all' | 'client' | 'both'>('all');
  const [sortBy, setSortBy] = useState<'relevance' | 'downloads' | 'follows' | 'newest' | 'updated'>('downloads');

  // Pagination State
  const [page, setPage] = useState(0);
  const pageSize = 12;

  // Data State
  const [hits, setHits] = useState<ModrinthSearchHit[]>([]);
  const [totalHits, setTotalHits] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [rateLimitCooldown, setRateLimitCooldown] = useState<number | null>(null);

  // Metadata tags
  const [categories, setCategories] = useState<ModrinthCategoryTag[]>([]);
  const [gameVersions, setGameVersions] = useState<string[]>(COMMON_MC_VERSIONS);

  // Modals
  const [inspectProject, setInspectProject] = useState<ModrinthProject | null>(null);
  const [isLoadingProject, setIsLoadingProject] = useState(false);
  const [quickInstallingSlug, setQuickInstallingSlug] = useState<string | null>(null);

  // Debounce search input
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedQuery(query);
      setPage(0);
    }, 350);
    return () => clearTimeout(timer);
  }, [query]);

  // Load tag options once on mount
  useEffect(() => {
    getModrinthCategories()
      .then((cats) => {
        if (Array.isArray(cats) && cats.length > 0) {
          setCategories(cats.filter((c) => c.project_type === 'mod'));
        }
      })
      .catch((err) => console.warn('Could not load Modrinth categories tag', err));

    getModrinthGameVersions()
      .then((vers) => {
        if (Array.isArray(vers) && vers.length > 0) {
          const releases = vers
            .filter((v) => v.version_type === 'release')
            .map((v) => v.version);
          if (releases.length > 0) {
            setGameVersions(releases.slice(0, 30));
          }
        }
      })
      .catch((err) => console.warn('Could not load Modrinth game versions tag', err));
  }, []);

  // Update selected loader & version when active installation changes
  useEffect(() => {
    if (activeInstallation.loader !== 'vanilla') {
      setSelectedLoader(activeInstallation.loader.toLowerCase());
    }
    if (activeInstallation.minecraftVersion) {
      setSelectedVersion(activeInstallation.minecraftVersion);
    }
  }, [activeInstallation.id]);

  // Execute Search
  const executeSearch = async () => {
    setIsLoading(true);
    setErrorMessage(null);

    try {
      const result = await searchModrinth({
        query: debouncedQuery.trim() || undefined,
        limit: pageSize,
        offset: page * pageSize,
        index: sortBy,
        loader: selectedLoader !== 'all' ? selectedLoader : undefined,
        gameVersion: selectedVersion !== 'all' ? selectedVersion : undefined,
        category: selectedCategory ? selectedCategory : undefined,
        environment:
          selectedEnvironment === 'client'
            ? 'client'
            : selectedEnvironment === 'both'
            ? 'server'
            : undefined,
      });

      setHits(result.hits || []);
      setTotalHits(result.total_hits || 0);
      setRateLimitCooldown(null);
    } catch (err: any) {
      console.error('Modrinth search error:', err);
      const msg = err.message || 'Błąd komunikacji z Modrinth API';
      setErrorMessage(msg);
      if (msg.includes('429') || msg.toLowerCase().includes('rate limit')) {
        setRateLimitCooldown(60);
      }
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    executeSearch();
  }, [debouncedQuery, selectedLoader, selectedVersion, selectedCategory, selectedEnvironment, sortBy, page]);

  // Open Details Modal
  const handleOpenProjectDetails = async (hit: ModrinthSearchHit) => {
    setIsLoadingProject(true);
    try {
      const proj = await getModrinthProject(hit.project_id || hit.slug);
      setInspectProject(proj);
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Nie udało się pobrać szczegółów projektu',
        message: err.message,
      });
    } finally {
      setIsLoadingProject(false);
    }
  };

  // Quick Install Latest Compatible Version
  const handleQuickInstall = async (hit: ModrinthSearchHit) => {
    setQuickInstallingSlug(hit.slug);
    try {
      // 1. Fetch versions for this project
      const vers = await getModrinthProjectVersions(
        hit.project_id,
        selectedLoader !== 'all' ? [selectedLoader] : undefined,
        selectedVersion !== 'all' ? [selectedVersion] : undefined
      );

      if (!vers || vers.length === 0) {
        addToast({
          type: 'error',
          title: 'Brak wersji',
          message: `Mod ${hit.title} nie posiada wersji kompatybilnej z wybranymi filtrami.`,
        });
        return;
      }

      // 2. Select first compatible release (or beta)
      const targetVersion = vers.find((v) => v.version_type === 'release') || vers[0];

      // 3. Trigger install
      const res = await installModrinthVersion({
        versionId: targetVersion.id,
        projectId: hit.project_id,
        instanceId: activeInstallation.id,
        overwrite: true,
      });

      if (res.success) {
        addToast({
          type: 'success',
          title: 'Pomyślnie zainstalowano',
          message: `${hit.title} został dodany do ${activeInstallation.name}`,
        });
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Błąd szybkiej instalacji',
        message: err.message,
      });
    } finally {
      setQuickInstallingSlug(null);
    }
  };

  const totalPages = Math.ceil(totalHits / pageSize) || 1;

  // Check if project matches active installation directly
  const isHitCompatible = (hit: ModrinthSearchHit) => {
    const curLoader = activeInstallation.loader.toLowerCase();
    const curMc = activeInstallation.minecraftVersion;

    const loaderMatches =
      hit.categories.includes(curLoader) ||
      (curLoader === 'quilt' && hit.categories.includes('fabric'));
    const versionMatches = hit.versions.includes(curMc);
    return loaderMatches && versionMatches;
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
      {/* Target Instance Context Banner */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '12px',
          padding: '16px 20px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '12px',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h2 style={{ fontSize: '17px', fontWeight: 800, color: '#ffffff', margin: 0 }}>
              Katalog Modrinth
            </h2>
            <Badge variant="success">OFICJALNE API v2</Badge>
            <Badge variant={activeInstallation.loader as any}>
              Cel: {activeInstallation.name} ({activeInstallation.loader.toUpperCase()} {activeInstallation.minecraftVersion})
            </Badge>
          </div>
          <p style={{ fontSize: '12.5px', color: 'var(--text-muted)', marginTop: '4px', margin: '4px 0 0 0' }}>
            Przeglądaj tysiące sprawdzonych modyfikacji z oficjalnego katalogu Modrinth. Pobrane pliki JAR są bezpiecznie weryfikowane sumą SHA-512.
          </p>
        </div>

        {/* Sync with instance button */}
        <Button
          variant="secondary"
          size="sm"
          icon={<FilterIcon size={14} />}
          onClick={() => {
            setSelectedLoader(activeInstallation.loader !== 'vanilla' ? activeInstallation.loader.toLowerCase() : 'all');
            setSelectedVersion(activeInstallation.minecraftVersion);
            setPage(0);
          }}
          title="Ustaw filtry na kompatybilne z aktualną instalacją"
        >
          Dopasuj do instalacji
        </Button>
      </div>

      {/* Vanilla Notice if active instance has no modloader */}
      {activeInstallation.loader === 'vanilla' && (
        <div
          style={{
            background: 'rgba(239, 68, 68, 0.1)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            borderRadius: '10px',
            padding: '12px 16px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
          }}
        >
          <WarningIcon size={18} color="#f87171" style={{ flexShrink: 0 }} />
          <div style={{ fontSize: '12.5px', color: '#fca5a5' }}>
            <strong>Uwaga:</strong> Aktywna instalacja ({activeInstallation.name}) to <strong>Vanilla Minecraft</strong>. Vanilla nie wczytuje modów z katalogu <code>mods/</code>. Aby grać z modami, stwórz lub wybierz instalację z loaderem Fabric, Forge, NeoForge lub Quilt.
          </div>
        </div>
      )}

      {/* 429 Rate Limit Alert if any */}
      {rateLimitCooldown && (
        <div
          style={{
            background: 'rgba(245, 158, 11, 0.12)',
            border: '1px solid rgba(245, 158, 11, 0.35)',
            borderRadius: '10px',
            padding: '14px 18px',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
          }}
        >
          <WarningIcon size={20} color="#fbbf24" style={{ flexShrink: 0 }} />
          <div style={{ flex: 1 }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: '#fbbf24' }}>
              Limit zapytań Modrinth (HTTP 429 Too Many Requests)
            </div>
            <div style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '2px' }}>
              ZombekClient chroni serwery Modrinth przed przeciążeniem. Odczekaj chwilę przed ponownym zapytaniem.
            </div>
          </div>
          <Button variant="secondary" size="sm" onClick={() => executeSearch()}>
            Spróbuj ponownie
          </Button>
        </div>
      )}

      {/* Search & Filter Toolbar */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '12px',
          padding: '16px',
          display: 'flex',
          flexDirection: 'column',
          gap: '14px',
        }}
      >
        {/* Search Bar + Sort Dropdown */}
        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '260px' }}>
            <SearchBar
              value={query}
              onChange={setQuery}
              placeholder="Szukaj modów w Modrinth (np. Sodium, JEI, JourneyMap, Iris)..."
            />
          </div>

          {/* Sort By */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Sortuj:</span>
            <select
              value={sortBy}
              onChange={(e) => {
                setSortBy(e.target.value as any);
                setPage(0);
              }}
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                color: '#ffffff',
                borderRadius: '8px',
                padding: '7px 12px',
                fontSize: '12.5px',
                fontWeight: 600,
                outline: 'none',
                cursor: 'pointer',
              }}
            >
              <option value="downloads">Najpopularniejsze (Pobrania)</option>
              <option value="relevance">Trafność wyszukiwania</option>
              <option value="follows">Najwięcej obserwujących</option>
              <option value="updated">Ostatnio zaktualizowane</option>
              <option value="newest">Najnowsze</option>
            </select>
          </div>
        </div>

        {/* Filter Pills / Dropdowns */}
        <div
          style={{
            display: 'flex',
            flexWrap: 'wrap',
            gap: '12px',
            alignItems: 'center',
            borderTop: '1px solid var(--border-subtle)',
            paddingTop: '12px',
          }}
        >
          {/* Loader Selection Pills */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '11.5px', color: 'var(--text-muted)', marginRight: '2px' }}>Loader:</span>
            {['all', 'fabric', 'forge', 'neoforge', 'quilt'].map((ld) => {
              const isSelected = selectedLoader === ld;
              return (
                <button
                  key={ld}
                  onClick={() => {
                    setSelectedLoader(ld);
                    setPage(0);
                  }}
                  style={{
                    padding: '4px 10px',
                    borderRadius: '6px',
                    fontSize: '11.5px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    background: isSelected ? 'var(--frost-500)' : 'var(--bg-surface)',
                    color: isSelected ? '#ffffff' : 'var(--text-secondary)',
                    border: `1px solid ${isSelected ? 'var(--frost-400)' : 'var(--border-subtle)'}`,
                    textTransform: ld === 'all' ? 'none' : 'capitalize',
                  }}
                >
                  {ld === 'all' ? 'Wszystkie' : ld}
                </button>
              );
            })}
          </div>

          {/* Minecraft Version Dropdown */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '11.5px', color: 'var(--text-muted)' }}>Wersja MC:</span>
            <select
              value={selectedVersion}
              onChange={(e) => {
                setSelectedVersion(e.target.value);
                setPage(0);
              }}
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                color: '#ffffff',
                borderRadius: '6px',
                padding: '5px 10px',
                fontSize: '12px',
                fontWeight: 600,
                outline: 'none',
                cursor: 'pointer',
              }}
            >
              <option value="all">Wszystkie wersje</option>
              {gameVersions.map((v) => (
                <option key={v} value={v}>
                  Minecraft {v}
                </option>
              ))}
            </select>
          </div>

          {/* Category Dropdown */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '11.5px', color: 'var(--text-muted)' }}>Kategoria:</span>
            <select
              value={selectedCategory}
              onChange={(e) => {
                setSelectedCategory(e.target.value);
                setPage(0);
              }}
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                color: '#ffffff',
                borderRadius: '6px',
                padding: '5px 10px',
                fontSize: '12px',
                fontWeight: 600,
                outline: 'none',
                cursor: 'pointer',
              }}
            >
              <option value="">Wszystkie kategorie</option>
              {categories.map((c) => (
                <option key={c.name} value={c.name}>
                  {c.name.charAt(0).toUpperCase() + c.name.slice(1)}
                </option>
              ))}
            </select>
          </div>

          {/* Environment Filter */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontSize: '11.5px', color: 'var(--text-muted)' }}>Środowisko:</span>
            <select
              value={selectedEnvironment}
              onChange={(e) => {
                setSelectedEnvironment(e.target.value as any);
                setPage(0);
              }}
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                color: '#ffffff',
                borderRadius: '6px',
                padding: '5px 10px',
                fontSize: '12px',
                fontWeight: 600,
                outline: 'none',
                cursor: 'pointer',
              }}
            >
              <option value="all">Wszystkie</option>
              <option value="client">Tylko Klient</option>
              <option value="both">Klient & Serwer</option>
            </select>
          </div>

          {/* Reset Filters */}
          <button
            onClick={() => {
              setQuery('');
              setSelectedLoader('all');
              setSelectedVersion('all');
              setSelectedCategory('');
              setSelectedEnvironment('all');
              setSortBy('downloads');
              setPage(0);
            }}
            style={{
              marginLeft: 'auto',
              background: 'transparent',
              border: 'none',
              color: 'var(--frost-400)',
              fontSize: '11.5px',
              textDecoration: 'underline',
              cursor: 'pointer',
            }}
          >
            Wyczyść filtry
          </button>
        </div>
      </div>

      {/* Results Header with Counts and Pagination Info */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0 4px' }}>
        <div style={{ fontSize: '12.5px', color: 'var(--text-secondary)' }}>
          Znaleziono <strong style={{ color: '#ffffff' }}>{totalHits.toLocaleString()}</strong> projektów
          {isLoading && <span style={{ marginLeft: '8px', color: 'var(--frost-400)' }}>• Odświeżanie...</span>}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
            Strona {page + 1} z {totalPages}
          </span>
          <Button
            variant="secondary"
            size="sm"
            disabled={page === 0 || isLoading}
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          >
            Poprzednia
          </Button>
          <Button
            variant="secondary"
            size="sm"
            disabled={page >= totalPages - 1 || isLoading}
            onClick={() => setPage((p) => p + 1)}
          >
            Następna
          </Button>
        </div>
      </div>

      {/* Main Grid of Project Cards */}
      {isLoading && hits.length === 0 ? (
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '60px 20px',
            background: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border-light)',
          }}
        >
          <RefreshIcon size={24} className="animate-spin" />
          <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)', marginTop: '12px' }}>
            Łączenie z Modrinth API...
          </div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px' }}>
            Pobieranie listy projektów i sprawdzanie kompatybilności
          </div>
        </div>
      ) : errorMessage ? (
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '40px 20px',
            background: 'rgba(239, 68, 68, 0.08)',
            borderRadius: '12px',
            border: '1px solid rgba(239, 68, 68, 0.25)',
            textAlign: 'center',
          }}
        >
          <WarningIcon size={28} color="#f87171" />
          <div style={{ fontSize: '14px', fontWeight: 700, color: '#f87171', marginTop: '10px' }}>
            Błąd pobierania modów z Modrinth
          </div>
          <div style={{ fontSize: '12.5px', color: 'var(--text-secondary)', marginTop: '4px', maxWidth: '500px' }}>
            {errorMessage}
          </div>
          <Button
            variant="secondary"
            size="sm"
            style={{ marginTop: '14px' }}
            icon={<RefreshIcon size={14} />}
            onClick={() => executeSearch()}
          >
            Ponów zapytanie
          </Button>
        </div>
      ) : hits.length === 0 ? (
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            padding: '60px 20px',
            background: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border-light)',
            textAlign: 'center',
          }}
        >
          <ModsIcon size={36} color="var(--text-muted)" />
          <div style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff', marginTop: '14px' }}>
            Nie znaleziono modyfikacji
          </div>
          <div style={{ fontSize: '12.5px', color: 'var(--text-muted)', marginTop: '4px', maxWidth: '420px' }}>
            Brak modów spełniających podane kryteria wyszukiwania. Spróbuj zmienić słowa kluczowe lub zresetować filtry.
          </div>
          <Button
            variant="secondary"
            size="sm"
            style={{ marginTop: '16px' }}
            onClick={() => {
              setQuery('');
              setSelectedLoader('all');
              setSelectedVersion('all');
              setSelectedCategory('');
              setPage(0);
            }}
          >
            Zresetuj wszystkie filtry
          </Button>
        </div>
      ) : (
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))',
            gap: '16px',
          }}
        >
          {hits.map((hit) => {
            const isComp = isHitCompatible(hit);
            const isQuickInstalling = quickInstallingSlug === hit.slug;

            return (
              <div
                key={hit.project_id}
                style={{
                  background: 'var(--bg-card)',
                  border: `1px solid ${isComp ? 'var(--border-subtle)' : 'var(--border-subtle)'}`,
                  borderRadius: '12px',
                  padding: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  gap: '14px',
                  transition: 'all 0.16s ease',
                  boxShadow: '0 2px 8px rgba(0, 0, 0, 0.2)',
                }}
              >
                <div>
                  {/* Top card row: Icon + Title + Compatibility Badge */}
                  <div style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                    <div
                      style={{
                        width: '52px',
                        height: '52px',
                        borderRadius: '10px',
                        overflow: 'hidden',
                        background: 'var(--bg-surface)',
                        border: '1px solid var(--border-light)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        flexShrink: 0,
                      }}
                    >
                      {hit.icon_url ? (
                        <img
                          src={hit.icon_url}
                          alt={hit.title}
                          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                          onError={(e) => {
                            (e.target as HTMLElement).style.display = 'none';
                          }}
                        />
                      ) : (
                        <ModsIcon size={24} color="var(--text-muted)" />
                      )}
                    </div>

                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '6px' }}>
                        <h3
                          style={{
                            fontSize: '14.5px',
                            fontWeight: 700,
                            color: '#ffffff',
                            margin: 0,
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                          }}
                          title={hit.title}
                        >
                          {hit.title}
                        </h3>
                      </div>

                      <div style={{ fontSize: '11.5px', color: 'var(--text-muted)', marginTop: '2px' }}>
                        Autor: <span style={{ color: 'var(--text-secondary)' }}>{hit.author}</span>
                      </div>

                      <div style={{ marginTop: '4px' }}>
                        {isComp ? (
                          <Badge variant="success" size="sm">
                            <CheckIcon size={11} style={{ marginRight: '3px' }} />
                            Zgodny z {activeInstallation.loader.toUpperCase()}
                          </Badge>
                        ) : (
                          <Badge variant="neutral" size="sm">
                            Wymaga innej konfiguracji
                          </Badge>
                        )}
                      </div>
                    </div>
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
                      height: '35px',
                    }}
                    title={hit.description}
                  >
                    {hit.description}
                  </p>

                  {/* Metadata Tags (Loaders & Categories) */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', marginTop: '10px' }}>
                    {hit.categories.slice(0, 4).map((c) => (
                      <span
                        key={c}
                        style={{
                          fontSize: '10.5px',
                          padding: '2px 6px',
                          borderRadius: '4px',
                          background: 'var(--bg-surface)',
                          color: 'var(--text-muted)',
                          border: '1px solid var(--border-subtle)',
                        }}
                      >
                        {c}
                      </span>
                    ))}
                  </div>
                </div>

                {/* Bottom Row: Metrics & Actions */}
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    borderTop: '1px solid var(--border-subtle)',
                    paddingTop: '12px',
                    marginTop: 'auto',
                  }}
                >
                  {/* Downloads & Follows */}
                  <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '3px', fontSize: '11px', color: 'var(--text-muted)' }}>
                      <DownloadIcon size={12} color="var(--frost-400)" />
                      <span>{formatNumber(hit.downloads)}</span>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '3px', fontSize: '11px', color: 'var(--text-muted)' }}>
                      <HeartIcon size={12} color="#f43f5e" />
                      <span>{formatNumber(hit.follows)}</span>
                    </div>
                  </div>

                  {/* Buttons */}
                  <div style={{ display: 'flex', gap: '6px' }}>
                    <Button
                      variant="secondary"
                      size="sm"
                      onClick={() => handleOpenProjectDetails(hit)}
                      disabled={isLoadingProject}
                    >
                      Szczegóły
                    </Button>

                    <Button
                      variant="primary"
                      size="sm"
                      icon={isQuickInstalling ? <RefreshIcon size={12} className="animate-spin" /> : <DownloadIcon size={12} />}
                      disabled={isQuickInstalling || activeInstallation.loader === 'vanilla'}
                      onClick={() => handleQuickInstall(hit)}
                      title={`Pobierz i zainstaluj najnowszą wersję do instalacji ${activeInstallation.name}`}
                    >
                      {isQuickInstalling ? 'Instalowanie...' : 'Zainstaluj'}
                    </Button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Bottom Pagination Controls */}
      {hits.length > 0 && (
        <div
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            gap: '12px',
            padding: '16px 0',
          }}
        >
          <Button
            variant="secondary"
            size="sm"
            disabled={page === 0 || isLoading}
            onClick={() => {
              setPage((p) => Math.max(0, p - 1));
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
          >
            ← Poprzednia strona
          </Button>

          <span style={{ fontSize: '12.5px', color: 'var(--text-secondary)' }}>
            Strona <strong style={{ color: '#ffffff' }}>{page + 1}</strong> z{' '}
            <strong style={{ color: '#ffffff' }}>{totalPages}</strong>
          </span>

          <Button
            variant="secondary"
            size="sm"
            disabled={page >= totalPages - 1 || isLoading}
            onClick={() => {
              setPage((p) => p + 1);
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
          >
            Następna strona →
          </Button>
        </div>
      )}

      {/* Project Details Modal */}
      {inspectProject && (
        <ModrinthProjectModal
          project={inspectProject}
          isOpen={!!inspectProject}
          onClose={() => setInspectProject(null)}
          onInstallSuccess={() => {
            // Optional callback
          }}
        />
      )}
    </div>
  );
};
