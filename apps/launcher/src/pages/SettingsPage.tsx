import React, { useState } from 'react';
import { useLauncherContext, LauncherSettingsState } from '../stores';
import { Button, Badge, Toggle, Slider, Dropdown } from '../components/common';
import { GearIcon, SnowflakeIcon, SparkleIcon, RefreshIcon } from '../components/icons';
import { MascotZambek } from '../assets';

type SettingsCategory = 'launcher' | 'game' | 'java' | 'downloads' | 'appearance' | 'about';

export const SettingsPage: React.FC = () => {
  const { settings, updateSettings, addToast, detectSystemJava } = useLauncherContext();
  const [activeCategory, setActiveCategory] = useState<SettingsCategory>('launcher');

  return (
    <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start', maxWidth: '1040px' }}>
      {/* Category Sidebar */}
      <div
        style={{
          width: '230px',
          minWidth: '200px',
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '12px',
          padding: '8px',
          display: 'flex',
          flexDirection: 'column',
          gap: '4px',
        }}
      >
        {[
          { id: 'launcher', label: 'Launcher' },
          { id: 'game', label: 'Gra i Minecraft' },
          { id: 'java', label: 'Środowisko Java' },
          { id: 'downloads', label: 'Pobieranie i Dysk' },
          { id: 'appearance', label: 'Wygląd i Śnieg' },
          { id: 'about', label: 'O Programie' },
        ].map((item) => {
          const isActive = activeCategory === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveCategory(item.id as SettingsCategory)}
              style={{
                width: '100%',
                display: 'flex',
                alignItems: 'center',
                padding: '10px 14px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '13px',
                fontWeight: isActive ? 700 : 500,
                background: isActive ? 'var(--bg-surface-active)' : 'transparent',
                border: `1px solid ${isActive ? 'var(--frost-500)' : 'transparent'}`,
                color: isActive ? '#ffffff' : 'var(--text-secondary)',
                transition: 'all 0.15s ease',
                textAlign: 'left',
              }}
            >
              {item.label}
            </button>
          );
        })}
      </div>

      {/* Main Settings Form */}
      <div
        style={{
          flex: 1,
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '12px',
          padding: '24px',
          display: 'flex',
          flexDirection: 'column',
          gap: '24px',
        }}
      >
        {/* Category: Launcher */}
        {activeCategory === 'launcher' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>Ustawienia Aplikacji Launchera</h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <Toggle
                label="Uruchamiaj ZombekClient wraz ze startem systemu Windows"
                checked={settings.startWithSystem}
                onChange={(val) => updateSettings({ startWithSystem: val })}
              />

              <Toggle
                label="Włącz powiadomienia systemowe o aktualizacjach i wydarzeniach"
                checked={settings.notifications}
                onChange={(val) => updateSettings({ notifications: val })}
              />

              <Toggle
                label="Minimalizuj launcher do zasobnika systemowego (Tray) po uruchomieniu gry"
                checked={settings.minimizeToTray}
                onChange={(val) => updateSettings({ minimizeToTray: val })}
              />
            </div>

            <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '16px' }}>
              <Dropdown
                label="Język interfejsu (Language)"
                options={[
                  { value: 'pl', label: 'Polski (Domyślny)' },
                  { value: 'en', label: 'English' },
                ]}
                value={settings.language}
                onChange={(val) => updateSettings({ language: val as any })}
              />
            </div>
          </div>
        )}

        {/* Category: Game */}
        {activeCategory === 'game' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>Ustawienia Gry Minecraft</h3>

            <Slider
              label="Pamięć RAM (Maksymalna)"
              description="Zalecana alokacja dla optymalnej liczby klatek na sekundę."
              min={1024}
              max={16384}
              step={512}
              unit="MB"
              value={settings.maxRamMb}
              onChange={(val) => updateSettings({ maxRamMb: val })}
            />

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
              <div>
                <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Szerokość okna gry (px)
                </label>
                <input
                  type="number"
                  value={settings.resolutionWidth}
                  onChange={(e) => updateSettings({ resolutionWidth: Number(e.target.value) })}
                  style={{
                    width: '100%',
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-light)',
                    borderRadius: '6px',
                    padding: '8px 12px',
                    color: '#ffffff',
                    fontSize: '13px',
                  }}
                />
              </div>

              <div>
                <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                  Wysokość okna gry (px)
                </label>
                <input
                  type="number"
                  value={settings.resolutionHeight}
                  onChange={(e) => updateSettings({ resolutionHeight: Number(e.target.value) })}
                  style={{
                    width: '100%',
                    background: 'var(--bg-surface)',
                    border: '1px solid var(--border-light)',
                    borderRadius: '6px',
                    padding: '8px 12px',
                    color: '#ffffff',
                    fontSize: '13px',
                  }}
                />
              </div>
            </div>

            <Toggle
              label="Uruchamiaj Minecraft w trybie pełnoekranowym (Fullscreen)"
              checked={settings.fullscreen}
              onChange={(val) => updateSettings({ fullscreen: val })}
            />

            <div>
              <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Katalog instalacji gry (.minecraft)
              </label>
              <input
                type="text"
                value={settings.gameDirectory}
                onChange={(e) => updateSettings({ gameDirectory: e.target.value })}
                style={{
                  width: '100%',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-light)',
                  borderRadius: '6px',
                  padding: '8px 12px',
                  color: 'var(--text-primary)',
                  fontSize: '12px',
                  fontFamily: 'monospace',
                }}
              />
            </div>
          </div>
        )}

        {/* Category: Java */}
        {activeCategory === 'java' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>Konfiguracja Środowiska Java</h3>

            <div
              style={{
                padding: '14px',
                borderRadius: '8px',
                background: 'rgba(16, 185, 129, 0.1)',
                border: '1px solid rgba(16, 185, 129, 0.3)',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <div>
                <span style={{ fontSize: '13px', fontWeight: 700, color: '#ffffff' }}>
                  Wykryta wersja systemowa Java:
                </span>
                <p style={{ fontSize: '12px', color: 'var(--pine-400)', marginTop: '2px' }}>
                  {settings.detectedJavaVersion}
                </p>
              </div>

              <Button
                variant="secondary"
                size="sm"
                icon={<RefreshIcon size={14} />}
                onClick={detectSystemJava}
              >
                Sprawdź ponownie
              </Button>
            </div>

            <Toggle
              label="Automatycznie wykrywaj i dobieraj odpowiednią wersję Javy dla profilu"
              checked={settings.autoDetectJava}
              onChange={(val) => updateSettings({ autoDetectJava: val })}
            />

            <div>
              <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Niestandardowa ścieżka do pliku wykonywalnego (javaw.exe)
              </label>
              <input
                type="text"
                disabled={settings.autoDetectJava}
                value={settings.customJavaPath}
                onChange={(e) => updateSettings({ customJavaPath: e.target.value })}
                style={{
                  width: '100%',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-light)',
                  borderRadius: '6px',
                  padding: '8px 12px',
                  color: settings.autoDetectJava ? 'var(--text-dim)' : '#ffffff',
                  fontSize: '12px',
                  fontFamily: 'monospace',
                }}
              />
            </div>
          </div>
        )}

        {/* Category: Downloads */}
        {activeCategory === 'downloads' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>Pobieranie i Pamięć Podręczna</h3>

            <Slider
              label="Maksymalna liczba równoczesnych pobierań assetów"
              min={1}
              max={16}
              step={1}
              value={settings.concurrentDownloads}
              onChange={(val) => updateSettings({ concurrentDownloads: val })}
            />

            <Toggle
              label="Zapisuj pliki instalatora w lokalnej pamięci podręcznej (Szybsze kolejne instalacje)"
              checked={settings.diskCache}
              onChange={(val) => updateSettings({ diskCache: val })}
            />
          </div>
        )}

        {/* Category: Appearance */}
        {activeCategory === 'appearance' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>Wygląd i Efekty Świąteczne</h3>

            <Dropdown
              label="Motyw kolorystyczny"
              options={[
                { value: 'festive-dark', label: 'Christmas Pine & Navy (Domyślny świąteczny)' },
                { value: 'midnight-pine', label: 'Ciemna Leśna Zieleń (Midnight Pine)' },
                { value: 'pure-dark', label: 'Czysty Mroczny (Pure Dark)' },
              ]}
              value={settings.theme}
              onChange={(val) => updateSettings({ theme: val as any })}
            />

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', borderTop: '1px solid var(--border-subtle)', paddingTop: '16px' }}>
              <Toggle
                label="Włącz padający śnieg w tle launchera"
                checked={settings.snowfall}
                onChange={(val) => updateSettings({ snowfall: val })}
              />

              <Dropdown
                label="Intensywność śniegu"
                options={[
                  { value: 'light', label: 'Subtelny i lekki (28 płatków)' },
                  { value: 'medium', label: 'Standardowy (50 płatków)' },
                  { value: 'heavy', label: 'Świąteczna zamieć (85 płatków)' },
                ]}
                value={settings.snowfallIntensity}
                onChange={(val) => updateSettings({ snowfallIntensity: val as any })}
              />

              <Toggle
                label="Ograniczenie animacji (Reduced Motion dla słabszych GPU)"
                checked={settings.reducedMotion}
                onChange={(val) => updateSettings({ reducedMotion: val })}
              />
            </div>
          </div>
        )}

        {/* Category: About */}
        {activeCategory === 'about' && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', alignItems: 'center', textAlign: 'center', padding: '16px 0' }}>
            <MascotZambek size="lg" withGlow />

            <div>
              <h2 className="pixel-text" style={{ fontSize: '22px', fontWeight: 900, color: '#ffffff' }}>
                ZOMBEK<span style={{ color: 'var(--pine-400)' }}>CLIENT</span>
              </h2>
              <div style={{ display: 'flex', gap: '6px', justifyContent: 'center', marginTop: '6px' }}>
                <Badge variant="festive">VERSION 0.1.0-ALPHA</Badge>
                <Badge variant="fabric">ETAP 2 • FRONTEND</Badge>
              </div>
            </div>

            <p style={{ fontSize: '13px', color: 'var(--text-secondary)', maxWidth: '480px', lineHeight: 1.5 }}>
              Nowoczesny launcher i klient Minecraft Java Edition łączący estetykę Lunar Client z autorskim,
              świątecznym motywem Minecrafta i maskotką Ząbek.
            </p>

            <div
              style={{
                width: '100%',
                background: 'var(--bg-surface)',
                borderRadius: '8px',
                border: '1px solid var(--border-subtle)',
                padding: '14px',
                fontSize: '12px',
                color: 'var(--text-muted)',
                textAlign: 'left',
                display: 'flex',
                flexDirection: 'column',
                gap: '6px',
              }}
            >
              <div><strong>Architektura:</strong> Electron 34 + React 19 + TypeScript + Vite + Fabric Loom</div>
              <div><strong>Status:</strong> Etap 2 ukończony (Pełny interfejs wizualny i interakcje)</div>
              <div><strong>Licencja:</strong> Własnościowa (ZombekClient Team 2026)</div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
