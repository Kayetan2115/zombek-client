import * as os from 'node:os';
import { exec, execSync } from 'node:child_process';
import type { HardwareSpecs } from '@zombek/shared-types';

/**
 * HardwareDetectionService
 * Detects local PC specifications (CPU, RAM, GPU, OS, Java) without any external telemetry.
 */
export class HardwareDetectionService {
  private static cachedSpecs: HardwareSpecs | null = null;

  public static async getHardwareSpecs(): Promise<HardwareSpecs> {
    if (this.cachedSpecs) {
      return this.cachedSpecs;
    }

    const cpus = os.cpus();
    const cpuModel = cpus.length > 0 ? cpus[0].model.trim() : 'Unknown CPU';
    const cpuThreads = cpus.length;
    const cpuCores = Math.max(1, Math.floor(cpuThreads / 2)); // rough estimate if hyperthreading, or cpus.length
    const cpuSpeedMhz = cpus.length > 0 ? cpus[0].speed : undefined;

    const totalRamMb = Math.round(os.totalmem() / (1024 * 1024));
    const freeRamMb = Math.round(os.freemem() / (1024 * 1024));

    // Calculate scientifically optimal Minecraft RAM (prevents GC pause spikes)
    let recommendedRamMb = 4096;
    if (totalRamMb >= 16384) {
      recommendedRamMb = 4096; // optimal for 1.20.4 vanilla/performance packs
    } else if (totalRamMb >= 8192) {
      recommendedRamMb = 3584;
    } else {
      recommendedRamMb = 2048;
    }

    const osName = `${os.type()} ${os.release()}`;
    const osArch = os.arch();

    const gpuModel = await this.detectGpu();
    const { javaVersion, javaPath } = this.detectJava();

    this.cachedSpecs = {
      cpuModel,
      cpuCores,
      cpuThreads,
      cpuSpeedMhz,
      gpuModel,
      totalRamMb,
      freeRamMb,
      recommendedRamMb,
      osName,
      osArch,
      javaVersion,
      javaPath
    };

    return this.cachedSpecs;
  }

  private static async detectGpu(): Promise<string> {
    if (process.platform === 'win32') {
      try {
        const output = execSync(
          'powershell -NoProfile -NonInteractive -Command "Get-CimInstance Win32_VideoController | Select-Object -ExpandProperty Caption"',
          { timeout: 3000, encoding: 'utf-8', stdio: ['pipe', 'pipe', 'ignore'] }
        );
        const gpus = output.split(/\r?\n/).map(s => s.trim()).filter(Boolean);
        if (gpus.length > 0) {
          // Prefer discrete GPUs (NVIDIA/AMD) over integrated Intel if multiple
          const discrete = gpus.find(g => /nvidia|geforce|rtx|gtx|radeon|rx\s/i.test(g));
          return discrete || gpus[0];
        }
      } catch {
        // Fallback below
      }
    }

    return 'Default Graphics Device';
  }

  private static detectJava(): { javaVersion: string; javaPath: string } {
    let javaPath = 'java';
    let javaVersion = 'Unknown';

    try {
      const output = execSync('java -version', {
        timeout: 2500,
        encoding: 'utf-8',
        stdio: ['pipe', 'pipe', 'pipe']
      });
      const match = output.match(/version\s+"([^"]+)"/i);
      if (match) {
        javaVersion = match[1];
      }
    } catch (err: any) {
      if (err.stderr) {
        const match = String(err.stderr).match(/version\s+"([^"]+)"/i);
        if (match) {
          javaVersion = match[1];
        }
      }
    }

    return { javaVersion, javaPath };
  }

  public static getRecommendedG1GcArgs(allocatedMb: number): string[] {
    return [
      `-Xms${Math.min(1024, allocatedMb)}M`,
      `-Xmx${allocatedMb}M`,
      '-XX:+UseG1GC',
      '-XX:+ParallelRefProcEnabled',
      '-XX:MaxGCPauseMillis=200',
      '-XX:+UnlockExperimentalVMOptions',
      '-XX:+DisableExplicitGC',
      '-XX:+AlwaysPreTouch',
      '-XX:G1NewSizePercent=30',
      '-XX:G1MaxNewSizePercent=40',
      '-XX:G1ReservePercent=20',
      '-XX:G1HeapWastePercent=5',
      '-XX:G1MixedGCCountTarget=4',
      '-XX:InitiatingHeapOccupancyPercent=15',
      '-XX:G1MixedGCLiveThresholdPercent=90',
      '-XX:G1RSetUpdatingPauseTimePercent=5',
      '-XX:SurvivorRatio=32',
      '-XX:+PerfDisableSharedMem',
      '-XX:MaxTenuringThreshold=1'
    ];
  }
}
