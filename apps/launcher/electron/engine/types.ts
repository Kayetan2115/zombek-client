export interface MojangDownloadArtifact {
  path?: string;
  sha1: string;
  size: number;
  url: string;
}

export interface MojangRuleOS {
  name?: string;
  version?: string;
  arch?: string;
}

export interface MojangRule {
  action: 'allow' | 'disallow';
  os?: MojangRuleOS;
  features?: Record<string, boolean>;
}

export interface MojangLibraryExtract {
  exclude?: string[];
}

export interface MojangLibrary {
  name: string;
  downloads?: {
    artifact?: MojangDownloadArtifact;
    classifiers?: Record<string, MojangDownloadArtifact>;
  };
  rules?: MojangRule[];
  natives?: Record<string, string>;
  extract?: MojangLibraryExtract;
  url?: string;
  sha1?: string;
  size?: number;
  md5?: string;
  sha256?: string;
}

export interface MojangAssetIndexRef {
  id: string;
  sha1: string;
  size: number;
  totalSize: number;
  url: string;
}

export interface MojangJavaVersionRef {
  component: string;
  majorVersion: number;
}

export interface MojangArgumentValue {
  rules?: MojangRule[];
  value: string | string[];
}

export type MojangArgumentItem = string | MojangArgumentValue;

export interface MojangVersionPackage {
  id: string;
  type: string;
  time: string;
  releaseTime: string;
  inheritsFrom?: string;
  mainClass: string;
  minecraftArguments?: string;
  arguments?: {
    game?: MojangArgumentItem[];
    jvm?: MojangArgumentItem[];
  };
  downloads: {
    client: MojangDownloadArtifact;
    server?: MojangDownloadArtifact;
  };
  libraries: MojangLibrary[];
  assetIndex: MojangAssetIndexRef;
  assets: string;
  complianceLevel?: number;
  javaVersion?: MojangJavaVersionRef;
  logging?: {
    client?: {
      argument?: string;
      file?: MojangDownloadArtifact;
      type?: string;
    };
  };
}

export interface MojangAssetObjectsManifest {
  objects: Record<string, { hash: string; size: number }>;
}

export interface MojangJavaRuntimeManifestAll {
  [platform: string]: {
    [component: string]: Array<{
      availability: { group: number; progress: number };
      manifest: {
        sha1: string;
        size: number;
        url: string;
      };
      version: {
        name: string;
        released: string;
      };
    }>;
  };
}

export interface MojangJavaRuntimeFileEntry {
  type: 'file' | 'directory' | 'link';
  executable?: boolean;
  downloads?: {
    raw?: MojangDownloadArtifact;
    lzma?: MojangDownloadArtifact;
  };
  target?: string;
}

export interface MojangJavaRuntimeFilesManifest {
  files: Record<string, MojangJavaRuntimeFileEntry>;
}

export interface DownloadTask {
  id: string;
  url: string;
  destination: string;
  sha1?: string;
  size?: number;
  description: string;
}
