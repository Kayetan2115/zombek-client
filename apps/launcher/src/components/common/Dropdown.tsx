import React, { useState, useRef, useEffect, ReactNode } from 'react';
import { ChevronIcon, CheckIcon } from '../icons';

export interface DropdownOption<T extends string | number> {
  value: T;
  label: string;
  icon?: ReactNode;
  subtitle?: string;
}

interface DropdownProps<T extends string | number> {
  options: DropdownOption<T>[];
  value: T;
  onChange: (value: T) => void;
  label?: string;
  placeholder?: string;
  width?: string | number;
}

export function Dropdown<T extends string | number>({
  options,
  value,
  onChange,
  label,
  placeholder = 'Wybierz...',
  width = '100%',
}: DropdownProps<T>) {
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  const selectedOption = options.find((o) => o.value === value);

  useEffect(() => {
    const handleOutsideClick = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleOutsideClick);
    return () => document.removeEventListener('mousedown', handleOutsideClick);
  }, []);

  return (
    <div ref={containerRef} style={{ width, display: 'flex', flexDirection: 'column', gap: '6px' }}>
      {label && (
        <span style={{ fontSize: '12.5px', fontWeight: 600, color: 'var(--text-secondary)' }}>
          {label}
        </span>
      )}
      <div style={{ position: 'relative' }}>
        <button
          type="button"
          onClick={() => setIsOpen(!isOpen)}
          style={{
            width: '100%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: '8px',
            background: 'var(--bg-surface)',
            border: `1px solid ${isOpen ? 'var(--frost-500)' : 'var(--border-light)'}`,
            borderRadius: '6px',
            padding: '8px 12px',
            color: 'var(--text-primary)',
            fontSize: '13px',
            cursor: 'pointer',
            boxShadow: isOpen ? '0 0 12px rgba(14, 165, 233, 0.25)' : 'none',
            transition: 'all 0.18s ease',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', overflow: 'hidden' }}>
            {selectedOption?.icon}
            <span style={{ textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
              {selectedOption ? selectedOption.label : placeholder}
            </span>
          </div>
          <ChevronIcon size={14} color="var(--text-muted)" direction={isOpen ? 'up' : 'down'} />
        </button>

        {isOpen && (
          <div
            style={{
              position: 'absolute',
              top: 'calc(100% + 4px)',
              left: 0,
              right: 0,
              maxHeight: '220px',
              overflowY: 'auto',
              background: 'var(--bg-card)',
              border: '1px solid var(--border-light)',
              borderRadius: '8px',
              boxShadow: '0 8px 24px rgba(0, 0, 0, 0.5)',
              zIndex: 50,
              padding: '4px',
            }}
          >
            {options.map((opt) => {
              const isSelected = opt.value === value;
              return (
                <div
                  key={String(opt.value)}
                  onClick={() => {
                    onChange(opt.value);
                    setIsOpen(false);
                  }}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '8px 10px',
                    borderRadius: '4px',
                    cursor: 'pointer',
                    background: isSelected ? 'var(--bg-surface-active)' : 'transparent',
                    color: isSelected ? 'var(--frost-400)' : 'var(--text-primary)',
                    fontSize: '12.5px',
                    transition: 'background 0.12s ease',
                  }}
                  onMouseEnter={(e) => {
                    if (!isSelected) e.currentTarget.style.background = 'var(--bg-surface-hover)';
                  }}
                  onMouseLeave={(e) => {
                    if (!isSelected) e.currentTarget.style.background = 'transparent';
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {opt.icon}
                    <div>
                      <div>{opt.label}</div>
                      {opt.subtitle && (
                        <div style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{opt.subtitle}</div>
                      )}
                    </div>
                  </div>
                  {isSelected && <CheckIcon size={14} color="var(--frost-400)" />}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
