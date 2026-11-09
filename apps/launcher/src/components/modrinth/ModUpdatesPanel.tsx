import React, { useState, useEffect, useMemo } from 'react';
import type {
  ModUpdateInfo,
  ModUpdateCheckResult,
  ModUpdatePolicy,
  ModConflictInfo,
} from '@zombek/shared-types';
import { useLauncherContext } from '../../stores';
import { Button, Badge, SearchBar } from '../common';
import {
  RefreshIcon,
  DownloadIcon,
  CheckIcon,
  WarningIcon,
  PinIcon,
  HistoryIcon,
  SparkleIcon,
} from '../icons';
import { ModChangelogModal } from './ModChangelogModal';
import { ModConflictsModal } from './ModConflictsModal';
import { ModBackupsModal } from './ModBackupsModal';

function formatBytes(bytes?: number): string {
  if (!bytes) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

export const ModUpdatesPanel: React.FC = () => {
  const {
    activeInstallation,
    checkModUpdates,
    pinMod,
    setModUpdatePolicy,
    applyModTransaction,
    transactionProgress,
    loadModsForInstance,
    addToast,
  } = useLauncherContext();

  const [updateResult, setUpdateResult] = useState<ModUpdateCheckResult | null>(null);
  const [isChecking, setIsChecking] = useState(false);
  const [selectedFileNames, setSelectedFileNames] = useState<Set<string>>(new Set());
  const [activeFilter, setActiveFilter] = useState<
    'all' | 'updates' | 'up-to-date' | 'pinned' | 'manual' | 'conflicts'
  >('all');
  const [searchQuery, setSearchQuery] = useState('');

  // Modals state
  const [changelogMod, setChangelogMod] = useState<ModUpdateInfo | null>(null);
  const [isBackupsModalOpen, setIsBackupsModalOpen] = useState(false);
  const [isConflictsModalOpen, setIsConflictsModalOpen] = useState(false);
  const [selectedConflictModId, setSelectedConflictModId] = useState<string | undefined>(undefined);
  const [isApplyingTransaction, setIsApplyingTransaction] = useState(false);

  // Trigger check on instance change or initial mount
  const handleCheckUpdates = async (forceRefresh = false) => {
    if (!activeInstallation?.id) return;
    setIsChecking(true);
    try {
      const result = await checkModUpdates(activeInstallation.id, forceRefresh);
      setUpdateResult(result);
      setSelectedFileNames(new Set());
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Błąd sprawdzania aktualizacji',
        message: err.message || 'Nie udało się połączyć z Modrinth API.',
      });
    } finally {
      setIsChecking(false);
    }
  };

  useEffect(() => {
    handleCheckUpdates(false);
  }, [activeInstallation?.id]);

  // Extract all conflicts
  const allConflicts = useMemo<ModConflictInfo[]>(() => {
    if (!updateResult) return [];
    const list: ModConflictInfo[] = [];
    for (const item of updateResult.updates) {
      if (item.status === 'conflict' && item.statusReason) {
        list.push({
          modA: item.modName,
          modB: item.fileName,
          conflictType: 'incompatible',
          description: item.statusReason,
          suggestedResolution: 'Zaktualizuj lub wyłącz niekompatybilny mod.',
        });
      }
    }
    return list;
  }, [updateResult]);

  // Toggle selection
  const handleToggleSelect = (fileName: string) => {
    setSelectedFileNames((prev) => {
      const next = new Set(prev);
      if (next.has(fileName)) {
        next.delete(fileName);
      } else {
        next.add(fileName);
      }
      return next;
    });
  };

  const handleSelectAllAvailable = () => {
    if (!updateResult) return;
    const available = updateResult.updates.filter(
      (m) => m.status === 'available' && !m.isPinned
    );
    if (selectedFileNames.size === available.length) {
      setSelectedFileNames(new Set());
    } else {
      setSelectedFileNames(new Set(available.map((m) => m.fileName)));
    }
  };

  // Toggle pin
  const handleTogglePin = async (mod: ModUpdateInfo) => {
    if (!activeInstallation?.id) return;
    const newPinnedState = !mod.isPinned;
    try {
      const success = await pinMod(activeInstallation.id, mod.fileName, newPinnedState);
      if (success) {
        await handleCheckUpdates(true);
      }
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd przypinania', message: err.message });
    }
  };

  // Change individual policy
  const handleChangePolicy = async (mod: ModUpdateInfo, policy: ModUpdatePolicy) => {
    if (!activeInstallation?.id) return;
    try {
      const success = await setModUpdatePolicy(activeInstallation.id, mod.fileName, policy);
      if (success) {
        await handleCheckUpdates(true);
      }
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd zmiany polityki', message: err.message });
    }
  };

  // Apply single mod update
  const handleUpdateSingleMod = async (mod: ModUpdateInfo) => {
    if (!activeInstallation?.id || !mod.targetVersion?.id) return;
    setIsApplyingTransaction(true);
    try {
      const res = await applyModTransaction({
        instanceId: activeInstallation.id,
        updates: [
          {
            versionId: mod.targetVersion.id,
            fileName: mod.fileName,
          },
        ],
        createBackup: true,
        backupDescription: `Aktualizacja: ${mod.modName} do ${mod.targetVersion.version_number}`,
      });

      if (res.success) {
        if (changelogMod?.fileName === mod.fileName) {
          setChangelogMod(null);
        }
        await loadModsForInstance(activeInstallation.id);
        await handleCheckUpdates(true);
      } else {
        addToast({
          type: 'error',
          title: 'Błąd aktualizacji',
          message: res.error || 'Nie udało się zaktualizować moda.',
        });
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Błąd transakcji',
        message: err.message || 'Wystąpił nieoczekiwany błąd podczas aktualizacji.',
      });
    } finally {
      setIsApplyingTransaction(false);
    }
  };

  // Apply batch updates (selected or all)
  const handleApplyBatchUpdates = async (fileNamesToUpdate: string[]) => {
    if (!activeInstallation?.id || fileNamesToUpdate.length === 0 || !updateResult) return;
    setIsApplyingTransaction(true);

    const itemsToUpdate = updateResult.updates.filter(
      (m) =>
        fileNamesToUpdate.includes(m.fileName) &&
        m.status === 'available' &&
        m.targetVersion?.id
    );

    if (itemsToUpdate.length === 0) {
      addToast({
        type: 'info',
        title: 'Brak aktualizacji',
        message: 'Żaden z wybranych modów nie posiada gotowej aktualizacji.',
      });
      setIsApplyingTransaction(false);
      return;
    }

    try {
      const res = await applyModTransaction({
        instanceId: activeInstallation.id,
        updates: itemsToUpdate.map((m) => ({
          versionId: m.targetVersion.id,
          fileName: m.fileName,
        })),
        createBackup: true,
        backupDescription: `Zbiorcza aktualizacja ${itemsToUpdate.length} modów`,
      });

      if (res.success) {
        setSelectedFileNames(new Set());
        await loadModsForInstance(activeInstallation.id);
        await handleCheckUpdates(true);
      } else {
        addToast({
          type: 'error',
          title: 'Błąd transakcji aktualizacji',
          message:
            res.error ||
            'Transakcja została wycofana (rollback), stan plików nienaruszony.',
        });
      }
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Błąd transakcji',
        message: err.message || 'Wystąpił nieoczekiwany błąd podczas aktualizacji.',
      });
    } finally {
      setIsApplyingTransaction(false);
    }
  };

  // Filtered items
  const filteredItems = useMemo(() => {
    if (!updateResult) return [];
    return updateResult.updates.filter((item) => {
      // Search
      const q = searchQuery.toLowerCase().trim();
      const matchesSearch =
        !q ||
        item.modName.toLowerCase().includes(q) ||
        item.fileName.toLowerCase().includes(q);
      if (!matchesSearch) return false;

      // Filter tab
      if (activeFilter === 'updates') return item.status === 'available';
      if (activeFilter === 'up-to-date') return item.status === 'up-to-date';
      if (activeFilter === 'pinned') return item.isPinned;
      if (activeFilter === 'manual') return item.status === 'manual-action';
      if (activeFilter === 'conflicts') return item.status === 'conflict';

      return true;
    });
  }, [updateResult, searchQuery, activeFilter]);

  const availableUpdates = useMemo(() => {
    return (updateResult?.updates || []).filter((u) => u.status === 'available');
  }, [updateResult]);

  const availableUpdatesCount = availableUpdates.length;
  const isAllAvailableSelected =
    availableUpdatesCount > 0 && selectedFileNames.size === availableUpdatesCount;

  // Calculate percentage for progress
  const progressPercent = useMemo(() => {
    if (!transactionProgress) return 0;
    if (transactionProgress.downloadProgress?.percent !== undefined) {
      return Math.round(transactionProgress.downloadProgress.percent);
    }
    if (transactionProgress.totalSteps > 0) {
      return Math.round((transactionProgress.currentStep / transactionProgress.totalSteps) * 100);
    }
    return 50;
  }, [transactionProgress]);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
      {/* Overview Statistics Banner */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(170px, 1fr))',
          gap: '12px',
        }}
      >
        <div
          onClick={() => setActiveFilter('updates')}
          style={{
            padding: '16px',
            background:
              activeFilter === 'updates'
                ? 'linear-gradient(135deg, rgba(56, 189, 248, 0.2), rgba(14, 165, 233, 0.1))'
                : 'var(--bg-card)',
            border: `1px solid ${activeFilter === 'updates' ? 'var(--frost-500)' : 'var(--border-light)'}`,
            borderRadius: '10px',
            cursor: 'pointer',
            transition: 'all 0.16s ease',
          }}
        >
          <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Dostępne aktualizacje</div>
          <div style={{ fontSize: '24px', fontWeight: 800, color: 'var(--frost-400)', marginTop: '4px' }}>
            {availableUpdatesCount}
          </div>
        </div>

        <div
          onClick={() => setActiveFilter('up-to-date')}
          style={{
            padding: '16px',
            background:
              activeFilter === 'up-to-date'
                ? 'linear-gradient(135deg, rgba(34, 197, 94, 0.2), rgba(22, 163, 74, 0.1))'
                : 'var(--bg-card)',
            border: `1px solid ${activeFilter === 'up-to-date' ? '#22c55e' : 'var(--border-light)'}`,
            borderRadius: '10px',
            cursor: 'pointer',
            transition: 'all 0.16s ease',
          }}
        >
          <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Aktualne mody</div>
          <div style={{ fontSize: '24px', fontWeight: 800, color: '#22c55e', marginTop: '4px' }}>
            {updateResult?.upToDateCount ?? 0}
          </div>
        </div>

        <div
          onClick={() => setActiveFilter('pinned')}
          style={{
            padding: '16px',
            background:
              activeFilter === 'pinned'
                ? 'linear-gradient(135deg, rgba(234, 179, 8, 0.2), rgba(202, 138, 4, 0.1))'
                : 'var(--bg-card)',
            border: `1px solid ${activeFilter === 'pinned' ? '#eab308' : 'var(--border-light)'}`,
            borderRadius: '10px',
            cursor: 'pointer',
            transition: 'all 0.16s ease',
          }}
        >
          <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Przypięte wersje</div>
          <div style={{ fontSize: '24px', fontWeight: 800, color: '#eab308', marginTop: '4px' }}>
            {updateResult?.pinnedCount ?? 0}
          </div>
        </div>

        <div
          onClick={() => setActiveFilter('manual')}
          style={{
            padding: '16px',
            background:
              activeFilter === 'manual'
                ? 'linear-gradient(135deg, rgba(168, 85, 247, 0.2), rgba(147, 51, 234, 0.1))'
                : 'var(--bg-card)',
            border: `1px solid ${activeFilter === 'manual' ? '#a855f7' : 'var(--border-light)'}`,
            borderRadius: '10px',
            cursor: 'pointer',
            transition: 'all 0.16s ease',
          }}
        >
          <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Pliki lokalne / Nieznane</div>
          <div style={{ fontSize: '24px', fontWeight: 800, color: '#a855f7', marginTop: '4px' }}>
            {updateResult?.manualCount ?? 0}
          </div>
        </div>

        <div
          onClick={() => {
            if ((updateResult?.conflictsCount ?? 0) > 0) {
              setSelectedConflictModId(undefined);
              setIsConflictsModalOpen(true);
            }
          }}
          style={{
            padding: '16px',
            background:
              (updateResult?.conflictsCount ?? 0) > 0
                ? 'linear-gradient(135deg, rgba(239, 68, 68, 0.2), rgba(220, 38, 38, 0.1))'
                : 'var(--bg-card)',
            border: `1px solid ${(updateResult?.conflictsCount ?? 0) > 0 ? '#ef4444' : 'var(--border-light)'}`,
            borderRadius: '10px',
            cursor: (updateResult?.conflictsCount ?? 0) > 0 ? 'pointer' : 'default',
            transition: 'all 0.16s ease',
          }}
        >
          <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Wykryte konflikty</div>
          <div
            style={{
              fontSize: '24px',
              fontWeight: 800,
              color: (updateResult?.conflictsCount ?? 0) > 0 ? '#ef4444' : 'var(--text-muted)',
              marginTop: '4px',
            }}
          >
            {updateResult?.conflictsCount ?? 0}
          </div>
        </div>
      </div>

      {/* Real-time Transaction Progress Bar */}
      {transactionProgress &&
        transactionProgress.stage !== 'completed' &&
        transactionProgress.stage !== 'failed' && (
          <div
            style={{
              padding: '16px 20px',
              background: 'linear-gradient(90deg, rgba(14, 165, 233, 0.15), rgba(56, 189, 248, 0.05))',
              borderRadius: '10px',
              border: '1px solid var(--frost-500)',
              display: 'flex',
              flexDirection: 'column',
              gap: '10px',
            }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div
                  style={{
                    width: '18px',
                    height: '18px',
                    border: '2px solid var(--frost-400)',
                    borderTopColor: 'transparent',
                    borderRadius: '50%',
                    animation: 'spin 1s linear infinite',
                  }}
                />
                <span style={{ fontWeight: 700, fontSize: '14px', color: '#ffffff' }}>
                  {transactionProgress.message}
                </span>
              </div>
              <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--frost-400)' }}>
                {progressPercent}%
              </span>
            </div>

            <div
              style={{
                width: '100%',
                height: '6px',
                background: 'var(--bg-card)',
                borderRadius: '3px',
                overflow: 'hidden',
              }}
            >
              <div
                style={{
                  width: `${progressPercent}%`,
                  height: '100%',
                  background: 'var(--frost-500)',
                  transition: 'width 0.25s ease',
                }}
              />
            </div>

            {transactionProgress.downloadProgress?.filename && (
              <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                Pobierany plik:{' '}
                <code style={{ color: 'var(--frost-300)' }}>
                  {transactionProgress.downloadProgress.filename}
                </code>
              </div>
            )}
          </div>
        )}

      {/* Main Controls & Filters Bar */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '12px',
          background: 'var(--bg-card)',
          padding: '14px 18px',
          borderRadius: '12px',
          border: '1px solid var(--border-light)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          <Button
            variant="primary"
            onClick={() => handleCheckUpdates(true)}
            disabled={isChecking || isApplyingTransaction}
          >
            <RefreshIcon size={16} />
            <span>{isChecking ? 'Sprawdzanie...' : 'Sprawdź aktualizacje'}</span>
          </Button>

          <Button
            variant="secondary"
            onClick={() => handleApplyBatchUpdates(Array.from(selectedFileNames))}
            disabled={selectedFileNames.size === 0 || isApplyingTransaction}
          >
            <DownloadIcon size={16} />
            <span>Zaktualizuj zaznaczone ({selectedFileNames.size})</span>
          </Button>

          <Button
            variant="ghost"
            onClick={() =>
              handleApplyBatchUpdates(
                availableUpdates.filter((m) => !m.isPinned).map((m) => m.fileName)
              )
            }
            disabled={availableUpdatesCount === 0 || isApplyingTransaction}
          >
            <SparkleIcon size={16} />
            <span>Zaktualizuj wszystkie ({availableUpdatesCount})</span>
          </Button>

          <Button
            variant="ghost"
            onClick={() => setIsBackupsModalOpen(true)}
          >
            <HistoryIcon size={16} />
            <span>Historia i Rollback</span>
          </Button>
        </div>
      </div>

      {/* Filter Tabs & Search */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '12px',
        }}
      >
        <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
          <button
            onClick={() => setActiveFilter('all')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              background: activeFilter === 'all' ? 'var(--frost-500)' : 'var(--bg-surface)',
              color: activeFilter === 'all' ? '#ffffff' : 'var(--text-secondary)',
              border: '1px solid var(--border-light)',
            }}
          >
            Wszystkie ({updateResult?.updates.length ?? 0})
          </button>

          <button
            onClick={() => setActiveFilter('updates')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              background: activeFilter === 'updates' ? 'var(--frost-500)' : 'var(--bg-surface)',
              color: activeFilter === 'updates' ? '#ffffff' : 'var(--text-secondary)',
              border: '1px solid var(--border-light)',
            }}
          >
            Dostępne aktualizacje ({availableUpdatesCount})
          </button>

          <button
            onClick={() => setActiveFilter('up-to-date')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              background: activeFilter === 'up-to-date' ? '#22c55e' : 'var(--bg-surface)',
              color: activeFilter === 'up-to-date' ? '#ffffff' : 'var(--text-secondary)',
              border: '1px solid var(--border-light)',
            }}
          >
            Aktualne ({updateResult?.upToDateCount ?? 0})
          </button>

          <button
            onClick={() => setActiveFilter('pinned')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              background: activeFilter === 'pinned' ? '#eab308' : 'var(--bg-surface)',
              color: activeFilter === 'pinned' ? '#000000' : 'var(--text-secondary)',
              border: '1px solid var(--border-light)',
            }}
          >
            Przypięte ({updateResult?.pinnedCount ?? 0})
          </button>

          <button
            onClick={() => setActiveFilter('manual')}
            style={{
              padding: '6px 12px',
              borderRadius: '6px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              background: activeFilter === 'manual' ? '#a855f7' : 'var(--bg-surface)',
              color: activeFilter === 'manual' ? '#ffffff' : 'var(--text-secondary)',
              border: '1px solid var(--border-light)',
            }}
          >
            Pliki lokalne ({updateResult?.manualCount ?? 0})
          </button>

          {allConflicts.length > 0 && (
            <button
              onClick={() => setActiveFilter('conflicts')}
              style={{
                padding: '6px 12px',
                borderRadius: '6px',
                fontSize: '12px',
                fontWeight: 600,
                cursor: 'pointer',
                background: activeFilter === 'conflicts' ? '#ef4444' : 'rgba(239, 68, 68, 0.15)',
                color: activeFilter === 'conflicts' ? '#ffffff' : '#fca5a5',
                border: '1px solid rgba(239, 68, 68, 0.3)',
              }}
            >
              Konflikty ({allConflicts.length})
            </button>
          )}
        </div>

        <div style={{ width: '260px' }}>
          <SearchBar
            value={searchQuery}
            onChange={setSearchQuery}
            placeholder="Szukaj moda na liście..."
          />
        </div>
      </div>

      {/* Mod List Content */}
      {isChecking ? (
        <div style={{ padding: '60px', textAlign: 'center', color: 'var(--text-muted)' }}>
          Sprawdzanie stanu modów w Modrinth API...
        </div>
      ) : filteredItems.length === 0 ? (
        <div
          style={{
            padding: '60px',
            textAlign: 'center',
            color: 'var(--text-muted)',
            background: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border-light)',
          }}
        >
          {searchQuery
            ? 'Brak wyników odpowiadających wyszukiwaniu.'
            : 'Wszystkie zainstalowane mody są zaktualizowane i zgodne!'}
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {/* Header row with select all */}
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              padding: '8px 16px',
              fontSize: '12px',
              color: 'var(--text-secondary)',
              borderBottom: '1px solid var(--border-light)',
            }}
          >
            <div style={{ width: '40px' }}>
              <input
                type="checkbox"
                checked={isAllAvailableSelected}
                onChange={handleSelectAllAvailable}
                disabled={availableUpdatesCount === 0}
                style={{ cursor: 'pointer' }}
              />
            </div>
            <div style={{ flex: 2 }}>Nazwa Modyfikacji / Plik</div>
            <div style={{ flex: 1.5 }}>Wersja (Aktualna &rarr; Najnowsza)</div>
            <div style={{ flex: 1 }}>Status / Kanał</div>
            <div style={{ width: '280px', textAlign: 'right' }}>Akcje</div>
          </div>

          {filteredItems.map((item) => {
            const isSelected = selectedFileNames.has(item.fileName);
            const hasUpdate = item.status === 'available';

            return (
              <div
                key={item.fileName}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  padding: '14px 16px',
                  background: 'var(--bg-card)',
                  borderRadius: '10px',
                  border: `1px solid ${
                    item.status === 'conflict'
                      ? 'rgba(239, 68, 68, 0.4)'
                      : hasUpdate
                      ? 'rgba(56, 189, 248, 0.25)'
                      : 'var(--border-light)'
                  }`,
                  gap: '12px',
                  transition: 'border-color 0.16s ease',
                }}
              >
                {/* Checkbox */}
                <div style={{ width: '30px' }}>
                  <input
                    type="checkbox"
                    checked={isSelected}
                    onChange={() => handleToggleSelect(item.fileName)}
                    disabled={!hasUpdate || item.isPinned || item.status === 'conflict'}
                    style={{
                      cursor: hasUpdate && !item.isPinned && item.status !== 'conflict'
                        ? 'pointer'
                        : 'not-allowed',
                    }}
                  />
                </div>

                {/* Mod info */}
                <div style={{ flex: 2, display: 'flex', alignItems: 'center', gap: '12px', minWidth: 0 }}>
                  <div
                    style={{
                      width: '36px',
                      height: '36px',
                      borderRadius: '8px',
                      background: 'var(--bg-surface)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: 'var(--frost-400)',
                      flexShrink: 0,
                    }}
                  >
                    <SparkleIcon size={18} />
                  </div>

                  <div style={{ minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span
                        style={{
                          fontWeight: 700,
                          fontSize: '14px',
                          color: '#ffffff',
                          whiteSpace: 'nowrap',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                        }}
                      >
                        {item.modName}
                      </span>
                      {item.isPinned && <Badge variant="gold">Przypięty</Badge>}
                    </div>
                    <div
                      style={{
                        fontSize: '11px',
                        color: 'var(--text-muted)',
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        marginTop: '2px',
                      }}
                    >
                      {item.fileName}
                    </div>
                  </div>
                </div>

                {/* Version comparison */}
                <div style={{ flex: 1.5, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                    <span style={{ fontSize: '13px', color: 'var(--text-primary)', fontWeight: 600 }}>
                      {item.currentVersion}
                    </span>
                    {hasUpdate && item.targetVersion && (
                      <>
                        <span style={{ color: 'var(--frost-400)' }}>&rarr;</span>
                        <span style={{ fontSize: '13px', color: 'var(--frost-400)', fontWeight: 700 }}>
                          {item.targetVersion.version_number}
                        </span>
                      </>
                    )}
                  </div>
                  {hasUpdate && item.targetSizeBytes > 0 && (
                    <div style={{ fontSize: '11px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                      Rozmiar: {formatBytes(item.targetSizeBytes)}
                    </div>
                  )}
                </div>

                {/* Status & Policy */}
                <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '4px' }}>
                  {item.status === 'conflict' ? (
                    <Badge variant="danger">Konflikt</Badge>
                  ) : item.isPinned ? (
                    <Badge variant="gold">Przypięty</Badge>
                  ) : hasUpdate ? (
                    <Badge variant="fabric">Nowa wersja</Badge>
                  ) : item.status === 'manual-action' ? (
                    <Badge variant="neutral">Plik lokalny</Badge>
                  ) : (
                    <Badge variant="success">Aktualny</Badge>
                  )}

                  {/* Mod policy selector */}
                  <select
                    value={item.updatePolicy}
                    onChange={(e) => handleChangePolicy(item, e.target.value as ModUpdatePolicy)}
                    style={{
                      background: 'var(--bg-surface)',
                      color: 'var(--text-secondary)',
                      border: '1px solid var(--border-light)',
                      borderRadius: '4px',
                      padding: '2px 4px',
                      fontSize: '10px',
                      width: 'fit-content',
                    }}
                  >
                    <option value="stable-only">Stable</option>
                    <option value="stable-and-beta">Beta</option>
                    <option value="all-versions">Alpha</option>
                  </select>
                </div>

                {/* Action buttons */}
                <div
                  style={{
                    width: '280px',
                    display: 'flex',
                    justifyContent: 'flex-end',
                    alignItems: 'center',
                    gap: '6px',
                  }}
                >
                  {/* Pin/Unpin button */}
                  <Button
                    variant={item.isPinned ? 'secondary' : 'ghost'}
                    size="sm"
                    onClick={() => handleTogglePin(item)}
                    title={item.isPinned ? 'Odepnij wersję' : 'Przypnij tę wersję (blokada aktualizacji)'}
                  >
                    <PinIcon size={14} />
                    <span>{item.isPinned ? 'Odepnij' : 'Przypnij'}</span>
                  </Button>

                  {/* Conflict button if any */}
                  {item.status === 'conflict' && (
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => {
                        setSelectedConflictModId(item.fileName);
                        setIsConflictsModalOpen(true);
                      }}
                    >
                      <WarningIcon size={14} />
                      <span>Konflikt</span>
                    </Button>
                  )}

                  {/* Changelog button */}
                  {item.targetVersion && (
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setChangelogMod(item)}
                    >
                      Changelog
                    </Button>
                  )}

                  {/* Single Update Button */}
                  {hasUpdate && !item.isPinned && item.status !== 'conflict' && (
                    <Button
                      variant="primary"
                      size="sm"
                      onClick={() => handleUpdateSingleMod(item)}
                      disabled={isApplyingTransaction}
                    >
                      <DownloadIcon size={14} />
                      <span>Aktualizuj</span>
                    </Button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Changelog Modal */}
      <ModChangelogModal
        isOpen={changelogMod !== null}
        onClose={() => setChangelogMod(null)}
        modInfo={changelogMod}
        onApplyUpdate={handleUpdateSingleMod}
        isApplying={isApplyingTransaction}
      />

      {/* Conflicts Modal */}
      <ModConflictsModal
        isOpen={isConflictsModalOpen}
        onClose={() => {
          setIsConflictsModalOpen(false);
          setSelectedConflictModId(undefined);
        }}
        conflicts={allConflicts}
        selectedModId={selectedConflictModId}
      />

      {/* Backups Modal */}
      <ModBackupsModal
        isOpen={isBackupsModalOpen}
        onClose={() => setIsBackupsModalOpen(false)}
        instanceId={activeInstallation?.id || ''}
      />
    </div>
  );
};
