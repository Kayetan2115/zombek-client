import { describe, it, expect } from 'vitest';
import { IPC_CHANNELS } from '../src/index';

describe('contracts package', () => {
  it('should define isolated channel namespace with prefix zombek:', () => {
    Object.values(IPC_CHANNELS).forEach((channel) => {
      expect(channel.startsWith('zombek:')).toBe(true);
    });
  });

  it('should contain fundamental channels', () => {
    expect(IPC_CHANNELS.APP_PING).toBe('zombek:app:ping');
    expect(IPC_CHANNELS.APP_GET_INFO).toBe('zombek:app:get-info');
    expect(IPC_CHANNELS.CONFIG_GET).toBe('zombek:config:get');
    expect(IPC_CHANNELS.CONFIG_SET).toBe('zombek:config:set');
    expect(IPC_CHANNELS.SYSTEM_GET_DIAGNOSTICS).toBe('zombek:system:get-diagnostics');
    expect(IPC_CHANNELS.WINDOW_MINIMIZE).toBe('zombek:window:minimize');
    expect(IPC_CHANNELS.WINDOW_MAXIMIZE).toBe('zombek:window:maximize');
    expect(IPC_CHANNELS.WINDOW_CLOSE).toBe('zombek:window:close');
    expect(IPC_CHANNELS.VERSIONS_GET_MANIFEST).toBe('zombek:versions:get-manifest');
    expect(IPC_CHANNELS.GAME_LAUNCH).toBe('zombek:game:launch');
    expect(IPC_CHANNELS.INSTANCES_GET_ALL).toBe('zombek:instances:get-all');
    expect(IPC_CHANNELS.ACCOUNTS_GET_ALL).toBe('zombek:accounts:get-all');
    expect(IPC_CHANNELS.ACCOUNTS_LOGIN_MICROSOFT).toBe('zombek:accounts:login-microsoft');
    expect(IPC_CHANNELS.EVENT_AUTH_PROGRESS).toBe('zombek:event:auth-progress');
  });
});
