import React from 'react';

interface MascotProps {
  size?: 'sm' | 'md' | 'lg' | 'xl';
  className?: string;
  withGlow?: boolean;
}

export const MascotZambek: React.FC<MascotProps> = ({ size = 'md', className = '', withGlow = false }) => {
  const dim = size === 'sm' ? 36 : size === 'md' ? 64 : size === 'lg' ? 120 : 180;

  return (
    <div
      className={`mascot-zambek-container ${className}`}
      style={{
        width: dim,
        height: dim,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        filter: withGlow ? 'drop-shadow(0 0 16px rgba(16, 185, 129, 0.4))' : undefined,
        transition: 'transform 0.2s ease',
      }}
    >
      <svg
        width={dim}
        height={dim}
        viewBox="0 0 32 32"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        style={{ shapeRendering: 'crispEdges' }}
      >
        {/* Santa Hat Top / Pompom */}
        <rect x="22" y="1" width="4" height="4" fill="#FFFFFF" />
        <rect x="20" y="3" width="3" height="4" fill="#DC2626" />
        <rect x="15" y="4" width="6" height="3" fill="#DC2626" />
        <rect x="9" y="5" width="8" height="3" fill="#B91C1C" />
        <rect x="6" y="6" width="18" height="3" fill="#EF4444" />
        <rect x="5" y="7" width="20" height="2" fill="#DC2626" />

        {/* Santa Hat Fluffy Rim */}
        <rect x="4" y="9" width="22" height="3" fill="#FFFFFF" />
        <rect x="4" y="11" width="22" height="1" fill="#E2E8F0" />

        {/* Zombie Head Base (Green tones) */}
        <rect x="6" y="12" width="18" height="15" fill="#4D7C0F" />
        <rect x="8" y="12" width="14" height="13" fill="#65A30D" />
        <rect x="10" y="13" width="10" height="11" fill="#84CC16" />

        {/* Dark Zombie Texture patches */}
        <rect x="7" y="13" width="2" height="3" fill="#365314" />
        <rect x="18" y="13" width="3" height="2" fill="#365314" />
        <rect x="7" y="21" width="3" height="2" fill="#365314" />
        <rect x="19" y="22" width="2" height="2" fill="#365314" />

        {/* Big Cute Pixel Eyes */}
        {/* Left Eye */}
        <rect x="8" y="16" width="4" height="4" fill="#0F172A" />
        <rect x="8" y="16" width="2" height="2" fill="#38BDF8" />
        <rect x="9" y="17" width="1" height="1" fill="#FFFFFF" />

        {/* Right Eye */}
        <rect x="16" y="16" width="4" height="4" fill="#0F172A" />
        <rect x="16" y="16" width="2" height="2" fill="#38BDF8" />
        <rect x="17" y="17" width="1" height="1" fill="#FFFFFF" />

        {/* Zombie Nose Block */}
        <rect x="13" y="19" width="3" height="2" fill="#365314" />

        {/* Friendly Mouth & Signature Little Tooth ("Ząbek") */}
        <rect x="10" y="22" width="9" height="2" fill="#14532D" />
        {/* Signature Single Tooth sticking out */}
        <rect x="12" y="22" width="2" height="3" fill="#FFFFFF" />
        <rect x="12" y="24" width="2" height="1" fill="#F8FAFC" />

        {/* Cozy Winter Scarf */}
        <rect x="5" y="26" width="20" height="3" fill="#059669" />
        <rect x="8" y="26" width="4" height="3" fill="#DC2626" />
        <rect x="16" y="26" width="4" height="3" fill="#DC2626" />
        {/* Scarf tail hanging down */}
        <rect x="7" y="29" width="4" height="3" fill="#059669" />
        <rect x="7" y="30" width="4" height="1" fill="#DC2626" />
        <rect x="7" y="31" width="4" height="1" fill="#F59E0B" />
      </svg>
    </div>
  );
};
