import React from 'react';

interface SliderProps {
  label: string;
  value: number;
  min: number;
  max: number;
  step?: number;
  unit?: string;
  onChange: (value: number) => void;
  description?: string;
}

export const Slider: React.FC<SliderProps> = ({
  label,
  value,
  min,
  max,
  step = 1,
  unit = '',
  onChange,
  description,
}) => {
  const percentage = Math.round(((value - min) / (max - min)) * 100);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', width: '100%' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <span style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>{label}</span>
          {description && (
            <p style={{ fontSize: '11.5px', color: 'var(--text-muted)', marginTop: '2px' }}>{description}</p>
          )}
        </div>
        <span
          className="mono-text"
          style={{
            fontSize: '12.5px',
            fontWeight: 700,
            color: 'var(--frost-400)',
            background: 'var(--bg-surface-active)',
            padding: '2px 8px',
            borderRadius: '4px',
            border: '1px solid var(--border-light)',
          }}
        >
          {value} {unit}
        </span>
      </div>

      <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
        <input
          type="range"
          min={min}
          max={max}
          step={step}
          value={value}
          onChange={(e) => onChange(Number(e.target.value))}
          style={{
            width: '100%',
            height: '6px',
            borderRadius: '4px',
            background: `linear-gradient(to right, #0ea5e9 ${percentage}%, var(--border-subtle) ${percentage}%)`,
            outline: 'none',
            cursor: 'pointer',
            appearance: 'none',
          }}
        />
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: 'var(--text-dim)' }}>
        <span>{min} {unit}</span>
        <span>{max} {unit}</span>
      </div>
    </div>
  );
};
