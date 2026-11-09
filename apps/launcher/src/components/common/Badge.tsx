import React from 'react';

export type BadgeVariant =
  | 'demo'
  | 'preview'
  | 'fabric'
  | 'forge'
  | 'neoforge'
  | 'quilt'
  | 'vanilla'
  | 'festive'
  | 'gold'
  | 'success'
  | 'danger'
  | 'neutral';

interface BadgeProps {
  variant?: BadgeVariant;
  children: React.ReactNode;
  size?: 'sm' | 'md';
}

export const Badge: React.FC<BadgeProps> = ({ variant = 'neutral', children, size = 'sm' }) => {
  const getStyles = (): React.CSSProperties => {
    switch (variant) {
      case 'demo':
      case 'preview':
        return {
          background: 'rgba(239, 68, 68, 0.15)',
          color: '#f87171',
          border: '1px solid rgba(239, 68, 68, 0.35)',
        };
      case 'fabric':
        return {
          background: 'rgba(6, 182, 212, 0.15)',
          color: '#38bdf8',
          border: '1px solid rgba(6, 182, 212, 0.4)',
        };
      case 'forge':
        return {
          background: 'rgba(245, 158, 11, 0.15)',
          color: '#fbbf24',
          border: '1px solid rgba(245, 158, 11, 0.4)',
        };
      case 'neoforge':
        return {
          background: 'rgba(249, 115, 22, 0.15)',
          color: '#fb923c',
          border: '1px solid rgba(249, 115, 22, 0.4)',
        };
      case 'quilt':
        return {
          background: 'rgba(168, 85, 247, 0.15)',
          color: '#c084fc',
          border: '1px solid rgba(168, 85, 247, 0.4)',
        };
      case 'festive':
        return {
          background: 'linear-gradient(135deg, rgba(220, 38, 38, 0.25) 0%, rgba(16, 185, 129, 0.25) 100%)',
          color: '#fef08a',
          border: '1px solid rgba(251, 191, 36, 0.4)',
          fontWeight: 700,
        };
      case 'gold':
        return {
          background: 'rgba(245, 158, 11, 0.2)',
          color: '#fcd34d',
          border: '1px solid rgba(245, 158, 11, 0.4)',
          fontWeight: 700,
        };
      case 'success':
        return {
          background: 'rgba(16, 185, 129, 0.15)',
          color: '#34d399',
          border: '1px solid rgba(16, 185, 129, 0.35)',
        };
      case 'danger':
        return {
          background: 'rgba(239, 68, 68, 0.15)',
          color: '#f87171',
          border: '1px solid rgba(239, 68, 68, 0.35)',
        };
      case 'neutral':
      default:
        return {
          background: 'var(--bg-surface-active)',
          color: 'var(--text-secondary)',
          border: '1px solid var(--border-light)',
        };
    }
  };

  const isSm = size === 'sm';

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        padding: isSm ? '2px 7px' : '4px 10px',
        fontSize: isSm ? '10.5px' : '12px',
        fontWeight: 600,
        borderRadius: '4px',
        letterSpacing: '0.4px',
        textTransform: 'uppercase',
        whiteSpace: 'nowrap',
        ...getStyles(),
      }}
    >
      {children}
    </span>
  );
};
