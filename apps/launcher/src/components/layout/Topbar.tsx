import React, { useState } from 'react';
import { useLauncherContext, TabType } from '../../stores';
import { useZombekApi } from '../../hooks';
import {
  BellIcon,
  SnowflakeIcon,
  MinimizeIcon,
  MaximizeIcon,
  CloseIcon,
  SparkleIcon,
  ChevronIcon,
} from '../icons';

const TAB_TITLES: Record<TabType, string> = {
  home: 'Pulpit Główny',
  play: 'Uruchamianie Gry',
  versions: 'Instalacje i Wersje',
  modpacks: 'Menedżer Paczek Modów',
  mods: 'Katalog Modułów i HUD',
  performance: 'Optymalizacja i Wydajność (Zombek Performance)',
  cosmetics: 'Garderoba i Kosmetyki',
  accounts: 'Zarządzanie Kontami',
  settings: 'Ustawienia Launchera',
  logs: 'Konsola i Dziennik Zdarzeń',
};

export const Topbar: React.FC = () => {
  const {
    currentTab,
    setCurrentTab,
    activeInstallation,
    installations,
    setActiveInstallationId,
    activeAccount,
    settings,
    updateSettings,
    addToast,
  } = useLauncherContext();

  const api = useZombekApi();
  const [showNotifications, setShowNotifications] = useState(false);
  const [showInstDropdown, setShowInstDropdown] = useState(false);

  const toggleSnow = () => {
    const next = !settings.snowfall;
    updateSettings({ snowfall: next });
    addToast({
      type: 'info',
      title: next ? 'Śnieg włączony' : 'Śnieg wyłączony',
      message: next ? 'Zimowy klimat aktywny!' : 'Wyłączono cząsteczki śniegu.',
    });
  };

  const handleMinimize = () => {
    if (api?.minimizeWindow) {
      api.minimizeWindow();
    } else {
      addToast({ type: 'info', title: 'Minimalizacja (wersja przeglądarkowa)' });
    }
  };

  const handleMaximize = () => {
    if (api?.maximizeWindow) {
      api.maximizeWindow();
    } else {
      addToast({ type: 'info', title: 'Maksymalizacja (wersja przeglądarkowa)' });
    }
  };

  const handleClose = () => {
    if (api?.closeWindow) {
      api.closeWindow();
    } else {
      addToast({ type: 'warning', title: 'Zamknięcie okna (wersja przeglądarkowa)' });
    }
  };

  return (
    <header
      style={{
        height: 'var(--topbar-height)',
        background: 'var(--bg-topbar)',
        borderBottom: '1px solid var(--border-subtle)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 20px',
        zIndex: 30,
        userSelect: 'none',
      }}
    >
      {/* Left: View Breadcrumb */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        <h2 style={{ fontSize: '15px', fontWeight: 700, color: 'var(--text-snow)' }}>
          {TAB_TITLES[currentTab]}
        </h2>
        <span style={{ color: 'var(--text-dim)', fontSize: '13px' }}>/</span>
        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>ZombekClient 0.1.0-alpha</span>
      </div>

      {/* Right: Quick actions, Profile, Window Controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
        {/* Quick Installation Switcher */}
        <div style={{ position: 'relative' }}>
          <button
            onClick={() => setShowInstDropdown(!showInstDropdown)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              padding: '5px 12px',
              borderRadius: '6px',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-light)',
              color: 'var(--text-primary)',
              fontSize: '12.5px',
              cursor: 'pointer',
              transition: 'border-color 0.15s ease',
            }}
          >
            <span
              style={{
                width: '7px',
                height: '7px',
                borderRadius: '50%',
                background: '#10b981',
                display: 'inline-block',
              }}
            />
            <span style={{ fontWeight: 600 }}>{activeInstallation.name}</span>
            <ChevronIcon size={13} color="var(--text-muted)" direction={showInstDropdown ? 'up' : 'down'} />
          </button>

          {showInstDropdown && (
            <div
              style={{
                position: 'absolute',
                top: 'calc(100% + 6px)',
                right: 0,
                width: '260px',
                background: 'var(--bg-card)',
                border: '1px solid var(--border-light)',
                borderRadius: '8px',
                boxShadow: '0 10px 30px rgba(0, 0, 0, 0.6)',
                zIndex: 100,
                padding: '6px',
              }}
            >
              <div
                style={{
                  padding: '6px 8px',
                  fontSize: '11px',
                  fontWeight: 700,
                  color: 'var(--text-dim)',
                  textTransform: 'uppercase',
                  borderBottom: '1px solid var(--border-subtle)',
                }}
              >
                Szybki wybór profilu
              </div>
              {installations.map((inst) => {
                const isSelected = inst.id === activeInstallation.id;
                return (
                  <div
                    key={inst.id}
                    onClick={() => {
                      setActiveInstallationId(inst.id);
                      setShowInstDropdown(false);
                      addToast({
                        type: 'info',
                        title: 'Zmieniono profil',
                        message: `Aktywna instalacja: ${inst.name}`,
                      });
                    }}
                    style={{
                      padding: '8px 10px',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: '2px',
                      background: isSelected ? 'var(--bg-surface-active)' : 'transparent',
                      marginTop: '4px',
                    }}
                    onMouseEnter={(e) => {
                      if (!isSelected) e.currentTarget.style.background = 'var(--bg-surface-hover)';
                    }}
                    onMouseLeave={(e) => {
                      if (!isSelected) e.currentTarget.style.background = 'transparent';
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span
                        style={{
                          fontSize: '12.5px',
                          fontWeight: 600,
                          color: isSelected ? 'var(--pine-400)' : 'var(--text-primary)',
                        }}
                      >
                        {inst.name}
                      </span>
                      <span
                        style={{
                          fontSize: '10.5px',
                          background: 'rgba(56, 189, 248, 0.15)',
                          color: '#38bdf8',
                          padding: '1px 5px',
                          borderRadius: '3px',
                        }}
                      >
                        {inst.loader}
                      </span>
                    </div>
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                      MC {inst.minecraftVersion} • {inst.allocatedRam} MB RAM
                    </span>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Snowfall quick toggle */}
        <button
          onClick={toggleSnow}
          style={{
            width: '32px',
            height: '32px',
            borderRadius: '6px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            background: settings.snowfall ? 'rgba(56, 189, 248, 0.15)' : 'var(--bg-surface)',
            border: `1px solid ${settings.snowfall ? 'rgba(56, 189, 248, 0.4)' : 'var(--border-subtle)'}`,
            color: settings.snowfall ? 'var(--frost-400)' : 'var(--text-muted)',
            transition: 'all 0.15s ease',
          }}
          title={settings.snowfall ? 'Wyłącz padający śnieg' : 'Włącz padający śnieg'}
        >
          <SnowflakeIcon size={16} />
        </button>

        {/* Notifications Button */}
        <div style={{ position: 'relative' }}>
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            style={{
              width: '32px',
              height: '32px',
              borderRadius: '6px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              color: 'var(--text-secondary)',
              position: 'relative',
              transition: 'all 0.15s ease',
            }}
            title="Powiadomienia i wiadomości"
          >
            <BellIcon size={16} />
            <span
              style={{
                position: 'absolute',
                top: '5px',
                right: '5px',
                width: '6px',
                height: '6px',
                borderRadius: '50%',
                background: '#ef4444',
                boxShadow: '0 0 6px #ef4444',
              }}
            />
          </button>

          {showNotifications && (
            <div
              style={{
                position: 'absolute',
                top: 'calc(100% + 6px)',
                right: 0,
                width: '300px',
                background: 'var(--bg-card)',
                border: '1px solid var(--border-light)',
                borderRadius: '8px',
                boxShadow: '0 10px 30px rgba(0, 0, 0, 0.6)',
                zIndex: 100,
                padding: '12px',
              }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  marginBottom: '10px',
                  paddingBottom: '8px',
                  borderBottom: '1px solid var(--border-subtle)',
                }}
              >
                <span style={{ fontSize: '13px', fontWeight: 700, color: '#ffffff' }}>Powiadomienia</span>
                <span style={{ fontSize: '11px', color: 'var(--gold-400)', fontWeight: 600 }}>Nowość</span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                <div
                  style={{
                    padding: '8px',
                    borderRadius: '6px',
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-subtle)',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--pine-400)' }}>
                    <SparkleIcon size={13} />
                    <span style={{ fontSize: '12px', fontWeight: 700 }}>Świąteczna Edycja ZombekClient</span>
                  </div>
                  <p style={{ fontSize: '11.5px', color: 'var(--text-secondary)', marginTop: '4px', lineHeight: 1.4 }}>
                    Witaj w Etapie 2! Zobacz nowy motyw zimowy, maskotkę Ząbek oraz katalog modów w stylu Lunar Client.
                  </p>
                </div>

                <div
                  style={{
                    padding: '8px',
                    borderRadius: '6px',
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-subtle)',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--frost-400)' }}>
                    <SnowflakeIcon size={13} />
                    <span style={{ fontSize: '12px', fontWeight: 700 }}>Etap 3 wkrótce</span>
                  </div>
                  <p style={{ fontSize: '11.5px', color: 'var(--text-secondary)', marginTop: '4px', lineHeight: 1.4 }}>
                    Silnik pobierania Minecrafta i integracja JVM zostaną podłączone w kolejnym etapie.
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Account Pill (Quick Jump to Accounts) */}
        <button
          onClick={() => setCurrentTab('accounts')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            padding: '4px 10px 4px 6px',
            borderRadius: '20px',
            background: 'var(--bg-surface)',
            border: '1px solid var(--border-subtle)',
            cursor: 'pointer',
            transition: 'all 0.15s ease',
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.borderColor = 'var(--frost-500)';
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.borderColor = 'var(--border-subtle)';
          }}
          title="Przejdź do zarządzania kontami"
        >
          <img
            src={activeAccount.avatar}
            alt={activeAccount.username}
            style={{ width: '22px', height: '22px', borderRadius: '50%', objectFit: 'cover' }}
          />
          <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-snow)' }}>
            {activeAccount.username}
          </span>
          <span
            style={{
              fontSize: '10px',
              padding: '1px 5px',
              borderRadius: '3px',
              background:
                activeAccount.type === 'microsoft'
                  ? 'rgba(16, 185, 129, 0.2)'
                  : 'rgba(245, 158, 11, 0.2)',
              color: activeAccount.type === 'microsoft' ? '#34d399' : '#fbbf24',
              fontWeight: 700,
            }}
          >
            {activeAccount.type === 'microsoft' ? 'MS' : 'OFFLINE'}
          </span>
        </button>

        {/* Window Controls */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            marginLeft: '8px',
            paddingLeft: '12px',
            borderLeft: '1px solid var(--border-subtle)',
          }}
        >
          <button
            onClick={handleMinimize}
            style={{
              width: '28px',
              height: '28px',
              borderRadius: '4px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-muted)',
              cursor: 'pointer',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.background = 'var(--bg-surface-active)';
              e.currentTarget.style.color = '#ffffff';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background = 'transparent';
              e.currentTarget.style.color = 'var(--text-muted)';
            }}
            title="Minimalizuj"
          >
            <MinimizeIcon size={12} />
          </button>

          <button
            onClick={handleMaximize}
            style={{
              width: '28px',
              height: '28px',
              borderRadius: '4px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-muted)',
              cursor: 'pointer',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.background = 'var(--bg-surface-active)';
              e.currentTarget.style.color = '#ffffff';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background = 'transparent';
              e.currentTarget.style.color = 'var(--text-muted)';
            }}
            title="Maksymalizuj / Przywróć"
          >
            <MaximizeIcon size={12} />
          </button>

          <button
            onClick={handleClose}
            style={{
              width: '28px',
              height: '28px',
              borderRadius: '4px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-muted)',
              cursor: 'pointer',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.background = '#dc2626';
              e.currentTarget.style.color = '#ffffff';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background = 'transparent';
              e.currentTarget.style.color = 'var(--text-muted)';
            }}
            title="Zamknij"
          >
            <CloseIcon size={14} />
          </button>
        </div>
      </div>
    </header>
  );
};
