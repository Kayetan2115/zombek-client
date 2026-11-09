import React, { ReactNode } from 'react';
import { Sidebar } from './Sidebar';
import { Topbar } from './Topbar';
import { Snowfall, ToastContainer } from '../common';

export const MainLayout: React.FC<{ children: ReactNode }> = ({ children }) => {
  return (
    <div className="app-container">
      {/* Snowfall overlay */}
      <Snowfall />

      {/* Floating Notifications */}
      <ToastContainer />

      {/* Navigation Sidebar */}
      <Sidebar />

      {/* Main View Area */}
      <div
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          height: '100vh',
          overflow: 'hidden',
          background: 'var(--bg-content)',
          position: 'relative',
        }}
      >
        <Topbar />
        <main className="content-scrollable animate-view">{children}</main>
      </div>
    </div>
  );
};
