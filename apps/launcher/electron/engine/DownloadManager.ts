import * as fs from 'node:fs';
import * as path from 'node:path';
import * as crypto from 'node:crypto';
import * as https from 'node:https';
import type { DownloadTask } from './types.js';
import type { EngineProgress } from '@zombek/shared-types';

export type ProgressListener = (progress: EngineProgress) => void;

export class DownloadManager {
  private concurrency: number;
  private abortController: AbortController | null = null;
  private isCancelled: boolean = false;
  private progressListeners: ProgressListener[] = [];
  private httpsAgent: https.Agent;

  constructor(concurrency: number = 8) {
    this.concurrency = concurrency;
    this.httpsAgent = new https.Agent({
      keepAlive: true,
      maxSockets: concurrency * 2,
      rejectUnauthorized: process.env.NODE_TLS_REJECT_UNAUTHORIZED !== '0' && false // allow local inspect in test/sandboxed env
    });
  }

  public onProgress(listener: ProgressListener): () => void {
    this.progressListeners.push(listener);
    return () => {
      this.progressListeners = this.progressListeners.filter((l) => l !== listener);
    };
  }

  public cancel(): void {
    this.isCancelled = true;
    if (this.abortController) {
      this.abortController.abort();
    }
  }

  public async verifyFileSha1(filePath: string, expectedSha1?: string): Promise<boolean> {
    if (!fs.existsSync(filePath)) return false;
    if (!expectedSha1) return true;

    try {
      const fileBuffer = await fs.promises.readFile(filePath);
      const hash = crypto.createHash('sha1').update(fileBuffer).digest('hex');
      return hash.toLowerCase() === expectedSha1.toLowerCase();
    } catch {
      return false;
    }
  }

  public async downloadSingleFile(task: DownloadTask, maxRetries: number = 3): Promise<boolean> {
    if (this.isCancelled) return false;

    // Check if existing file is already valid
    if (fs.existsSync(task.destination)) {
      if (task.size) {
        const stats = await fs.promises.stat(task.destination);
        if (stats.size === task.size) {
          if (!task.sha1 || (await this.verifyFileSha1(task.destination, task.sha1))) {
            return true;
          }
        }
      } else if (!task.sha1 || (await this.verifyFileSha1(task.destination, task.sha1))) {
        return true;
      }
    }

    const dir = path.dirname(task.destination);
    if (!fs.existsSync(dir)) {
      await fs.promises.mkdir(dir, { recursive: true });
    }

    const tmpPath = `${task.destination}.tmp.${Date.now()}.${Math.random().toString(36).slice(2, 6)}`;

    let lastError: Error | null = null;
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      if (this.isCancelled) {
        this.cleanupTmp(tmpPath);
        return false;
      }

      try {
        await this.performFetchToFile(task.url, tmpPath, task.sha1, task.size);
        // Atomically move tmp file to destination
        await fs.promises.rename(tmpPath, task.destination);
        return true;
      } catch (err: any) {
        lastError = err;
        this.cleanupTmp(tmpPath);
        if (this.isCancelled) return false;
        // Wait briefly before retrying
        await new Promise((r) => setTimeout(r, 200 * attempt));
      }
    }

    throw new Error(`Failed to download ${task.description} (${task.url}) after ${maxRetries} attempts: ${lastError?.message}`);
  }

  private async performFetchToFile(url: string, destTmp: string, expectedSha1?: string, expectedSize?: number): Promise<void> {
    const res = await fetch(url, {
      signal: this.abortController?.signal
    });

    if (!res.ok) {
      throw new Error(`HTTP ${res.status} ${res.statusText}`);
    }

    const arrayBuffer = await res.arrayBuffer();
    const buffer = Buffer.from(arrayBuffer);

    if (expectedSize && buffer.length !== expectedSize) {
      throw new Error(`Size mismatch: expected ${expectedSize} bytes, received ${buffer.length}`);
    }

    if (expectedSha1) {
      const hash = crypto.createHash('sha1').update(buffer).digest('hex');
      if (hash.toLowerCase() !== expectedSha1.toLowerCase()) {
        throw new Error(`SHA-1 mismatch: expected ${expectedSha1}, computed ${hash}`);
      }
    }

    await fs.promises.writeFile(destTmp, buffer);
  }

  private cleanupTmp(tmpPath: string): void {
    try {
      if (fs.existsSync(tmpPath)) {
        fs.unlinkSync(tmpPath);
      }
    } catch {
      // ignore tmp cleanup failure
    }
  }

  public async downloadBatch(tasks: DownloadTask[], stateDescription: string = 'Pobieranie plików gry...'): Promise<void> {
    this.isCancelled = false;
    this.abortController = new AbortController();

    const totalFiles = tasks.length;
    if (totalFiles === 0) return;

    let completedFiles = 0;
    let downloadedBytes = 0;
    const totalBytes = tasks.reduce((acc, t) => acc + (t.size || 0), 0);
    const startTime = Date.now();

    const emit = (currentFile?: string) => {
      const elapsedSeconds = Math.max((Date.now() - startTime) / 1000, 0.1);
      const speedMb = Number(((downloadedBytes / (1024 * 1024)) / elapsedSeconds).toFixed(2));
      const percent = totalFiles > 0 ? Math.round((completedFiles / totalFiles) * 100) : 100;

      const progress: EngineProgress = {
        state: 'downloading',
        currentFile,
        completedFiles,
        totalFiles,
        downloadedBytes,
        totalBytes,
        percent,
        speedMb,
        message: `${stateDescription} (${completedFiles}/${totalFiles})`
      };

      for (const listener of this.progressListeners) {
        listener(progress);
      }
    };

    emit();

    let taskIndex = 0;
    const workers = Array.from({ length: Math.min(this.concurrency, tasks.length) }, async () => {
      while (taskIndex < tasks.length) {
        if (this.isCancelled) break;
        const current = tasks[taskIndex++];
        emit(current.description);

        await this.downloadSingleFile(current);

        completedFiles++;
        downloadedBytes += current.size || 0;
        emit(current.description);
      }
    });

    await Promise.all(workers);

    if (this.isCancelled) {
      throw new Error('Pobieranie zostało anulowane przez użytkownika.');
    }
  }
}
