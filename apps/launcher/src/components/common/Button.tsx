import React, { ButtonHTMLAttributes, ReactNode } from 'react';

export type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost' | 'festive-gold' | 'play';
export type ButtonSize = 'sm' | 'md' | 'lg' | 'xl';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  icon?: ReactNode;
  children: ReactNode;
  className?: string;
  glow?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  icon,
  children,
  className = '',
  glow = false,
  disabled,
  style,
  ...props
}) => {
  const getPaddingAndFont = () => {
    switch (size) {
      case 'sm':
        return { padding: '6px 12px', fontSize: '12px', gap: '6px' };
      case 'lg':
        return { padding: '12px 24px', fontSize: '15px', gap: '10px' };
      case 'xl':
        return { padding: '16px 36px', fontSize: '18px', gap: '12px', fontWeight: 700 };
      case 'md':
      default:
        return { padding: '9px 18px', fontSize: '13.5px', gap: '8px' };
    }
  };

  const getVariantStyles = (): React.CSSProperties => {
    switch (variant) {
      case 'play':
        return {
          background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
          color: '#ffffff',
          border: '1px solid #34d399',
          boxShadow: glow ? '0 0 25px rgba(16, 185, 129, 0.45)' : '0 4px 14px rgba(5, 150, 105, 0.3)',
          letterSpacing: '0.8px',
          textTransform: 'uppercase',
        };
      case 'festive-gold':
        return {
          background: 'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
          color: '#0f172a',
          fontWeight: 700,
          border: '1px solid #fbbf24',
          boxShadow: '0 4px 16px rgba(245, 158, 11, 0.3)',
        };
      case 'secondary':
        return {
          background: 'var(--bg-surface)',
          color: 'var(--text-primary)',
          border: '1px solid var(--border-light)',
        };
      case 'danger':
        return {
          background: 'linear-gradient(135deg, #ef4444 0%, #dc2626 100%)',
          color: '#ffffff',
          border: '1px solid #f87171',
        };
      case 'ghost':
        return {
          background: 'transparent',
          color: 'var(--text-secondary)',
          border: '1px solid transparent',
        };
      case 'primary':
      default:
        return {
          background: 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)',
          color: '#ffffff',
          border: '1px solid #38bdf8',
          boxShadow: '0 4px 14px rgba(2, 132, 199, 0.25)',
        };
    }
  };

  const { padding, fontSize, gap, fontWeight } = getPaddingAndFont();

  return (
    <button
      disabled={disabled}
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: '6px',
        fontWeight: fontWeight || 600,
        cursor: disabled ? 'not-allowed' : 'pointer',
        opacity: disabled ? 0.5 : 1,
        transition: 'all 0.18s ease',
        outline: 'none',
        whiteSpace: 'nowrap',
        padding,
        fontSize,
        gap,
        ...getVariantStyles(),
        ...style,
      }}
      className={`zombek-button ${className}`}
      onMouseEnter={(e) => {
        if (!disabled) {
          e.currentTarget.style.transform = 'translateY(-1px)';
          if (variant === 'secondary') {
            e.currentTarget.style.background = 'var(--bg-surface-hover)';
            e.currentTarget.style.borderColor = 'var(--frost-500)';
          }
        }
      }}
      onMouseLeave={(e) => {
        if (!disabled) {
          e.currentTarget.style.transform = 'translateY(0)';
          if (variant === 'secondary') {
            e.currentTarget.style.background = 'var(--bg-surface)';
            e.currentTarget.style.borderColor = 'var(--border-light)';
          }
        }
      }}
      {...props}
    >
      {icon && <span style={{ display: 'inline-flex', alignItems: 'center' }}>{icon}</span>}
      <span>{children}</span>
    </button>
  );
};
