import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";

interface TutorReportPayload {
  tutorId: number;
  formato: "PDF" | "EXCEL";
  periodo: string;
}

interface CareerReportPayload {
  codigoCarrera: string;
  formato: "PDF" | "EXCEL";
  periodo: string;
}

export const downloadTutorReport = async ({
  tutorId,
  formato,
  periodo,
}: TutorReportPayload) => {
  const { data } = await api.get(
    `${API_URLS.reportes.tutores(tutorId)}?formato=${formato}&periodo=${periodo}`,
    {
      responseType: "blob",
    },
  );
  return data;
};

export const downloadCareerReport = async ({
  codigoCarrera,
  formato,
  periodo,
}: CareerReportPayload) => {
  const { data } = await api.get(
    `${API_URLS.reportes.carreras(codigoCarrera)}?formato=${formato}&periodo=${periodo}`,
    {
      responseType: "blob",
    },
  );
  return data;
};
