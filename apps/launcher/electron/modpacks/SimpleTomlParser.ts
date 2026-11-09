/**
 * Lightweight, zero-dependency TOML parser specifically designed
 * for Forge mods.toml and NeoForge neoforge.mods.toml files.
 */
export class SimpleTomlParser {
  public static parse(tomlContent: string): Record<string, any> {
    const root: Record<string, any> = {};
    let currentTarget: Record<string, any> = root;

    const lines = tomlContent.split(/\r?\n/);
    let inMultiline = false;
    let multilineKey = '';
    let multilineQuote = '';
    let multilineAccumulator: string[] = [];

    for (let i = 0; i < lines.length; i++) {
      let line = lines[i];

      // Handle multiline string reading
      if (inMultiline) {
        const endQuoteIndex = line.indexOf(multilineQuote);
        if (endQuoteIndex !== -1) {
          multilineAccumulator.push(line.substring(0, endQuoteIndex));
          currentTarget[multilineKey] = multilineAccumulator.join('\n').trim();
          inMultiline = false;
          multilineKey = '';
          multilineQuote = '';
          multilineAccumulator = [];
        } else {
          multilineAccumulator.push(line);
        }
        continue;
      }

      // Strip comments (handling quotes carefully)
      const commentIndex = this.findCommentIndex(line);
      if (commentIndex !== -1) {
        line = line.substring(0, commentIndex);
      }
      line = line.trim();
      if (!line) continue;

      // Table Array: [[table.sub]]
      if (line.startsWith('[[') && line.endsWith(']]')) {
        const fullPath = line.substring(2, line.length - 2).trim();
        currentTarget = this.ensureTableArray(root, fullPath);
        continue;
      }

      // Single Table: [table.sub]
      if (line.startsWith('[') && line.endsWith(']')) {
        const fullPath = line.substring(1, line.length - 1).trim();
        currentTarget = this.ensureTable(root, fullPath);
        continue;
      }

      // Key-Value pair
      const eqIdx = line.indexOf('=');
      if (eqIdx !== -1) {
        const rawKey = line.substring(0, eqIdx).trim();
        const rawVal = line.substring(eqIdx + 1).trim();

        // Check for start of multiline string: ''' or """
        if (rawVal.startsWith("'''") || rawVal.startsWith('"""')) {
          const quote = rawVal.substring(0, 3);
          const afterQuote = rawVal.substring(3);
          const endQuoteIdx = afterQuote.indexOf(quote);
          if (endQuoteIdx !== -1) {
            currentTarget[rawKey] = afterQuote.substring(0, endQuoteIdx).trim();
          } else {
            inMultiline = true;
            multilineKey = rawKey;
            multilineQuote = quote;
            multilineAccumulator = [afterQuote];
          }
          continue;
        }

        currentTarget[rawKey] = this.parseValue(rawVal);
      }
    }

    return root;
  }

  private static findCommentIndex(line: string): number {
    let inSingleQuote = false;
    let inDoubleQuote = false;

    for (let i = 0; i < line.length; i++) {
      const char = line[i];
      if (char === "'" && !inDoubleQuote) {
        inSingleQuote = !inSingleQuote;
      } else if (char === '"' && !inSingleQuote) {
        inDoubleQuote = !inDoubleQuote;
      } else if (char === '#' && !inSingleQuote && !inDoubleQuote) {
        return i;
      }
    }
    return -1;
  }

  private static ensureTable(root: Record<string, any>, fullPath: string): Record<string, any> {
    const parts = fullPath.split('.').map((p) => p.trim());
    let cur = root;

    for (const part of parts) {
      if (!cur[part] || typeof cur[part] !== 'object' || Array.isArray(cur[part])) {
        cur[part] = {};
      }
      cur = cur[part];
    }
    return cur;
  }

  private static ensureTableArray(root: Record<string, any>, fullPath: string): Record<string, any> {
    const parts = fullPath.split('.').map((p) => p.trim());
    let cur = root;

    for (let i = 0; i < parts.length - 1; i++) {
      const part = parts[i];
      if (!cur[part] || typeof cur[part] !== 'object' || Array.isArray(cur[part])) {
        cur[part] = {};
      }
      cur = cur[part];
    }

    const lastPart = parts[parts.length - 1];
    if (!Array.isArray(cur[lastPart])) {
      cur[lastPart] = [];
    }

    const newObj: Record<string, any> = {};
    cur[lastPart].push(newObj);
    return newObj;
  }

  private static parseValue(raw: string): any {
    const trimmed = raw.trim();

    // String: "..." or '...'
    if (
      (trimmed.startsWith('"') && trimmed.endsWith('"')) ||
      (trimmed.startsWith("'") && trimmed.endsWith("'"))
    ) {
      return trimmed.substring(1, trimmed.length - 1);
    }

    // Boolean
    if (trimmed.toLowerCase() === 'true') return true;
    if (trimmed.toLowerCase() === 'false') return false;

    // Number
    if (/^-?\d+(\.\d+)?$/.test(trimmed)) {
      return Number(trimmed);
    }

    // Inline Array: [...]
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      const inner = trimmed.substring(1, trimmed.length - 1).trim();
      if (!inner) return [];
      const parts = inner.split(',').map((p) => this.parseValue(p.trim()));
      return parts;
    }

    return trimmed;
  }
}
