import { MrpackImporter } from './MrpackImporter.js';
import { MrpackExporter } from './MrpackExporter.js';
import type {
  MrpackInspectionResult,
  MrpackImportOptions,
  MrpackImportProgress,
  MrpackImportResult,
  MrpackExportOptions,
  MrpackExportResult,
} from '@zombek/shared-types';

export class MrpackFormatManager {
  private static instance: MrpackFormatManager | null = null;

  public static getInstance(): MrpackFormatManager {
    if (!MrpackFormatManager.instance) {
      MrpackFormatManager.instance = new MrpackFormatManager();
    }
    return MrpackFormatManager.instance;
  }

  public async inspect(archivePath: string): Promise<MrpackInspectionResult> {
    return MrpackImporter.inspect(archivePath);
  }

  public async import(
    options: MrpackImportOptions,
    onProgress?: (progress: MrpackImportProgress) => void
  ): Promise<MrpackImportResult> {
    return MrpackImporter.import(options, onProgress);
  }

  public async export(options: MrpackExportOptions): Promise<MrpackExportResult> {
    return MrpackExporter.export(options);
  }
}
