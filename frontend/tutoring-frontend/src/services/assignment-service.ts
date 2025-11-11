import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type { AssignmentProcess, ProcessAssignmentsPayload } from "@/types";

export const fetchAssignmentProcesses = async () => {
  const { data } = await api.get<AssignmentProcess[]>(API_URLS.asignacion.procesos);
  return data;
};

export const processAssignments = async (payload: ProcessAssignmentsPayload) => {
  const { data } = await api.post(API_URLS.asignacion.procesar, payload);
  return data;
};

export const uploadAssignmentList = async (formData: FormData) => {
  const { data } = await api.post(API_URLS.asignacion.procesar, formData, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return data;
};
