import type { ZombekDesktopApi } from '@zombek/contracts';

/**
 * Access the safely exposed desktop API bridge.
 * Returns null if running in browser / testing environment without Electron preload.
 */
export function useZombekApi(): ZombekDesktopApi | null {
  if (typeof window !== 'undefined' && window.zombekDesktopApi) {
    return window.zombekDesktopApi;
  }
  return null;
}
