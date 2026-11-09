import React, { useState } from 'react';
import { useLauncherContext } from '../stores';
import { Button, Badge, Slider } from '../components/common';
import { PlayIcon, VersionsIcon, GearIcon, CopyIcon, TrashIcon } from '../components/icons';

export const PlayPage: React.FC = () => {
  const {
    activeInstallation,
    activeAccount,
    accounts,
    isLaunching,
    isRunning,
    launchStatusText,
    engineState,
    engineProgress,
    launchGame,
    cancelLaunch,
    logs,
    addToast,
  } = useLauncherContext();

  const effectiveAccount =
    (activeInstallation.assignedAccountId
      ? accounts.find((a) => a.id === activeInstallation.assignedAccountId)
      : activeAccount) || activeAccount;

  const [ramValue, setRamValue] = useState<number>(activeInstallation.allocatedRam || 4096);
  const [showConsolePreview] = useState(true);

  const jvmArgsFormatted = `${activeInstallation.customJvmArgs || '-XX:+UseG1GC'} -Xmx${ramValue}M -Xms${Math.round(
    ramValue / 2
  )}M -Dminecraft.launcher.brand=zombekclient -Dminecraft.launcher.version=0.1.0-alpha`;

  const copyJvmArgs = () => {
    navigator.clipboard.writeText(jvmArgsFormatted);
    addToast({ type: 'success', title: 'Skopiowano', message: 'Argumenty JVM skopiowane do schowka.' });
  };

  const handleAction = () => {
    if (isLaunching || isRunning) {
      cancelLaunch();
    } else {
      launchGame(activeInstallation.id);
    }
  };

  const recentLogs = logs.slice(-12);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', maxWidth: '1080px' }}>
      {/* Top Header Card */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '14px',
          padding: '24px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: '20px',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '18px' }}>
          <div
            style={{
              width: '64px',
              height: '64px',
              borderRadius: '12px',
              background: 'linear-gradient(135deg, #064e3b 0%, #047857 100%)',
              border: '2px solid #10b981',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
              boxShadow: '0 0 20px rgba(16, 185, 129, 0.3)',
            }}
          >
            <VersionsIcon size={34} />
          </div>

          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h2 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--text-snow)' }}>
                {activeInstallation.name}
              </h2>
              <Badge variant={activeInstallation.loader as any} size="md">
                {activeInstallation.loader.toUpperCase()} {activeInstallation.loaderVersion}
              </Badge>
              {isRunning && <Badge variant="success">GRA AKTYWNA</Badge>}
              {isLaunching && <Badge variant="preview">ŁADOWANIE SILNIKA</Badge>}
            </div>

            <p style={{ fontSize: '13px', color: 'var(--text-muted)', marginTop: '4px' }}>
              Profil gracza:{' '}
              <strong style={{ color: 'var(--text-primary)' }}>{effectiveAccount.username}</strong> (
              {effectiveAccount.type === 'microsoft'
                ? effectiveAccount.hasEntitlement
                  ? 'Microsoft Premium'
                  : 'Microsoft'
                : 'Profil Offline'}
              ) • Minecraft {activeInstallation.minecraftVersion}
            </p>
          </div>
        </div>

        {/* Big Action Button */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Button
            variant={isRunning || isLaunching ? 'danger' : 'play'}
            size="xl"
            glow={!isLaunching && !isRunning}
            icon={isLaunching || isRunning ? undefined : <PlayIcon size={24} />}
            onClick={handleAction}
            style={{ minWidth: '220px' }}
          >
            {isRunning ? 'ZAMKNIJ GRĘ' : isLaunching ? 'ANULUJ START' : 'URUCHOM GRĘ'}
          </Button>
        </div>
      </div>

      {/* Real Progress Bar Display when downloading or preparing */}
      {(isLaunching || engineProgress) && (
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--frost-500)',
            borderRadius: '12px',
            padding: '16px 20px',
            display: 'flex',
            flexDirection: 'column',
            gap: '10px',
            boxShadow: '0 0 15px rgba(56, 189, 248, 0.15)',
          }}
        >
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span
                style={{
                  width: '10px',
                  height: '10px',
                  borderRadius: '50%',
                  background: '#38bdf8',
                  boxShadow: '0 0 8px #38bdf8',
                }}
              />
              <span style={{ fontSize: '13px', fontWeight: 700, color: '#ffffff' }}>
                {launchStatusText}
              </span>
            </div>
            <span style={{ fontSize: '13px', fontWeight: 800, color: 'var(--frost-400)' }}>
              {engineProgress ? `${engineProgress.percent}%` : 'Inicjalizacja...'}
            </span>
          </div>

          {/* Progress bar line */}
          <div
            style={{
              width: '100%',
              height: '8px',
              background: 'rgba(255, 255, 255, 0.08)',
              borderRadius: '4px',
              overflow: 'hidden',
            }}
          >
            <div
              style={{
                width: `${engineProgress ? Math.min(100, Math.max(3, engineProgress.percent)) : 15}%`,
                height: '100%',
                background: 'linear-gradient(90deg, #10b981 0%, #38bdf8 100%)',
                borderRadius: '4px',
                transition: 'width 0.25s ease-out',
              }}
            />
          </div>

          {engineProgress && (
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                fontSize: '11.5px',
                color: 'var(--text-muted)',
              }}
            >
              <span>
                Pliki: <strong>{engineProgress.completedFiles}</strong> / {engineProgress.totalFiles}
                {engineProgress.currentFile ? ` (${engineProgress.currentFile})` : ''}
              </span>
              <span>
                Prędkość: <strong>{engineProgress.speedMb ?? 0} MB/s</strong>
              </span>
            </div>
          )}
        </div>
      )}

      {/* Launch Configuration Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
        {/* Memory & Performance */}
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '12px',
            padding: '20px',
            display: 'flex',
            flexDirection: 'column',
            gap: '16px',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <GearIcon size={18} color="var(--frost-400)" />
            <h3 style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff' }}>Pamięć RAM i Wydajność</h3>
          </div>

          <Slider
            label="Przydział Pamięci RAM dla JVM"
            description="Zalecane: 4096 MB dla Vanilla i modów PvP, 6144 MB dla ciężkich paczek."
            min={1024}
            max={16384}
            step={512}
            unit="MB"
            value={ramValue}
            onChange={setRamValue}
          />

          <div
            style={{
              padding: '12px',
              borderRadius: '8px',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-subtle)',
              fontSize: '12px',
              color: 'var(--text-secondary)',
              lineHeight: 1.4,
            }}
          >
            Alokujesz <strong>{(ramValue / 1024).toFixed(1)} GB</strong> pamięci operacyjnej.
            Garbage Collector: G1GC (optymalny dla minimalizacji mikro-przycięć w Minecraft).
          </div>
        </div>

        {/* JVM Launch Arguments Preview */}
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '12px',
            padding: '20px',
            display: 'flex',
            flexDirection: 'column',
            gap: '12px',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <h3 style={{ fontSize: '15px', fontWeight: 700, color: '#ffffff' }}>Podgląd parametrów JVM</h3>
            <button
              onClick={copyJvmArgs}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                fontSize: '12px',
                color: 'var(--frost-400)',
                cursor: 'pointer',
              }}
            >
              <CopyIcon size={14} /> Kopiuj
            </button>
          </div>

          <div
            className="mono-text"
            style={{
              flex: 1,
              background: 'var(--bg-app)',
              border: '1px solid var(--border-subtle)',
              borderRadius: '8px',
              padding: '12px',
              fontSize: '11.5px',
              color: 'var(--text-secondary)',
              wordBreak: 'break-all',
              lineHeight: 1.5,
              maxHeight: '120px',
              overflowY: 'auto',
            }}
          >
            {jvmArgsFormatted}
          </div>

          <p style={{ fontSize: '11.5px', color: 'var(--text-muted)' }}>
            Silnik automatycznie dołącza zweryfikowany classpath, ścieżkę do natywnych bibliotek i token gracza offline.
          </p>
        </div>
      </div>

      {/* Pre-launch Execution Stream / Live Console Preview */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-subtle)',
          borderRadius: '12px',
          overflow: 'hidden',
        }}
      >
        <div
          style={{
            padding: '14px 18px',
            background: 'var(--bg-surface)',
            borderBottom: '1px solid var(--border-subtle)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span
              style={{
                width: '8px',
                height: '8px',
                borderRadius: '50%',
                background: isRunning ? '#10b981' : isLaunching ? '#f59e0b' : '#64748b',
              }}
            />
            <h3 style={{ fontSize: '14px', fontWeight: 700, color: '#ffffff' }}>
              Konsola silnika gry (Live Console Stream)
            </h3>
          </div>
          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
            Stan: <strong style={{ color: 'var(--text-primary)' }}>{engineState.toUpperCase()}</strong>
          </span>
        </div>

        {showConsolePreview && (
          <div
            className="mono-text"
            style={{
              padding: '16px',
              background: '#070a0f',
              minHeight: '180px',
              maxHeight: '240px',
              overflowY: 'auto',
              fontSize: '12px',
              color: '#94a3b8',
              lineHeight: 1.6,
            }}
          >
            {recentLogs.map((l) => (
              <div key={l.id} style={{ display: 'flex', gap: '8px' }}>
                <span style={{ color: '#64748b' }}>[{l.timestamp}]</span>
                <span
                  style={{
                    color:
                      l.category === 'MINECRAFT'
                        ? '#10b981'
                        : l.category === 'JAVA'
                        ? '#f59e0b'
                        : '#38bdf8',
                    fontWeight: 600,
                  }}
                >
                  [{l.category}]
                </span>
                <span
                  style={{
                    color:
                      l.level === 'ERROR'
                        ? '#f87171'
                        : l.level === 'WARNING'
                        ? '#fbbf24'
                        : '#cbd5e1',
                  }}
                >
                  {l.message}
                </span>
              </div>
            ))}
            {isLaunching && (
              <div style={{ color: '#38bdf8', fontWeight: 700, marginTop: '4px' }}>
                &gt; {launchStatusText}...
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
