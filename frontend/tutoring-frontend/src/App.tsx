import { useEffect } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { DashboardLayout } from "@/components/layout/DashboardLayout";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { DashboardPage } from "@/pages/DashboardPage";
import { AssignmentPage } from "@/pages/AssignmentPage";
import { TutorsPage } from "@/pages/TutorsPage";
import { StudentsPage } from "@/pages/StudentsPage";
import { InactiveStudentsPage } from "@/pages/InactiveStudentsPage";
import { TutorChangePage } from "@/pages/TutorChangePage";
import { ReportsPage } from "@/pages/ReportsPage";
import { LoginPage } from "@/pages/LoginPage";
import { SemestresPage } from "@/pages/SemestresPage";
import { useSemestreStore } from "@/store/semestre-store";
import { useAuthStore } from "@/store/auth-store";

function App() {
  const { fetchSemestreActivo } = useSemestreStore();
  const user = useAuthStore((state) => state.user);

  useEffect(() => {
    // Solo intentamos cargar el semestre activo cuando hay sesión iniciada
    if (!user) return;

    fetchSemestreActivo().catch((error) => {
      console.error("Error al cargar semestre activo:", error);
      // El error ya está guardado en el store (semestre-store.ts:30-37)
      // Los componentes pueden acceder a useSemestreStore().error para mostrarlo
    });
  }, [fetchSemestreActivo, user]);

  return (
    <>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <DashboardLayout>
                <Routes>
                  <Route index element={<Navigate to="/dashboard" replace />} />
                  <Route path="dashboard" element={<DashboardPage />} />
                  <Route path="alumnos" element={<StudentsPage />} />
                  <Route path="asignaciones" element={<AssignmentPage />} />
                  <Route path="tutores" element={<TutorsPage />} />
                  <Route path="alumnos-inactivos" element={<InactiveStudentsPage />} />
                  <Route path="cambio-tutor" element={<TutorChangePage />} />
                  <Route path="reportes" element={<ReportsPage />} />
                  <Route path="semestres" element={<SemestresPage />} />
                  <Route path="*" element={<Navigate to="/dashboard" replace />} />
                </Routes>
              </DashboardLayout>
            </ProtectedRoute>
          }
        />
      </Routes>
      <ToastContainer
        position="bottom-right"
        autoClose={3000}
        hideProgressBar={false}
        newestOnTop={true}
        closeOnClick
        rtl={false}
        pauseOnFocusLoss
        draggable
        pauseOnHover
        theme="light"
      />
    </>
  );
}

export default App;
