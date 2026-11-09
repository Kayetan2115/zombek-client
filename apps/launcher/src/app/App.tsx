import React from 'react';
import { LauncherProvider, useLauncherContext } from '../stores';
import { MainLayout } from '../components/layout';
import {
  HomePage,
  PlayPage,
  VersionsPage,
  ModpacksPage,
  ModsPage,
  PerformancePage,
  CosmeticsPage,
  AccountsPage,
  SettingsPage,
  LogsPage,
} from '../pages';

const ViewRouter: React.FC = () => {
  const { currentTab } = useLauncherContext();

  switch (currentTab) {
    case 'play':
      return <PlayPage />;
    case 'versions':
      return <VersionsPage />;
    case 'modpacks':
      return <ModpacksPage />;
    case 'mods':
      return <ModsPage />;
    case 'performance':
      return <PerformancePage />;
    case 'cosmetics':
      return <CosmeticsPage />;
    case 'accounts':
      return <AccountsPage />;
    case 'settings':
      return <SettingsPage />;
    case 'logs':
      return <LogsPage />;
    case 'home':
    default:
      return <HomePage />;
  }
};

export const App: React.FC = () => {
  return (
    <LauncherProvider>
      <MainLayout>
        <ViewRouter />
      </MainLayout>
    </LauncherProvider>
  );
};
