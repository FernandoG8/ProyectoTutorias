import { useState, useCallback } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Upload, FileCheck, Zap } from "lucide-react";
import dayjs from "dayjs";

import { Stepper } from "@/components/common/Stepper";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { FormField } from "@/components/ui/FormField";
import { Badge } from "@/components/ui/Badge";
import { colors } from "@/constants/colors";
import { useNotification } from "@/hooks/useNotification";

import {
  startAssignmentProcess,
  getAssignmentProcessStatus,
} from "@/services/asignaciones-service";
import type { EstadoProceso, EstadoProcesoResponse } from "@/types";

/**
 * Assignment Wizard Component
 *
 * Unified wizard combining ListUploadPage + AssignmentPage flows:
 *
 * Step 1: File Upload
 * - Semester selection
 * - File upload (CSV/Excel)
 * - User responsibility
 *
 * Step 2: Processing
 * - Real-time progress polling
 * - Status visualization
 * - Auto-advance on completion
 *
 * Step 3: Results
 * - Summary of assigned students
 * - Error handling
 * - Actions (retry, back, finish)
 *
 * Part of Week 2 unified flows
 *
 * Decision Log:
 * - Single wizard replaces two separate pages
 * - Real-time polling for process status
 * - Step validation before progression
 * - Notification integration for feedback
 * - Responsive design for mobile/tablet
 */

const uploadSchema = z.object({
  semestreAcademico: z
    .string()
    .min(1, "Semestre académico requerido")
    .regex(/\d{4}-[12]/, "Usa el formato YYYY-1 o YYYY-2"),
  usuario: z.string().min(1, "Usuario responsable requerido"),
  archivo: z
    .custom<FileList>(
      (file) => file instanceof FileList && file.length > 0,
      "Selecciona un archivo"
    )
    .refine(
      (files) => files?.item(0) instanceof File,
      "Archivo inválido"
    )
    .refine(
      (files) => {
        const file = files?.item(0);
        if (!file) return false;
        const allowedMimes = [
          "text/csv",
          "application/vnd.ms-excel",
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        ];
        const allowedExtensions = [".csv", ".xls", ".xlsx"];
        const ext = file.name.substring(file.name.lastIndexOf(".")).toLowerCase();
        return allowedMimes.includes(file.type) || allowedExtensions.includes(ext);
      },
      "Solo se permiten archivos CSV y Excel (.csv, .xls, .xlsx)"
    ),
});

type UploadFormValues = z.infer<typeof uploadSchema>;

const estadoLabels: Record<EstadoProceso, { label: string; badge: "info" | "warning" | "success" | "danger" }> = {
  INICIADO: { label: "Iniciado", badge: "info" },
  COMPARANDO: { label: "Comparando datos", badge: "info" },
  LIBERANDO_CUPOS: { label: "Liberando cupos", badge: "warning" },
  ASIGNANDO: { label: "Asignando tutores", badge: "info" },
  COMPLETADO: { label: "Completado", badge: "success" },
  FALLIDO: { label: "Error", badge: "danger" },
};

interface AssignmentWizardProps {
  onClose?: () => void;
  onSuccess?: (procesoId: number) => void;
}

