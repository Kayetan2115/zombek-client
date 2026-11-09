import React, { useState, useMemo } from 'react';
import { useLauncherContext } from '../stores';
import { Button, Badge, Modal, SearchBar, Dropdown } from '../components/common';
import {
  PlusIcon,
  DownloadIcon,
  CopyIcon,
  TrashIcon,
  ModpacksIcon,
  ExternalLinkIcon,
  CheckIcon,
  RefreshIcon,
} from '../components/icons';
import { MrpackImportModal, MrpackExportModal } from '../components/modpacks';
import type { MinecraftLoaderType, ModpackRecord, ModCompatibilityStatus } from '@zombek/shared-types';

export const ModpacksPage: React.FC = () => {
  const {
    installations,
    activeInstallation,
    realModpacks,
    isLoadingModpacks,
    loadModpacks,
    createRealModpack,
    duplicateRealModpack,
    deleteRealModpack,
    applyModpackToInstance,
    addToast,
  } = useLauncherContext();

  const [search, setSearch] = useState('');
  const [loaderFilter, setLoaderFilter] = useState<string>('all');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [inspectingPack, setInspectingPack] = useState<ModpackRecord | null>(null);
  const [packToDelete, setPackToDelete] = useState<ModpackRecord | null>(null);
  const [packToApply, setPackToApply] = useState<ModpackRecord | null>(null);
  const [targetInstanceId, setTargetInstanceId] = useState<string>(activeInstallation.id);

  // MRPACK Import & Export (Etap 6D)
  const [showImportModal, setShowImportModal] = useState(false);
  const [droppedMrpackPath, setDroppedMrpackPath] = useState<string | null>(null);
  const [showExportModal, setShowExportModal] = useState(false);
  const [exportInstanceId, setExportInstanceId] = useState<string | undefined>(undefined);

  // Form state for creating
  const [packName, setPackName] = useState('');
  const [mcVersion, setMcVersion] = useState('1.20.4');
  const [loader, setLoader] = useState<MinecraftLoaderType>('fabric');
  const [description, setDescription] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Filtered Modpacks
  const filteredPacks = useMemo(() => {
    return realModpacks.filter((p) => {
      const matchesSearch =
        p.name.toLowerCase().includes(search.toLowerCase()) ||
        p.description.toLowerCase().includes(search.toLowerCase()) ||
        p.minecraftVersion.includes(search);
      const matchesLoader = loaderFilter === 'all' || p.loader === loaderFilter;
      return matchesSearch && matchesLoader;
    });
  }, [realModpacks, search, loaderFilter]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!packName.trim()) {
      addToast({ type: 'warning', title: 'Podaj nazwę', message: 'Wpisz nazwę nowego profilu modpacka.' });
      return;
    }

    setIsSubmitting(true);
    try {
      await createRealModpack({
        name: packName.trim(),
        description: description.trim() || 'Własny profil modpacka ZombekClient.',
        minecraftVersion: mcVersion,
        loader,
      });

      setPackName('');
      setDescription('');
      setShowCreateModal(false);
    } catch (err: any) {
      addToast({ type: 'error', title: 'Błąd tworzenia paczki', message: err.message });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleApply = async () => {
    if (!packToApply) return;
    setIsSubmitting(true);
    try {
      const ok = await applyModpackToInstance(packToApply.id, targetInstanceId);
      if (ok) {
        setPackToApply(null);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const getStatusBadge = (status: ModCompatibilityStatus) => {
    switch (status) {
      case 'Compatible':
        return <Badge variant="success">Kompatybilny</Badge>;
      case 'Incompatible':
        return <Badge variant="danger">Niekompatybilny</Badge>;
      default:
        return <Badge variant="neutral">{status}</Badge>;
    }
  };

  return (
    <div
      style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}
      onDragOver={(e) => e.preventDefault()}
      onDrop={(e) => {
        e.preventDefault();
        if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
          const file = e.dataTransfer.files[0];
          if (file.name.endsWith('.mrpack')) {
            const path = (file as any).path || file.name;
            setDroppedMrpackPath(path);
            setShowImportModal(true);
          }
        }
      }}
    >
      {/* Top Banner & Header */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '12px',
          padding: '18px 22px',
          display: 'flex',
          flexDirection: 'column',
          gap: '14px',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <h2 style={{ fontSize: '18px', fontWeight: 800, color: '#ffffff' }}>
                Profile Modpacków
              </h2>
              <Badge variant="festive">MODPACK CORE</Badge>
            </div>
            <p style={{ fontSize: '12.5px', color: 'var(--text-muted)', marginTop: '4px' }}>
              Twórz własne zestawy modów i konfiguracji, duplikuj je i przypisuj do dowolnej instalacji.
              Profile są w pełni odizolowane i zapisywane lokalnie w <code>modpacks.json</code>.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <Button
              variant="secondary"
              size="sm"
              icon={<RefreshIcon size={14} />}
              onClick={() => loadModpacks()}
              title="Odśwież listę paczek z dysku"
            >
              Odśwież
            </Button>

            <Button
              variant="secondary"
              size="sm"
              icon={<DownloadIcon size={14} />}
              onClick={() => {
                setDroppedMrpackPath(null);
                setShowImportModal(true);
              }}
              title="Importuj paczkę .mrpack z dysku lub upuść plik"
            >
              Importuj (.mrpack)
            </Button>

            <Button
              variant="secondary"
              size="sm"
              icon={<ExternalLinkIcon size={14} />}
              onClick={() => {
                setExportInstanceId(activeInstallation.id);
                setShowExportModal(true);
              }}
              title="Eksportuj wybraną instalację gry do formatu .mrpack"
            >
              Eksportuj (.mrpack)
            </Button>

            <Button
              variant="primary"
              size="sm"
              icon={<PlusIcon size={16} />}
              onClick={() => setShowCreateModal(true)}
            >
              Utwórz Modpack
            </Button>
          </div>
        </div>

        {/* Filters and Search Bar */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '12px',
            borderTop: '1px solid var(--border-subtle)',
            paddingTop: '12px',
          }}
        >
          <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
            {['all', 'fabric', 'forge', 'neoforge', 'quilt', 'vanilla'].map((l) => (
              <button
                key={l}
                onClick={() => setLoaderFilter(l)}
                style={{
                  padding: '6px 12px',
                  borderRadius: '6px',
                  fontSize: '12px',
                  fontWeight: 600,
                  cursor: 'pointer',
                  textTransform: 'capitalize',
                  background: loaderFilter === l ? 'var(--bg-surface-active)' : 'transparent',
                  border: `1px solid ${loaderFilter === l ? 'var(--frost-500)' : 'var(--border-subtle)'}`,
                  color: loaderFilter === l ? 'var(--frost-400)' : 'var(--text-secondary)',
                }}
              >
                {l === 'all' ? `Wszystkie (${realModpacks.length})` : l}
              </button>
            ))}
          </div>

          <SearchBar value={search} onChange={setSearch} placeholder="Szukaj paczek po nazwie, wersji..." />
        </div>
      </div>

      {/* Loading state */}
      {isLoadingModpacks && (
        <div
          style={{
            padding: '36px',
            textAlign: 'center',
            color: 'var(--text-muted)',
            fontSize: '13px',
            background: 'var(--bg-card)',
            borderRadius: '12px',
            border: '1px solid var(--border-light)',
          }}
        >
          Wczytywanie profili modpacków...
        </div>
      )}

      {/* Empty State */}
      {!isLoadingModpacks && filteredPacks.length === 0 && (
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px dashed var(--border-light)',
            borderRadius: '12px',
            padding: '48px 24px',
            textAlign: 'center',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '12px',
          }}
        >
          <div
            style={{
              width: '48px',
              height: '48px',
              borderRadius: '50%',
              background: 'var(--bg-surface)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--frost-400)',
            }}
          >
            <ModpacksIcon size={24} />
          </div>
          <h3 style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>
            Nie znaleziono modpacków
          </h3>
          <p style={{ fontSize: '13px', color: 'var(--text-muted)', maxWidth: '420px' }}>
            {realModpacks.length === 0
              ? 'Nie utworzono jeszcze żadnego profilu modpacka. Kliknij przycisk poniżej, aby stworzyć swój pierwszy zestaw.'
              : 'Zmień kryteria filtrowania lub wyczyść wyszukiwarkę.'}
          </p>
          {realModpacks.length === 0 && (
            <Button
              variant="primary"
              icon={<PlusIcon size={16} />}
              onClick={() => setShowCreateModal(true)}
            >
              Utwórz Modpack
            </Button>
          )}
        </div>
      )}

      {/* Modpacks Grid */}
      {!isLoadingModpacks && filteredPacks.length > 0 && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '18px' }}>
          {filteredPacks.map((pack) => (
            <div
              key={pack.id}
              style={{
                background: 'var(--bg-card)',
                border: '1px solid var(--border-subtle)',
                borderRadius: '12px',
                padding: '20px',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between',
                gap: '16px',
                transition: 'all 0.18s ease',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = 'var(--frost-500)';
                e.currentTarget.style.transform = 'translateY(-2px)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = 'var(--border-subtle)';
                e.currentTarget.style.transform = 'translateY(0)';
              }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '10px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div
                      style={{
                        width: '38px',
                        height: '38px',
                        borderRadius: '8px',
                        background: 'var(--bg-surface)',
                        border: '1px solid var(--border-light)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'var(--frost-400)',
                      }}
                    >
                      <ModpacksIcon size={20} />
                    </div>
                    <div>
                      <h3 style={{ fontSize: '15.5px', fontWeight: 700, color: '#ffffff' }}>{pack.name}</h3>
                      <div style={{ display: 'flex', gap: '6px', marginTop: '3px' }}>
                        <Badge variant={pack.loader as any}>{pack.loader.toUpperCase()}</Badge>
                        <Badge variant="neutral">{pack.minecraftVersion}</Badge>
                        {pack.isDefault && <Badge variant="gold">Domyślna</Badge>}
                      </div>
                    </div>
                  </div>

                  {getStatusBadge(pack.compatibilityStatus)}
                </div>

                <p style={{ fontSize: '12.5px', color: 'var(--text-secondary)', marginTop: '12px', lineHeight: 1.45 }}>
                  {pack.description}
                </p>

                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    marginTop: '12px',
                    fontSize: '12px',
                    color: 'var(--text-muted)',
                  }}
                >
                  <span>Pliki modów: <strong style={{ color: '#ffffff' }}>{pack.modsCount} modów</strong></span>
                  <span>Mod: {pack.lastModified.substring(0, 10)}</span>
                </div>
              </div>

              {/* Bottom Actions */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  paddingTop: '14px',
                  borderTop: '1px solid var(--border-subtle)',
                }}
              >
                <div style={{ display: 'flex', gap: '6px' }}>
                  <Button
                    variant="primary"
                    size="sm"
                    onClick={() => {
                      setPackToApply(pack);
                      setTargetInstanceId(activeInstallation.id);
                    }}
                  >
                    Zastosuj w grze
                  </Button>

                  <Button
                    variant="secondary"
                    size="sm"
                    onClick={() => setInspectingPack(pack)}
                  >
                    Szczegóły
                  </Button>
                </div>

                <div style={{ display: 'flex', gap: '6px' }}>
                  <Button
                    variant="ghost"
                    size="sm"
                    icon={<CopyIcon size={14} />}
                    onClick={() => duplicateRealModpack(pack.id)}
                    title="Utwórz niezależną kopię (deep copy modów i configu)"
                  >
                    Kopia
                  </Button>

                  <Button
                    variant="ghost"
                    size="sm"
                    icon={<TrashIcon size={14} />}
                    onClick={() => setPackToDelete(pack)}
                    title="Usuń profil paczki"
                  >
                    Usuń
                  </Button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* ========================================================================= */}
      {/* MODAL: CREATE MODPACK                                                     */}
      {/* ========================================================================= */}
      {showCreateModal && (
        <Modal
          isOpen={showCreateModal}
          onClose={() => setShowCreateModal(false)}
          title="Utwórz Nowy Profil Modpacka"
          subtitle="Utworzy niezależny profil z własnym folderem modów i konfiguracji."
          footer={
            <>
              <Button variant="ghost" onClick={() => setShowCreateModal(false)}>
                Anuluj
              </Button>
              <Button variant="primary" disabled={isSubmitting} onClick={handleCreate}>
                {isSubmitting ? 'Tworzenie...' : 'Utwórz Modpack'}
              </Button>
            </>
          }
        >
          <form onSubmit={handleCreate} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div>
              <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Nazwa paczki
              </label>
              <input
                type="text"
                value={packName}
                onChange={(e) => setPackName(e.target.value)}
                placeholder="np. Świąteczny Fabric PvP"
                style={{
                  width: '100%',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-light)',
                  borderRadius: '6px',
                  padding: '8px 12px',
                  color: '#ffffff',
                  fontSize: '13px',
                  outline: 'none',
                }}
              />
            </div>

            <div>
              <label style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Opis paczki
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="Krótki opis przeznaczenia paczki..."
                rows={3}
                style={{
                  width: '100%',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-light)',
                  borderRadius: '6px',
                  padding: '8px 12px',
                  color: '#ffffff',
                  fontSize: '13px',
                  resize: 'none',
                  outline: 'none',
                }}
              />
            </div>

            <Dropdown
              label="Wersja docelowa Minecraft"
              options={[
                { value: '1.20.4', label: '1.20.4 (Zalecana)' },
                { value: '1.21.1', label: '1.21.1 (Najnowsza)' },
                { value: '1.20.2', label: '1.20.2' },
                { value: '1.19.4', label: '1.19.4' },
                { value: '1.8.9', label: '1.8.9 (Klasyk PvP)' },
              ]}
              value={mcVersion}
              onChange={(val) => setMcVersion(val as string)}
            />

            <Dropdown
              label="Modloader"
              options={[
                { value: 'fabric', label: 'Fabric Loader' },
                { value: 'forge', label: 'Minecraft Forge' },
                { value: 'neoforge', label: 'NeoForge' },
                { value: 'quilt', label: 'Quilt Loader' },
                { value: 'vanilla', label: 'Vanilla (Bez loadera)' },
              ]}
              value={loader}
              onChange={(val) => setLoader(val as MinecraftLoaderType)}
            />
          </form>
        </Modal>
      )}

      {/* ========================================================================= */}
      {/* MODAL: APPLY MODPACK TO INSTANCE                                          */}
      {/* ========================================================================= */}
      {packToApply && (
        <Modal
          isOpen={!!packToApply}
          onClose={() => setPackToApply(null)}
          title={`Zastosuj paczkę "${packToApply.name}" w instalacji`}
          subtitle="Skopiuje pliki modów i konfigurację paczki do wybranego środowiska gry."
          maxWidth="500px"
          footer={
            <>
              <Button variant="ghost" onClick={() => setPackToApply(null)}>
                Anuluj
              </Button>
              <Button variant="primary" disabled={isSubmitting} onClick={handleApply}>
                {isSubmitting ? 'Kopiowanie...' : 'Zastosuj w instalacji'}
              </Button>
            </>
          }
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '13px', color: 'var(--text-secondary)' }}>
            <p>
              Wybierz instalację gry, do której chcesz wdrożyć pliki z modpacka <strong>{packToApply.name}</strong>:
            </p>

            <div>
              <label style={{ fontSize: '12px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Docelowa instalacja Minecraft:
              </label>
              <select
                value={targetInstanceId}
                onChange={(e) => setTargetInstanceId(e.target.value)}
                style={{
                  width: '100%',
                  background: 'var(--bg-surface)',
                  border: '1px solid var(--border-light)',
                  color: '#ffffff',
                  borderRadius: '6px',
                  padding: '8px 12px',
                  fontSize: '13px',
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

            <div
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                borderRadius: '8px',
                padding: '12px',
                fontSize: '12px',
              }}
            >
              <div><strong>Liczba modów do wdrożenia:</strong> {packToApply.modsCount} plików</div>
              <div><strong>Wymagany loader paczki:</strong> {packToApply.loader.toUpperCase()}</div>
              <div><strong>Wersja MC paczki:</strong> {packToApply.minecraftVersion}</div>
            </div>

            <p style={{ fontSize: '11.5px', color: 'var(--text-muted)' }}>
              Operacja utworzy plik <code>modpack.json</code> w katalogu instalacji i skopiuje zawartość katalogu <code>mods/</code>.
            </p>
          </div>
        </Modal>
      )}

      {/* ========================================================================= */}
      {/* MODAL: INSPECT MODPACK                                                    */}
      {/* ========================================================================= */}
      {inspectingPack && (
        <Modal
          isOpen={!!inspectingPack}
          onClose={() => setInspectingPack(null)}
          title={`Szczegóły Modpacka: ${inspectingPack.name}`}
          subtitle={`ID: ${inspectingPack.id}`}
          maxWidth="560px"
          footer={
            <Button variant="primary" onClick={() => setInspectingPack(null)}>
              Zamknij
            </Button>
          }
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', fontSize: '12.5px' }}>
            <p style={{ color: 'var(--text-secondary)' }}>
              {inspectingPack.description}
            </p>

            <div
              style={{
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-light)',
                borderRadius: '8px',
                padding: '14px',
                display: 'grid',
                gridTemplateColumns: '1fr 1fr',
                gap: '8px',
              }}
            >
              <div><strong style={{ color: 'var(--text-muted)' }}>Loader:</strong> {inspectingPack.loader.toUpperCase()}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Wersja MC:</strong> {inspectingPack.minecraftVersion}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Liczba modów:</strong> {inspectingPack.modsCount}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Status:</strong> {inspectingPack.compatibilityStatus}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Utworzono:</strong> {inspectingPack.created.substring(0, 10)}</div>
              <div><strong style={{ color: 'var(--text-muted)' }}>Ostatnia modyfikacja:</strong> {inspectingPack.lastModified.substring(0, 10)}</div>
            </div>

            {inspectingPack.modFiles.length > 0 ? (
              <div>
                <h4 style={{ fontSize: '13px', fontWeight: 700, color: '#ffffff', marginBottom: '8px' }}>
                  Pliki modów w paczce ({inspectingPack.modFiles.length})
                </h4>
                <div
                  style={{
                    maxHeight: '180px',
                    overflowY: 'auto',
                    background: 'var(--bg-surface)',
                    borderRadius: '6px',
                    padding: '8px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '4px',
                  }}
                >
                  {inspectingPack.modFiles.map((f, i) => (
                    <div key={i} style={{ fontSize: '11.5px', color: '#ffffff' }}>
                      • <code>{f}</code>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div style={{ color: 'var(--text-muted)', fontSize: '12px' }}>
                Brak plików modów przypisanych do tego profilu (pusty szablon).
              </div>
            )}
          </div>
        </Modal>
      )}

      {/* ========================================================================= */}
      {/* MODAL: DELETE MODPACK CONFIRMATION                                        */}
      {/* ========================================================================= */}
      {packToDelete && (
        <Modal
          isOpen={!!packToDelete}
          onClose={() => setPackToDelete(null)}
          title="Usunąć profil modpacka?"
          subtitle="Ta operacja usunie definicję paczki z pamięci klienta."
          maxWidth="460px"
          footer={
            <>
              <Button variant="ghost" onClick={() => setPackToDelete(null)}>
                Anuluj
              </Button>
              <Button
                variant="danger"
                onClick={async () => {
                  const id = packToDelete.id;
                  setPackToDelete(null);
                  await deleteRealModpack(id);
                }}
              >
                Usuń Modpack
              </Button>
            </>
          }
        >
          <div style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
            <p>
              Czy na pewno chcesz usunąć profil <strong>{packToDelete.name}</strong>?
            </p>
            <p style={{ marginTop: '8px', fontSize: '12px', color: 'var(--text-muted)' }}>
              Usunięcie profilu nie wpływa na zapisy światów gracza ani aktywne pliki w uruchomionych instancjach.
            </p>
          </div>
        </Modal>
      )}

      {/* MRPACK Modals (Etap 6D) */}
      <MrpackImportModal
        isOpen={showImportModal}
        onClose={() => {
          setShowImportModal(false);
          setDroppedMrpackPath(null);
        }}
        initialFilePath={droppedMrpackPath}
      />

      <MrpackExportModal
        isOpen={showExportModal}
        onClose={() => setShowExportModal(false)}
        defaultInstanceId={exportInstanceId}
      />
    </div>
  );
};
