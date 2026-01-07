# Servicio de Reportes

Cliente para interactuar con los endpoints de reportes y exportación de datos.

## Ubicación

`src/services/reports-service.ts`

## Métodos Disponibles

### getReportAlumnos()

Obtiene reporte de alumnos.

```typescript
import { reportService } from '@/services/reports-service';

const report = await reportService.getReportAlumnos();
// Retorna: ReporteAlumnosDTO
```

### getReportTutores()

Obtiene reporte de tutores.

```typescript
const report = await reportService.getReportTutores();
// Retorna: ReporteTutoresDTO
```

### getReportAsignaciones()

Obtiene reporte de asignaciones.

```typescript
const report = await reportService.getReportAsignaciones();
// Retorna: ReporteAsignacionesDTO
```

### exportToExcel(data, filename)

Exporta datos a Excel.

```typescript
const excelBlob = await reportService.exportToExcel(
  alumnos,
  'estudiantes.xlsx'
);
// Retorna: Blob
```

### exportToPDF(data, options)

Exporta datos a PDF.

```typescript
const pdfBlob = await reportService.exportToPDF(
  alumnos,
  { title: 'Reporte de Estudiantes' }
);
// Retorna: Blob
```

### downloadFile(blob, filename)

Descarga un archivo (Excel, PDF, etc).

```typescript
const file = await reportService.exportToExcel(data, 'reporte.xlsx');
reportService.downloadFile(file, 'reporte.xlsx');
```

## Uso en Componentes

### Exportar a Excel

```typescript
import { Button } from '@/components/ui/Button';
import { reportService } from '@/services/reports-service';
import { useQuery } from '@tanstack/react-query';
import { alumnService } from '@/services/alumnos-service';

export function ExportStudentsButton() {
  const { data: alumnos } = useQuery({
    queryKey: ['alumnos'],
    queryFn: () => alumnService.getAll()
  });

  const handleExport = async () => {
    if (!alumnos) return;

    const excelBlob = await reportService.exportToExcel(
      alumnos,
      'estudiantes.xlsx'
    );
    reportService.downloadFile(excelBlob, 'estudiantes.xlsx');
  };

  return (
    <Button onClick={handleExport}>
      Descargar Excel
    </Button>
  );
}
```

### Ver Reporte

```typescript
import { useQuery } from '@tanstack/react-query';
import { reportService } from '@/services/reports-service';
import { Card } from '@/components/ui/Card';

export function ReportDashboard() {
  const { data: report, isLoading } = useQuery({
    queryKey: ['reportes', 'alumnos'],
    queryFn: () => reportService.getReportAlumnos()
  });

  if (isLoading) return <div>Cargando reporte...</div>;

  return (
    <div className="grid grid-cols-4 gap-4">
      <Card>
        <h3>Total Alumnos</h3>
        <p className="text-2xl">{report?.totalAlumnos}</p>
      </Card>
      <Card>
        <h3>Alumnos Activos</h3>
        <p className="text-2xl">{report?.alumnosActivos}</p>
      </Card>
      <Card>
        <h3>Con Tutor</h3>
        <p className="text-2xl">{report?.conTutor}</p>
      </Card>
      <Card>
        <h3>Sin Tutor</h3>
        <p className="text-2xl">{report?.sinTutor}</p>
      </Card>
    </div>
  );
}
```

## Tipos de Datos

```typescript
interface ReporteAlumnosDTO {
  totalAlumnos: number;
  alumnosActivos: number;
  alumnosInactivos: number;
  conTutor: number;
  sinTutor: number;
  porCarrera: Record<string, number>;
  porSemestre: Record<string, number>;
  tasaRendimiento: number;
  fechaGeneracion: Date;
}

interface ReporteTutoresDTO {
  totalTutores: number;
  tutoresActivos: number;
  tutoresInactivos: number;
  alumnosAsignados: number;
  capacidadPromedio: number;
  saturation: number;
  especialidades: string[];
  cargaPromedio: number;
  fechaGeneracion: Date;
}

interface ReporteAsignacionesDTO {
  totalAsignaciones: number;
  asignacionesActivas: number;
  asignacionesCompletadas: number;
  asignacionesCanceladas: number;
  tiespoPromedio: number;
  efectividad: number;
  porMotivo: Record<string, number>;
  fechaGeneracion: Date;
}
```

---

**Ver también**: [Servicios Alumnos](./04-servicios-alumnos.md) | [Servicios Asignaciones](./06-servicios-asignaciones.md)
