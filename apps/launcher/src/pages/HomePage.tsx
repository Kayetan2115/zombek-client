import React, { useState } from 'react';
import { useLauncherContext } from '../stores';
import { Button, Badge, Modal } from '../components/common';
import { MascotZambek, WinterLandscape } from '../assets';
import {
  PlayIcon,
  SparkleIcon,
  ModsIcon,
  CosmeticsIcon,
  VersionsIcon,
  ChevronIcon,
  ExternalLinkIcon,
} from '../components/icons';

interface NewsItem {
  id: string;
  title: string;
  date: string;
  category: string;
  summary: string;
  imageUrl: string;
}

const NEWS_DATA: NewsItem[] = [
  {
    id: 'news-1',
    title: 'ZombekClient — Świąteczna Kraina i Nowy Interfejs v0.1.0',
    date: '2 Listopada 2026',
    category: 'Aktualizacja',
    summary: 'Oficjalny start Etapu 2! Całkowicie nowy design system inspirowany Lunar Client, maskotka Ząbek oraz pełny katalog modów.',
    imageUrl: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&auto=format&fit=crop&q=80',
  },
  {
    id: 'news-2',
    title: 'Świąteczny Turniej Bedwars i Ząbek Cup 2026',
    date: '31 Października 2026',
    category: 'Wydarzenie',
    summary: 'Dołącz do zimowych zmagań! Do wygrania unikalne peleryny Ząbek Santa Cape oraz ranga Festive Supporter.',
    imageUrl: 'https://images.unsplash.com/photo-1511512578047-dfb367046420?w=600&auto=format&fit=crop&q=80',
  },
  {
    id: 'news-3',
    title: 'Przewodnik: Jak skonfigurować moduły HUD i Keystrokes',
    date: '28 Października 2026',
    category: 'Poradnik',
    summary: 'Dowiedz się jak precyzyjnie dostosować pozycję, kolory i przezroczystość nakładek ekranowych w naszym edytorze modułów.',
    imageUrl: 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80',
  },
];

