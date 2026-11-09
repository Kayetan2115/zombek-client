import React from 'react';
import type { ModUpdateInfo } from '@zombek/shared-types';
import { Modal, Button, Badge } from '../common';
import { SparkleIcon, DownloadIcon } from '../icons';

interface ModChangelogModalProps {
  isOpen: boolean;
  onClose: () => void;
  modInfo: ModUpdateInfo | null;
  onApplyUpdate?: (mod: ModUpdateInfo) => void;
  isApplying?: boolean;
}

function formatDate(isoString?: string): string {
  if (!isoString) return 'Nieznana data';
  try {
    const d = new Date(isoString);
    return d.toLocaleDateString('pl-PL', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  } catch {
    return isoString;
  }
}

function formatBytes(bytes?: number): string {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

export const ModChangelogModal: React.FC<ModChangelogModalProps> = ({
  isOpen,
  onClose,
  modInfo,
  onApplyUpdate,
  isApplying = false,
}) => {
  if (!isOpen || !modInfo) return null;

  const targetVer = modInfo.targetVersion;

  const versionTypeBadge = (type?: 'release' | 'beta' | 'alpha') => {
    switch (type) {
      case 'release':
        return <Badge variant="success">Wersja stabilna (Release)</Badge>;
      case 'beta':
        return <Badge variant="gold">Wersja testowa (Beta)</Badge>;
      case 'alpha':
        return <Badge variant="danger">Wersja wczesna (Alpha)</Badge>;
      default:
        return <Badge variant="neutral">Wydanie</Badge>;
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Dziennik zmian: ${modInfo.modName}`} maxWidth="720px">
      <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
        {/* Header summary */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '16px',
            padding: '16px',
            background: 'var(--bg-surface)',
            borderRadius: '10px',
            border: '1px solid var(--border-light)',
          }}
        >
          <div
            style={{
              width: '48px',
              height: '48px',
              borderRadius: '8px',
              background: 'var(--bg-card)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--frost-400)',
              flexShrink: 0,
            }}
          >
            <SparkleIcon size={24} />
          </div>

          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
              <h3 style={{ margin: 0, fontSize: '18px', fontWeight: 700, color: '#ffffff' }}>
                {targetVer?.name || targetVer?.version_number || 'Aktualizacja'}
              </h3>
              {versionTypeBadge(targetVer?.version_type || modInfo.updateType)}
            </div>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '14px',
                marginTop: '6px',
                fontSize: '12px',
                color: 'var(--text-secondary)',
                flexWrap: 'wrap',
              }}
            >
              <span>
                Obecna wersja:{' '}
                <strong style={{ color: 'var(--text-primary)' }}>{modInfo.currentVersion}</strong>
              </span>
              <span>&rarr;</span>
              <span>
                Nowa wersja:{' '}
                <strong style={{ color: 'var(--frost-400)' }}>
                  {targetVer?.version_number || 'Brak'}
                </strong>
              </span>
              {modInfo.targetSizeBytes > 0 && (
                <span>Rozmiar: {formatBytes(modInfo.targetSizeBytes)}</span>
              )}
              {targetVer?.date_published && (
                <span>Wydano: {formatDate(targetVer.date_published)}</span>
              )}
            </div>
          </div>
        </div>

        {/* Dependencies if any */}
        {modInfo.dependencies && modInfo.dependencies.length > 0 && (
          <div
            style={{
              padding: '12px 16px',
              background: 'rgba(56, 189, 248, 0.05)',
              borderRadius: '8px',
              border: '1px solid rgba(56, 189, 248, 0.2)',
            }}
          >
            <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--frost-400)', marginBottom: '8px' }}>
              Wymagane / Powiązane zależności ({modInfo.dependencies.length})
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {modInfo.dependencies.map((dep, idx) => (
                <div
                  key={idx}
                  style={{
                    fontSize: '11px',
                    padding: '4px 8px',
                    background: 'var(--bg-surface)',
                    borderRadius: '6px',
                    border: '1px solid var(--border-light)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                  }}
                >
                  <span style={{ fontWeight: 600, color: '#ffffff' }}>
                    {dep.file_name || dep.project_id || dep.version_id || 'Zależność'}
                  </span>
                  <Badge
                    variant={
                      dep.dependency_type === 'required'
                        ? 'danger'
                        : dep.dependency_type === 'embedded'
                        ? 'neutral'
                        : dep.dependency_type === 'incompatible'
                        ? 'danger'
                        : 'fabric'
                    }
                  >
                    {dep.dependency_type}
                  </Badge>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Changelog Content */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-secondary)' }}>
            Zmiany i poprawki w tej wersji:
          </div>
          <div
            style={{
              maxHeight: '340px',
              overflowY: 'auto',
              padding: '16px',
              background: 'var(--bg-surface)',
              borderRadius: '8px',
              border: '1px solid var(--border-light)',
              fontSize: '13px',
              lineHeight: 1.6,
              color: 'var(--text-primary)',
              whiteSpace: 'pre-wrap',
              fontFamily: 'inherit',
            }}
          >
            {targetVer?.changelog && targetVer.changelog.trim()
              ? targetVer.changelog
              : modInfo.changelog && modInfo.changelog.trim()
              ? modInfo.changelog
              : 'Twórca moda nie załączył szczegółowego opisu zmian (changelog) dla tej wersji.'}
          </div>
        </div>

        {/* Footer Actions */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginTop: '10px',
            paddingTop: '16px',
            borderTop: '1px solid var(--border-light)',
          }}
        >
          <Button variant="ghost" onClick={onClose}>
            Zamknij
          </Button>

          {onApplyUpdate && modInfo.status === 'available' && (
            <Button
              variant="primary"
              onClick={() => onApplyUpdate(modInfo)}
              disabled={isApplying}
            >
              <DownloadIcon size={16} />
              <span>{isApplying ? 'Instalowanie...' : 'Zaktualizuj ten mod'}</span>
            </Button>
          )}
        </div>
      </div>
    </Modal>
  );
};
