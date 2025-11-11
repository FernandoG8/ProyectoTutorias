import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  Tutor,
  TutorPayload,
  TutorReassignmentPayload,
  TutorStudent,
} from "@/types";

export const fetchTutors = async () => {
  const { data } = await api.get<Tutor[]>(API_URLS.tutores.root);
  return data;
};

export const createTutor = async (payload: TutorPayload) => {
  const { data } = await api.post<Tutor>(API_URLS.tutores.root, payload);
  return data;
};

export const updateTutor = async (id: number, payload: TutorPayload) => {
  const { data } = await api.put<Tutor>(API_URLS.tutores.detalle(id), payload);
  return data;
};

export const fetchTutorStudents = async (id: number) => {
  const { data } = await api.get<TutorStudent[]>(API_URLS.tutores.alumnos(id));
  return data;
};

export const reassignTutor = async (payload: TutorReassignmentPayload) => {
  const { data } = await api.post(API_URLS.tutores.reasignar, payload);
  return data;
};