export const HomePage: React.FC = () => {
  const {
    activeInstallation,
    installations,
    setActiveInstallationId,
    setCurrentTab,
    launchGame,
    cancelLaunch,
    isLaunching,
    isRunning,
    launchStatusText,
    addToast,
  } = useLauncherContext();

  const [selectedNews, setSelectedNews] = useState<NewsItem | null>(null);

  const handlePlayClick = () => {
    if (isLaunching || isRunning) {
      cancelLaunch();
    } else {
      launchGame(activeInstallation.id);
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
      {/* Hero Banner with Winter Landscape and Mascot */}
      <div
        style={{
          position: 'relative',
          borderRadius: '16px',
          overflow: 'hidden',
          minHeight: '260px',
          border: '1px solid var(--border-light)',
          boxShadow: '0 12px 36px rgba(0, 0, 0, 0.45)',
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'flex-end',
          padding: '32px',
        }}
      >
        <WinterLandscape />

        {/* Hero Content Overlay */}
        <div
          style={{
            position: 'relative',
            zIndex: 2,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '24px',
            flexWrap: 'wrap',
          }}
        >
          <div style={{ maxWidth: '580px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
              <Badge variant="festive">EDYCJA ŚWIĄTECZNA 2026</Badge>
              <Badge variant="preview">ETAP 2 • FRONTEND SHELL</Badge>
            </div>

            <h1
              className="pixel-text"
              style={{
                fontSize: '28px',
                fontWeight: 900,
                color: '#ffffff',
                textShadow: '0 2px 10px rgba(0,0,0,0.8)',
                lineHeight: 1.2,
              }}
            >
              WITAJ W <span style={{ color: 'var(--pine-400)' }}>ZOMBEK</span>CLIENT
            </h1>

            <p
              style={{
                fontSize: '13.5px',
                color: 'var(--text-primary)',
                marginTop: '8px',
                lineHeight: 1.5,
                textShadow: '0 1px 6px rgba(0,0,0,0.85)',
              }}
            >
              Zimowa kraina Minecrafta spotyka profesjonalną wydajność PvP. Odkryj wbudowane moduły HUD,
              unikalne kosmetyki z maskotką Ząbek i zoptymalizowaną rozgrywkę.
            </p>

            <div style={{ display: 'flex', gap: '10px', marginTop: '16px' }}>
              <Button
                variant="secondary"
                size="sm"
                icon={<ModsIcon size={14} />}
                onClick={() => setCurrentTab('mods')}
              >
                Przeglądaj Mody
              </Button>
              <Button
                variant="festive-gold"
                size="sm"
                icon={<CosmeticsIcon size={14} />}
                onClick={() => setCurrentTab('cosmetics')}
              >
                Garderoba Ząbek
              </Button>
            </div>
          </div>

          {/* Big Festive Mascot Showcase */}
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              filter: 'drop-shadow(0 10px 20px rgba(0,0,0,0.6))',
            }}
          >
            <MascotZambek size="lg" withGlow />
            <div
              style={{
                marginTop: '6px',
                fontSize: '11px',
                fontWeight: 700,
                color: 'var(--gold-400)',
                background: 'rgba(0,0,0,0.6)',
                padding: '2px 8px',
                borderRadius: '999px',
                border: '1px solid rgba(245, 158, 11, 0.4)',
              }}
            >
              Maskotka Ząbek
            </div>
          </div>
        </div>
      </div>

      {/* Main Action Bar: PLAY Section */}
      <div
        style={{
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '14px',
          padding: '20px 24px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '20px',
          flexWrap: 'wrap',
          boxShadow: '0 8px 24px rgba(0,0,0,0.3)',
        }}
      >
        {/* Selected Installation Info */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div
            style={{
              width: '50px',
              height: '50px',
              borderRadius: '10px',
              background: 'linear-gradient(135deg, #0f2e20 0%, #13402c 100%)',
              border: '1px solid #10b981',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#34d399',
            }}
          >
            <VersionsIcon size={26} />
          </div>

          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <h3 style={{ fontSize: '17px', fontWeight: 700, color: '#ffffff' }}>
                {activeInstallation.name}
              </h3>
              <Badge variant={activeInstallation.loader as any}>
                {activeInstallation.loader} {activeInstallation.loaderVersion}
              </Badge>
            </div>
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                fontSize: '12.5px',
                color: 'var(--text-muted)',
                marginTop: '4px',
              }}
            >
              <span>Minecraft {activeInstallation.minecraftVersion}</span>
              <span>•</span>
              <span>Przydzielony RAM: {activeInstallation.allocatedRam} MB</span>
              <span>•</span>
              <span style={{ color: isRunning ? '#4ade80' : isLaunching ? '#facc15' : 'var(--pine-400)', fontWeight: 600 }}>
                Status: {isRunning ? 'Gra jest uruchomiona' : isLaunching ? (launchStatusText || 'Ładowanie plików...') : 'Gotowy do gry'}
              </span>
            </div>
          </div>
        </div>

        {/* Big PLAY Button */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <Button
            variant="play"
            size="xl"
            glow
            icon={<PlayIcon size={22} />}
            onClick={handlePlayClick}
            style={{ minWidth: '180px' }}
          >
            {isRunning ? 'GRA AKTYWNA (ZAMKNIJ)' : isLaunching ? 'URUCHAMIANIE...' : 'GRAJ TERAZ'}
          </Button>
        </div>
      </div>

      {/* Quick Profile Cards */}
      <div>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            marginBottom: '14px',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-snow)' }}>
              Ostatnio uruchamiane profile
            </h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>({installations.length})</span>
          </div>
          <button
            onClick={() => setCurrentTab('versions')}
            style={{
              fontSize: '12.5px',
              color: 'var(--frost-400)',
              display: 'flex',
              alignItems: 'center',
              gap: '4px',
              cursor: 'pointer',
            }}
          >
            Zarządzaj wszystkimi <ChevronIcon size={14} direction="right" />
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '14px' }}>
          {installations.map((inst) => {
            const isSelected = inst.id === activeInstallation.id;
            return (
              <div
                key={inst.id}
                onClick={() => {
                  setActiveInstallationId(inst.id);
                  addToast({
                    type: 'info',
                    title: 'Aktywny profil',
                    message: `Wybrano ${inst.name}`,
                  });
                }}
                style={{
                  background: isSelected ? 'var(--bg-surface-active)' : 'var(--bg-card)',
                  border: `1px solid ${isSelected ? 'var(--pine-500)' : 'var(--border-subtle)'}`,
                  borderRadius: '10px',
                  padding: '16px',
                  cursor: 'pointer',
                  transition: 'all 0.16s ease',
                  boxShadow: isSelected ? '0 4px 18px rgba(16, 185, 129, 0.15)' : 'none',
                }}
                onMouseEnter={(e) => {
                  if (!isSelected) {
                    e.currentTarget.style.borderColor = 'var(--border-light)';
                    e.currentTarget.style.transform = 'translateY(-2px)';
                  }
                }}
                onMouseLeave={(e) => {
                  if (!isSelected) {
                    e.currentTarget.style.borderColor = 'var(--border-subtle)';
                    e.currentTarget.style.transform = 'translateY(0)';
                  }
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <span style={{ fontSize: '14px', fontWeight: 700, color: isSelected ? 'var(--pine-400)' : '#ffffff' }}>
                    {inst.name}
                  </span>
                  <Badge variant={inst.loader as any} size="sm">
                    {inst.loader}
                  </Badge>
                </div>
                <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '8px' }}>
                  Wersja: Minecraft {inst.minecraftVersion}
                </div>
                <div
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    marginTop: '12px',
                    fontSize: '11px',
                    color: 'var(--text-dim)',
                  }}
                >
                  <span>Gry: {inst.lastPlayed}</span>
                  {isSelected && (
                    <span style={{ color: 'var(--pine-400)', fontWeight: 700 }}>● AKTYWNY</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* News & Community Section */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
          <h3 style={{ fontSize: '16px', fontWeight: 700, color: 'var(--text-snow)' }}>
            Aktualności i Komunikaty ZombekClient
          </h3>
          <span style={{ fontSize: '12px', color: 'var(--gold-400)', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <SparkleIcon size={12} color="var(--gold-400)" />
            Sezon Zimowy 2026
          </span>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(310px, 1fr))', gap: '16px' }}>
          {NEWS_DATA.map((item) => (
            <div
              key={item.id}
              onClick={() => setSelectedNews(item)}
              style={{
                background: 'var(--bg-card)',
                border: '1px solid var(--border-subtle)',
                borderRadius: '12px',
                overflow: 'hidden',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                display: 'flex',
                flexDirection: 'column',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.borderColor = 'var(--frost-500)';
                e.currentTarget.style.transform = 'translateY(-3px)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.borderColor = 'var(--border-subtle)';
                e.currentTarget.style.transform = 'translateY(0)';
              }}
            >
              <div style={{ height: '140px', overflow: 'hidden', position: 'relative' }}>
                <img
                  src={item.imageUrl}
                  alt={item.title}
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
                <div
                  style={{
                    position: 'absolute',
                    top: '10px',
                    left: '10px',
                    background: 'rgba(0,0,0,0.7)',
                    backdropFilter: 'blur(4px)',
                    padding: '2px 8px',
                    borderRadius: '4px',
                    fontSize: '11px',
                    fontWeight: 700,
                    color: 'var(--gold-400)',
                  }}
                >
                  {item.category}
                </div>
              </div>

              <div style={{ padding: '16px', flex: 1, display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{item.date}</span>
                <h4 style={{ fontSize: '14.5px', fontWeight: 700, color: '#ffffff', marginTop: '6px', lineHeight: 1.3 }}>
                  {item.title}
                </h4>
                <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '8px', lineHeight: 1.4, flex: 1 }}>
                  {item.summary}
                </p>
                <div
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    fontSize: '12px',
                    fontWeight: 600,
                    color: 'var(--frost-400)',
                    marginTop: '12px',
                  }}
                >
                  Czytaj więcej <ChevronIcon size={12} direction="right" />
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Modal: News Detail */}
      {selectedNews && (
        <Modal
          isOpen={!!selectedNews}
          onClose={() => setSelectedNews(null)}
          title={selectedNews.title}
          subtitle={`${selectedNews.category} • ${selectedNews.date}`}
          maxWidth="640px"
          footer={
            <Button variant="secondary" onClick={() => setSelectedNews(null)}>
              Zamknij
            </Button>
          }
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            <div style={{ width: '100%', height: '220px', borderRadius: '8px', overflow: 'hidden' }}>
              <img
                src={selectedNews.imageUrl}
                alt={selectedNews.title}
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
              />
            </div>
            <p style={{ fontSize: '13.5px', color: 'var(--text-primary)', lineHeight: 1.6 }}>
              {selectedNews.summary}
            </p>
            <p style={{ fontSize: '12.5px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
              ZombekClient w Etapie 2 prezentuje autorski motyw zimowy, w pełni funkcjonalne demonstracje przełączania
              modów w stylu Lunar Client, wybór instalacji oraz podgląd postaci z pelerynami.
            </p>
          </div>
        </Modal>
      )}
    </div>
  );
};
