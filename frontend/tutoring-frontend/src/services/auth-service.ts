import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type { AuthResponse, UserProfile } from "@/types";

interface LoginPayload {
  username: string;
  password: string;
}

export const login = async (payload: LoginPayload) => {
  const { data } = await api.post<AuthResponse>(API_URLS.auth.login, payload);
  return data;
};

export const fetchProfile = async () => {
  const { data } = await api.get<UserProfile>(API_URLS.auth.me);
  return data;
};
