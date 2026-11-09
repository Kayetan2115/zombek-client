import React from 'react';
import { SearchIcon, CloseIcon } from '../icons';

interface SearchBarProps {
  value: string;
  onChange: (val: string) => void;
  placeholder?: string;
  className?: string;
}

export const SearchBar: React.FC<SearchBarProps> = ({
  value,
  onChange,
  placeholder = 'Szukaj...',
  className = '',
}) => {
  return (
    <div
      className={`zombek-searchbar ${className}`}
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        background: 'var(--bg-surface)',
        border: '1px solid var(--border-light)',
        borderRadius: '6px',
        padding: '7px 12px',
        transition: 'all 0.18s ease',
        minWidth: '220px',
      }}
      onFocus={(e) => {
        e.currentTarget.style.borderColor = 'var(--frost-500)';
        e.currentTarget.style.boxShadow = '0 0 10px rgba(14, 165, 233, 0.2)';
      }}
      onBlur={(e) => {
        e.currentTarget.style.borderColor = 'var(--border-light)';
        e.currentTarget.style.boxShadow = 'none';
      }}
    >
      <SearchIcon size={16} color="var(--text-muted)" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        style={{
          width: '100%',
          fontSize: '13px',
          color: 'var(--text-primary)',
        }}
      />
      {value && (
        <button
          onClick={() => onChange('')}
          style={{
            display: 'flex',
            alignItems: 'center',
            color: 'var(--text-muted)',
            cursor: 'pointer',
          }}
          title="Wyczyść"
        >
          <CloseIcon size={14} />
        </button>
      )}
    </div>
  );
};
