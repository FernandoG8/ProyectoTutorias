import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { Semestre } from "@/types";
import { getActiveSemester } from "@/services/semestres-service";

interface SemestreState {
  semestreActivo: Semestre | null;
  isLoading: boolean;
  error: string | null;
  setSemestreActivo: (semestre: Semestre | null) => void;
  fetchSemestreActivo: () => Promise<void>;
  clearError: () => void;
}

export const useSemestreStore = create<SemestreState>()(
  persist(
    (set) => ({
      semestreActivo: null,
      isLoading: false,
      error: null,
      setSemestreActivo: (semestre) => set({ semestreActivo: semestre, error: null }),
      fetchSemestreActivo: async () => {
        set({ isLoading: true, error: null });
        try {
          const semestre = await getActiveSemester();
          set({ semestreActivo: semestre, isLoading: false, error: null });
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : "Error al obtener el semestre activo";
          set({ 
            semestreActivo: null, 
            isLoading: false, 
            error: errorMessage 
          });
        }
      },
      clearError: () => set({ error: null }),
    }),
    {
      name: "semestre-storage",
      partialize: (state) => ({ semestreActivo: state.semestreActivo }),
    }
  )
);

