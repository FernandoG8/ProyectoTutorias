import { create } from "zustand";
import type { UserInfoResponse } from "@/types";

export type Role = "COORDINADOR_TUTORIAS" | "SECRETARIO_ACADEMICO";

interface AuthState {
  user: UserInfoResponse | null;
  setUser: (user: UserInfoResponse | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
  logout: () => set({ user: null }),
}));
