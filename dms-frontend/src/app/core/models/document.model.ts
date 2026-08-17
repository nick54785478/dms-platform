export interface FileResponse {
  id: string;
  originalFileName: string;
  mimeType: string;
  size: number;
}

export interface DocumentCreatedResource {
  id: string;
  title: string;
  description: string;
  fileId: string;
  status: string;
  createdAt: string;
}

export interface PresignedUrlResponse {
  fileId: string;
  url: string; // FileService 使用 'url'
}

export interface InitiateMultipartResult {
  fileId: string;
  uploadId: string;
}

export interface DocumentSearchedResource {
  id: string;
  title: string;
  description: string;
  fileId: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  semanticVersion: string;
}

export interface PageRetrievedResource<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

export interface DocumentVersionResource {
  versionId: string;
  majorVersion: number;
  minorVersion: number;
  semanticVersion: string;
  title: string;
  description: string;
  fileId: string;
  createdAt: string;
}

export interface DocumentRetrievedResource {
  id: string;
  title: string;
  description: string;
  fileId: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  semanticVersion: string;
  history: DocumentVersionResource[];
}
