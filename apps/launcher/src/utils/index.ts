/**
 * Module: apps/launcher/src/utils
 * Responsibility: Shared utilities, formatting helpers, and runtime sanitizers for launcher frontend.
 */

export function formatBytesToMb(bytes: number): number {
  return Math.round(bytes / (1024 * 1024));
}