export const AssignmentWizard = ({
  onClose,
  onSuccess,
}: AssignmentWizardProps) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [procesoId, setProcesoId] = useState<number | null>(null);
  const queryClient = useQueryClient();
  const { success, error: showError, info } = useNotification();

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset: resetForm,
  } = useForm<UploadFormValues>({
    resolver: zodResolver(uploadSchema),
    defaultValues: {
      semestreAcademico: "",
      usuario: "",
    },
  });

  // Mutation for starting process
  const startMutation = useMutation({
    mutationFn: startAssignmentProcess,
    onSuccess: (result) => {
      if (result.procesoId) {
        setProcesoId(result.procesoId);
        setCurrentStep(1); // Move to processing step
        success("Proceso de asignación iniciado");
        queryClient.invalidateQueries({ queryKey: ["assignment-processes"] });
      }
    },
    onError: (err) => {
      showError(
        err instanceof Error ? err.message : "Error al iniciar el proceso"
      );
    },
  });

  // Query for process status
  const statusQuery = useQuery<EstadoProcesoResponse>({
    queryKey: ["assignment-process-status", procesoId],
    queryFn: () => getAssignmentProcessStatus(procesoId as number),
    enabled: procesoId !== null && currentStep === 1,
    refetchInterval: (query) => {
      const data = query.state.data;
      if (!data) return 3000;
      // Auto-advance to results when complete
      if (
        (data.estado === "COMPLETADO" || data.estado === "FALLIDO") &&
        currentStep === 1
      ) {
        setCurrentStep(2);
        if (data.estado === "COMPLETADO") {
          info("Asignación completada exitosamente");
        }
        return false;
      }
      return 3000;
    },
  });

  const onUploadSubmit = useCallback(
    async (values: UploadFormValues) => {
      const file = values.archivo.item(0);
      if (!file) return;

      await startMutation.mutateAsync({
        archivo: file,
        semestreAcademico: values.semestreAcademico,
        usuario: values.usuario,
      });
    },
    [startMutation]
  );

  const handleReset = useCallback(() => {
    setCurrentStep(0);
    setProcesoId(null);
    resetForm();
    startMutation.reset();
  }, [resetForm, startMutation]);

  const handleFinish = useCallback(() => {
    if (procesoId) {
      onSuccess?.(procesoId);
    }
    onClose?.();
  }, [procesoId, onSuccess, onClose]);

  const stepConfig = [
    {
      label: "Carga de archivo",
      description: "Selecciona el archivo de alumnos",
    },
    {
      label: "Procesando",
      description: "Asignando tutores",
      disabled: true,
    },
    {
      label: "Resultados",
      description: "Revisa el resultado",
      disabled: true,
    },
  ];

  return (
    <div className="space-y-6">
      {/* Stepper */}
      <Stepper steps={stepConfig} currentStep={currentStep} />

      {/* Step 1: Upload */}
      {currentStep === 0 && (
        <Card>
          <div className="space-y-6">
            <div>
              <h2
                className="text-lg font-semibold mb-2"
                style={{ color: colors.semantic.text.primary }}
              >
                Cargar lista de alumnos
              </h2>
              <p
                className="text-sm"
                style={{ color: colors.semantic.text.secondary }}
              >
                Proporciona los datos necesarios y sube el archivo con la lista de alumnos
              </p>
            </div>

            <form className="space-y-4" onSubmit={handleSubmit(onUploadSubmit)}>
              <div className="grid gap-4 md:grid-cols-2">
                <FormField
                  label="Semestre académico"
                  placeholder="2025-1"
                  error={errors.semestreAcademico}
                  required
                  {...register("semestreAcademico")}
                />
                <FormField
                  label="Usuario responsable"
                  placeholder="coord_tutorias"
                  error={errors.usuario}
                  required
                  {...register("usuario")}
                />
              </div>

              <div>
                <label
                  className="text-sm font-semibold"
                  style={{ color: colors.semantic.text.primary }}
                >
                  Archivo CSV/Excel *
                </label>
                <div className="relative mt-2">
                  <input
                    type="file"
                    accept=".csv,.xlsx,.xls"
                    className="sr-only"
                    id="archivo-input"
                    {...register("archivo")}
                  />
                  <label
                    htmlFor="archivo-input"
                    className="flex items-center justify-center gap-3 rounded-lg border-2 border-dashed px-6 py-8 cursor-pointer transition-colors hover:bg-gray-50"
                    style={{ borderColor: colors.primary[300] }}
                  >
                    <Upload
                      className="h-5 w-5"
                      style={{ color: colors.primary[400] }}
                    />
                    <div className="text-center">
                      <p
                        className="font-medium"
                        style={{ color: colors.semantic.text.primary }}
                      >
                        Arrastra o haz clic para seleccionar
                      </p>
                      <p
                        className="text-xs"
                        style={{ color: colors.semantic.text.muted }}
                      >
                        CSV o Excel (máx. 10 MB)
                      </p>
                    </div>
                  </label>
                </div>
                {errors.archivo && (
                  <p className="text-xs mt-2" style={{ color: colors.danger[600] }}>
                    {errors.archivo.message}
                  </p>
                )}
              </div>

              <div className="flex gap-3">
                <Button
                  type="submit"
                  loading={startMutation.isPending}
                  disabled={startMutation.isPending}
                  style={{ backgroundColor: colors.primary[400] }}
                >
                  <Zap className="h-4 w-4 mr-2" />
                  Iniciar asignación
                </Button>
                {onClose && (
                  <button
                    type="button"
                    onClick={onClose}
                    className="px-4 py-2 rounded-lg border transition-colors hover:bg-gray-50"
                    style={{
                      borderColor: colors.semantic.border,
                      color: colors.semantic.text.primary,
                    }}
                  >
                    Cancelar
                  </button>
                )}
              </div>
            </form>

            {/* Recommendations */}
            <div
              className="rounded-lg border p-4"
              style={{ borderColor: colors.info[200], backgroundColor: colors.info[50] }}
            >
              <p
                className="text-sm font-medium mb-2"
                style={{ color: colors.info[900] }}
              >
                Recomendaciones:
              </p>
              <ul className="text-sm space-y-1" style={{ color: colors.info[800] }}>
                <li>• Usa las plantillas institucionales para garantizar el orden de columnas</li>
                <li>• El tamaño máximo del archivo es de 10 MB</li>
                <li>• Verifica que el período académico sea correcto</li>
              </ul>
            </div>
          </div>
        </Card>
      )}

      {/* Step 2: Processing */}
      {currentStep === 1 && statusQuery.data && (
        <Card>
          <div className="space-y-6">
            <div>
              <h2
                className="text-lg font-semibold mb-2"
                style={{ color: colors.semantic.text.primary }}
              >
                Procesando asignación
              </h2>
              <p
                className="text-sm"
                style={{ color: colors.semantic.text.secondary }}
              >
                El sistema está procesando la lista. Este proceso puede tomar unos minutos.
              </p>
            </div>

            {/* Status */}
            <div
              className="rounded-lg border p-4 space-y-4"
              style={{ borderColor: colors.semantic.border }}
            >
              <div className="space-y-2">
                <p
                  className="text-sm font-medium"
                  style={{ color: colors.semantic.text.secondary }}
                >
                  Estado actual:
                </p>
                <div className="flex items-center gap-2">
                  {statusQuery.data && (
                    <Badge variant={estadoLabels[statusQuery.data.estado]?.badge ?? "info"}>
                      {estadoLabels[statusQuery.data.estado]?.label ?? "Estado desconocido"}
                    </Badge>
                  )}
                </div>
              </div>

              {/* Progress Indicator */}
              <div className="space-y-2">
                <p
                  className="text-xs"
                  style={{ color: colors.semantic.text.muted }}
                >
                  Progreso: {statusQuery.data?.progreso?.porcentaje ?? 0}%
                </p>
                <div className="w-full h-2 rounded-full overflow-hidden" style={{ backgroundColor: colors.semantic.border }}>
                  <div
                    className="h-full rounded-full transition-all duration-300"
                    style={{
                      width: `${statusQuery.data?.progreso?.porcentaje ?? 0}%`,
                      backgroundColor: colors.primary[400],
                    }}
                  />
                </div>
              </div>

              {/* Details */}
              <div className="grid grid-cols-3 gap-4 pt-2">
                <div>
                  <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                    Asignados
                  </p>
                  <p
                    className="text-lg font-semibold"
                    style={{ color: colors.success[600] }}
                  >
                    {statusQuery.data?.progreso?.alumnosAsignados ?? 0}
                  </p>
                </div>
                <div>
                  <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                    Procesados
                  </p>
                  <p
                    className="text-lg font-semibold"
                    style={{ color: colors.primary[600] }}
                  >
                    {statusQuery.data?.progreso?.alumnosProcesados ?? 0}
                  </p>
                </div>
                <div>
                  <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                    Errores
                  </p>
                  <p
                    className="text-lg font-semibold"
                    style={{ color: colors.danger[600] }}
                  >
                    {statusQuery.data?.progreso?.errores ?? 0}
                  </p>
                </div>
              </div>
            </div>

            {statusQuery.isLoading && (
              <div className="flex justify-center py-4">
                <div
                  className="inline-block h-5 w-5 animate-spin rounded-full border-2 border-current border-r-transparent"
                  style={{ borderColor: colors.primary[400] }}
                />
              </div>
            )}
          </div>
        </Card>
      )}

      {/* Step 3: Results */}
      {currentStep === 2 && statusQuery.data && (
        <Card>
          <div className="space-y-6">
            <div className="flex items-start gap-3">
              <FileCheck
                className="h-6 w-6 flex-shrink-0 mt-1"
                style={{
                  color:
                    statusQuery.data.estado === "COMPLETADO"
                      ? colors.success[400]
                      : colors.danger[400],
                }}
              />
              <div>
                <h2
                  className="text-lg font-semibold"
                  style={{ color: colors.semantic.text.primary }}
                >
                  {statusQuery.data.estado === "COMPLETADO"
                    ? "Asignación completada"
                    : "Procesamiento fallido"}
                </h2>
                <p
                  className="text-sm mt-1"
                  style={{ color: colors.semantic.text.secondary }}
                >
                  {statusQuery.data.estado === "COMPLETADO"
                    ? `Se asignaron ${statusQuery.data?.progreso?.alumnosAsignados ?? 0} estudiantes exitosamente`
                    : `Ocurrieron ${statusQuery.data?.progreso?.errores ?? 0} errores durante el procesamiento`}
                </p>
              </div>
            </div>

            {/* Summary */}
            <div
              className="rounded-lg border p-4"
              style={{ borderColor: colors.semantic.border }}
            >
              <div className="grid grid-cols-3 gap-4">
                <div>
                  <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                    Total Procesados
                  </p>
                  <p className="text-2xl font-bold" style={{ color: colors.primary[600] }}>
                    {statusQuery.data?.progreso?.alumnosProcesados ?? 0}
                  </p>
                </div>
                <div>
                  <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                    Asignados
                  </p>
                  <p className="text-2xl font-bold" style={{ color: colors.success[600] }}>
                    {statusQuery.data?.progreso?.alumnosAsignados ?? 0}
                  </p>
                </div>
                <div>
                  <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                    Errores
                  </p>
                  <p className="text-2xl font-bold" style={{ color: colors.danger[600] }}>
                    {statusQuery.data?.progreso?.errores ?? 0}
                  </p>
                </div>
              </div>
            </div>

            {/* Error List */}
            {(statusQuery.data?.progreso?.errores ?? 0) > 0 && (
              <div
                className="rounded-lg border p-4 max-h-64 overflow-y-auto"
                style={{
                  borderColor: colors.danger[200],
                  backgroundColor: colors.danger[50],
                }}
              >
                <p className="text-sm font-medium" style={{ color: colors.danger[900] }}>
                  {(statusQuery.data?.progreso?.errores ?? 0) === 1 ? "Error encontrado" : "Errores encontrados"}:
                </p>
                <p className="mt-2 text-xs" style={{ color: colors.danger[800] }}>
                  Se registraron {statusQuery.data?.progreso?.errores ?? 0} error{(statusQuery.data?.progreso?.errores ?? 0) !== 1 ? "es" : ""} durante el procesamiento.
                </p>
              </div>
            )}

            {/* Timestamp */}
            <p
              className="text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              Fecha de proceso: {dayjs(statusQuery.data?.fechaInicio).format("DD/MM/YYYY HH:mm")}
            </p>

            {/* Actions */}
            <div className="flex gap-3">
              <Button
                onClick={handleFinish}
                style={{ backgroundColor: colors.primary[400] }}
              >
                Finalizar
              </Button>
              <button
                onClick={handleReset}
                className="px-4 py-2 rounded-lg border transition-colors hover:bg-gray-50"
                style={{
                  borderColor: colors.semantic.border,
                  color: colors.semantic.text.primary,
                }}
              >
                Procesar otro archivo
              </button>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
};
