import { api, extractErrorMessage } from "@/lib/api-client";
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

/**
 * Inicia sesión y establece cookies de autenticación
 * POST /auth/login
 */
export const login = async (payload: LoginRequest): Promise<LoginResponse> => {
  try {
    const { data } = await api.post<ApiResponse<LoginResponse>>(
      API_URLS.auth.login,
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al iniciar sesión: ${extractErrorMessage(error)}`);
  }
};

/**
 * Registra un nuevo usuario (requiere rol COORDINADOR_TUTORIAS)
 * POST /auth/register
 */
export const register = async (
  payload: RegisterRequest,
): Promise<RegisterResponse> => {
  try {
    const { data } = await api.post<ApiResponse<RegisterResponse>>(
      API_URLS.auth.register,
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al registrar usuario: ${extractErrorMessage(error)}`);
  }
};

/**
 * Renueva el token de acceso usando el refresh token
 * POST /auth/refresh
 */
export const refreshToken = async (
  payload?: RefreshTokenRequest,
): Promise<RefreshTokenResponse> => {
  try {
    const { data } = await api.post<ApiResponse<RefreshTokenResponse>>(
      API_URLS.auth.refresh,
      payload,
    );
    return data.data;
  } catch (error) {
    throw new Error(`Error al renovar token: ${extractErrorMessage(error)}`);
  }
};

/**
 * Cierra sesión y limpia las cookies
 * POST /auth/logout
 */
export const logout = async (payload?: RefreshTokenRequest): Promise<void> => {
  try {
    await api.post<ApiResponse<null>>(API_URLS.auth.logout, payload);
  } catch (error) {
    // No lanzar error en logout, solo loguear
    console.error("Error al cerrar sesión:", error);
  }
};

/**
 * Obtiene información del usuario autenticado
 * GET /auth/me
 */
export const fetchCurrentUser = async (): Promise<UserInfoResponse> => {
  try {
    const { data } = await api.get<ApiResponse<UserInfoResponse>>(API_URLS.auth.me);
    return data.data;
  } catch (error) {
    throw new Error(`Error al obtener información del usuario: ${extractErrorMessage(error)}`);
  }
};
