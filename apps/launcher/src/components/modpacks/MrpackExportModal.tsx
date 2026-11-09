import React, { useState, useMemo } from 'react';
import { useLauncherContext } from '../../stores';
import { Modal, Button, Badge } from '../common';
import {
  DownloadIcon,
  CheckIcon,
  RefreshIcon,
  ExternalLinkIcon,
  ModpacksIcon,
} from '../icons';

interface MrpackExportModalProps {
  isOpen: boolean;
  onClose: () => void;
  defaultInstanceId?: string;
}

export const MrpackExportModal: React.FC<MrpackExportModalProps> = ({
  isOpen,
  onClose,
  defaultInstanceId,
}) => {
  const {
    installations,
    activeInstallation,
    mrpackExport,
    mrpackOpenSaveDialog,
    addToast,
  } = useLauncherContext();

  const selectedInstId = defaultInstanceId || activeInstallation.id;
  const [instanceId, setInstanceId] = useState<string>(selectedInstId);

  const currentInstance = useMemo(() => {
    return installations.find((i) => i.id === instanceId) || activeInstallation;
  }, [installations, instanceId, activeInstallation]);

  const [name, setName] = useState<string>(currentInstance.name || 'Mój Modpack');
  const [versionId, setVersionId] = useState<string>('1.0.0');
  const [summary, setSummary] = useState<string>('Modpack wyeksportowany z launchera ZombekClient');

  // Overrides options
  const [includeConfigs, setIncludeConfigs] = useState<boolean>(true);
  const [includeOptions, setIncludeOptions] = useState<boolean>(true);
  const [includeResourcepacks, setIncludeResourcepacks] = useState<boolean>(false);
  const [includeShaderpacks, setIncludeShaderpacks] = useState<boolean>(false);

  // Custom JARs & rights
  const [exportCustomJars, setExportCustomJars] = useState<boolean>(true);
  const [userRightsConfirmed, setUserRightsConfirmed] = useState<boolean>(false);

  const [isExporting, setIsExporting] = useState<boolean>(false);

  const handleExport = async () => {
    if (!name.trim()) {
      addToast({ type: 'warning', title: 'Nazwa modpacka', message: 'Wpisz nazwę paczki.' });
      return;
    }
    if (!versionId.trim()) {
      addToast({ type: 'warning', title: 'Wersja paczki', message: 'Wpisz identyfikator wersji (np. 1.0.0).' });
      return;
    }

    if (exportCustomJars && !userRightsConfirmed) {
      addToast({
        type: 'warning',
        title: 'Prawa autorskie',
        message: 'Musisz potwierdzić posiadanie praw do redystrybucji modów niestandardowych lub wyłączyć ich eksport.',
      });
      return;
    }

    try {
      const sanitizedName = name.replace(/[^a-zA-Z0-9_-]/g, '_');
      const defaultFileName = `${sanitizedName}-${versionId}.mrpack`;
      const targetPath = await mrpackOpenSaveDialog(defaultFileName);

      if (!targetPath) {
        return; // user cancelled save dialog
      }

      setIsExporting(true);

      const res = await mrpackExport({
        instanceId,
        outputFilePath: targetPath,
        name: name.trim(),
        versionId: versionId.trim(),
        summary: summary.trim(),
        includeCustomJars: exportCustomJars,
        selectedOverrides: {
          config: includeConfigs,
          optionsTxt: includeOptions,
          resourcepacks: includeResourcepacks,
          shaderpacks: includeShaderpacks,
        },
      });

      if (res.success) {
        onClose();
      }
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd eksportu paczki', message: err.message });
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Eksport Modpacka (.mrpack)"
      subtitle="Zbuduj oficjalną paczkę Modrinth Modpack gotową do udostępnienia graczom."
      footer={
        <>
          <Button variant="ghost" disabled={isExporting} onClick={onClose}>
            Anuluj
          </Button>

          <Button
            variant="primary"
            icon={<DownloadIcon size={16} />}
            disabled={isExporting || (exportCustomJars && !userRightsConfirmed)}
            onClick={handleExport}
          >
            {isExporting ? 'Eksportowanie...' : 'Wybierz miejsce i Zapisz'}
          </Button>
        </>
      }
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {/* Source instance selector */}
        <div>
          <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
            Źródłowa instalacja gry
          </label>
          <select
            value={instanceId}
            onChange={(e) => {
              setInstanceId(e.target.value);
              const found = installations.find((i) => i.id === e.target.value);
              if (found) {
                setName(found.name);
              }
            }}
            style={{
              width: '100%',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-light)',
              borderRadius: '8px',
              padding: '10px 14px',
              fontSize: '13px',
              color: '#ffffff',
              outline: 'none',
            }}
          >
            {installations.map((inst) => (
              <option key={inst.id} value={inst.id}>
                {inst.name} ({inst.loader.toUpperCase()} {inst.minecraftVersion})
              </option>
            ))}
          </select>
        </div>

        {/* Basic metadata */}
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '12px' }}>
          <div>
            <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
              Nazwa paczki
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="np. Mój Mega Pack"
              style={{
                width: '100%',
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                borderRadius: '8px',
                padding: '10px 14px',
                fontSize: '13px',
                color: '#ffffff',
                outline: 'none',
              }}
            />
          </div>

          <div>
            <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
              Wersja (version_id)
            </label>
            <input
              type="text"
              value={versionId}
              onChange={(e) => setVersionId(e.target.value)}
              placeholder="1.0.0"
              style={{
                width: '100%',
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                borderRadius: '8px',
                padding: '10px 14px',
                fontSize: '13px',
                color: '#ffffff',
                outline: 'none',
              }}
            />
          </div>
        </div>

        <div>
          <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
            Opis / Podsumowanie (Summary)
          </label>
          <textarea
            value={summary}
            onChange={(e) => setSummary(e.target.value)}
            rows={2}
            placeholder="Krótki opis zawartości paczki..."
            style={{
              width: '100%',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-light)',
              borderRadius: '8px',
              padding: '10px 14px',
              fontSize: '13px',
              color: '#ffffff',
              outline: 'none',
              resize: 'vertical',
            }}
          />
        </div>

        {/* Overrides Selection */}
        <div
          style={{
            border: '1px solid var(--border-light)',
            borderRadius: '8px',
            padding: '12px 14px',
            background: 'var(--bg-surface)',
          }}
        >
          <div style={{ fontSize: '13px', fontWeight: 600, color: '#ffffff', marginBottom: '8px' }}>
            Zawartość do wyeksportowania (Overrides)
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12.5px', color: 'var(--text-secondary)', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={includeConfigs}
                onChange={(e) => setIncludeConfigs(e.target.checked)}
              />
              <span>Pliki konfiguracyjne (config/)</span>
            </label>

            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12.5px', color: 'var(--text-secondary)', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={includeOptions}
                onChange={(e) => setIncludeOptions(e.target.checked)}
              />
              <span>Ustawienia gry (options.txt)</span>
            </label>

            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12.5px', color: 'var(--text-secondary)', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={includeResourcepacks}
                onChange={(e) => setIncludeResourcepacks(e.target.checked)}
              />
              <span>Paczki zasobów (resourcepacks/)</span>
            </label>

            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12.5px', color: 'var(--text-secondary)', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={includeShaderpacks}
                onChange={(e) => setIncludeShaderpacks(e.target.checked)}
              />
              <span>Shadery (shaderpacks/)</span>
            </label>
          </div>
        </div>

        {/* Custom JARs and Copyright confirmation */}
        <div
          style={{
            border: '1px solid var(--border-light)',
            borderRadius: '8px',
            padding: '12px 14px',
            background: 'var(--bg-surface)',
            display: 'flex',
            flexDirection: 'column',
            gap: '10px',
          }}
        >
          <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', fontWeight: 600, color: '#ffffff', cursor: 'pointer' }}>
            <input
              type="checkbox"
              checked={exportCustomJars}
              onChange={(e) => setExportCustomJars(e.target.checked)}
            />
            <span>Dołącz lokalne pliki JAR (niepochodzące z Modrinth) do overrides/mods/</span>
          </label>

          {exportCustomJars && (
            <div
              style={{
                background: 'rgba(56, 189, 248, 0.08)',
                border: '1px solid rgba(56, 189, 248, 0.25)',
                borderRadius: '6px',
                padding: '10px 12px',
              }}
            >
              <label style={{ display: 'flex', alignItems: 'flex-start', gap: '8px', fontSize: '12px', color: 'var(--frost-300)', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={userRightsConfirmed}
                  onChange={(e) => setUserRightsConfirmed(e.target.checked)}
                  style={{ marginTop: '2px' }}
                />
                <span>
                  Oświadczam, że posiadam prawa do redystrybucji zawartych modów niestandardowych lub zezwala na to ich licencja.
                </span>
              </label>
            </div>
          )}
        </div>

        {/* Security & Privacy Notice */}
        <div
          style={{
            background: 'rgba(34, 197, 94, 0.08)',
            border: '1px solid rgba(34, 197, 94, 0.25)',
            borderRadius: '8px',
            padding: '10px 14px',
            fontSize: '12px',
            color: '#86efac',
            lineHeight: 1.45,
          }}
        >
          <strong>Bezpieczeństwo prywatności:</strong> Zgodnie ze specyfikacją Modrinth oraz standardami ZombekClient, pliki prywatne (światy z folderu saves, zrzuty ekranu, logi, raporty błędów, tokeny sesji i konta) zostaną automatycznie pominięte podczas tworzenia archiwum.
        </div>
      </div>
    </Modal>
  );
};
