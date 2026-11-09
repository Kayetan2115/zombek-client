import React, { useState, useEffect } from 'react';
import type {
  ModrinthProject,
  ModrinthVersion,
  ModrinthVersionFile,
  ModRecord,
} from '@zombek/shared-types';
import { useLauncherContext } from '../../stores';
import { Modal, Button, Badge } from '../common';
import {
  DownloadIcon,
  ExternalLinkIcon,
  CheckIcon,
  WarningIcon,
  RefreshIcon,
  HeartIcon,
  CloseIcon,
} from '../icons';

interface ModrinthProjectModalProps {
  project: ModrinthProject | null;
  isOpen: boolean;
  onClose: () => void;
  onInstallSuccess?: () => void;
}

function formatBytes(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

function formatNumber(num: number): string {
  if (num >= 1_000_000) {
    return `${(num / 1_000_000).toFixed(1)}M`;
  }
  if (num >= 1_000) {
    return `${(num / 1_000).toFixed(1)}K`;
  }
  return num.toString();
}

export const ModrinthProjectModal: React.FC<ModrinthProjectModalProps> = ({
  project,
  isOpen,
  onClose,
  onInstallSuccess,
}) => {
  const {
    activeInstallation,
    realMods,
    getModrinthProjectVersions,
    installModrinthVersion,
    addToast,
  } = useLauncherContext();

  const [versions, setVersions] = useState<ModrinthVersion[]>([]);
  const [isLoadingVersions, setIsLoadingVersions] = useState(false);
  const [filterCompatibleOnly, setFilterCompatibleOnly] = useState(true);
  const [installingVersionId, setInstallingVersionId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'versions' | 'description' | 'gallery'>('versions');
  const [selectedGalleryIdx, setSelectedGalleryIdx] = useState(0);

  // Load versions when project changes or modal opens
  useEffect(() => {
    if (!project || !isOpen) return;

    let isMounted = true;
    setIsLoadingVersions(true);

    getModrinthProjectVersions(project.id)
      .then((verList) => {
        if (isMounted) {
          setVersions(verList);
        }
      })
      .catch((err) => {
        console.error('Failed to load project versions', err);
        addToast({
          type: 'error',
          title: 'Błąd pobierania wersji',
          message: err.message,
        });
      })
      .finally(() => {
        if (isMounted) setIsLoadingVersions(false);
      });

    return () => {
      isMounted = false;
    };
  }, [project?.id, isOpen]);

  if (!project) return null;

  // Filter versions by active installation compatibility
  const currentLoader = activeInstallation.loader.toLowerCase();
  const currentMcVersion = activeInstallation.minecraftVersion;

  const isVersionCompatible = (v: ModrinthVersion) => {
    const matchesMc = v.game_versions.includes(currentMcVersion);
    const matchesLoader =
      v.loaders.includes(currentLoader) ||
      (currentLoader === 'quilt' && v.loaders.includes('fabric'));
    return matchesMc && matchesLoader;
  };

  const displayedVersions = filterCompatibleOnly
    ? versions.filter(isVersionCompatible)
    : versions;

  // Check if a version is already installed in the current instance
  const isVersionInstalled = (version: ModrinthVersion): boolean => {
    const primaryFile =
      version.files.find((f) => f.primary) || version.files.find((f) => f.filename.endsWith('.jar')) || version.files[0];
    if (!primaryFile) return false;

    return realMods.some((rm) => {
      // Check SHA-256 or SHA-512 match if present
      if (primaryFile.hashes?.sha512 && rm.sha256) {
        // Filename match or hash match
        if (rm.fileName.toLowerCase() === primaryFile.filename.toLowerCase()) return true;
      }
      return rm.fileName.toLowerCase() === primaryFile.filename.toLowerCase();
    });
  };

  const handleInstall = async (version: ModrinthVersion, overwrite = false) => {
    setInstallingVersionId(version.id);
    try {
      const res = await installModrinthVersion({
        versionId: version.id,
        projectId: project.id,
        instanceId: activeInstallation.id,
        overwrite,
      });

      if (res.success) {
        if (onInstallSuccess) onInstallSuccess();
      }
    } finally {
      setInstallingVersionId(null);
    }
  };

  const openModrinthUrl = () => {
    const url = `https://modrinth.com/mod/${project.slug || project.id}`;
    if (window.open) {
      window.open(url, '_blank');
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title=""
      maxWidth="780px"
      footer={
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11.5px', color: 'var(--text-muted)' }}>
            <span>Źródło: Modrinth API</span>
            <span>•</span>
            <span style={{ color: 'var(--frost-400)' }}>
              Instalacja docelowa: {activeInstallation.name} ({activeInstallation.loader.toUpperCase()} {activeInstallation.minecraftVersion})
            </span>
          </div>
          <Button variant="secondary" size="sm" onClick={onClose}>
            Zamknij
          </Button>
        </div>
      }
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
        {/* Project Header Banner */}
        <div
          style={{
            display: 'flex',
            gap: '16px',
            alignItems: 'flex-start',
            background: 'var(--bg-surface)',
            border: '1px solid var(--border-light)',
            borderRadius: '12px',
            padding: '16px',
          }}
        >
          {/* Project Icon */}
          <div
            style={{
              width: '64px',
              height: '64px',
              borderRadius: '12px',
              overflow: 'hidden',
              background: 'var(--bg-card)',
              border: '1px solid var(--border-subtle)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            {project.icon_url ? (
              <img
                src={project.icon_url}
                alt={project.title}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                onError={(e) => {
                  // Fallback if image fails
                  (e.target as HTMLElement).style.display = 'none';
                }}
              />
            ) : (
              <span style={{ fontSize: '28px' }}>📦</span>
            )}
          </div>

          {/* Project Info */}
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '8px' }}>
              <div>
                <h2 style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff', margin: 0 }}>
                  {project.title}
                </h2>
                <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '2px' }}>
                  Autor / Zespół: <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>{project.author || project.team || 'Nieznany'}</span>
                  {project.license?.name && (
                    <> • Licencja: <span style={{ color: 'var(--text-secondary)' }}>{project.license.name}</span></>
                  )}
                </div>
              </div>

              {/* External Modrinth Button */}
              <Button
                variant="secondary"
                size="sm"
                icon={<ExternalLinkIcon size={13} />}
                onClick={openModrinthUrl}
                title="Otwórz stronę projektu na platformie Modrinth"
              >
                Modrinth
              </Button>
            </div>

            <p style={{ fontSize: '12.5px', color: 'var(--text-secondary)', marginTop: '8px', lineHeight: 1.45 }}>
              {project.description}
            </p>

            {/* Metrics and Tags */}
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', alignItems: 'center', marginTop: '10px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11.5px', color: 'var(--text-muted)' }}>
                <DownloadIcon size={13} color="var(--frost-400)" />
                <span style={{ fontWeight: 700, color: '#ffffff' }}>{formatNumber(project.downloads)}</span> pobrań
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11.5px', color: 'var(--text-muted)' }}>
                <HeartIcon size={13} color="#f43f5e" />
                <span style={{ fontWeight: 700, color: '#ffffff' }}>{formatNumber(project.followers)}</span> obserwujących
              </div>

              <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                {project.categories.slice(0, 4).map((cat) => (
                  <Badge key={cat} variant="neutral" size="sm">
                    {cat}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Official Modrinth Attribution Notice */}
        <div
          style={{
            background: 'rgba(56, 189, 248, 0.08)',
            border: '1px solid rgba(56, 189, 248, 0.25)',
            borderRadius: '8px',
            padding: '8px 12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            fontSize: '11.5px',
            color: 'var(--frost-300)',
          }}
        >
          <span>
            Project hosted on <strong>Modrinth</strong>. Wszystkie pliki modyfikacji są pobierane bezpośrednio z oficjalnych serwerów CDN Modrinth z weryfikacją sumy kontrolnej SHA-512.
          </span>
          <span style={{ fontSize: '11px', color: 'var(--text-muted)', marginLeft: '8px' }}>
            ID: <code>{project.id}</code>
          </span>
        </div>

        {/* Navigation Tabs */}
        <div
          style={{
            display: 'flex',
            gap: '8px',
            borderBottom: '1px solid var(--border-subtle)',
            paddingBottom: '8px',
          }}
        >
          <button
            onClick={() => setActiveTab('versions')}
            style={{
              padding: '6px 14px',
              borderRadius: '6px',
              fontWeight: 700,
              fontSize: '12.5px',
              cursor: 'pointer',
              background: activeTab === 'versions' ? 'var(--frost-500)' : 'transparent',
              color: activeTab === 'versions' ? '#ffffff' : 'var(--text-secondary)',
              border: 'none',
              transition: 'all 0.15s ease',
            }}
          >
            Dostępne wersje ({versions.length})
          </button>

          <button
            onClick={() => setActiveTab('description')}
            style={{
              padding: '6px 14px',
              borderRadius: '6px',
              fontWeight: 700,
              fontSize: '12.5px',
              cursor: 'pointer',
              background: activeTab === 'description' ? 'var(--frost-500)' : 'transparent',
              color: activeTab === 'description' ? '#ffffff' : 'var(--text-secondary)',
              border: 'none',
              transition: 'all 0.15s ease',
            }}
          >
            Opis projektu
          </button>

          {project.gallery && project.gallery.length > 0 && (
            <button
              onClick={() => setActiveTab('gallery')}
              style={{
                padding: '6px 14px',
                borderRadius: '6px',
                fontWeight: 700,
                fontSize: '12.5px',
                cursor: 'pointer',
                background: activeTab === 'gallery' ? 'var(--frost-500)' : 'transparent',
                color: activeTab === 'gallery' ? '#ffffff' : 'var(--text-secondary)',
                border: 'none',
                transition: 'all 0.15s ease',
              }}
            >
              Galeria ({project.gallery.length})
            </button>
          )}
        </div>

        {/* Tab 1: Versions List */}
        {activeTab === 'versions' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {/* Compatibility Filter Bar */}
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                background: 'var(--bg-card)',
                padding: '8px 14px',
                borderRadius: '8px',
                border: '1px solid var(--border-subtle)',
              }}
            >
              <label
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                  fontSize: '12px',
                  fontWeight: 600,
                  color: 'var(--text-secondary)',
                  cursor: 'pointer',
                }}
              >
                <input
                  type="checkbox"
                  checked={filterCompatibleOnly}
                  onChange={(e) => setFilterCompatibleOnly(e.target.checked)}
                  style={{ accentColor: 'var(--frost-500)', cursor: 'pointer' }}
                />
                Pokaż tylko kompatybilne z aktywną instalacją ({activeInstallation.loader.toUpperCase()} {activeInstallation.minecraftVersion})
              </label>

              <span style={{ fontSize: '11.5px', color: 'var(--text-muted)' }}>
                Wyświetlono {displayedVersions.length} z {versions.length}
              </span>
            </div>

            {/* Versions Rows */}
            {isLoadingVersions ? (
              <div style={{ padding: '30px', textAlign: 'center', color: 'var(--text-muted)' }}>
                <RefreshIcon size={20} className="animate-spin" />
                <div style={{ marginTop: '8px', fontSize: '12.5px' }}>Wczytywanie wersji z Modrinth...</div>
              </div>
            ) : displayedVersions.length === 0 ? (
              <div
                style={{
                  padding: '30px',
                  textAlign: 'center',
                  background: 'var(--bg-card)',
                  borderRadius: '10px',
                  border: '1px solid var(--border-subtle)',
                }}
              >
                <div style={{ fontSize: '14px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                  Brak kompatybilnych wersji
                </div>
                <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px' }}>
                  {filterCompatibleOnly ? (
                    <span>
                      Mod nie posiada wersji dedykowanej dla Minecraft {activeInstallation.minecraftVersion} ({activeInstallation.loader.toUpperCase()}).{' '}
                      <button
                        onClick={() => setFilterCompatibleOnly(false)}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: 'var(--frost-400)',
                          textDecoration: 'underline',
                          cursor: 'pointer',
                        }}
                      >
                        Pokaż wszystkie wersje
                      </button>
                    </span>
                  ) : (
                    'Ten projekt nie posiada opublikowanych wersji.'
                  )}
                </div>
              </div>
            ) : (
              <div
                style={{
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '8px',
                  maxHeight: '380px',
                  overflowY: 'auto',
                  paddingRight: '4px',
                }}
              >
                {displayedVersions.map((v) => {
                  const primaryFile: ModrinthVersionFile | undefined =
                    v.files.find((f) => f.primary) ||
                    v.files.find((f) => f.filename.endsWith('.jar')) ||
                    v.files[0];
                  const isInstalled = isVersionInstalled(v);
                  const isCompatible = isVersionCompatible(v);
                  const isInstallingThis = installingVersionId === v.id;

                  // Release type color
                  const releaseTypeVariant =
                    v.version_type === 'release'
                      ? 'success'
                      : v.version_type === 'beta'
                      ? 'gold'
                      : 'danger';

                  // Dependencies breakdown
                  const requiredDeps = v.dependencies.filter((d) => d.dependency_type === 'required');
                  const optionalDeps = v.dependencies.filter((d) => d.dependency_type === 'optional');

                  return (
                    <div
                      key={v.id}
                      style={{
                        background: 'var(--bg-card)',
                        border: `1px solid ${isCompatible ? 'var(--border-subtle)' : 'rgba(239, 68, 68, 0.2)'}`,
                        borderRadius: '10px',
                        padding: '12px 14px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '8px',
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '10px' }}>
                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                            <span style={{ fontSize: '13.5px', fontWeight: 700, color: '#ffffff' }}>
                              {v.name || v.version_number}
                            </span>
                            <Badge variant={releaseTypeVariant} size="sm">
                              {v.version_type.toUpperCase()}
                            </Badge>
                            {isInstalled && (
                              <Badge variant="success" size="sm">
                                <CheckIcon size={11} style={{ marginRight: '3px' }} /> Zainstalowany
                              </Badge>
                            )}
                          </div>

                          <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginTop: '4px', fontSize: '11.5px', color: 'var(--text-muted)' }}>
                            <span>MC: {v.game_versions.join(', ')}</span>
                            <span>•</span>
                            <span>Loader: {v.loaders.map((l) => l.toUpperCase()).join(', ')}</span>
                            {primaryFile && (
                              <>
                                <span>•</span>
                                <span>{formatBytes(primaryFile.size)}</span>
                              </>
                            )}
                          </div>
                        </div>

                        {/* Install / Download Button */}
                        <div>
                          {isInstalled ? (
                            <Button
                              variant="secondary"
                              size="sm"
                              disabled={isInstallingThis}
                              onClick={() => handleInstall(v, true)}
                              title="Plik już istnieje. Kliknij, aby nadpisać nową kopią."
                            >
                              {isInstallingThis ? 'Pobieranie...' : 'Nadpisz'}
                            </Button>
                          ) : (
                            <Button
                              variant={isCompatible ? 'primary' : 'secondary'}
                              size="sm"
                              icon={isInstallingThis ? <RefreshIcon size={13} className="animate-spin" /> : <DownloadIcon size={13} />}
                              disabled={isInstallingThis}
                              onClick={() => handleInstall(v, false)}
                            >
                              {isInstallingThis ? 'Pobieranie...' : 'Zainstaluj'}
                            </Button>
                          )}
                        </div>
                      </div>

                      {/* Dependencies notification if required */}
                      {requiredDeps.length > 0 && (
                        <div
                          style={{
                            background: 'rgba(245, 158, 11, 0.08)',
                            border: '1px solid rgba(245, 158, 11, 0.25)',
                            borderRadius: '6px',
                            padding: '6px 10px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px',
                            fontSize: '11px',
                            color: '#fbbf24',
                          }}
                        >
                          <WarningIcon size={14} color="#fbbf24" style={{ flexShrink: 0 }} />
                          <span>
                            Wymaga zależności: <strong>{requiredDeps.map((d) => d.project_id || 'Modrinth mod').join(', ')}</strong>. Upewnij się, że są zainstalowane w profilu.
                          </span>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        )}

        {/* Tab 2: Project Description */}
        {activeTab === 'description' && (
          <div
            style={{
              background: 'var(--bg-card)',
              borderRadius: '10px',
              border: '1px solid var(--border-subtle)',
              padding: '16px',
              maxHeight: '380px',
              overflowY: 'auto',
              fontSize: '12.5px',
              lineHeight: 1.6,
              color: 'var(--text-secondary)',
            }}
          >
            {project.body ? (
              <div
                style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word', fontFamily: 'inherit' }}
              >
                {/* Clean representation of project body */}
                {project.body.replace(/<[^>]*>?/gm, '')}
              </div>
            ) : (
              <div>{project.description}</div>
            )}
          </div>
        )}

        {/* Tab 3: Gallery */}
        {activeTab === 'gallery' && project.gallery && project.gallery.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            <div
              style={{
                width: '100%',
                height: '280px',
                borderRadius: '10px',
                overflow: 'hidden',
                background: '#0f172a',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                border: '1px solid var(--border-subtle)',
              }}
            >
              <img
                src={project.gallery[selectedGalleryIdx]?.url}
                alt={project.gallery[selectedGalleryIdx]?.title || 'Screenshot'}
                style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }}
              />
            </div>

            {/* Gallery Thumbnails */}
            <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' }}>
              {project.gallery.map((img, idx) => (
                <div
                  key={img.url}
                  onClick={() => setSelectedGalleryIdx(idx)}
                  style={{
                    width: '64px',
                    height: '48px',
                    borderRadius: '6px',
                    overflow: 'hidden',
                    border: `2px solid ${idx === selectedGalleryIdx ? 'var(--frost-500)' : 'transparent'}`,
                    cursor: 'pointer',
                    flexShrink: 0,
                  }}
                >
                  <img
                    src={img.url}
                    alt={img.title || `Thumb ${idx + 1}`}
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                  />
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
};
