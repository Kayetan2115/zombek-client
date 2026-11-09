import React from 'react';
import type { ModConflictInfo } from '@zombek/shared-types';
import { Modal, Button, Badge } from '../common';
import { WarningIcon } from '../icons';

interface ModConflictsModalProps {
  isOpen: boolean;
  onClose: () => void;
  conflicts: ModConflictInfo[];
  selectedModId?: string;
}

export const ModConflictsModal: React.FC<ModConflictsModalProps> = ({
  isOpen,
  onClose,
  conflicts,
  selectedModId,
}) => {
  if (!isOpen) return null;

  const displayedConflicts = selectedModId
    ? conflicts.filter((c) => c.modA === selectedModId || c.modB === selectedModId)
    : conflicts;

  const getConflictBadge = (type: ModConflictInfo['conflictType']) => {
    switch (type) {
      case 'loader-mismatch':
        return <Badge variant="danger">Niezgodny Mod Loader</Badge>;
      case 'mc-version-mismatch':
        return <Badge variant="danger">Niezgodna wersja MC</Badge>;
      case 'incompatible':
        return <Badge variant="danger">Zadeklarowany konflikt</Badge>;
      case 'pinned-version':
        return <Badge variant="gold">Kolizja wersji / Pinned</Badge>;
      case 'version-mismatch':
        return <Badge variant="danger">Niezgodna wersja zależności</Badge>;
      default:
        return <Badge variant="danger">Konflikt</Badge>;
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Wykryte konflikty modyfikacji (${displayedConflicts.length})`}
      maxWidth="760px"
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div
          style={{
            padding: '12px 16px',
            background: 'rgba(239, 68, 68, 0.08)',
            border: '1px solid rgba(239, 68, 68, 0.25)',
            borderRadius: '8px',
            fontSize: '13px',
            color: '#fca5a5',
            lineHeight: 1.5,
          }}
        >
          Silnik ZombekClient wykrył potencjalne niezgodności, które mogą uniemożliwić uruchomienie
          Minecrafta lub doprowadzić do awarii podczas wczytywania świata. Poniżej znajduje się
          lista konfliktów wraz z rekomendowanymi krokami naprawczymi.
        </div>

        {displayedConflicts.length === 0 ? (
          <div
            style={{
              padding: '32px',
              textAlign: 'center',
              color: 'var(--text-muted)',
              fontSize: '14px',
            }}
          >
            Brak wykrytych konfliktów dla wybranego elementu.
          </div>
        ) : (
          <div
            style={{
              maxHeight: '440px',
              overflowY: 'auto',
              display: 'flex',
              flexDirection: 'column',
              gap: '12px',
            }}
          >
            {displayedConflicts.map((c, idx) => (
              <div
                key={idx}
                style={{
                  padding: '16px',
                  background: 'var(--bg-surface)',
                  borderRadius: '10px',
                  border: '1px solid var(--border-light)',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: '10px',
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <WarningIcon size={18} />
                    <span style={{ fontWeight: 700, fontSize: '15px', color: '#ffffff' }}>
                      {c.modA}
                    </span>
                    {c.modB && c.modB !== c.modA && (
                      <span style={{ color: 'var(--text-muted)', fontSize: '13px' }}>
                        &harr; {c.modB}
                      </span>
                    )}
                  </div>
                  {getConflictBadge(c.conflictType)}
                </div>

                <div style={{ fontSize: '13px', color: 'var(--text-primary)' }}>
                  {c.description}
                </div>

                {c.suggestedResolution && (
                  <div
                    style={{
                      padding: '8px 12px',
                      background: 'rgba(56, 189, 248, 0.05)',
                      borderLeft: '3px solid var(--frost-500)',
                      borderRadius: '0 6px 6px 0',
                      fontSize: '12px',
                      color: 'var(--text-secondary)',
                    }}
                  >
                    <strong style={{ color: 'var(--frost-400)' }}>Rekomendacja:</strong>{' '}
                    {c.suggestedResolution}
                  </div>
                )}
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
  );
};
