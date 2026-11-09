import React, { useState, useEffect, useRef } from 'react';
import { useLauncherContext } from '../../stores';
import { Modal, Button, Badge } from '../common';
import {
  DownloadIcon,
  CheckIcon,
  ExternalLinkIcon,
  RefreshIcon,
  ModpacksIcon,
} from '../icons';
import type { MrpackInspectionResult, MrpackInspectionFileItem } from '@zombek/shared-types';

interface MrpackImportModalProps {
  isOpen: boolean;
  onClose: () => void;
  initialFilePath?: string | null;
}

export const MrpackImportModal: React.FC<MrpackImportModalProps> = ({
  isOpen,
  onClose,
  initialFilePath,
}) => {
  const {
    mrpackInspect,
    mrpackImport,
    mrpackOpenFileDialog,
    mrpackProgress,
    activeAccount,
    addToast,
  } = useLauncherContext();

  const [filePath, setFilePath] = useState<string | null>(initialFilePath || null);
  const [isInspecting, setIsInspecting] = useState<boolean>(false);
  const [inspection, setInspection] = useState<MrpackInspectionResult | null>(null);
  const [inspectError, setInspectError] = useState<string | null>(null);

  const [instanceName, setInstanceName] = useState<string>('');
  const [selectedOptionalFiles, setSelectedOptionalFiles] = useState<Set<string>>(new Set());
  const [showAllFiles, setShowAllFiles] = useState<boolean>(false);
  const [isImporting, setIsImporting] = useState<boolean>(false);
  const [isDragging, setIsDragging] = useState<boolean>(false);

  const dropZoneRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (initialFilePath) {
      handleInspectPath(initialFilePath);
    }
  }, [initialFilePath]);

  const handleInspectPath = async (path: string) => {
    setFilePath(path);
    setIsInspecting(true);
    setInspectError(null);
    setInspection(null);

    try {
      const res = await mrpackInspect(path);
      if (!res.valid) {
        setInspectError(res.warnings[0] || 'Nieprawidłowa paczka .mrpack');
      } else {
        setInspection(res);
        setInstanceName(res.index?.name || 'Zaimportowany Modpack');
        // Preselect all optional files by default
        const optionals = new Set<string>();
        res.files.forEach((f) => {
          if (f.isOptional) {
            optionals.add(f.path);
          }
        });
        setSelectedOptionalFiles(optionals);
      }
    } catch (err: any) {
      setInspectError(err.message || 'Błąd podczas inspekcji pliku');
    } finally {
      setIsInspecting(false);
    }
  };

  const handleBrowseFile = async () => {
    try {
      const selected = await mrpackOpenFileDialog();
      if (selected) {
        handleInspectPath(selected);
      }
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd wyboru pliku', message: err.message });
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const file = e.dataTransfer.files[0];
      if (file.name.endsWith('.mrpack')) {
        // file.path is available in Electron Renderer
        const path = (file as any).path || file.name;
        handleInspectPath(path);
      } else {
        addToast({
          type: 'warning',
          title: 'Nieprawidłowy plik',
          message: 'Upuszczony plik musi mieć rozszerzenie .mrpack',
        });
      }
    }
  };

  const toggleOptionalFile = (path: string) => {
    setSelectedOptionalFiles((prev) => {
      const next = new Set(prev);
      if (next.has(path)) {
        next.delete(path);
      } else {
        next.add(path);
      }
      return next;
    });
  };

  const handleImport = async () => {
    if (!filePath || !inspection) return;

    if (!instanceName.trim()) {
      addToast({ type: 'warning', title: 'Nazwa instalacji', message: 'Podaj nazwę dla tworzonej instalacji.' });
      return;
    }

    setIsImporting(true);
    try {
      const res = await mrpackImport({
        archivePath: filePath,
        instanceName: instanceName.trim(),
        selectedOptionalPaths: Array.from(selectedOptionalFiles),
      });

      if (res.success) {
        onClose();
      }
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd importu paczki', message: err.message });
    } finally {
      setIsImporting(false);
    }
  };

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 B';
    const mb = bytes / (1024 * 1024);
    if (mb >= 1) return `${mb.toFixed(1)} MB`;
    const kb = bytes / 1024;
    return `${kb.toFixed(0)} KB`;
  };

  const renderContent = () => {
    if (isImporting || (mrpackProgress && mrpackProgress.stage !== 'completed')) {
      const percent = mrpackProgress?.percent || 0;
      const stageName =
        mrpackProgress?.stage === 'downloading-files'
          ? 'Pobieranie plików modów z Modrinth CDN'
          : mrpackProgress?.stage === 'verifying-hashes'
          ? 'Weryfikacja sum kontrolnych SHA-512'
          : mrpackProgress?.stage === 'applying-overrides'
          ? 'Wypakowywanie plików overrides i konfiguracji'
          : mrpackProgress?.stage === 'installing-engine'
          ? 'Rejestrowanie nowej instalacji Minecraft'
          : mrpackProgress?.message || 'Przetwarzanie paczki .mrpack...';

      return (
        <div style={{ padding: '36px 16px', textAlign: 'center', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'center' }}>
            <div
              style={{
                width: '54px',
                height: '54px',
                borderRadius: '50%',
                background: 'var(--bg-surface-active)',
                border: '2px solid var(--frost-500)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--frost-400)',
              }}
            >
              <DownloadIcon size={28} />
            </div>
          </div>

          <div>
            <h3 style={{ fontSize: '17px', fontWeight: 700, color: '#ffffff' }}>
              Importowanie Modpacka
            </h3>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '4px' }}>
              {stageName}
            </p>
          </div>

          {/* Progress bar */}
          <div style={{ width: '100%', maxWidth: '480px', margin: '0 auto' }}>
            <div
              style={{
                width: '100%',
                height: '8px',
                background: 'var(--bg-surface)',
                borderRadius: '4px',
                overflow: 'hidden',
              }}
            >
              <div
                style={{
                  width: `${percent}%`,
                  height: '100%',
                  background: 'linear-gradient(90deg, var(--frost-500), var(--frost-400))',
                  transition: 'width 0.2s ease',
                }}
              />
            </div>

            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                marginTop: '8px',
                fontSize: '12px',
                color: 'var(--text-secondary)',
              }}
            >
              <span>{mrpackProgress?.currentFile ? mrpackProgress.currentFile : 'Przygotowywanie...'}</span>
              <span>{percent}%</span>
            </div>

            {mrpackProgress && typeof mrpackProgress.totalFiles === 'number' && mrpackProgress.totalFiles > 0 && (
              <div style={{ fontSize: '11.5px', color: 'var(--text-muted)', marginTop: '4px' }}>
                Pobrano: {mrpackProgress.completedFiles ?? 0} z {mrpackProgress.totalFiles} plików
              </div>
            )}
          </div>
        </div>
      );
    }

    if (isInspecting) {
      return (
        <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
          <RefreshIcon size={24} style={{ animation: 'spin 1s linear infinite', marginBottom: '12px' }} />
          <p style={{ fontSize: '14px', color: '#ffffff', fontWeight: 600 }}>
            Weryfikacja i inspekcja paczki .mrpack...
          </p>
          <p style={{ fontSize: '12px', marginTop: '4px' }}>
            Sprawdzanie formatVersion, sum SHA-512, zależności i struktury overrides.
          </p>
        </div>
      );
    }

    if (!inspection) {
      // Step 1: File selection & Drag-and-Drop
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div
            ref={dropZoneRef}
            onDragOver={(e) => {
              e.preventDefault();
              setIsDragging(true);
            }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={handleDrop}
            style={{
              border: `2px dashed ${isDragging ? 'var(--frost-500)' : 'var(--border-light)'}`,
              background: isDragging ? 'var(--bg-surface-active)' : 'var(--bg-card)',
              borderRadius: '12px',
              padding: '40px 24px',
              textAlign: 'center',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '12px',
              cursor: 'pointer',
              transition: 'all 0.18s ease',
            }}
            onClick={handleBrowseFile}
          >
            <div
              style={{
                width: '50px',
                height: '50px',
                borderRadius: '50%',
                background: 'var(--bg-surface)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--frost-400)',
              }}
            >
              <ModpacksIcon size={26} />
            </div>

            <div>
              <h4 style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff' }}>
                Przeciągnij i upuść plik .mrpack
              </h4>
              <p style={{ fontSize: '12.5px', color: 'var(--text-muted)', marginTop: '4px' }}>
                lub kliknij, aby wybrać paczkę Modrinth Modpack z dysku
              </p>
            </div>

            <Button variant="secondary" size="sm" icon={<ExternalLinkIcon size={14} />}>
              Przeglądaj pliki...
            </Button>
          </div>

          {inspectError && (
            <div
              style={{
                background: 'rgba(239, 68, 68, 0.12)',
                border: '1px solid rgba(239, 68, 68, 0.35)',
                borderRadius: '8px',
                padding: '12px 14px',
                fontSize: '13px',
                color: '#f87171',
              }}
            >
              <strong>Błąd paczki:</strong> {inspectError}
            </div>
          )}

          <div
            style={{
              background: 'var(--bg-surface)',
              borderRadius: '8px',
              padding: '12px 14px',
              fontSize: '12px',
              color: 'var(--text-secondary)',
              lineHeight: 1.45,
            }}
          >
            <strong style={{ color: '#ffffff' }}>Obsługiwane cechy formatu Modrinth .mrpack:</strong>
            <ul style={{ margin: '6px 0 0 16px', padding: 0 }}>
              <li>Pełna zgodność ze specyfikacją <code>formatVersion: 1</code> dla Minecraft</li>
              <li>Wykrywanie loaderów: Fabric, Forge, NeoForge, Quilt oraz Vanilla</li>
              <li>Weryfikacja integralności sum kontrolnych SHA-512 / SHA-1</li>
              <li>Wypakowywanie warstw <code>overrides/</code> oraz <code>client-overrides/</code></li>
              <li>Zabezpieczenie przed Zip Slip, atakami ścieżek i przeciążeniem pamięci</li>
            </ul>
          </div>
        </div>
      );
    }

    // Step 2: Inspection summary & Confirmation
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {/* Modpack Header Details */}
        <div
          style={{
            background: 'var(--bg-surface)',
            border: '1px solid var(--border-light)',
            borderRadius: '10px',
            padding: '16px',
            display: 'flex',
            flexDirection: 'column',
            gap: '12px',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '12px' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <h3 style={{ fontSize: '17px', fontWeight: 800, color: '#ffffff' }}>
                  {inspection.index?.name || 'Modpack'}
                </h3>
                <Badge variant="festive">v{inspection.index?.versionId || '1.0.0'}</Badge>
              </div>
              {inspection.index?.summary && (
                <p style={{ fontSize: '13px', color: 'var(--text-secondary)', marginTop: '4px' }}>
                  {inspection.index.summary}
                </p>
              )}
            </div>

            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setInspection(null);
                setFilePath(null);
              }}
              title="Wybierz inną paczkę"
            >
              Zmień plik
            </Button>
          </div>

          <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
            <Badge variant={inspection.loaderType as any}>{inspection.loaderType.toUpperCase()}</Badge>
            {inspection.loaderVersion && <Badge variant="neutral">{inspection.loaderVersion}</Badge>}
            <Badge variant="neutral">Minecraft {inspection.minecraftVersion}</Badge>
          </div>
        </div>

        {/* Instance Name Input */}
        <div>
          <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
            Nazwa nowej instalacji Minecraft
          </label>
          <input
            type="text"
            value={instanceName}
            onChange={(e) => setInstanceName(e.target.value)}
            style={{
              width: '100%',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-light)',
              borderRadius: '8px',
              padding: '10px 14px',
              fontSize: '13.5px',
              color: '#ffffff',
              outline: 'none',
            }}
            placeholder="Wpisz nazwę instalacji..."
          />
        </div>

        {/* Stats Row */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: '10px',
            background: 'var(--bg-surface)',
            borderRadius: '8px',
            padding: '12px',
            textAlign: 'center',
          }}
        >
          <div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Pliki zdalne (CDN)</div>
            <div style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff', marginTop: '2px' }}>
              {inspection.totalFilesCount} modów
            </div>
          </div>
          <div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Rozmiar pobierania</div>
            <div style={{ fontSize: '15px', fontWeight: 700, color: 'var(--frost-400)', marginTop: '2px' }}>
              {formatBytes(inspection.totalDownloadSizeBytes)}
            </div>
          </div>
          <div>
            <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Pliki Overrides</div>
            <div style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff', marginTop: '2px' }}>
              {inspection.overridesCount} plików
            </div>
          </div>
        </div>

        {/* Warnings (if any) */}
        {inspection.warnings.length > 0 && (
          <div
            style={{
              background: 'rgba(234, 179, 8, 0.12)',
              border: '1px solid rgba(234, 179, 8, 0.35)',
              borderRadius: '8px',
              padding: '10px 14px',
              fontSize: '12px',
              color: '#fbbf24',
            }}
          >
            <strong style={{ display: 'block', marginBottom: '4px' }}>Ostrzeżenia dotyczące paczki:</strong>
            <ul style={{ margin: '0 0 0 16px', padding: 0 }}>
              {inspection.warnings.map((w, idx) => (
                <li key={idx}>{w}</li>
              ))}
            </ul>
          </div>
        )}

        {/* Optional Files Selection */}
        {inspection.optionalFilesCount > 0 && (
          <div
            style={{
              border: '1px solid var(--border-light)',
              borderRadius: '8px',
              padding: '12px 14px',
              background: 'var(--bg-surface)',
            }}
          >
            <div style={{ fontSize: '13px', fontWeight: 600, color: '#ffffff', marginBottom: '8px' }}>
              Pliki opcjonalne ({selectedOptionalFiles.size} / {inspection.optionalFilesCount} zaznaczonych)
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', maxHeight: '120px', overflowY: 'auto' }}>
              {inspection.files
                .filter((f) => f.isOptional)
                .map((f) => (
                  <label
                    key={f.path}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px',
                      fontSize: '12px',
                      color: 'var(--text-secondary)',
                      cursor: 'pointer',
                    }}
                  >
                    <input
                      type="checkbox"
                      checked={selectedOptionalFiles.has(f.path)}
                      onChange={() => toggleOptionalFile(f.path)}
                    />
                    <span>{f.path}</span>
                    <span style={{ color: 'var(--text-muted)', fontSize: '11px' }}>({formatBytes(f.fileSize)})</span>
                  </label>
                ))}
            </div>
          </div>
        )}

        {/* Files Preview Toggle */}
        <div>
          <button
            type="button"
            onClick={() => setShowAllFiles(!showAllFiles)}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--frost-400)',
              fontSize: '12.5px',
              cursor: 'pointer',
              padding: '4px 0',
              fontWeight: 600,
            }}
          >
            {showAllFiles ? 'Ukryj listę plików modów' : `Pokaż wszystkie pliki (${inspection.files.length})`}
          </button>

          {showAllFiles && (
            <div
              style={{
                marginTop: '8px',
                border: '1px solid var(--border-subtle)',
                borderRadius: '8px',
                background: 'var(--bg-surface)',
                maxHeight: '160px',
                overflowY: 'auto',
                fontSize: '11.5px',
                padding: '8px',
                display: 'flex',
                flexDirection: 'column',
                gap: '4px',
              }}
            >
              {inspection.files.map((file, idx) => (
                <div
                  key={idx}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    padding: '4px 6px',
                    borderRadius: '4px',
                    background: 'var(--bg-card)',
                  }}
                >
                  <span style={{ color: '#ffffff', fontFamily: 'monospace' }}>{file.path}</span>
                  <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
                    {file.isOptional && <Badge variant="neutral">Opcjonalny</Badge>}
                    <span style={{ color: 'var(--text-muted)' }}>{formatBytes(file.fileSize)}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Import Modpacka Modrinth (.mrpack)"
      subtitle="Zainstaluj gotowy zestaw modów bezpośrednio z oficjalnego formatu paczki."
      footer={
        <>
          <Button variant="ghost" disabled={isImporting} onClick={onClose}>
            Anuluj
          </Button>

          {inspection && !isImporting && (
            <Button
              variant="primary"
              icon={<DownloadIcon size={16} />}
              onClick={handleImport}
              disabled={isImporting || !instanceName.trim()}
            >
              Zainstaluj Modpack
            </Button>
          )}
        </>
      }
    >
      {renderContent()}
    </Modal>
  );
};
