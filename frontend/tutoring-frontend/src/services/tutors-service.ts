import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  TutorResponse,
  TutorCreateInput,
  TutorUpdateInput,
  TutorFilter,
  TutorConAlumnos,
} from "@/types";

export interface SearchTutorsParams extends TutorFilter {
  q?: string;
  limit?: number;
}

export const listTutors = async (
  params?: TutorFilter,
): Promise<TutorResponse[]> => {
  const { data } = await api.get<ApiResponse<TutorResponse[]>>(API_URLS.tutores.root, {
    params,
  });
  return data.data;
};

export const searchTutors = async (
  params: SearchTutorsParams,
): Promise<TutorResponse[]> => {
  const { data } = await api.get<ApiResponse<TutorResponse[]>>(API_URLS.tutores.root, {
    params,
  });
  return data.data;
};

export const autocompleteTutors = async (
  query: string,
  limit: number = 8,
): Promise<TutorResponse[]> => {
  const { data } = await api.get<ApiResponse<TutorResponse[]>>(API_URLS.tutores.root, {
    params: {
      q: query,
      limit,
    },
  });
  return data.data;
};

export const getTutor = async (id: number): Promise<TutorResponse> => {
  const { data } = await api.get<ApiResponse<TutorResponse>>(API_URLS.tutores.detail(id));
  return data.data;
};

export const createTutor = async (
  payload: TutorCreateInput,
): Promise<TutorResponse> => {
  const { data } = await api.post<ApiResponse<TutorResponse>>(API_URLS.tutores.root, payload);
  return data.data;
};

export const updateTutor = async (
  id: number,
  payload: TutorUpdateInput,
): Promise<TutorResponse> => {
  const { data } = await api.put<ApiResponse<TutorResponse>>(API_URLS.tutores.detail(id), payload);
  return data.data;
};

export const deleteTutor = async (id: number): Promise<void> => {
  await api.delete<ApiResponse<void>>(API_URLS.tutores.detail(id));
};

export const getTutorWithStudents = async (
  tutorId: number,
  semestreAcademico?: string,
): Promise<TutorConAlumnos> => {
  const { data } = await api.get<ApiResponse<TutorConAlumnos>>(API_URLS.tutores.alumnos(tutorId), {
    params: semestreAcademico ? { semestreAcademico } : undefined,
  });
  return data.data;
};

export const fetchTutors = listTutors;
