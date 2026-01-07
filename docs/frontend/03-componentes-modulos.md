# Componentes de Módulos

Componentes específicos para cada módulo funcional de la aplicación.

## Módulo Alumnos

### StudentList

Lista de estudiantes con filtros y paginación.

```typescript
import { StudentList } from '@/components/modules/students/StudentList';

export function StudentsPage() {
  return <StudentList />;
}
```

### StudentForm

Formulario para crear/editar estudiante.

```typescript
import { StudentForm } from '@/components/modules/students/StudentForm';

export function EditStudentPage({ id }) {
  return <StudentForm studentId={id} />;
}
```

### StudentDetail

Vista detallada de un estudiante.

```typescript
import { StudentDetail } from '@/components/modules/students/StudentDetail';

export function StudentDetailPage({ id }) {
  return <StudentDetail studentId={id} />;
}
```

## Módulo Tutores

### TutorList

Lista de tutores disponibles.

```typescript
import { TutorList } from '@/components/modules/tutors/TutorList';

export function TutorsPage() {
  return <TutorList />;
}
```

### TutorForm

Formulario para crear/editar tutor.

```typescript
import { TutorForm } from '@/components/modules/tutors/TutorForm';

export function CreateTutorPage() {
  return <TutorForm />;
}
```

## Módulo Asignaciones

### AssignmentWizard

Asistente para asignar tutores.

```typescript
import { AssignmentWizard } from '@/components/modules/assignments/AssignmentWizard';

export function AssignPage() {
  return <AssignmentWizard />;
}
```

### AssignmentList

Listado de asignaciones.

```typescript
import { AssignmentList } from '@/components/modules/assignments/AssignmentList';

export function AssignmentsPage() {
  return <AssignmentList />;
}
```

## Módulo Semestres

### SemesterSelector

Selector de semestre activo.

```typescript
import { SemesterSelector } from '@/components/modules/semesters/SemesterSelector';

export function Dashboard() {
  return (
    <>
      <SemesterSelector />
      {/* Contenido principal */}
    </>
  );
}
```

### SemesterForm

Formulario para crear/editar semestre.

```typescript
import { SemesterForm } from '@/components/modules/semesters/SemesterForm';

export function CreateSemesterPage() {
  return <SemesterForm />;
}
```

## Módulo Reportes

### ReportGenerator

Generador de reportes.

```typescript
import { ReportGenerator } from '@/components/modules/reports/ReportGenerator';

export function ReportsPage() {
  return <ReportGenerator />;
}
```

### ExportButton

Botón para exportar datos.

```typescript
import { ExportButton } from '@/components/modules/reports/ExportButton';

export function StudentList() {
  return (
    <div>
      <ExportButton
        data={students}
        filename="estudiantes.xlsx"
        format="xlsx"
      />
    </div>
  );
}
```

## Módulo Mantenimiento

### MaintenancePanel

Panel de diagnóstico y mantenimiento.

```typescript
import { MaintenancePanel } from '@/components/modules/maintenance/MaintenancePanel';

export function MaintenancePage() {
  return <MaintenancePanel />;
}
```

### DiagnosticStatus

Estado del diagnóstico del sistema.

```typescript
import { DiagnosticStatus } from '@/components/modules/maintenance/DiagnosticStatus';

export function DashboardPage() {
  return <DiagnosticStatus />;
}
```

---

**Ver también**: [Componentes Comunes](./01-componentes-comunes.md) | [Componentes UI](./02-componentes-ui.md)
