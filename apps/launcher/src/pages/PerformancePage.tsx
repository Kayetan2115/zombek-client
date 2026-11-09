import React, { useState, useEffect } from 'react';
import { useLauncherContext } from '../stores';
import {
  ZapIcon,
  RefreshIcon,
  ShieldCheckIcon,
  HistoryIcon,
  WarningIcon,
} from '../components/icons';
import type {
  PerformanceProfileType,
  PerformanceStatusInfo,
  HardwareSpecs,
  OptimizationModEntry,
  GraphicsPresetOptions,
  BenchmarkSummary,
} from '@zombek/shared-types';

export const PerformancePage: React.FC = () => {
  const { activeInstallation } = useLauncherContext();

  const [loading, setLoading] = useState(true);
  const [applying, setApplying] = useState(false);
  const [restoring, setRestoring] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [statusInfo, setStatusInfo] = useState<PerformanceStatusInfo | null>(null);
  const [selectedProfile, setSelectedProfile] = useState<PerformanceProfileType>('MAX_FPS');
  const [graphicsOptions, setGraphicsOptions] = useState<GraphicsPresetOptions>({
    renderDistance: 8,
    simulationDistance: 5,
    graphicsMode: 'fast',
    particles: 'minimal',
    clouds: false,
    entityDistance: 75,
    entityCulling: true,
    framerateLimit: 0,
    biomeBlend: 0,
    enableVsync: false,
  });
  const [benchmarks, setBenchmarks] = useState<BenchmarkSummary[]>([]);

  const loadData = async () => {
    if (!window.zombekDesktopApi || !activeInstallation) {
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setErrorMessage(null);

      const status = await window.zombekDesktopApi.getPerformanceStatus(activeInstallation.id);
      setStatusInfo(status);

      if (status.currentProfile && status.currentProfile !== 'CUSTOM') {
        setSelectedProfile(status.currentProfile);
      }

      const recommended = await window.zombekDesktopApi.getRecommendedGraphicsOptions(
        activeInstallation.id,
        selectedProfile
      );
      setGraphicsOptions(recommended);

      const bench = await window.zombekDesktopApi.getBenchmarkHistory(activeInstallation.id);
      setBenchmarks(bench);
    } catch (err: any) {
      console.error('Błąd ładowania danych optymalizacji:', err);
      setErrorMessage(err.message || 'Nie udało się załadować danych optymalizacji.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [activeInstallation?.id]);

  const handleApplyPreset = async (profileToApply: PerformanceProfileType) => {
    if (!window.zombekDesktopApi || !activeInstallation) return;

    try {
      setApplying(true);
      setStatusMessage(null);
      setErrorMessage(null);

      const res = await window.zombekDesktopApi.applyPerformancePreset(
        activeInstallation.id,
        profileToApply
      );

      if (res.success) {
        setStatusMessage(
          `Profil [${profileToApply}] został pomyślnie zastosowany! Zainstalowano mody: ${res.installedCount || 0}. Utworzono kopię zapasową.`
        );
        await loadData();
      } else {
        setErrorMessage(res.error || 'Wystąpił błąd podczas stosowania profilu.');
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Wystąpił nieoczekiwany błąd.');
    } finally {
      setApplying(false);
    }
  };

  const handleRestoreBackup = async () => {
    if (!window.zombekDesktopApi || !activeInstallation) return;

    if (!confirm('Czy na pewno chcesz przywrócić poprzednią konfigurację modów i ustawień graficznych?')) {
      return;
    }

    try {
      setRestoring(true);
      setStatusMessage(null);
      setErrorMessage(null);

      const res = await window.zombekDesktopApi.restorePerformanceBackup(activeInstallation.id);
      if (res.success) {
        setStatusMessage('Poprzednia konfiguracja modów i opcji została pomyślnie przywrócona.');
        await loadData();
      } else {
        setErrorMessage(res.error || 'Nie udało się przywrócić kopii zapasowej.');
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Wystąpił błąd podczas przywracania.');
    } finally {
      setRestoring(false);
    }
  };

  const handleSaveGraphics = async () => {
    if (!window.zombekDesktopApi || !activeInstallation) return;

    try {
      setApplying(true);
      const res = await window.zombekDesktopApi.applyGraphicsOptions(
        activeInstallation.id,
        graphicsOptions
      );
      if (res.success) {
        setStatusMessage('Ustawienia graficzne i plik options.txt zostały zapisane.');
      } else {
        setErrorMessage(res.error || 'Nie udało się zapisać ustawień graficznych.');
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Błąd zapisu opcji.');
    } finally {
      setApplying(false);
    }
  };

  if (!activeInstallation) {
    return (
      <div style={{ padding: '30px', textAlign: 'center', color: 'var(--text-subtle)' }}>
        <WarningIcon size={40} color="var(--warning)" />
        <h3 style={{ margin: '15px 0 8px 0', color: 'var(--text-main)' }}>Brak aktywnej instalacji Minecraft</h3>
        <p>Wybierz lub utwórz profil gry w zakładce Wersje, aby zarządzać optymalizacją wydajności.</p>
      </div>
    );
  }

  const specs = statusInfo?.hardwareSpecs;

  return (
    <div style={{ padding: '24px 30px', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <h1 style={{ margin: 0, fontSize: '24px', fontWeight: 700, color: 'var(--text-main)' }}>
              Zombek Performance Pack
            </h1>
            <span
              style={{
                background: 'rgba(74, 222, 128, 0.15)',
                color: '#4ade80',
                border: '1px solid rgba(74, 222, 128, 0.3)',
                padding: '2px 8px',
                borderRadius: '6px',
                fontSize: '11px',
                fontWeight: 600,
              }}
            >
              ETAP 8D1
            </span>
            <span
              style={{
                background: 'rgba(255, 255, 255, 0.08)',
                color: 'var(--text-subtle)',
                padding: '2px 8px',
                borderRadius: '6px',
                fontSize: '11px',
              }}
            >
              Instalacja: {activeInstallation.name} ({activeInstallation.loader.toUpperCase()} {activeInstallation.minecraftVersion})
            </span>
          </div>
          <p style={{ margin: '6px 0 0 0', color: 'var(--text-subtle)', fontSize: '13px' }}>
            Autorski system maksymalizacji liczby FPS, eliminacji przycięć i inteligentnego doboru parametrów renderowania.
          </p>
        </div>

        <button
          onClick={loadData}
          disabled={loading}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            background: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            color: 'var(--text-main)',
            padding: '8px 14px',
            borderRadius: '8px',
            cursor: 'pointer',
            fontSize: '13px',
          }}
        >
          <RefreshIcon size={16} />
          Odśwież
        </button>
      </div>

      {/* Notifications */}
      {statusMessage && (
        <div
          style={{
            background: 'rgba(74, 222, 128, 0.12)',
            border: '1px solid rgba(74, 222, 128, 0.3)',
            borderRadius: '8px',
            padding: '12px 16px',
            color: '#4ade80',
            fontSize: '13px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
          }}
        >
          <ShieldCheckIcon size={18} />
          {statusMessage}
        </div>
      )}

      {errorMessage && (
        <div
          style={{
            background: 'rgba(248, 113, 113, 0.12)',
            border: '1px solid rgba(248, 113, 113, 0.3)',
            borderRadius: '8px',
            padding: '12px 16px',
            color: '#f87171',
            fontSize: '13px',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
          }}
        >
          <WarningIcon size={18} />
          {errorMessage}
        </div>
      )}

      {/* Hardware Specs Card */}
      {specs && (
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '12px',
            padding: '18px 20px',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
            <ZapIcon size={18} color="#facc15" />
            <h3 style={{ margin: 0, fontSize: '15px', color: 'var(--text-main)' }}>
              Wykryta konfiguracja sprzętowa (Zero Telemetrii)
            </h3>
          </div>

          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '16px',
              fontSize: '12px',
            }}
          >
            <div>
              <div style={{ color: 'var(--text-subtle)', marginBottom: '4px' }}>Procesor (CPU):</div>
              <div style={{ color: 'var(--text-main)', fontWeight: 600 }}>{specs.cpuModel}</div>
              <div style={{ color: 'var(--text-subtle)', fontSize: '11px' }}>
                Wątki: {specs.cpuThreads} | Rdzenie: ~{specs.cpuCores}
              </div>
            </div>

            <div>
              <div style={{ color: 'var(--text-subtle)', marginBottom: '4px' }}>Karta Graficzna (GPU):</div>
              <div style={{ color: 'var(--text-main)', fontWeight: 600 }}>{specs.gpuModel}</div>
              <div style={{ color: 'var(--text-subtle)', fontSize: '11px' }}>Dedykowany akcelerator graficzny</div>
            </div>

            <div>
              <div style={{ color: 'var(--text-subtle)', marginBottom: '4px' }}>Pamięć RAM Systemu:</div>
              <div style={{ color: 'var(--text-main)', fontWeight: 600 }}>
                {Math.round(specs.totalRamMb / 1024)} GB Całkowita ({Math.round(specs.freeRamMb / 1024)} GB Wolna)
              </div>
              <div style={{ color: '#4ade80', fontSize: '11px', fontWeight: 600 }}>
                Zalecana alokacja MC: {specs.recommendedRamMb} MB
              </div>
            </div>

            <div>
              <div style={{ color: 'var(--text-subtle)', marginBottom: '4px' }}>Środowisko Java & OS:</div>
              <div style={{ color: 'var(--text-main)', fontWeight: 600 }}>Java {specs.javaVersion}</div>
              <div style={{ color: 'var(--text-subtle)', fontSize: '11px' }}>
                {specs.osName} ({specs.osArch})
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Profiles Cards */}
      <div>
        <h2 style={{ margin: '0 0 12px 0', fontSize: '16px', color: 'var(--text-main)' }}>
          Profile Wydajnościowe ZombekClient
        </h2>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '16px' }}>
          {/* MAX FPS */}
          <div
            onClick={() => setSelectedProfile('MAX_FPS')}
            style={{
              background: 'var(--bg-card)',
              border: `2px solid ${selectedProfile === 'MAX_FPS' ? '#4ade80' : 'var(--border-subtle)'}`,
              borderRadius: '12px',
              padding: '18px',
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              transition: 'all 0.15s ease',
            }}
          >
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span
                  style={{
                    background: 'rgba(239, 68, 68, 0.15)',
                    color: '#f87171',
                    padding: '3px 8px',
                    borderRadius: '6px',
                    fontSize: '11px',
                    fontWeight: 700,
                  }}
                >
                  MAKSYMALNY FPS
                </span>
                {statusInfo?.currentProfile === 'MAX_FPS' && (
                  <span style={{ color: '#4ade80', fontSize: '11px', fontWeight: 600 }}>✓ AKTYWNY</span>
                )}
              </div>
              <h3 style={{ margin: '12px 0 6px 0', fontSize: '16px', color: 'var(--text-main)' }}>
                Agresywna Wydajność
              </h3>
              <p style={{ margin: 0, color: 'var(--text-subtle)', fontSize: '12px', lineHeight: 1.5 }}>
                Maksymalna liczba klatek na sekundę. Render distance: 8, Fast graphics, minimalne cząsteczki, brak chmur.
              </p>
            </div>

            <button
              onClick={(e) => {
                e.stopPropagation();
                handleApplyPreset('MAX_FPS');
              }}
              disabled={applying}
              style={{
                marginTop: '16px',
                background: selectedProfile === 'MAX_FPS' ? '#22c55e' : 'rgba(255,255,255,0.06)',
                color: selectedProfile === 'MAX_FPS' ? '#000' : 'var(--text-main)',
                border: 'none',
                padding: '10px',
                borderRadius: '8px',
                fontWeight: 600,
                fontSize: '13px',
                cursor: 'pointer',
              }}
            >
              {applying && selectedProfile === 'MAX_FPS' ? 'Stosowanie...' : 'Zastosuj Profil'}
            </button>
          </div>

          {/* BALANCED */}
          <div
            onClick={() => setSelectedProfile('BALANCED')}
            style={{
              background: 'var(--bg-card)',
              border: `2px solid ${selectedProfile === 'BALANCED' ? '#38bdf8' : 'var(--border-subtle)'}`,
              borderRadius: '12px',
              padding: '18px',
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              transition: 'all 0.15s ease',
            }}
          >
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span
                  style={{
                    background: 'rgba(56, 189, 248, 0.15)',
                    color: '#38bdf8',
                    padding: '3px 8px',
                    borderRadius: '6px',
                    fontSize: '11px',
                    fontWeight: 700,
                  }}
                >
                  ZRÓWNOWAŻONY
                </span>
                {statusInfo?.currentProfile === 'BALANCED' && (
                  <span style={{ color: '#38bdf8', fontSize: '11px', fontWeight: 600 }}>✓ AKTYWNY</span>
                )}
              </div>
              <h3 style={{ margin: '12px 0 6px 0', fontSize: '16px', color: 'var(--text-main)' }}>
                Płynność i Wygląd
              </h3>
              <p style={{ margin: 0, color: 'var(--text-subtle)', fontSize: '12px', lineHeight: 1.5 }}>
                Kompromis między FPS a ładnym światem. Render distance: 12, Fancy graphics, limit 144 FPS.
              </p>
            </div>

            <button
              onClick={(e) => {
                e.stopPropagation();
                handleApplyPreset('BALANCED');
              }}
              disabled={applying}
              style={{
                marginTop: '16px',
                background: selectedProfile === 'BALANCED' ? '#38bdf8' : 'rgba(255,255,255,0.06)',
                color: selectedProfile === 'BALANCED' ? '#000' : 'var(--text-main)',
                border: 'none',
                padding: '10px',
                borderRadius: '8px',
                fontWeight: 600,
                fontSize: '13px',
                cursor: 'pointer',
              }}
            >
              {applying && selectedProfile === 'BALANCED' ? 'Stosowanie...' : 'Zastosuj Profil'}
            </button>
          </div>

          {/* VISUAL QUALITY */}
          <div
            onClick={() => setSelectedProfile('VISUAL_QUALITY')}
            style={{
              background: 'var(--bg-card)',
              border: `2px solid ${selectedProfile === 'VISUAL_QUALITY' ? '#c084fc' : 'var(--border-subtle)'}`,
              borderRadius: '12px',
              padding: '18px',
              cursor: 'pointer',
              display: 'flex',
              flexDirection: 'column',
              justifyContent: 'space-between',
              transition: 'all 0.15s ease',
            }}
          >
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span
                  style={{
                    background: 'rgba(192, 132, 252, 0.15)',
                    color: '#c084fc',
                    padding: '3px 8px',
                    borderRadius: '6px',
                    fontSize: '11px',
                    fontWeight: 700,
                  }}
                >
                  WYSOKA JAKOŚĆ
                </span>
                {statusInfo?.currentProfile === 'VISUAL_QUALITY' && (
                  <span style={{ color: '#c084fc', fontSize: '11px', fontWeight: 600 }}>✓ AKTYWNY</span>
                )}
              </div>
              <h3 style={{ margin: '12px 0 6px 0', fontSize: '16px', color: 'var(--text-main)' }}>
                Pełna Grafika + Optymalizacja
              </h3>
              <p style={{ margin: 0, color: 'var(--text-subtle)', fontSize: '12px', lineHeight: 1.5 }}>
                Render distance: 16, pełne cząsteczki, obsługa shaderów przy zachowaniu rdzennych optymalizacji pamięci.
              </p>
            </div>

            <button
              onClick={(e) => {
                e.stopPropagation();
                handleApplyPreset('VISUAL_QUALITY');
              }}
              disabled={applying}
              style={{
                marginTop: '16px',
                background: selectedProfile === 'VISUAL_QUALITY' ? '#c084fc' : 'rgba(255,255,255,0.06)',
                color: selectedProfile === 'VISUAL_QUALITY' ? '#000' : 'var(--text-main)',
                border: 'none',
                padding: '10px',
                borderRadius: '8px',
                fontWeight: 600,
                fontSize: '13px',
                cursor: 'pointer',
              }}
            >
              {applying && selectedProfile === 'VISUAL_QUALITY' ? 'Stosowanie...' : 'Zastosuj Profil'}
            </button>
          </div>
        </div>

        {/* Action Toolbar */}
        <div style={{ marginTop: '16px', display: 'flex', gap: '12px', alignItems: 'center' }}>
          <button
            onClick={() => handleApplyPreset(selectedProfile)}
            disabled={applying}
            style={{
              background: '#22c55e',
              color: '#000',
              fontWeight: 700,
              padding: '12px 20px',
              borderRadius: '8px',
              border: 'none',
              cursor: 'pointer',
              fontSize: '14px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
            }}
          >
            <ZapIcon size={18} />
            {applying ? 'Trwa optymalizacja...' : `Automatyczna optymalizacja (${selectedProfile})`}
          </button>

          {statusInfo?.hasBackup && (
            <button
              onClick={handleRestoreBackup}
              disabled={restoring}
              style={{
                background: 'rgba(255,255,255,0.06)',
                color: 'var(--text-main)',
                border: '1px solid var(--border-subtle)',
                padding: '12px 18px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '13px',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <HistoryIcon size={16} />
              {restoring ? 'Przywracanie...' : 'Przywróć poprzednią konfigurację (Rollback)'}
            </button>
          )}
        </div>
      </div>

      {/* Optimization Mods Status Table */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-subtle)',
          borderRadius: '12px',
          padding: '18px 20px',
        }}
      >
        <h3 style={{ margin: '0 0 14px 0', fontSize: '15px', color: 'var(--text-main)' }}>
          Aktywne i Zalecane Mody Wydajnościowe
        </h3>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
          {(statusInfo?.activePresetMods || []).map((mod) => (
            <div
              key={mod.slug}
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '12px 16px',
                background: 'rgba(255,255,255,0.02)',
                borderRadius: '8px',
                border: '1px solid rgba(255,255,255,0.05)',
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
                <span
                  style={{
                    display: 'inline-block',
                    width: '10px',
                    height: '10px',
                    borderRadius: '50%',
                    background: mod.installed ? '#4ade80' : 'rgba(255,255,255,0.2)',
                  }}
                />
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <span style={{ fontWeight: 600, color: 'var(--text-main)', fontSize: '14px' }}>{mod.name}</span>
                    <span
                      style={{
                        fontSize: '10px',
                        padding: '2px 6px',
                        borderRadius: '4px',
                        background: 'rgba(255,255,255,0.08)',
                        color: 'var(--text-subtle)',
                        textTransform: 'uppercase',
                      }}
                    >
                      {mod.category}
                    </span>
                    {mod.version && (
                      <span style={{ fontSize: '11px', color: 'var(--text-subtle)' }}>v{mod.version}</span>
                    )}
                  </div>
                  <div style={{ fontSize: '12px', color: 'var(--text-subtle)', marginTop: '2px' }}>
                    {mod.description}
                  </div>
                </div>
              </div>

              <div>
                <span
                  style={{
                    padding: '4px 10px',
                    borderRadius: '6px',
                    fontSize: '11px',
                    fontWeight: 600,
                    background: mod.installed ? 'rgba(74, 222, 128, 0.15)' : 'rgba(255,255,255,0.05)',
                    color: mod.installed ? '#4ade80' : 'var(--text-subtle)',
                  }}
                >
                  {mod.installed ? 'ZAINSTALOWANY' : 'BRAK'}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Recommended Graphics Settings */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-subtle)',
          borderRadius: '12px',
          padding: '18px 20px',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 style={{ margin: 0, fontSize: '15px', color: 'var(--text-main)' }}>
            Zalecane Ustawienia Graficzne (options.txt / Sodium)
          </h3>
          <button
            onClick={handleSaveGraphics}
            disabled={applying}
            style={{
              background: 'var(--accent, #22c55e)',
              color: '#000',
              fontWeight: 600,
              padding: '6px 14px',
              borderRadius: '6px',
              border: 'none',
              cursor: 'pointer',
              fontSize: '12px',
            }}
          >
            Zapisz Ustawienia
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', fontSize: '13px' }}>
          <div>
            <label style={{ display: 'block', color: 'var(--text-subtle)', marginBottom: '6px' }}>
              Zasięg renderowania (Chunks): {graphicsOptions.renderDistance}
            </label>
            <input
              type="range"
              min="4"
              max="32"
              value={graphicsOptions.renderDistance}
              onChange={(e) =>
                setGraphicsOptions({ ...graphicsOptions, renderDistance: parseInt(e.target.value, 10) })
              }
              style={{ width: '100%' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', color: 'var(--text-subtle)', marginBottom: '6px' }}>
              Zasięg symulacji: {graphicsOptions.simulationDistance}
            </label>
            <input
              type="range"
              min="4"
              max="16"
              value={graphicsOptions.simulationDistance}
              onChange={(e) =>
                setGraphicsOptions({ ...graphicsOptions, simulationDistance: parseInt(e.target.value, 10) })
              }
              style={{ width: '100%' }}
            />
          </div>

          <div>
            <label style={{ display: 'block', color: 'var(--text-subtle)', marginBottom: '6px' }}>
              Jakość grafiki:
            </label>
            <select
              value={graphicsOptions.graphicsMode}
              onChange={(e) =>
                setGraphicsOptions({ ...graphicsOptions, graphicsMode: e.target.value as any })
              }
              style={{
                width: '100%',
                background: 'rgba(255,255,255,0.06)',
                border: '1px solid var(--border-subtle)',
                color: 'var(--text-main)',
                padding: '6px 10px',
                borderRadius: '6px',
              }}
            >
              <option value="fast">Szybka (Fast - najwyższy FPS)</option>
              <option value="fancy">Ładna (Fancy)</option>
              <option value="fabulous">Bajeczna (Fabulous)</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', color: 'var(--text-subtle)', marginBottom: '6px' }}>
              Cząsteczki:
            </label>
            <select
              value={graphicsOptions.particles}
              onChange={(e) =>
                setGraphicsOptions({ ...graphicsOptions, particles: e.target.value as any })
              }
              style={{
                width: '100%',
                background: 'rgba(255,255,255,0.06)',
                border: '1px solid var(--border-subtle)',
                color: 'var(--text-main)',
                padding: '6px 10px',
                borderRadius: '6px',
              }}
            >
              <option value="minimal">Minimalne (Maksymalny FPS)</option>
              <option value="decreased">Zmniejszone</option>
              <option value="all">Wszystkie</option>
            </select>
          </div>
        </div>

        {/* Clean G1GC JVM Arguments Card */}
        <div style={{ marginTop: '16px', background: 'rgba(0,0,0,0.25)', padding: '12px 14px', borderRadius: '8px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
            <span style={{ fontSize: '11px', color: 'var(--text-subtle)', fontWeight: 600 }}>
              ZALECANE ARGUMENTY JVM (G1GC - BEZ ZBĘDNYCH FLAG):
            </span>
          </div>
          <code style={{ fontSize: '11px', color: '#a3e635', wordBreak: 'break-all', display: 'block' }}>
            {statusInfo?.recommendedJvmArgs.join(' ')}
          </code>
        </div>
      </div>

      {/* Benchmark & Measurements Section */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-subtle)',
          borderRadius: '12px',
          padding: '18px 20px',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
          <div>
            <h3 style={{ margin: 0, fontSize: '15px', color: 'var(--text-main)' }}>
              Pomiary Wydajności i Benchmark (60-sekundowy test w grze)
            </h3>
            <p style={{ margin: '4px 0 0 0', fontSize: '12px', color: 'var(--text-subtle)' }}>
              Rzeczywiste pomiary frametime, 1% Low FPS i zużycia pamięci RAM na Twojej konfiguracji.
            </p>
          </div>
        </div>

        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-subtle)', textAlign: 'left', color: 'var(--text-subtle)' }}>
                <th style={{ padding: '8px 12px' }}>Scenariusz</th>
                <th style={{ padding: '8px 12px' }}>Średni FPS</th>
                <th style={{ padding: '8px 12px' }}>1% Low FPS</th>
                <th style={{ padding: '8px 12px' }}>Min / Max FPS</th>
                <th style={{ padding: '8px 12px' }}>Frametime (ms)</th>
                <th style={{ padding: '8px 12px' }}>Średni RAM</th>
                <th style={{ padding: '8px 12px' }}>Ładowanie świata</th>
              </tr>
            </thead>
            <tbody>
              {benchmarks.map((b, idx) => {
                const isPack = b.profileName.includes('Performance Pack');
                return (
                  <tr
                    key={idx}
                    style={{
                      borderBottom: '1px solid rgba(255,255,255,0.05)',
                      background: isPack ? 'rgba(74, 222, 128, 0.05)' : 'transparent',
                    }}
                  >
                    <td style={{ padding: '10px 12px', fontWeight: 600, color: isPack ? '#4ade80' : 'var(--text-main)' }}>
                      {b.profileName}
                    </td>
                    <td style={{ padding: '10px 12px', color: isPack ? '#4ade80' : 'var(--text-main)', fontWeight: 700 }}>
                      {b.avgFps.toFixed(1)} FPS
                    </td>
                    <td style={{ padding: '10px 12px', color: isPack ? '#a3e635' : 'var(--text-subtle)', fontWeight: 600 }}>
                      {b.onePercentLowFps.toFixed(1)} FPS
                    </td>
                    <td style={{ padding: '10px 12px', color: 'var(--text-subtle)' }}>
                      {b.minFps} / {b.maxFps}
                    </td>
                    <td style={{ padding: '10px 12px', color: isPack ? '#4ade80' : 'var(--text-subtle)' }}>
                      {b.avgFrameTimeMs.toFixed(2)} ms (szczyt {b.peakFrameTimeMs.toFixed(1)} ms)
                    </td>
                    <td style={{ padding: '10px 12px', color: 'var(--text-subtle)' }}>
                      {b.avgMemoryMb} MB
                    </td>
                    <td style={{ padding: '10px 12px', color: 'var(--text-subtle)' }}>
                      {(b.worldLoadTimeMs / 1000).toFixed(2)} s
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
