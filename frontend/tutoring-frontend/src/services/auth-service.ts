import { api } from "@/lib/api-client";
import { API_URLS } from "@/lib/api-urls";
import type {
  ApiResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  UserInfoResponse,
} from "@/types";

export const login = async (payload: LoginRequest): Promise<LoginResponse> => {
  const { data } = await api.post<ApiResponse<LoginResponse>>(API_URLS.auth.login, payload);
  return data.data;
};

export const register = async (
  payload: RegisterRequest,
): Promise<RegisterResponse> => {
  const { data } = await api.post<ApiResponse<RegisterResponse>>(API_URLS.auth.register, payload);
  return data.data;
};

export const refreshToken = async (
  payload?: RefreshTokenRequest,
): Promise<RefreshTokenResponse> => {
  const { data } = await api.post<ApiResponse<RefreshTokenResponse>>(API_URLS.auth.refresh, payload);
  return data.data;
};

export const logout = async (payload?: RefreshTokenRequest): Promise<void> => {
  await api.post<ApiResponse<null>>(API_URLS.auth.logout, payload);
};

export const fetchCurrentUser = async (): Promise<UserInfoResponse> => {
  const { data } = await api.get<ApiResponse<UserInfoResponse>>(API_URLS.auth.me);
  return data.data;
};
