import type { RolUsuario } from "./enums";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  mensaje: string;
}

export interface RefreshTokenRequest {
  refreshToken?: string;
}

export interface RefreshTokenResponse {
  mensaje: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  role: RolUsuario;
}

export interface RegisterResponse {
  id: number;
  username: string;
  roles: string[];
}

export interface UserInfoResponse {
  id: number;
  username: string;
  roles: string[];
}
