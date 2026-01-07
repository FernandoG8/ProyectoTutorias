export interface DriveStatus {
  linked: boolean;
  driveConnected: boolean;
  googleEmail?: string;
}

export interface DriveExportItem {
  entity: string;
  status: string;
  link?: string;
  errorCode?: string;
}

export interface DriveExportResult {
  total: number;
  okCount: number;
  failCount: number;
  items: DriveExportItem[];
}

export type ExportJobStatus = "QUEUED" | "RUNNING" | "SUCCESS" | "FAILED" | "PARTIAL_SUCCESS" | "CANCELLED";

export interface ExportJobFile {
  entity?: string;
  tutorId?: number;
  fileName?: string;
  fileId?: string;
  webViewLink?: string;
  folderId?: string;
  folderLink?: string;
  status?: string;
  error?: string;
}

export interface ExportJobStatusResponse {
  jobId: string;
  status: ExportJobStatus;
  total?: number;
  processed?: number;
  successCount?: number;
  failCount?: number;
  progressPct?: number;
  message?: string;
  folderId?: string;
  folderLink?: string;
  files?: ExportJobFile[];
  errorJson?: string;
}
