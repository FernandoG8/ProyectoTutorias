import { useEffect } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { MainLayout } from "@/components/layout/MainLayout";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { DashboardPage } from "@/pages/DashboardPage";
import { ListUploadPage } from "@/pages/ListUploadPage";
import { AssignmentPage } from "@/pages/AssignmentPage";
import { TutorsPage } from "@/pages/TutorsPage";
import { StudentsPage } from "@/pages/StudentsPage";
import { InactiveStudentsPage } from "@/pages/InactiveStudentsPage";
import { TutorChangePage } from "@/pages/TutorChangePage";
import { ReportsPage } from "@/pages/ReportsPage";
import { SettingsPage } from "@/pages/SettingsPage";
import { LoginPage } from "@/pages/LoginPage";
import { SemestresPage } from "@/pages/SemestresPage";
import { useSemestreStore } from "@/store/semestre-store";

function App() {
  const { fetchSemestreActivo } = useSemestreStore();

  useEffect(() => {
    fetchSemestreActivo();
  }, [fetchSemestreActivo]);

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="students" element={<StudentsPage />} />
        <Route path="list-upload" element={<ListUploadPage />} />
        <Route path="assignment" element={<AssignmentPage />} />
        <Route path="tutors" element={<TutorsPage />} />
        <Route path="inactive-students" element={<InactiveStudentsPage />} />
        <Route path="tutor-change" element={<TutorChangePage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="semestres" element={<SemestresPage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
