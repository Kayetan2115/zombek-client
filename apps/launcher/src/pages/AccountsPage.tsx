import React, { useState } from 'react';
import { useLauncherContext, UserAccount } from '../stores';
import { Button, Badge, Modal } from '../components/common';
import { PlusIcon, TrashIcon, CheckIcon, AccountsIcon, SparkleIcon, RefreshIcon } from '../components/icons';

export const AccountsPage: React.FC = () => {
  const {
    accounts,
    activeAccount,
    setActiveAccountId,
    addOfflineAccount,
    loginMicrosoft,
    refreshAccountSession,
    removeAccount,
    authProgress,
    addToast,
  } = useLauncherContext();

  const [showAddModal, setShowAddModal] = useState(false);
  const [accountType, setAccountType] = useState<'microsoft' | 'offline'>('microsoft');
  const [offlineNickname, setOfflineNickname] = useState('');
  const [isLoggingInMs, setIsLoggingInMs] = useState(false);
  const [accountToDelete, setAccountToDelete] = useState<UserAccount | null>(null);

  const handleAddOffline = async (e: React.FormEvent) => {
    e.preventDefault();
    const cleanNick = offlineNickname.trim();
    if (!cleanNick) {
      addToast({ type: 'warning', title: 'Podaj nick', message: 'Wpisz swój pseudonim w grze.' });
      return;
    }

    const success = await addOfflineAccount(cleanNick);
    if (success) {
      setOfflineNickname('');
      setShowAddModal(false);
    }
  };

  const handleMicrosoftLogin = async () => {
    setIsLoggingInMs(true);
    try {
      const res = await loginMicrosoft();
      if (res.success) {
        setShowAddModal(false);
      }
    } finally {
      setIsLoggingInMs(false);
    }
  };

  const confirmDelete = async () => {
    if (!accountToDelete) return;
    await removeAccount(accountToDelete.id);
    setAccountToDelete(null);
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '22px', maxWidth: '940px' }}>
      {/* Header bar */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '12px',
          padding: '20px 24px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '16px',
        }}
      >
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h2 style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff' }}>Zarządzanie Kontami</h2>
            <Badge variant="success">SYSTEM KONT V4</Badge>
          </div>
          <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '4px' }}>
            Obsługa kont Microsoft (Xbox Live, XSTS, Minecraft Services) oraz lokalnych profili Offline.
          </p>
        </div>

        <Button variant="primary" icon={<PlusIcon size={16} />} onClick={() => setShowAddModal(true)}>
          Dodaj Konto
        </Button>
      </div>

      {/* Account Cards List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
        {accounts.map((acc) => {
          const isActive = acc.id === activeAccount.id;
          const isMicrosoft = acc.type === 'microsoft';
          const isSessionValid = acc.sessionStatus === 'valid';

          return (
            <div
              key={acc.id}
              style={{
                background: isActive ? 'var(--bg-surface-active)' : 'var(--bg-card)',
                border: `1px solid ${isActive ? 'var(--pine-500)' : 'var(--border-subtle)'}`,
                borderRadius: '12px',
                padding: '18px 22px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: '16px',
                flexWrap: 'wrap',
                boxShadow: isActive ? '0 4px 18px rgba(16, 185, 129, 0.15)' : 'none',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                <div
                  style={{
                    width: '52px',
                    height: '52px',
                    borderRadius: '10px',
                    overflow: 'hidden',
                    border: `2px solid ${isActive ? 'var(--pine-400)' : 'var(--border-light)'}`,
                    flexShrink: 0,
                    background: '#070a0f',
                  }}
                >
                  <img
                    src={acc.avatar}
                    alt={acc.username}
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    onError={(e) => {
                      // Fallback if avatar fails to load
                      (e.target as HTMLImageElement).src = `https://mc-heads.net/avatar/${acc.uuid}/100`;
                    }}
                  />
                </div>

                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                    <h4 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>{acc.username}</h4>
                    {isMicrosoft ? (
                      acc.hasEntitlement ? (
                        <Badge variant="success">MICROSOFT PREMIUM</Badge>
                      ) : (
                        <Badge variant="preview">BRAK LICENCJI JAVA</Badge>
                      )
                    ) : (
                      <Badge variant="gold">PROFIL OFFLINE</Badge>
                    )}

                    {isMicrosoft && (
                      <Badge variant={isSessionValid ? 'success' : 'danger'}>
                        {isSessionValid ? 'SESJA WAŻNA' : 'WYMAGANE LOGOWANIE'}
                      </Badge>
                    )}

                    {isActive && <Badge variant="festive">AKTYWNY PROFIL</Badge>}
                  </div>

                  <div
                    className="mono-text"
                    style={{ fontSize: '11.5px', color: 'var(--text-dim)', marginTop: '4px' }}
                  >
                    UUID: {acc.uuid} • Ostatnie logowanie: {acc.lastUsed}
                  </div>
                </div>
              </div>

              {/* Actions */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                {isMicrosoft && (
                  <Button
                    variant="secondary"
                    size="sm"
                    icon={<RefreshIcon size={14} />}
                    onClick={() => refreshAccountSession(acc.id)}
                    title="Odśwież tokeny autoryzacyjne"
                  >
                    Odśwież
                  </Button>
                )}

                {!isActive ? (
                  <Button
                    variant="secondary"
                    size="sm"
                    icon={<CheckIcon size={14} />}
                    onClick={() => setActiveAccountId(acc.id)}
                  >
                    Aktywuj
                  </Button>
                ) : (
                  <span style={{ fontSize: '12.5px', color: 'var(--pine-400)', fontWeight: 700 }}>
                    ✓ Wybrane
                  </span>
                )}

                <Button
                  variant="ghost"
                  size="sm"
                  icon={<TrashIcon size={14} />}
                  onClick={() => setAccountToDelete(acc)}
                  title="Usuń profil z launchera"
                >
                  Usuń
                </Button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Modal: Add Account */}
      <Modal
        isOpen={showAddModal}
        onClose={() => {
          if (!isLoggingInMs) setShowAddModal(false);
        }}
        title="Dodaj konto gracza ZombekClient"
        subtitle="Zaloguj się kontem Microsoft z licencją lub utwórz lokalny profil offline."
        footer={
          <Button variant="secondary" onClick={() => setShowAddModal(false)} disabled={isLoggingInMs}>
            Anuluj
          </Button>
        }
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
          {/* Type Selector */}
          <div style={{ display: 'flex', gap: '10px' }}>
            <button
              onClick={() => setAccountType('microsoft')}
              style={{
                flex: 1,
                padding: '12px',
                borderRadius: '8px',
                cursor: isLoggingInMs ? 'not-allowed' : 'pointer',
                background: accountType === 'microsoft' ? 'var(--bg-surface-active)' : 'var(--bg-surface)',
                border: `1px solid ${accountType === 'microsoft' ? '#10b981' : 'var(--border-subtle)'}`,
                color: accountType === 'microsoft' ? '#ffffff' : 'var(--text-secondary)',
                fontWeight: 600,
                fontSize: '13px',
                textAlign: 'center',
              }}
              disabled={isLoggingInMs}
            >
              Konto Microsoft (Premium)
            </button>

            <button
              onClick={() => setAccountType('offline')}
              style={{
                flex: 1,
                padding: '12px',
                borderRadius: '8px',
                cursor: isLoggingInMs ? 'not-allowed' : 'pointer',
                background: accountType === 'offline' ? 'var(--bg-surface-active)' : 'var(--bg-surface)',
                border: `1px solid ${accountType === 'offline' ? '#f59e0b' : 'var(--border-subtle)'}`,
                color: accountType === 'offline' ? '#ffffff' : 'var(--text-secondary)',
                fontWeight: 600,
                fontSize: '13px',
                textAlign: 'center',
              }}
              disabled={isLoggingInMs}
            >
              Profil Lokalny (Offline)
            </button>
          </div>

          {accountType === 'microsoft' ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', textAlign: 'center', padding: '12px' }}>
              <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                Logowanie odbywa się bezpiecznie w zewnętrznej przeglądarce systemowej (OAuth 2.0 PKCE).
                Tokeny sesji są szyfrowane sprzętowo przez <strong>Windows DPAPI (safeStorage)</strong> i nigdy nie trafiają do logów.
              </p>

              {authProgress && (
                <div
                  style={{
                    padding: '12px',
                    borderRadius: '8px',
                    background: authProgress.step === 'failed' ? 'rgba(239, 68, 68, 0.15)' : 'rgba(56, 189, 248, 0.12)',
                    border: `1px solid ${authProgress.step === 'failed' ? '#ef4444' : '#38bdf8'}`,
                    fontSize: '12px',
                    color: authProgress.step === 'failed' ? '#f87171' : '#38bdf8',
                    textAlign: 'left',
                    lineHeight: 1.4,
                  }}
                >
                  <strong>Krok: {authProgress.step}</strong>
                  <p style={{ marginTop: '4px' }}>{authProgress.message}</p>
                </div>
              )}

              <Button
                variant="play"
                size="lg"
                onClick={handleMicrosoftLogin}
                disabled={isLoggingInMs}
                glow={!isLoggingInMs}
              >
                {isLoggingInMs ? 'Trwa autoryzacja Microsoft...' : 'Zaloguj przez Microsoft (Przeglądarka)'}
              </Button>
            </div>
          ) : (
            <form onSubmit={handleAddOffline} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Pseudonim gracza (Nick)
                </label>
                <input
                  type="text"
                  value={offlineNickname}
                  onChange={(e) => setOfflineNickname(e.target.value)}
                  placeholder="np. ZombekMaster"
                  maxLength={16}
                  style={{
                    width: '100%',
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-light)',
                    borderRadius: '6px',
                    padding: '9px 12px',
                    color: '#ffffff',
                    fontSize: '13.5px',
                  }}
                />
                <span style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '4px', display: 'block' }}>
                  Od 3 do 16 znaków: litery, cyfry i znak podkreślenia (_).
                </span>
              </div>

              <div
                style={{
                  padding: '10px 12px',
                  borderRadius: '6px',
                  background: 'rgba(245, 158, 11, 0.1)',
                  border: '1px solid rgba(245, 158, 11, 0.3)',
                  fontSize: '12px',
                  color: '#fbbf24',
                }}
              >
                Profil offline umożliwia rozgrywkę Singleplayer oraz grę na serwerach z wyłączonym online-mode.
              </div>

              <Button variant="festive-gold" size="md" onClick={handleAddOffline}>
                Dodaj Profil Offline
              </Button>
            </form>
          )}
        </div>
      </Modal>

      {/* Modal: Confirm Delete */}
      <Modal
        isOpen={Boolean(accountToDelete)}
        onClose={() => setAccountToDelete(null)}
        title="Potwierdzenie usunięcia konta"
        subtitle={`Czy na pewno chcesz usunąć konto gracza ${accountToDelete?.username}?`}
        footer={
          <>
            <Button variant="secondary" onClick={() => setAccountToDelete(null)}>
              Anuluj
            </Button>
            <Button variant="danger" onClick={confirmDelete}>
              Usuń konto
            </Button>
          </>
        }
      >
        <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
          Usunięcie konta spowoduje wyczyszczenie zaszyfrowanych tokenów sesji z magazynu.
          <strong> Twoje światy gry (saves), instalacje oraz konfiguracje Minecrafta NIE zostaną usunięte.</strong>
        </p>
      </Modal>
    </div>
  );
};
