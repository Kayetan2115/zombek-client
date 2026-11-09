import React, { useState } from 'react';
import { useLauncherContext, CosmeticItem } from '../stores';
import { Button, Badge, Modal } from '../components/common';
import { CosmeticsIcon, SparkleIcon, CheckIcon, GiftIcon } from '../components/icons';
import { MascotZambek } from '../assets';

export const CosmeticsPage: React.FC = () => {
  const { cosmetics, equippedCosmetics, equipCosmetic, addToast } = useLauncherContext();

  const [activeTab, setActiveTab] = useState<'cape' | 'skin' | 'accessory' | 'rank'>('cape');
  const [characterView, setCharacterView] = useState<'front' | 'back'>('back');
  const [customizingItem, setCustomizingItem] = useState<CosmeticItem | null>(null);

  const filteredCosmetics = cosmetics.filter((c) => c.type === activeTab);

  const activeCape = cosmetics.find((c) => c.id === equippedCosmetics.capeId);
  const activeSkin = cosmetics.find((c) => c.id === equippedCosmetics.skinId);
  const activeAccessory = cosmetics.find((c) => c.id === equippedCosmetics.accessoryId);
  const activeRank = cosmetics.find((c) => c.id === equippedCosmetics.rankId);

  return (
    <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start', flexWrap: 'wrap' }}>
      {/* Left: 3D/2D Character Showcase Canvas Preview */}
      <div
        style={{
          width: '340px',
          minWidth: '300px',
          background: 'var(--bg-card)',
          border: '1px solid var(--border-light)',
          borderRadius: '16px',
          padding: '24px',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          boxShadow: '0 8px 30px rgba(0,0,0,0.4)',
          position: 'sticky',
          top: '20px',
        }}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center', marginBottom: '14px' }}>
          <span style={{ fontSize: '12px', fontWeight: 700, color: 'var(--gold-400)', textTransform: 'uppercase' }}>
            Podgląd Garderoby 3D
          </span>
          <div style={{ display: 'flex', gap: '4px' }}>
            <button
              onClick={() => setCharacterView('front')}
              style={{
                padding: '3px 8px',
                borderRadius: '4px',
                fontSize: '11px',
                fontWeight: 600,
                cursor: 'pointer',
                background: characterView === 'front' ? 'var(--frost-500)' : 'var(--bg-surface)',
                color: characterView === 'front' ? '#ffffff' : 'var(--text-muted)',
              }}
            >
              Przód
            </button>
            <button
              onClick={() => setCharacterView('back')}
              style={{
                padding: '3px 8px',
                borderRadius: '4px',
                fontSize: '11px',
                fontWeight: 600,
                cursor: 'pointer',
                background: characterView === 'back' ? 'var(--frost-500)' : 'var(--bg-surface)',
                color: characterView === 'back' ? '#ffffff' : 'var(--text-muted)',
              }}
            >
              Tył (Peleryna)
            </button>
          </div>
        </div>

        {/* Character Visual Box */}
        <div
          style={{
            width: '100%',
            height: '280px',
            background: 'radial-gradient(circle, #162438 0%, #0a101d 100%)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '12px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            position: 'relative',
            overflow: 'hidden',
          }}
        >
          {/* Subtle pedestal glow */}
          <div
            style={{
              position: 'absolute',
              bottom: '15px',
              width: '160px',
              height: '24px',
              borderRadius: '50%',
              background: 'radial-gradient(ellipse, rgba(16, 185, 129, 0.4) 0%, rgba(0,0,0,0) 70%)',
            }}
          />

          {/* Minecraft Character representation */}
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', position: 'relative' }}>
            {characterView === 'back' ? (
              // Cape back view
              <div
                style={{
                  width: '90px',
                  height: '150px',
                  background: activeCape?.previewColor || '#dc2626',
                  borderRadius: '4px 4px 6px 6px',
                  boxShadow: '0 8px 24px rgba(0, 0, 0, 0.65)',
                  border: '2px solid rgba(255,255,255,0.2)',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  justifyContent: 'center',
                  position: 'relative',
                  transition: 'all 0.25s ease',
                }}
              >
                {/* Cape Design Texture */}
                <div style={{ width: '40px', height: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <MascotZambek size="sm" />
                </div>
                <div
                  style={{
                    position: 'absolute',
                    bottom: '10px',
                    fontSize: '10px',
                    fontWeight: 800,
                    color: '#ffffff',
                    letterSpacing: '1px',
                    textTransform: 'uppercase',
                  }}
                >
                  ZOMBEK
                </div>
              </div>
            ) : (
              // Front view with mascot head
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                <MascotZambek size="lg" withGlow />
                {/* Body block */}
                <div
                  style={{
                    width: '64px',
                    height: '75px',
                    background: '#047857',
                    border: '2px solid #065f46',
                    borderRadius: '2px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#ffffff',
                    fontSize: '11px',
                    fontWeight: 700,
                  }}
                >
                  SWETER
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Active Cosmetic Summary */}
        <div style={{ width: '100%', marginTop: '16px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
            <span style={{ color: 'var(--text-muted)' }}>Peleryna:</span>
            <span style={{ fontWeight: 600, color: 'var(--pine-400)' }}>{activeCape?.name}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
            <span style={{ color: 'var(--text-muted)' }}>Dodatek:</span>
            <span style={{ fontWeight: 600, color: 'var(--frost-400)' }}>{activeAccessory?.name}</span>
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
            <span style={{ color: 'var(--text-muted)' }}>Ranga:</span>
            <span style={{ fontWeight: 600, color: 'var(--gold-400)' }}>{activeRank?.name}</span>
          </div>
        </div>
      </div>

      {/* Right: Cosmetics Catalogue */}
      <div style={{ flex: 1, minWidth: '320px', display: 'flex', flexDirection: 'column', gap: '18px' }}>
        {/* Tabs Bar */}
        <div
          style={{
            background: 'var(--bg-card)',
            border: '1px solid var(--border-light)',
            borderRadius: '12px',
            padding: '8px',
            display: 'flex',
            gap: '8px',
          }}
        >
          {[
            { id: 'cape', label: 'Peleryny (Capes)' },
            { id: 'skin', label: 'Skiny Postaci' },
            { id: 'accessory', label: 'Dodatki i Skrzydła' },
            { id: 'rank', label: 'Rangi i Tytuły' },
          ].map((tab) => {
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                style={{
                  flex: 1,
                  padding: '9px 14px',
                  borderRadius: '8px',
                  fontSize: '13px',
                  fontWeight: 600,
                  cursor: 'pointer',
                  background: isActive ? 'var(--bg-surface-active)' : 'transparent',
                  border: `1px solid ${isActive ? 'var(--frost-500)' : 'transparent'}`,
                  color: isActive ? '#ffffff' : 'var(--text-secondary)',
                  transition: 'all 0.15s ease',
                  textAlign: 'center',
                }}
              >
                {tab.label}
              </button>
            );
          })}
        </div>

        {/* Cosmetics Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '16px' }}>
          {filteredCosmetics.map((item) => {
            const isEquipped =
              equippedCosmetics[`${item.type}Id` as keyof typeof equippedCosmetics] === item.id;

            return (
              <div
                key={item.id}
                style={{
                  background: 'var(--bg-card)',
                  border: `1px solid ${isEquipped ? 'var(--pine-500)' : 'var(--border-subtle)'}`,
                  borderRadius: '12px',
                  padding: '16px',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'space-between',
                  gap: '14px',
                  boxShadow: isEquipped ? '0 4px 18px rgba(16, 185, 129, 0.2)' : 'none',
                  transition: 'all 0.18s ease',
                }}
              >
                <div>
                  {/* Preview Swatch Banner */}
                  <div
                    style={{
                      height: '90px',
                      borderRadius: '8px',
                      background: item.previewColor,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      position: 'relative',
                      overflow: 'hidden',
                      boxShadow: 'inset 0 0 20px rgba(0,0,0,0.3)',
                    }}
                  >
                    <MascotZambek size="sm" />
                    <div
                      style={{
                        position: 'absolute',
                        top: '8px',
                        right: '8px',
                      }}
                    >
                      <Badge variant={item.rarity === 'Festive' ? 'festive' : item.rarity === 'Legendary' ? 'gold' : 'neutral'}>
                        {item.rarity}
                      </Badge>
                    </div>
                  </div>

                  <h4 style={{ fontSize: '14px', fontWeight: 700, color: '#ffffff', marginTop: '12px' }}>
                    {item.name}
                  </h4>
                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginTop: '4px', lineHeight: 1.4 }}>
                    {item.description}
                  </p>
                </div>

                {/* Bottom Card Action */}
                <div style={{ display: 'flex', gap: '8px' }}>
                  <Button
                    variant={isEquipped ? 'secondary' : 'primary'}
                    size="sm"
                    style={{ flex: 1 }}
                    icon={isEquipped ? <CheckIcon size={14} color="var(--pine-400)" /> : undefined}
                    onClick={() => equipCosmetic(item.type, item.id)}
                  >
                    {isEquipped ? 'Założone' : 'Załóż'}
                  </Button>

                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setCustomizingItem(item)}
                    title="Dostosuj wzór i barwę"
                  >
                    Opcje
                  </Button>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Modal: Customize Cosmetic */}
      {customizingItem && (
        <Modal
          isOpen={!!customizingItem}
          onClose={() => setCustomizingItem(null)}
          title={`Personalizacja: ${customizingItem.name}`}
          subtitle="Dostosuj odcień, efekty cząsteczkowe i widoczność dla innych graczy ZombekClient."
          footer={
            <Button
              variant="primary"
              onClick={() => {
                addToast({ type: 'success', title: 'Zapisano personalizację kosmetyku' });
                setCustomizingItem(null);
              }}
            >
              Zastosuj Zmiany
            </Button>
          }
        >
          <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <div
              style={{
                padding: '12px',
                borderRadius: '8px',
                background: 'rgba(245, 158, 11, 0.1)',
                border: '1px solid rgba(245, 158, 11, 0.3)',
                fontSize: '12.5px',
                color: 'var(--gold-400)',
              }}
            >
              Wszystkie peleryny i kosmetyki ZombekClient będą widoczne dla wszystkich innych graczy
              korzystających z ZombekClient po wdrożeniu mikroserwisu <strong>cosmetics-api</strong> w Etapie 8.
            </div>

            <div>
              <span style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                Podstawowy Odcień
              </span>
              <input
                type="color"
                defaultValue={customizingItem.previewColor}
                style={{ width: '100%', height: '40px', borderRadius: '6px', cursor: 'pointer', background: 'transparent' }}
              />
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};
