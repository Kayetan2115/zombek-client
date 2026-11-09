import { ModRepository } from './ModRepository.js';
import type { ModpackValidationSummary } from '@zombek/shared-types';

export class ModpackValidator {
  /**
   * Performs complete validation of all installed mods in an instance before launch or review.
   */
  public static async validateInstanceMods(instanceId: string): Promise<ModpackValidationSummary> {
    const mods = await ModRepository.getModsForInstance(instanceId);

    const criticalErrors: string[] = [];
    const warnings: string[] = [];

    let totalActive = 0;
    let totalDisabled = 0;

    for (const m of mods) {
      if (m.enabled) {
        totalActive++;

        if (m.compatibilityStatus === 'Broken File') {
          criticalErrors.push(`Plik ${m.fileName} jest uszkodzony lub nieprawidłowy.`);
        } else if (m.compatibilityStatus === 'Incompatible') {
          criticalErrors.push(`Mod ${m.metadata.name} jest niekompatybilny: ${m.compatibilityDetails || ''}`);
        } else if (m.compatibilityStatus === 'Dependency Conflict') {
          criticalErrors.push(`Mod ${m.metadata.name} powoduje konflikt z innymi modami: ${m.conflictingMods?.join(', ')}`);
        } else if (m.compatibilityStatus === 'Missing Dependency') {
          criticalErrors.push(`Mod ${m.metadata.name} wymaga brakujących zależności: ${m.missingDependencies?.join(', ')}`);
        } else if (m.compatibilityStatus === 'Unknown Metadata') {
          warnings.push(`Mod ${m.metadata.name} ma nierozpoznane metadane (Unknown Metadata).`);
        } else if (m.compatibilityStatus === 'Possibly Compatible') {
          warnings.push(`Mod ${m.metadata.name} jest oznaczony jako Possibly Compatible: ${m.compatibilityDetails || ''}`);
        }
      } else {
        totalDisabled++;
      }
    }

    return {
      valid: criticalErrors.length === 0,
      criticalErrors,
      warnings,
      totalActive,
      totalDisabled,
    };
  }
}
