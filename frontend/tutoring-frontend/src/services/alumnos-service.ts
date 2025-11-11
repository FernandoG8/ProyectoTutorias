import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  AlumnoCreateInput,
  AlumnoPagedResponse,
  AlumnoPatchInput,
  AlumnoResponse,
  AlumnoUpdateInput,
} from "@/types";

export interface ListStudentsParams {
  page?: number;
  limit?: number;
  estado?: string;
  carrera?: string;
  semestre?: number;
}

export const listStudents = async (
  params?: ListStudentsParams,
): Promise<AlumnoPagedResponse> => {
  const { data } = await api.get<ApiResponse<AlumnoPagedResponse>>(API_URLS.alumnos.root, {
    params,
  });
  return data.data;
};

export const getStudent = async (id: number): Promise<AlumnoResponse> => {
  const { data } = await api.get<ApiResponse<AlumnoResponse>>(API_URLS.alumnos.detail(id));
  return data.data;
};

export const createStudent = async (
  payload: AlumnoCreateInput,
): Promise<AlumnoResponse> => {
  const { data } = await api.post<ApiResponse<AlumnoResponse>>(API_URLS.alumnos.root, payload);
  return data.data;
};

export const updateStudent = async (
  id: number,
  payload: AlumnoUpdateInput,
): Promise<AlumnoResponse> => {
  const { data } = await api.put<ApiResponse<AlumnoResponse>>(API_URLS.alumnos.detail(id), payload);
  return data.data;
};

export const patchStudent = async (
  id: number,
  payload: AlumnoPatchInput,
): Promise<AlumnoResponse> => {
  const { data } = await api.patch<ApiResponse<AlumnoResponse>>(API_URLS.alumnos.detail(id), payload);
  return data.data;
};

export const deleteStudent = async (id: number): Promise<void> => {
  await api.delete<ApiResponse<void>>(API_URLS.alumnos.detail(id));
};
