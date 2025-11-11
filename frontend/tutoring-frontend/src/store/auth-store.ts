import { create } from "zustand";

export type Role = "COORDINADOR_TUTORIAS" | "SECRETARIO_ACADEMICO";

interface AuthState {
  token: string | null;
  role: Role | null;
  setAuth: (token: string, role: Role | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  role: null,
  setAuth: (token, role) => set({ token, role }),
  logout: () => set({ token: null, role: null }),
}));
