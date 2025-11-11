import type { RolUsuario } from "./enums";

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  roles: string[];
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshTokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  roles: string[];
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
