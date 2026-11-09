import React from 'react';

interface ToggleProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  disabled?: boolean;
  label?: string;
  size?: 'sm' | 'md';
}

export const Toggle: React.FC<ToggleProps> = ({
  checked,
  onChange,
  disabled = false,
  label,
  size = 'md',
}) => {
  const isSm = size === 'sm';
  const width = isSm ? 36 : 44;
  const height = isSm ? 20 : 24;
  const knobDim = isSm ? 14 : 18;
  const knobTranslate = isSm ? 17 : 21;

  return (
    <label
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '10px',
        cursor: disabled ? 'not-allowed' : 'pointer',
        userSelect: 'none',
        opacity: disabled ? 0.5 : 1,
      }}
      onClick={(e) => {
        e.preventDefault();
        if (!disabled) onChange(!checked);
      }}
    >
      <div
        style={{
          width,
          height,
          borderRadius: 999,
          background: checked
            ? 'linear-gradient(135deg, #10b981 0%, #059669 100%)'
            : 'var(--border-subtle)',
          border: `1px solid ${checked ? '#34d399' : 'var(--border-light)'}`,
          boxShadow: checked ? '0 0 10px rgba(16, 185, 129, 0.35)' : 'none',
          position: 'relative',
          transition: 'all 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
        }}
      >
        <div
          style={{
            width: knobDim,
            height: knobDim,
            borderRadius: '50%',
            background: '#ffffff',
            boxShadow: '0 2px 4px rgba(0,0,0,0.3)',
            position: 'absolute',
            top: 2,
            left: 2,
            transform: checked ? `translateX(${knobTranslate}px)` : 'translateX(0)',
            transition: 'transform 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
          }}
        />
      </div>
      {label && <span style={{ fontSize: isSm ? '12px' : '13.5px', color: 'var(--text-primary)' }}>{label}</span>}
    </label>
  );
};
