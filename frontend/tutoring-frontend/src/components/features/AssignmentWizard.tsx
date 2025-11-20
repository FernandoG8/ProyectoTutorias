import { useState, useCallback, useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Upload, FileCheck, Zap, AlertCircle, CheckCircle, X } from "lucide-react";
import dayjs from "dayjs";

import { Stepper } from "@/components/common/Stepper";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { FormField } from "@/components/ui/FormField";
import { Select } from "@/components/ui/Select";
import { Badge } from "@/components/ui/Badge";
import { colors } from "@/constants/colors";
import { useNotification } from "@/hooks/useNotification";

import {
  validateExcelFile,
  executeAssignment,
  getAssignmentProcessStatus,
} from "@/services/asignaciones-service";
import {
  extractErrorMessage,
  extractExcelErrors,
  extractFieldErrors,
  extractErrorCode,
} from "@/lib/api-client";
import type { EstadoProceso, EstadoProcesoResponse, EjecucionAsignacionResponse, Semestre } from "@/types";

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

/**
 * Formatea el tamaño de archivo en formato legible (KB, MB, etc.)
 */
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return "0 Bytes";
  const k = 1024;
  const sizes = ["Bytes", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
};

const uploadSchema = z.object({
  semestreId: z.string().min(1, "Semestre académico requerido"),
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
  semestres: Semestre[];
  onClose?: () => void;
  onSuccess?: (procesoId: number) => void;
}

export const AssignmentWizard = ({
  semestres,
  onClose,
  onSuccess,
}: AssignmentWizardProps) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [procesoId, setProcesoId] = useState<number | null>(null);
  const [semestreId, setSemestreId] = useState<number | null>(null);
  const [validationData, setValidationData] = useState<any>(null);
  const [validationErrors, setValidationErrors] = useState<any[]>([]);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
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
      semestreId: "",
      usuario: "",
    },
  });

  // Watch file input and update selectedFile state
  useEffect(() => {
    const fileInput = document.getElementById("archivo-input") as HTMLInputElement;
    const handleFileChange = () => {
      if (fileInput.files && fileInput.files.length > 0) {
        setSelectedFile(fileInput.files[0]);
      } else {
        setSelectedFile(null);
      }
    };

    if (fileInput) {
      fileInput.addEventListener("change", handleFileChange);
      return () => fileInput.removeEventListener("change", handleFileChange);
    }
  }, []);

  // Block body scroll when modal is open (wizard is displayed)
  useEffect(() => {
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = "unset";
    };
  }, []);

  // Mutation for validating Excel
  const validationMutation = useMutation({
    mutationFn: async ({ archivo, semId }: { archivo: File; semId: number }) => {
      return validateExcelFile(archivo, semId);
    },
    onSuccess: (result) => {
      // Guardar datos de validación
      setValidationData(result.data || []);
      setValidationErrors(result.errors || []);

      // Si no hay errores, saltar directamente a confirmación
      if (result.status === "OK" && (!result.errors || result.errors.length === 0)) {
        setCurrentStep(2); // Skip validation errors step
        success("Excel validado correctamente sin errores");
      } else {
        setCurrentStep(1); // Show validation errors
        info(`Se encontraron ${result.totalErrores || result.errors?.length || 0} error(es) en la validación`);
      }
    },
    onError: (err) => {
      // Usar nuevos helpers para extraer errores del backend
      const errorCode = extractErrorCode(err);
      const excelErrors = extractExcelErrors(err);
      const fieldErrors = extractFieldErrors(err);
      const errorMessage = extractErrorMessage(err);

      if (errorCode === "EXCEL_VALIDATION_ERROR" && excelErrors && excelErrors.length > 0) {
        // Convertir excelErrors del backend al formato esperado por el componente
        const formattedErrors = excelErrors.map((e) => ({
          filaExcel: e.rowNumber,
          campo: e.column,
          valor: e.value,
          descripcion: e.message,
        }));
        setValidationErrors(formattedErrors);
        setCurrentStep(1);
        info(`Se encontraron ${formattedErrors.length} error(es) en la validación`);
      } else if (errorCode === "VALIDATION_ERROR" && fieldErrors && fieldErrors.length > 0) {
        // Mostrar errores de campos
        showError(`Error de validación: ${fieldErrors.map((f) => f.message).join(", ")}`);
      } else if (errorCode === "DOMAIN_VALIDATION_ERROR") {
        // Mostrar error de dominio (semestre no existe, etc.)
        showError(errorMessage);
      } else if (errorCode === "EXCEL_FORMAT_ERROR") {
        // Error de formato de archivo
        showError(errorMessage);
      } else {
        // Error genérico
        showError(errorMessage || "Error al validar el Excel");
      }
    },
  });

  // Estado para almacenar resultado de ejecución
  const [executionResult, setExecutionResult] = useState<EjecucionAsignacionResponse | null>(null);

  // Mutation for executing assignment
  const executionMutation = useMutation({
    mutationFn: async () => {
      if (!semestreId || !validationData) {
        throw new Error("Datos de validación faltantes");
      }
      return executeAssignment(semestreId, validationData);
    },
    onSuccess: (result) => {
      // Guardar resultado
      setExecutionResult(result);
      // Saltar directamente a resultados (sin polling)
      setCurrentStep(4);
      success("Asignación completada: " + result.message);
      queryClient.invalidateQueries({ queryKey: ["assignment-processes"] });
    },
    onError: (err) => {
      showError(
        err instanceof Error ? err.message : "Error al ejecutar la asignación"
      );
    },
  });

  // Query for process status (kept for backward compatibility with old /iniciar flow)
  const statusQuery = useQuery<EstadoProcesoResponse>({
    queryKey: ["assignment-process-status", procesoId],
    queryFn: () => getAssignmentProcessStatus(procesoId as number),
    enabled: procesoId !== null && currentStep === 3,
    refetchInterval: (query) => {
      const data = query.state.data;
      if (!data) return 3000;
      // Auto-advance to results when complete
      if (
        (data.estado === "COMPLETADO" || data.estado === "FALLIDO") &&
        currentStep === 3
      ) {
        setCurrentStep(4); // Move to final results
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

      // Obtener semestreId del formulario y convertir a número
      const semId = parseInt(values.semestreId, 10);
      if (isNaN(semId) || semId <= 0) {
        showError("Semestre inválido");
        return;
      }
      setSemestreId(semId);

      // STEP 1: Validate Excel first
      await validationMutation.mutateAsync({
        archivo: file,
        semId,
      });
    },
    [validationMutation, showError]
  );

  const handleReset = useCallback(() => {
    setCurrentStep(0);
    setProcesoId(null);
    setSemestreId(null);
    setValidationData(null);
    setValidationErrors([]);
    setSelectedFile(null);
    resetForm();
    validationMutation.reset();
    executionMutation.reset();
  }, [resetForm, validationMutation, executionMutation]);

  const handleExecute = useCallback(async () => {
    // STEP 2: Execute with validated data
    await executionMutation.mutateAsync();
  }, [executionMutation]);

  const handleFinish = useCallback(() => {
    // En el nuevo flujo, el procesoId no viene en la respuesta
    // El historial se refrescará automáticamente desde la página principal
    if (procesoId) {
      onSuccess?.(procesoId);
    }
    onClose?.();
  }, [procesoId, onSuccess, onClose]);

  const handleBackToUpload = useCallback(() => {
    setCurrentStep(0);
    setValidationData(null);
    setValidationErrors([]);
    setSelectedFile(null);
    resetForm();
  }, [resetForm]);

  const stepConfig = [
    {
      label: "Carga de archivo",
      description: "Selecciona el archivo de alumnos",
    },
    {
      label: "Validación",
      description: "Revisa los errores encontrados",
      disabled: true,
    },
    {
      label: "Confirmación",
      description: "Confirma la ejecución",
      disabled: true,
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
                <div className="space-y-2">
                  <label
                    className="text-sm font-semibold"
                    style={{ color: colors.semantic.text.primary }}
                  >
                    Semestre académico
                    <span
                      className="ml-1"
                      style={{ color: colors.danger[400] }}
                      aria-label="required"
                    >
                      *
                    </span>
                  </label>
                  <Select
                    {...register("semestreId")}
                    style={{
                      borderColor: errors.semestreId
                        ? colors.danger[300]
                        : colors.semantic.border,
                      backgroundColor: errors.semestreId ? colors.danger[50] : "white",
                    }}
                  >
                    <option value="">Selecciona un semestre</option>
                    {semestres.map((sem) => (
                      <option key={sem.id} value={sem.id}>
                        {sem.codigo} - {sem.nombre}
                      </option>
                    ))}
                  </Select>
                  {errors.semestreId && (
                    <p className="text-xs font-medium" style={{ color: colors.danger[600] }}>
                      {errors.semestreId.message}
                    </p>
                  )}
                </div>
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
                    className="flex items-center justify-between gap-3 rounded-lg border-2 border-dashed px-6 py-8 cursor-pointer transition-colors hover:bg-gray-50"
                    style={{
                      borderColor: selectedFile ? colors.success[300] : colors.primary[300],
                      backgroundColor: selectedFile ? colors.success[50] : "transparent",
                    }}
                  >
                    <div className="flex items-center gap-3">
                      {selectedFile ? (
                        <FileCheck
                          className="h-5 w-5 flex-shrink-0"
                          style={{ color: colors.success[400] }}
                        />
                      ) : (
                        <Upload
                          className="h-5 w-5 flex-shrink-0"
                          style={{ color: colors.primary[400] }}
                        />
                      )}
                      <div className="text-left">
                        {selectedFile ? (
                          <>
                            <p
                              className="font-medium"
                              style={{ color: colors.success[900] }}
                            >
                              {selectedFile.name}
                            </p>
                            <p
                              className="text-xs"
                              style={{ color: colors.success[700] }}
                            >
                              {formatFileSize(selectedFile.size)}
                            </p>
                          </>
                        ) : (
                          <>
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
                          </>
                        )}
                      </div>
                    </div>
                    {selectedFile && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.preventDefault();
                          setSelectedFile(null);
                          // Reset file input
                          const input = document.getElementById("archivo-input") as HTMLInputElement;
                          if (input) input.value = "";
                        }}
                        className="flex-shrink-0 p-1 rounded hover:bg-gray-200"
                        style={{ color: colors.danger[600] }}
                        title="Quitar archivo"
                      >
                        <X className="h-5 w-5" />
                      </button>
                    )}
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
                  loading={validationMutation.isPending}
                  disabled={!selectedFile || validationMutation.isPending}
                  style={{ backgroundColor: colors.primary[400] }}
                >
                  <Zap className="h-4 w-4 mr-2" />
                  Validar archivo
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

      {/* Step 1 (bis): Validation Errors - Solo si hay errores */}
      {currentStep === 1 && validationErrors.length > 0 && (
        <Card>
          <div className="space-y-6">
            <div className="flex items-start gap-3">
              <AlertCircle
                className="h-6 w-6 flex-shrink-0 mt-1"
                style={{ color: colors.warning[400] }}
              />
              <div>
                <h2
                  className="text-lg font-semibold"
                  style={{ color: colors.semantic.text.primary }}
                >
                  Errores en la validación
                </h2>
                <p
                  className="text-sm mt-1"
                  style={{ color: colors.semantic.text.secondary }}
                >
                  Se encontraron {validationErrors.length} error(es) en el archivo. Revísalos antes de continuar.
                </p>
              </div>
            </div>

            {/* Error Table */}
            <div
              className="rounded-lg border p-4 max-h-96 overflow-y-auto"
              style={{
                borderColor: colors.warning[200],
                backgroundColor: colors.warning[50],
              }}
            >
              <div className="space-y-2">
                {validationErrors.map((error, idx) => (
                  <div
                    key={idx}
                    className="text-sm p-3 rounded border"
                    style={{
                      borderColor: colors.warning[300],
                      backgroundColor: colors.warning[100],
                    }}
                  >
                    <p className="font-medium" style={{ color: colors.warning[900] }}>
                      Fila {error.filaExcel || error.fila || "desconocida"}: {error.campo || "campo desconocido"}
                    </p>
                    <p style={{ color: colors.warning[800] }}>
                      {error.descripcion || error.error || "Error desconocido"}
                    </p>
                    {error.valor && (
                      <p className="text-xs mt-1" style={{ color: colors.warning[700] }}>
                        Valor: {error.valor}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </div>

            {/* Actions */}
            <div className="flex gap-3">
              <Button
                onClick={handleBackToUpload}
                style={{ backgroundColor: colors.primary[400] }}
              >
                Volver a subir archivo
              </Button>
              <Button
                onClick={handleExecute}
                loading={executionMutation.isPending}
                disabled={executionMutation.isPending}
                className="bg-amber-600 hover:bg-amber-700"
              >
                Continuar de todas formas
              </Button>
            </div>
          </div>
        </Card>
      )}

      {/* Step 2: Confirmation - Solo si pasa validación sin errores */}
      {currentStep === 2 && validationData && (
        <Card>
          <div className="space-y-6">
            <div className="flex items-start gap-3">
              <CheckCircle
                className="h-6 w-6 flex-shrink-0 mt-1"
                style={{ color: colors.success[400] }}
              />
              <div>
                <h2
                  className="text-lg font-semibold"
                  style={{ color: colors.semantic.text.primary }}
                >
                  Confirmación de asignación
                </h2>
                <p
                  className="text-sm mt-1"
                  style={{ color: colors.semantic.text.secondary }}
                >
                  Revisa el resumen antes de ejecutar la asignación masiva
                </p>
              </div>
            </div>

            {/* Summary */}
            <div
              className="rounded-lg border p-4 grid grid-cols-3 gap-4"
              style={{ borderColor: colors.semantic.border }}
            >
              <div>
                <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                  Total a asignar
                </p>
                <p className="text-2xl font-bold" style={{ color: colors.primary[600] }}>
                  {validationData?.length || 0}
                </p>
              </div>
              <div>
                <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                  Errores previos
                </p>
                <p className="text-2xl font-bold" style={{ color: colors.warning[600] }}>
                  {validationErrors.length || 0}
                </p>
              </div>
              <div>
                <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                  Semestre
                </p>
                <p className="text-lg font-bold" style={{ color: colors.primary[600] }}>
                  {semestres.find((s) => s.id === semestreId)?.codigo || semestreId || "N/A"}
                </p>
              </div>
            </div>

            {/* Actions */}
            <div className="flex gap-3">
              <Button
                onClick={handleExecute}
                loading={executionMutation.isPending}
                disabled={executionMutation.isPending}
                style={{ backgroundColor: colors.success[600] }}
              >
                <Zap className="h-4 w-4 mr-2" />
                Ejecutar asignación
              </Button>
              <Button
                onClick={handleBackToUpload}
                className="bg-gray-200 hover:bg-gray-300 text-gray-900"
              >
                Volver
              </Button>
            </div>
          </div>
        </Card>
      )}

      {/* Step 3: Processing */}
      {currentStep === 3 && statusQuery.data && (
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

      {/* Step 4: Results */}
      {currentStep === 4 && (executionResult || statusQuery.data) && (() => {
        // Unify both response formats (new /ejecutar and old /iniciar polling)
        const isSuccess =
          executionResult?.status === "OK" || statusQuery.data?.estado === "COMPLETADO";
        const totalProcesados =
          executionResult?.totalAlumnos ?? statusQuery.data?.progreso?.alumnosProcesados ?? 0;
        const asignados =
          executionResult?.alumnosAsignados ?? statusQuery.data?.progreso?.alumnosAsignados ?? 0;
        const errores =
          executionResult?.alumnosConError ?? statusQuery.data?.progreso?.errores ?? 0;

        return (
          <Card>
            <div className="space-y-6">
              <div className="flex items-start gap-3">
                <FileCheck
                  className="h-6 w-6 flex-shrink-0 mt-1"
                  style={{
                    color: isSuccess ? colors.success[400] : colors.danger[400],
                  }}
                />
                <div>
                  <h2
                    className="text-lg font-semibold"
                    style={{ color: colors.semantic.text.primary }}
                  >
                    {isSuccess ? "Asignación completada" : "Procesamiento fallido"}
                  </h2>
                  <p
                    className="text-sm mt-1"
                    style={{ color: colors.semantic.text.secondary }}
                  >
                    {isSuccess
                      ? `Se asignaron ${asignados} estudiantes exitosamente`
                      : `Ocurrieron ${errores} errores durante el procesamiento`}
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
                      {totalProcesados}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                      Asignados
                    </p>
                    <p className="text-2xl font-bold" style={{ color: colors.success[600] }}>
                      {asignados}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs" style={{ color: colors.semantic.text.muted }}>
                      Errores
                    </p>
                    <p className="text-2xl font-bold" style={{ color: colors.danger[600] }}>
                      {errores}
                    </p>
                </div>
              </div>
            </div>

              {/* Error List */}
              {errores > 0 && executionResult?.erroresDetalle && executionResult.erroresDetalle.length > 0 && (
                <div
                  className="rounded-lg border p-4 max-h-64 overflow-y-auto"
                  style={{
                    borderColor: colors.danger[200],
                    backgroundColor: colors.danger[50],
                  }}
                >
                  <p className="text-sm font-medium mb-2" style={{ color: colors.danger[900] }}>
                    {errores === 1 ? "Error encontrado" : "Errores encontrados"}:
                  </p>
                  <div className="space-y-2">
                    {executionResult.erroresDetalle.map((error, idx) => (
                      <div
                        key={idx}
                        className="text-sm p-2 rounded border"
                        style={{
                          borderColor: colors.danger[300],
                          backgroundColor: colors.danger[100],
                        }}
                      >
                        <p className="font-medium" style={{ color: colors.danger[900] }}>
                          {error.matricula ? `Matrícula: ${error.matricula}` : `Alumno ID: ${error.alumnoId || "N/A"}`}
                        </p>
                        <p style={{ color: colors.danger[800] }}>
                          {error.descripcion}
                        </p>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Timestamp */}
              <p
                className="text-xs"
                style={{ color: colors.semantic.text.muted }}
              >
                Fecha de proceso: {dayjs(
                  executionResult?.timestamp || statusQuery.data?.fechaInicio
                ).format("DD/MM/YYYY HH:mm")}
              </p>

              {/* Actions */}
              <div className="flex gap-3 pt-4">
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
        );
      })()}

      {/* Actions solo se muestran si no estamos en el paso de resultados */}
      {currentStep !== 4 && (
        <div className="flex gap-3">
          {onClose && (
            <button
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
      )}
    </div>
  );
};
