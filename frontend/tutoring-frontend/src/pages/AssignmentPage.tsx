import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/Skeleton";
import { useNotification } from "@/hooks/useNotification";
import { AssignmentWizard } from "@/components/features/AssignmentWizard";
import { listAssignmentProcesses } from "@/services/asignaciones-service";
import { listSemestres } from "@/services/semestres-service";
import type { AssignmentProcessSummary, Semestre } from "@/types";
import { DataTable } from "@/components/ui/DataTable";
import type { ColumnDef } from "@tanstack/react-table";
import { Badge } from "@/components/ui/Badge";
import dayjs from "dayjs";

/**
 * PÁGINA DE ASIGNACIONES - NUEVA LÓGICA (FASE 4C)
 *
 * Módulo principal de asignaciones usando nueva lógica de 2 fases:
 * 1. POST /validar-excel - Validar sin ejecutar
 * 2. POST /ejecutar - Ejecutar con datos validados
 *
 * Features:
 * - ✅ Wizard de 5 pasos
 * - ✅ Validación explícita antes de ejecutar
 * - ✅ Tabla de errores si hay problemas
 * - ✅ Confirmación antes de ejecución
 * - ✅ Resultados directos (sin polling)
 * - ✅ Historial de procesos
 *
 * DIFERENCIA CON ANTERIOR:
 * Antes: Upload → /iniciar (combinaba validación + ejecución) → Polling
 * Ahora: Upload → /validar-excel → /ejecutar → Resultados directos
 */
export const AssignmentPage = () => {
  const { success } = useNotification();
  const [wizardOpen, setWizardOpen] = useState(false);

  // Obtener lista de semestres para el dropdown del wizard
  const semestresQuery = useQuery<Semestre[]>({
    queryKey: ["semestres"],
    queryFn: () => listSemestres(),
  });

  // Obtener historial de procesos ejecutados
  const processesQuery = useQuery<AssignmentProcessSummary[]>({
    queryKey: ["assignment-processes"],
    queryFn: () => listAssignmentProcesses(),
  });

  // Columnas para tabla de historial
  const columns: ColumnDef<AssignmentProcessSummary>[] = [
    {
      header: "Proceso",
      accessorKey: "id",
      cell: ({ getValue }) => <span className="font-semibold text-text">#{getValue<number>()}</span>,
    },
    {
      header: "Estado",
      accessorKey: "estado",
      cell: ({ getValue }) => {
        const estado = getValue<string>();
        const variant = estado === "COMPLETADO" ? "success" : estado === "FALLIDO" ? "danger" : "info";
        return <Badge variant={variant}>{estado}</Badge>;
      },
    },
    {
      header: "Inicio",
      accessorKey: "fechaInicio",
      cell: ({ getValue }) => dayjs(getValue<string>()).format("DD/MM/YYYY HH:mm"),
    },
    {
      header: "Asignados",
      accessorKey: "totalAsignados",
    },
    {
      header: "Errores",
      accessorKey: "totalErrores",
      cell: ({ getValue }) => {
        const value = getValue<number>();
        return value > 0 ? (
          <span className="font-semibold text-rose-600">{value}</span>
        ) : (
          <span className="text-slate-600">{value}</span>
        );
      },
    },
    {
      header: "Responsable",
      accessorKey: "usuarioEjecutor",
      cell: ({ getValue }) => getValue<string | null>() ?? "-",
    },
  ];

  const processes = processesQuery.data ?? [];

  const handleWizardClose = () => {
    setWizardOpen(false);
    processesQuery.refetch(); // Refrescar historial
  };

  const handleWizardSuccess = (procesoId: number) => {
    success(`Asignación completada: Proceso #${procesoId}`);
    handleWizardClose();
  };

  return (
    <div className="space-y-6">
      {/* Panel principal: Iniciar nuevo proceso */}
      <Card>
        <div className="space-y-4">
          <div>
            <h1 className="text-2xl font-semibold text-text">Gestión de Asignaciones</h1>
            <p className="mt-1 text-sm text-slate-600">
              Nuevo flujo optimizado con validación explícita y mejor control de errores
            </p>
          </div>

          <div className="flex items-center justify-between rounded-lg border border-blue-200 bg-blue-50/50 p-4">
            <div>
              <h3 className="font-medium text-blue-900">Nuevo proceso de asignación</h3>
              <p className="mt-1 text-sm text-blue-700">
                Sube un archivo de alumnos y el sistema validará los datos antes de ejecutar la asignación.
                Verás los errores primero, para que decidas si continuar o corregir.
              </p>
            </div>
            <Button
              onClick={() => setWizardOpen(true)}
              style={{ backgroundColor: "#315762" }}
            >
              Iniciar proceso
            </Button>
          </div>

          {/* Info sobre el nuevo flujo */}
          <div className="grid gap-4 md:grid-cols-3">
            <div className="rounded-lg border border-slate-200 p-3">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-600">
                  1
                </div>
                <span className="font-medium text-text">Validar</span>
              </div>
              <p className="mt-2 text-xs text-slate-600">
                Subir archivo y validar estructura sin ejecutar
              </p>
            </div>
            <div className="rounded-lg border border-slate-200 p-3">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-600">
                  2
                </div>
                <span className="font-medium text-text">Revisar errores</span>
              </div>
              <p className="mt-2 text-xs text-slate-600">
                Si hay problemas, ver tabla detallada antes de continuar
              </p>
            </div>
            <div className="rounded-lg border border-slate-200 p-3">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-600">
                  3
                </div>
                <span className="font-medium text-text">Ejecutar</span>
              </div>
              <p className="mt-2 text-xs text-slate-600">
                Confirmación explícita antes de hacer cambios en la BD
              </p>
            </div>
          </div>
        </div>
      </Card>

      {/* Historial de procesos */}
      <Card>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold text-text">Historial de asignaciones</h2>
              <p className="mt-1 text-sm text-slate-600">Últimos procesos ejecutados</p>
            </div>
            <Button
              type="button"
              variant="secondary"
              onClick={() => processesQuery.refetch()}
              disabled={processesQuery.isFetching}
            >
              Actualizar
            </Button>
          </div>

          {processesQuery.isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-12" />
              <Skeleton className="h-12" />
              <Skeleton className="h-12" />
            </div>
          ) : processes.length === 0 ? (
            <div className="rounded-lg border border-dashed border-slate-300 bg-slate-50/50 py-8 text-center">
              <p className="text-sm text-slate-600">
                No hay procesos registrados aún. Inicia uno nuevo para empezar.
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <DataTable
                columns={columns}
                data={processes}
                isLoading={processesQuery.isFetching && !processesQuery.isLoading}
                emptyMessage="No hay procesos registrados"
              />
            </div>
          )}
        </div>
      </Card>

      {/* Wizard Modal - Nueva lógica con 2 endpoints */}
      {wizardOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
          <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto">
            <AssignmentWizard
              semestres={semestresQuery.data ?? []}
              onClose={handleWizardClose}
              onSuccess={handleWizardSuccess}
            />
          </div>
        </div>
      )}
    </div>
  );
};
