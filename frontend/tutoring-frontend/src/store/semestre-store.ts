import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { Semestre } from "@/types";
import { getActiveSemester } from "@/services/semestres-service";

interface SemestreState {
  semestreActivo: Semestre | null;
  isLoading: boolean;
  error: string | null;
  hasAttemptedFetch: boolean;
  setSemestreActivo: (semestre: Semestre | null) => void;
  fetchSemestreActivo: () => Promise<void>;
  clearError: () => void;
  resetFetchAttempt: () => void;
}

export const useSemestreStore = create<SemestreState>()(
  persist(
    (set) => ({
      semestreActivo: null,
      isLoading: false,
      error: null,
      hasAttemptedFetch: false,
      setSemestreActivo: (semestre) => set({ semestreActivo: semestre, error: null, hasAttemptedFetch: true }),
      fetchSemestreActivo: async () => {
        set({ isLoading: true, error: null });
        try {
          const semestre = await getActiveSemester();
          set({ semestreActivo: semestre, isLoading: false, error: null, hasAttemptedFetch: true });
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : "Error al obtener el semestre activo";
          set({
            semestreActivo: null,
            isLoading: false,
            error: errorMessage,
            hasAttemptedFetch: true
          });
        }
      },
      clearError: () => set({ error: null }),
      resetFetchAttempt: () => set({ hasAttemptedFetch: false, error: null }),
    }),
    {
      name: "semestre-storage",
      partialize: (state) => ({ semestreActivo: state.semestreActivo }),
    }
  )
);

