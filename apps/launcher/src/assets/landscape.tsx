import React from 'react';

export const WinterLandscape: React.FC<{ className?: string }> = ({ className = '' }) => {
  return (
    <div
      className={`winter-landscape-container ${className}`}
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        overflow: 'hidden',
        pointerEvents: 'none',
        zIndex: 0,
      }}
    >
      <svg
        width="100%"
        height="100%"
        preserveAspectRatio="none"
        viewBox="0 0 1000 400"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        style={{ shapeRendering: 'crispEdges' }}
      >
        <defs>
          <linearGradient id="skyGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#0B132B" />
            <stop offset="45%" stopColor="#0E1E38" />
            <stop offset="85%" stopColor="#142C44" />
            <stop offset="100%" stopColor="#122538" />
          </linearGradient>

          <linearGradient id="aurora" x1="0" y1="0" x2="1" y2="0">
            <stop offset="10%" stopColor="rgba(16, 185, 129, 0.0)" />
            <stop offset="35%" stopColor="rgba(16, 185, 129, 0.18)" />
            <stop offset="55%" stopColor="rgba(6, 182, 212, 0.22)" />
            <stop offset="75%" stopColor="rgba(16, 185, 129, 0.15)" />
            <stop offset="90%" stopColor="rgba(16, 185, 129, 0.0)" />
          </linearGradient>

          <linearGradient id="mountainGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#334155" />
            <stop offset="35%" stopColor="#1E293B" />
            <stop offset="100%" stopColor="#0F172A" />
          </linearGradient>

          <linearGradient id="snowGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#F8FAFC" />
            <stop offset="100%" stopColor="#CBD5E1" />
          </linearGradient>
        </defs>

        {/* Sky Background */}
        <rect width="1000" height="400" fill="url(#skyGrad)" />

        {/* Aurora Borealis ribbon */}
        <path
          d="M0 60 Q 250 20 500 70 T 1000 40 L 1000 160 Q 750 190 500 140 T 0 170 Z"
          fill="url(#aurora)"
          style={{ filter: 'blur(14px)' }}
        />

        {/* Distant Pixel Stars */}
        <rect x="50" y="30" width="3" height="3" fill="#FFFFFF" opacity="0.9" />
        <rect x="180" y="55" width="2" height="2" fill="#FDE047" opacity="0.75" />
        <rect x="320" y="25" width="4" height="4" fill="#FFFFFF" opacity="0.85" />
        <rect x="460" y="65" width="2" height="2" fill="#BAE6FD" opacity="0.8" />
        <rect x="620" y="35" width="3" height="3" fill="#FFFFFF" opacity="0.95" />
        <rect x="740" y="80" width="2" height="2" fill="#FDE047" opacity="0.7" />
        <rect x="880" y="45" width="4" height="4" fill="#FFFFFF" opacity="0.9" />
        <rect x="940" y="70" width="2" height="2" fill="#BAE6FD" opacity="0.8" />
        <rect x="120" y="90" width="2" height="2" fill="#FFFFFF" opacity="0.6" />
        <rect x="820" y="20" width="3" height="3" fill="#FDE047" opacity="0.85" />

        {/* Distant Minecraft Mountains */}
        <polygon points="80,180 180,110 240,110 320,180" fill="url(#mountainGrad)" />
        <polygon points="180,110 240,110 220,135 195,135" fill="url(#snowGrad)" />

        <polygon points="260,200 410,95 490,95 620,200" fill="url(#mountainGrad)" />
        <polygon points="410,95 490,95 470,125 435,125" fill="url(#snowGrad)" />

        <polygon points="580,190 700,105 760,105 870,190" fill="url(#mountainGrad)" />
        <polygon points="700,105 760,105 745,130 715,130" fill="url(#snowGrad)" />

        {/* Midground Snowy Hills (Minecraft block terraces) */}
        <rect x="0" y="220" width="300" height="180" fill="#0C1B2A" />
        <rect x="0" y="215" width="290" height="10" fill="#E2E8F0" opacity="0.65" />

        <rect x="250" y="240" width="450" height="160" fill="#0E2135" />
        <rect x="250" y="235" width="440" height="10" fill="#E2E8F0" opacity="0.75" />

        <rect x="650" y="210" width="350" height="190" fill="#0A1826" />
        <rect x="660" y="205" width="340" height="10" fill="#E2E8F0" opacity="0.6" />

        {/* Pixel Pine Trees on the right */}
        {/* Tree 1 */}
        <rect x="820" y="160" width="8" height="60" fill="#451A03" />
        <polygon points="824,130 800,165 848,165" fill="#064E3B" />
        <polygon points="824,130 812,145 836,145" fill="#F8FAFC" />
        <polygon points="824,150 790,190 858,190" fill="#047857" />
        <polygon points="824,150 806,170 842,170" fill="#F8FAFC" />
        <polygon points="824,175 780,225 868,225" fill="#065F46" />
        <polygon points="824,175 800,198 848,198" fill="#F8FAFC" />

        {/* Tree 2 (Smaller) */}
        <rect x="910" y="180" width="6" height="40" fill="#451A03" />
        <polygon points="913,155 892,185 934,185" fill="#064E3B" />
        <polygon points="913,155 902,168 924,168" fill="#F8FAFC" />
        <polygon points="913,175 885,210 941,210" fill="#047857" />
        <polygon points="913,175 898,192 928,192" fill="#F8FAFC" />

        {/* Tree 3 (Left background) */}
        <rect x="90" y="185" width="6" height="45" fill="#451A03" />
        <polygon points="93,160 75,190 111,190" fill="#064E3B" />
        <polygon points="93,160 83,172 103,172" fill="#F8FAFC" />
        <polygon points="93,180 68,215 118,215" fill="#047857" />
        <polygon points="93,180 80,198 106,198" fill="#F8FAFC" />

        {/* Foreground Minecraft Snow layer */}
        <rect x="0" y="270" width="1000" height="130" fill="#0B1322" opacity="0.9" />
        <rect x="0" y="266" width="1000" height="6" fill="#F8FAFC" opacity="0.4" />

        {/* Pixel Christmas Gifts under the tree */}
        {/* Red Gift */}
        <rect x="850" y="240" width="22" height="22" fill="#DC2626" />
        <rect x="859" y="240" width="4" height="22" fill="#F59E0B" />
        <rect x="850" y="249" width="22" height="4" fill="#F59E0B" />
        {/* Ribbon bow */}
        <rect x="857" y="236" width="8" height="4" fill="#F59E0B" />

        {/* Green Gift */}
        <rect x="876" y="246" width="18" height="16" fill="#059669" />
        <rect x="883" y="246" width="4" height="16" fill="#EF4444" />
        <rect x="876" y="252" width="18" height="4" fill="#EF4444" />

        {/* Golden Minecraft Lantern with warm radial glow */}
        <circle cx="770" cy="245" r="45" fill="rgba(245, 158, 11, 0.15)" />
        <circle cx="770" cy="245" r="20" fill="rgba(251, 191, 36, 0.3)" />
        <rect x="765" y="235" width="10" height="15" fill="#1E293B" />
        <rect x="767" y="237" width="6" height="11" fill="#F59E0B" />
        <rect x="768" y="239" width="4" height="7" fill="#FEF08A" />
      </svg>
    </div>
  );
};
