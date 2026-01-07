import { api } from "@/lib/api";

export interface ExportMasivoParams {
  semestre: string;
  formato: "PDF" | "EXCEL";
}

export interface ExportTutorParams {
  tutorId: number;
  semestre: string;
  formato: "PDF" | "EXCEL";
}

export interface ExportCarreraParams {
  carreraCodigo: string;
  semestre: string;
  formato: "PDF" | "EXCEL";
}

export async function startExportMasivoDriveJob(params: ExportMasivoParams, idempotencyKey?: string) {
  const key = idempotencyKey || crypto.randomUUID();
  const { data } = await api.post(
    "/api/reportes/drive/export-masivo",
    {
      semestre: params.semestre,
      formato: params.formato,
    },
    {
      headers: {
        "Idempotency-Key": key,
      },
    },
  );
  return { ...data, idempotencyKey: key };
}

export async function getExportMasivoStatus(jobId: string) {
  const { data } = await api.get(`/api/reportes/drive/export-masivo/${jobId}/status`);
  return data;
}

export async function exportTutorDrive(params: ExportTutorParams) {
  const { data } = await api.post("/api/reportes/tutor/exportar-drive", {
    tutorId: params.tutorId,
    semestre: params.semestre,
    formato: params.formato,
  });
  return data;
}

export async function exportCarreraDrive(params: ExportCarreraParams) {
  const { data } = await api.post("/api/reportes/carrera/exportar-drive", {
    carreraCodigo: params.carreraCodigo,
    semestre: params.semestre,
    formato: params.formato,
  });
  return data;
}
