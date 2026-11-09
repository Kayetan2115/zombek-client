import React, { useState, useEffect } from 'react';
import type { ModpackBackupInfo } from '@zombek/shared-types';
import { Modal, Button, Badge } from '../common';
import { HistoryIcon, TrashIcon, CheckIcon, WarningIcon, RefreshIcon } from '../icons';
import { useLauncherContext } from '../../stores';

interface ModBackupsModalProps {
  isOpen: boolean;
  onClose: () => void;
  instanceId: string;
}

function formatDate(isoString: string): string {
  try {
    const d = new Date(isoString);
    return d.toLocaleDateString('pl-PL', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  } catch {
    return isoString;
  }
}

function formatBytes(bytes: number): string {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

export const ModBackupsModal: React.FC<ModBackupsModalProps> = ({
  isOpen,
  onClose,
  instanceId,
}) => {
  const { getModBackups, rollbackModBackup, loadModsForInstance, addToast } = useLauncherContext();
  const [backups, setBackups] = useState<ModpackBackupInfo[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isRollingBack, setIsRollingBack] = useState<string | null>(null);
  const [confirmBackup, setConfirmBackup] = useState<ModpackBackupInfo | null>(null);

  const fetchBackups = async () => {
    if (!instanceId) return;
    setIsLoading(true);
    try {
      const list = await getModBackups(instanceId);
      setBackups(list);
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Błąd pobierania kopii',
        message: err.message || 'Nie udało się wczytać listy kopii zapasowych.',
      });
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (isOpen) {
      fetchBackups();
    }
  }, [isOpen, instanceId]);

  const handleRollback = async (backup: ModpackBackupInfo) => {
    setIsRollingBack(backup.id);
    try {
      const res = await rollbackModBackup(instanceId, backup.id);
      if (res.success) {
        addToast({
          type: 'success',
          title: 'Przywrócono stan modpacka',
          message: `Pomyślnie przywrócono kopię zapasową z ${formatDate(backup.timestamp)} (${res.restoredCount ?? backup.filesCount} plików).`,
        });
        await loadModsForInstance(instanceId);
        setConfirmBackup(null);
        await fetchBackups();
      } else {
        addToast({
          type: 'error',
          title: 'Rollback nie powiódł się',
          message: res.error || 'Wystąpił błąd podczas odzyskiwania plików modów.',
        });
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Błąd przywracania',
        message: err.message || 'Nie udało się przywrócić kopii zapasowej.',
      });
    } finally {
      setIsRollingBack(null);
    }
  };

  if (!isOpen) return null;

  return (
    <>
      <Modal
        isOpen={isOpen}
        onClose={onClose}
        title="Kopie zapasowe i historia modpacka (Rollback)"
        maxWidth="780px"
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div
            style={{
              padding: '12px 16px',
              background: 'var(--bg-surface)',
              borderRadius: '8px',
              border: '1px solid var(--border-light)',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
            }}
          >
            <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
              ZombekClient automatycznie tworzy punkty przywracania przed każdą transakcją aktualizacji
              oraz zmianą zależności. W razie problemów możesz powrócić do dowolnego poprzedniego stanu.
            </div>
            <Button variant="ghost" size="sm" onClick={fetchBackups} disabled={isLoading}>
              <RefreshIcon size={14} />
              <span>Odśwież</span>
            </Button>
          </div>

          {isLoading ? (
            <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-muted)' }}>
              Ładowanie listy kopii zapasowych...
            </div>
          ) : backups.length === 0 ? (
            <div
              style={{
                padding: '40px',
                textAlign: 'center',
                color: 'var(--text-muted)',
                fontSize: '14px',
              }}
            >
              Brak utworzonych kopii zapasowych dla tej instalacji.
              Kopie tworzą się automatycznie podczas instalacji i aktualizacji modów.
            </div>
          ) : (
            <div
              style={{
                maxHeight: '420px',
                overflowY: 'auto',
                display: 'flex',
                flexDirection: 'column',
                gap: '12px',
              }}
            >
              {backups.map((b) => (
                <div
                  key={b.id}
                  style={{
                    padding: '16px',
                    background: 'var(--bg-surface)',
                    borderRadius: '10px',
                    border: '1px solid var(--border-light)',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    gap: '16px',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '14px', minWidth: 0 }}>
                    <div
                      style={{
                        width: '40px',
                        height: '40px',
                        borderRadius: '8px',
                        background: 'var(--bg-card)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'var(--frost-400)',
                        flexShrink: 0,
                      }}
                    >
                      <HistoryIcon size={20} />
                    </div>
                    <div style={{ minWidth: 0 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <span style={{ fontWeight: 700, fontSize: '14px', color: '#ffffff' }}>
                          {formatDate(b.timestamp)}
                        </span>
                        <Badge variant="neutral">{b.filesCount} plików</Badge>
                        <Badge variant="fabric">{formatBytes(b.totalSizeBytes)}</Badge>
                      </div>
                      <div
                        style={{
                          fontSize: '12px',
                          color: 'var(--text-secondary)',
                          marginTop: '4px',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                        }}
                      >
                        Powód: <span style={{ color: 'var(--text-primary)' }}>{b.description}</span>
                      </div>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexShrink: 0 }}>
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => setConfirmBackup(b)}
                      disabled={isRollingBack !== null}
                    >
                      <HistoryIcon size={14} />
                      <span>Przywróć (Rollback)</span>
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}

          <div
            style={{
              display: 'flex',
              justifyContent: 'flex-end',
              marginTop: '10px',
              paddingTop: '16px',
              borderTop: '1px solid var(--border-light)',
            }}
          >
            <Button variant="ghost" onClick={onClose}>
              Zamknij
            </Button>
          </div>
        </div>
      </Modal>

      {/* Confirmation Sub-Modal */}
      {confirmBackup && (
        <Modal
          isOpen={true}
          onClose={() => setConfirmBackup(null)}
          title="Potwierdź przywrócenie kopii"
          maxWidth="500px"
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: '12px',
                background: 'rgba(239, 68, 68, 0.1)',
                border: '1px solid rgba(239, 68, 68, 0.3)',
                borderRadius: '8px',
                color: '#fca5a5',
                fontSize: '13px',
              }}
            >
              <WarningIcon size={20} />
              <span>
                Czy na pewno chcesz przywrócić modpack do stanu z{' '}
                <strong>{formatDate(confirmBackup.timestamp)}</strong>?
              </span>
            </div>

            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', margin: 0 }}>
              Wszystkie aktualne mody w folderze <code>mods/</code> zostaną zastąpione plikami z tej
              kopii ({confirmBackup.filesCount} plików JAR), a plik <code>modpack.lock.json</code>{' '}
              zostanie odtworzony.
            </p>

            <div
              style={{
                display: 'flex',
                justifyContent: 'flex-end',
                gap: '10px',
                marginTop: '10px',
              }}
            >
              <Button
                variant="ghost"
                onClick={() => setConfirmBackup(null)}
                disabled={isRollingBack !== null}
              >
                Anuluj
              </Button>
              <Button
                variant="danger"
                onClick={() => handleRollback(confirmBackup)}
                disabled={isRollingBack !== null}
              >
                {isRollingBack === confirmBackup.id ? 'Przywracanie...' : 'Tak, przywróć kopię'}
              </Button>
            </div>
          </div>
        </Modal>
      )}
    </>
  );
};
