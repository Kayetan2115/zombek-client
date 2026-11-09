import React from 'react';
import { useLauncherContext, TabType } from '../../stores';
import { MascotZambek } from '../../assets';
import {
  HomeIcon,
  PlayIcon,
  VersionsIcon,
  ModpacksIcon,
  ModsIcon,
  CosmeticsIcon,
  AccountsIcon,
  SettingsIcon,
  LogsIcon,
  SparkleIcon,
  ZapIcon,
} from '../icons';

interface NavItem {
  id: TabType;
  label: string;
  icon: React.ReactNode;
  badge?: string;
}

const NAV_ITEMS: NavItem[] = [
  { id: 'home', label: 'Home', icon: <HomeIcon size={18} /> },
  { id: 'play', label: 'Graj (Play)', icon: <PlayIcon size={18} />, badge: 'LIVE' },
  { id: 'versions', label: 'Wersje', icon: <VersionsIcon size={18} /> },
  { id: 'modpacks', label: 'Paczki modów', icon: <ModpacksIcon size={18} /> },
  { id: 'mods', label: 'Moduły / Mody', icon: <ModsIcon size={18} />, badge: 'PREVIEW' },
  { id: 'performance', label: 'Wydajność (FPS)', icon: <ZapIcon size={18} />, badge: '8D1' },
  { id: 'cosmetics', label: 'Kosmetyki', icon: <CosmeticsIcon size={18} />, badge: 'FESTIVE' },
  { id: 'accounts', label: 'Konta', icon: <AccountsIcon size={18} /> },
  { id: 'settings', label: 'Ustawienia', icon: <SettingsIcon size={18} /> },
  { id: 'logs', label: 'Dziennik zdarzeń', icon: <LogsIcon size={18} /> },
];

export const Sidebar: React.FC = () => {
  const { currentTab, setCurrentTab, activeAccount, activeInstallation } = useLauncherContext();

  return (
    <aside
      style={{
        width: 'var(--sidebar-width)',
        minWidth: 'var(--sidebar-width)',
        height: '100vh',
        background: 'var(--bg-sidebar)',
        borderRight: '1px solid var(--border-subtle)',
        display: 'flex',
        flexDirection: 'column',
        zIndex: 20,
        userSelect: 'none',
      }}
    >
      {/* Brand Header */}
      <div
        style={{
          padding: '18px 20px',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          borderBottom: '1px solid var(--border-subtle)',
          background: 'linear-gradient(180deg, rgba(16, 185, 129, 0.08) 0%, rgba(11, 15, 25, 0) 100%)',
        }}
      >
        <MascotZambek size="sm" withGlow />
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <h1
              className="pixel-text"
              style={{
                fontSize: '17px',
                fontWeight: 800,
                color: '#ffffff',
                letterSpacing: '0.8px',
                lineHeight: 1.1,
              }}
            >
              ZOMBEK<span style={{ color: 'var(--pine-400)' }}>CLIENT</span>
            </h1>
          </div>
          <span
            style={{
              fontSize: '10.5px',
              color: 'var(--gold-400)',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              marginTop: '2px',
            }}
          >
            <SparkleIcon size={11} color="var(--gold-400)" />
            ŚWIĄTECZNY 1.20+
          </span>
        </div>
      </div>

      {/* Navigation List */}
      <nav
        style={{
          flex: 1,
          padding: '14px 12px',
          overflowY: 'auto',
          display: 'flex',
          flexDirection: 'column',
          gap: '3px',
        }}
      >
        {NAV_ITEMS.map((item) => {
          const isActive = currentTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setCurrentTab(item.id)}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '9px 12px',
                borderRadius: '8px',
                cursor: 'pointer',
                transition: 'all 0.16s ease',
                position: 'relative',
                background: isActive
                  ? 'linear-gradient(90deg, rgba(16, 185, 129, 0.22) 0%, rgba(14, 165, 233, 0.12) 100%)'
                  : 'transparent',
                color: isActive ? '#ffffff' : 'var(--text-secondary)',
                fontWeight: isActive ? 600 : 500,
                border: `1px solid ${isActive ? 'rgba(16, 185, 129, 0.4)' : 'transparent'}`,
              }}
              onMouseEnter={(e) => {
                if (!isActive) {
                  e.currentTarget.style.background = 'var(--bg-surface)';
                  e.currentTarget.style.color = '#ffffff';
                }
              }}
              onMouseLeave={(e) => {
                if (!isActive) {
                  e.currentTarget.style.background = 'transparent';
                  e.currentTarget.style.color = 'var(--text-secondary)';
                }
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '11px' }}>
                <span style={{ color: isActive ? 'var(--pine-400)' : 'inherit', display: 'flex' }}>
                  {item.icon}
                </span>
                <span style={{ fontSize: '13px' }}>{item.label}</span>
              </div>

              {item.badge && (
                <span
                  style={{
                    fontSize: '9.5px',
                    fontWeight: 700,
                    padding: '1px 6px',
                    borderRadius: '4px',
                    letterSpacing: '0.4px',
                    background:
                      item.badge === 'LIVE'
                        ? 'rgba(16, 185, 129, 0.25)'
                        : item.badge === 'FESTIVE'
                        ? 'rgba(239, 68, 68, 0.25)'
                        : 'rgba(56, 189, 248, 0.2)',
                    color:
                      item.badge === 'LIVE'
                        ? '#34d399'
                        : item.badge === 'FESTIVE'
                        ? '#f87171'
                        : '#38bdf8',
                  }}
                >
                  {item.badge}
                </span>
              )}

              {isActive && (
                <div
                  style={{
                    position: 'absolute',
                    left: 0,
                    top: '20%',
                    bottom: '20%',
                    width: '3px',
                    borderRadius: '0 4px 4px 0',
                    background: 'var(--pine-400)',
                    boxShadow: '0 0 8px var(--pine-400)',
                  }}
                />
              )}
            </button>
          );
        })}
      </nav>

      {/* Profile & Status Footer Card */}
      <div
        style={{
          padding: '14px 16px',
          borderTop: '1px solid var(--border-subtle)',
          background: 'var(--bg-surface)',
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
        }}
      >
        <div
          style={{
            width: '36px',
            height: '36px',
            borderRadius: '6px',
            overflow: 'hidden',
            border: '1px solid var(--border-light)',
            flexShrink: 0,
            background: '#1e293b',
          }}
        >
          <img
            src={activeAccount.avatar}
            alt={activeAccount.username}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
          />
        </div>

        <div style={{ flex: 1, minWidth: 0 }}>
          <div
            style={{
              fontSize: '12.5px',
              fontWeight: 600,
              color: '#ffffff',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}
          >
            {activeAccount.username}
          </div>
          <div
            style={{
              fontSize: '11px',
              color: 'var(--pine-400)',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
            }}
          >
            <span
              style={{
                width: '6px',
                height: '6px',
                borderRadius: '50%',
                background: '#10b981',
                display: 'inline-block',
              }}
            />
            {activeInstallation.name}
          </div>
        </div>
      </div>
    </aside>
  );
};
