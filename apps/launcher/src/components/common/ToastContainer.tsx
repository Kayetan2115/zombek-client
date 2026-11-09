import React from 'react';
import { useLauncherContext, ToastMessage } from '../../stores';
import { CloseIcon, CheckIcon, SnowflakeIcon } from '../icons';

export const ToastContainer: React.FC = () => {
  const { toasts, removeToast } = useLauncherContext();

  if (toasts.length === 0) return null;

  return (
    <div
      style={{
        position: 'fixed',
        top: '64px',
        right: '24px',
        display: 'flex',
        flexDirection: 'column',
        gap: '10px',
        zIndex: 1100,
        pointerEvents: 'none',
      }}
    >
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} onClose={() => removeToast(toast.id)} />
      ))}
    </div>
  );
};

const ToastItem: React.FC<{ toast: ToastMessage; onClose: () => void }> = ({ toast, onClose }) => {
  const getColors = () => {
    switch (toast.type) {
      case 'success':
        return {
          border: '#10b981',
          bg: 'rgba(8, 26, 18, 0.95)',
          icon: '#34d399',
          titleColor: '#34d399',
        };
      case 'warning':
        return {
          border: '#f59e0b',
          bg: 'rgba(45, 31, 5, 0.95)',
          icon: '#fbbf24',
          titleColor: '#fbbf24',
        };
      case 'error':
        return {
          border: '#ef4444',
          bg: 'rgba(43, 11, 13, 0.95)',
          icon: '#f87171',
          titleColor: '#f87171',
        };
      case 'info':
      default:
        return {
          border: '#0ea5e9',
          bg: 'rgba(8, 38, 53, 0.95)',
          icon: '#38bdf8',
          titleColor: '#38bdf8',
        };
    }
  };

  const { border, bg, icon, titleColor } = getColors();

  return (
    <div
      className="animate-toast"
      style={{
        width: '320px',
        background: bg,
        border: `1px solid ${border}`,
        borderRadius: '8px',
        boxShadow: '0 8px 24px rgba(0, 0, 0, 0.5)',
        backdropFilter: 'blur(8px)',
        padding: '12px 14px',
        display: 'flex',
        alignItems: 'flex-start',
        gap: '12px',
        pointerEvents: 'auto',
      }}
    >
      <div style={{ marginTop: '2px', display: 'flex' }}>
        {toast.type === 'success' ? (
          <CheckIcon size={18} color={icon} />
        ) : (
          <SnowflakeIcon size={18} color={icon} />
        )}
      </div>

      <div style={{ flex: 1 }}>
        <h4 style={{ fontSize: '13px', fontWeight: 700, color: titleColor }}>{toast.title}</h4>
        {toast.message && (
          <p style={{ fontSize: '12px', color: 'var(--text-primary)', marginTop: '2px', lineHeight: 1.4 }}>
            {toast.message}
          </p>
        )}
      </div>

      <button
        onClick={onClose}
        style={{
          color: 'var(--text-muted)',
          display: 'flex',
          padding: '2px',
          cursor: 'pointer',
        }}
      >
        <CloseIcon size={14} />
      </button>
    </div>
  );
};
