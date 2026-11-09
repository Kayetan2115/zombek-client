import React, { useState } from 'react';
import { useLauncherContext, LogEntry } from '../stores';
import { Button, Badge, SearchBar } from '../components/common';
import { CopyIcon, TrashIcon, LogsIcon, RefreshIcon } from '../components/icons';

export const LogsPage: React.FC = () => {
  const { logs, clearLogs, addLog, addToast } = useLauncherContext();
  const [search, setSearch] = useState('');
  const [filterLevel, setFilterLevel] = useState<string>('ALL');

  const filteredLogs = logs.filter((log) => {
    const matchesLevel = filterLevel === 'ALL' || log.level === filterLevel;
    const matchesSearch =
      log.message.toLowerCase().includes(search.toLowerCase()) ||
      log.category.toLowerCase().includes(search.toLowerCase());
    return matchesLevel && matchesSearch;
  });

  const copyAllLogs = () => {
    const text = filteredLogs
      .map((l) => `[${l.timestamp}] [${l.category}/${l.level}]: ${l.message}`)
      .join('\n');
    navigator.clipboard.writeText(text);
    addToast({ type: 'success', title: 'Skopiowano logi', message: `Skopiowano ${filteredLogs.length} wpisów do schowka.` });
  };

  const addSampleLog = () => {
    addLog('INFO', 'USER', 'Ręcznie wygenerowano wpis diagnostyczny w widoku dziennika zdarzeń.');
    addToast({ type: 'info', title: 'Dodano wpis', message: 'Wpis diagnostyczny dopisany do konsoli.' });
  };

  const getLevelStyle = (level: LogEntry['level']) => {
    switch (level) {
      case 'ERROR':
        return { color: '#f87171', bg: 'rgba(239, 68, 68, 0.15)', border: 'rgba(239, 68, 68, 0.3)' };
      case 'WARNING':
        return { color: '#fbbf24', bg: 'rgba(245, 158, 11, 0.15)', border: 'rgba(245, 158, 11, 0.3)' };
      case 'DEBUG':
        return { color: '#c084fc', bg: 'rgba(192, 132, 252, 0.15)', border: 'rgba(192, 132, 252, 0.3)' };
      case 'INFO':
      default:
        return { color: '#38bdf8', bg: 'rgba(56, 189, 248, 0.15)', border: 'rgba(56, 189, 248, 0.3)' };
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '18px', height: '100%' }}>
      {/* Action Toolbar */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          gap: '12px',
          flexWrap: 'wrap',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          <SearchBar value={search} onChange={setSearch} placeholder="Filtruj logi..." />

          {/* Level Filter Buttons */}
          <div style={{ display: 'flex', gap: '4px' }}>
            {['ALL', 'INFO', 'WARNING', 'ERROR', 'DEBUG'].map((lvl) => {
              const isSelected = filterLevel === lvl;
              return (
                <button
                  key={lvl}
                  onClick={() => setFilterLevel(lvl)}
                  style={{
                    padding: '6px 10px',
                    borderRadius: '4px',
                    fontSize: '11px',
                    fontWeight: 700,
                    cursor: 'pointer',
                    background: isSelected ? 'var(--bg-surface-active)' : 'var(--bg-surface)',
                    border: `1px solid ${isSelected ? 'var(--frost-500)' : 'var(--border-subtle)'}`,
                    color: isSelected ? 'var(--frost-400)' : 'var(--text-secondary)',
                  }}
                >
                  {lvl}
                </button>
              );
            })}
          </div>
        </div>

        <div style={{ display: 'flex', gap: '8px' }}>
          <Button variant="secondary" size="sm" icon={<RefreshIcon size={14} />} onClick={addSampleLog}>
            Dodaj wpis testowy
          </Button>

          <Button variant="secondary" size="sm" icon={<CopyIcon size={14} />} onClick={copyAllLogs}>
            Kopiuj Wszystko
          </Button>

          <Button variant="ghost" size="sm" icon={<TrashIcon size={14} />} onClick={clearLogs}>
            Wyczyść
          </Button>
        </div>
      </div>

      {/* Log Console Container */}
      <div
        className="mono-text"
        style={{
          flex: 1,
          minHeight: '400px',
          background: '#070a10',
          border: '1px solid var(--border-light)',
          borderRadius: '10px',
          padding: '16px',
          overflowY: 'auto',
          fontSize: '12px',
          display: 'flex',
          flexDirection: 'column',
          gap: '6px',
        }}
      >
        {filteredLogs.map((log) => {
          const lvl = getLevelStyle(log.level);
          return (
            <div
              key={log.id}
              style={{
                display: 'flex',
                alignItems: 'flex-start',
                gap: '10px',
                padding: '4px 6px',
                borderRadius: '4px',
                lineHeight: 1.5,
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.background = 'rgba(255,255,255,0.03)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.background = 'transparent';
              }}
            >
              <span style={{ color: 'var(--text-dim)', flexShrink: 0 }}>[{log.timestamp}]</span>

              <span
                style={{
                  color: lvl.color,
                  background: lvl.bg,
                  border: `1px solid ${lvl.border}`,
                  padding: '1px 6px',
                  borderRadius: '3px',
                  fontSize: '10.5px',
                  fontWeight: 700,
                  flexShrink: 0,
                }}
              >
                {log.level}
              </span>

              <span style={{ color: 'var(--pine-400)', fontWeight: 600, flexShrink: 0 }}>
                [{log.category}]
              </span>

              <span style={{ color: 'var(--text-primary)', wordBreak: 'break-word', flex: 1 }}>
                {log.message}
              </span>
            </div>
          );
        })}

        {filteredLogs.length === 0 && (
          <div style={{ padding: '30px', textAlign: 'center', color: 'var(--text-muted)' }}>
            Brak wpisów logów pasujących do filtra.
          </div>
        )}
      </div>
    </div>
  );
};
