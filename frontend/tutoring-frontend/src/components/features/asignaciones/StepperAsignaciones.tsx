import { useState, useCallback } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Stepper, type StepConfig } from "@/components/common/Stepper/Stepper";
import { StepNavigation } from "@/components/common/Stepper/StepNavigation";
import { Card } from "@/components/ui/Card";
import { useNotification } from "@/hooks/useNotification";
import { validateExcelFile, executeAssignment } from "@/services/asignaciones-service";
import { extractExcelErrors, extractErrorMessage } from "@/lib/api-client";
import type { 
  ExcelValidacionResponse, 
  EjecucionAsignacionResponse, 
  AlumnoValidadoDTO,
  Semestre 
} from "@/types";

// Importar los pasos
import { PasoSeleccion } from "./steps/PasoSeleccion";
import { PasoValidacion } from "./steps/PasoValidacion";
import { PasoRevision } from "./steps/PasoRevision";
import { PasoConfirmacion } from "./steps/PasoConfirmacion";

interface StepperAsignacionesProps {
  semestres: Semestre[];
  onComplete?: () => void;
  onCancel?: () => void;
}

interface WizardState {
  // Paso 1 - Selección
  semestreId: number | null;
  archivo: File | null;
  usuario: string;
  
  // Paso 2 - Validación
  validationResult: ExcelValidacionResponse | null;
  validationErrors: any[];
  
  // Paso 3 - Revisión
  alumnosParaAsignar: AlumnoValidadoDTO[];
  alumnosExcluidos: Set<string>;
  
  // Paso 4 - Confirmación
  executionResult: EjecucionAsignacionResponse | null;
}

const stepConfigs: StepConfig[] = [
  {
    id: "seleccion",
    label: "Selección",
    description: "Archivo y semestre"
  },
  {
    id: "validacion",
    label: "Validación",
    description: "Verificar datos"
  },
  {
    id: "revision",
    label: "Revisión",
    description: "Ajustar asignaciones"
  },
  {
    id: "confirmacion",
    label: "Confirmación",
    description: "Ejecutar proceso"
  }
];

/**
 * Stepper de 4 pasos para el proceso de asignaciones
 * 
 * Flujo:
 * 1. Selección: Semestre + archivo Excel + usuario
 * 2. Validación: POST /api/asignaciones/validar-excel
 * 3. Revisión: Mostrar datos, permitir exclusiones
 * 4. Confirmación: POST /api/asignaciones/ejecutar + resultados
 */
export const StepperAsignaciones = ({ 
  semestres, 
  onComplete, 
  onCancel 
}: StepperAsignacionesProps) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [wizardState, setWizardState] = useState<WizardState>({
    semestreId: null,
    archivo: null,
    usuario: "",
    validationResult: null,
    validationErrors: [],
    alumnosParaAsignar: [],
    alumnosExcluidos: new Set(),
    executionResult: null
  });

  const queryClient = useQueryClient();
  const { success, error: showError, info } = useNotification();

  // Actualizar estado del wizard
  const updateWizardState = useCallback((updates: Partial<WizardState>) => {
    setWizardState(prev => ({ ...prev, ...updates }));
  }, []);

  // Paso 1 -> 2: Validar Excel
  const handleValidateExcel = useCallback(async () => {
    if (!wizardState.archivo || !wizardState.semestreId) {
      showError("Faltan datos requeridos");
      return;
    }

    setIsLoading(true);
    try {
      const result = await validateExcelFile(wizardState.archivo, wizardState.semestreId);
      
      updateWizardState({
        validationResult: result,
        validationErrors: result.errors || [],
        alumnosParaAsignar: result.data || []
      });

      if (result.status === "OK") {
        success("Excel validado correctamente");
        setCurrentStep(2); // Saltar a revisión si no hay errores
      } else {
        info(`Validación completada con ${result.totalErrores} errores`);
        setCurrentStep(1); // Mostrar errores
      }
    } catch (err) {
      const excelErrors = extractExcelErrors(err);
      const message = extractErrorMessage(err) || "Error al validar Excel";

      if (excelErrors) {
        // Construir una respuesta sintética de validación para mostrar en el paso de errores
        const mappedErrors = excelErrors.map((e) => ({
          filaExcel: e.filaExcel,
          campo: e.campo,
          valor: e.valor,
          descripcion: e.descripcion,
          tipoError: e.campo ?? "VALIDACION",
          severidad: "ERROR" as const,
        }));

        const syntheticResult: ExcelValidacionResponse = {
          status: "ERROR",
          message,
          timestamp: new Date().toISOString(),
          totalFilas: mappedErrors.length,
          totalValidas: 0,
          totalErrores: mappedErrors.length,
          porcentajeExito: 0,
          errors: mappedErrors,
          data: null,
          resumen: "Corrige el archivo Excel y vuelve a cargarlo.",
        };

        updateWizardState({
          validationResult: syntheticResult,
          validationErrors: mappedErrors,
          alumnosParaAsignar: [],
        });
        setCurrentStep(1); // Mostrar paso de validación con errores
        showError(message);
      } else {
        showError(message);
      }
    } finally {
      setIsLoading(false);
    }
  }, [wizardState.archivo, wizardState.semestreId, updateWizardState, success, showError, info]);

  // Paso 2 -> 3: Continuar con errores o corregir
  const handleContinueWithErrors = useCallback(() => {
    setCurrentStep(2);
    info("Continuando con datos válidos solamente");
  }, [info]);

  // Paso 3 -> 4: Ejecutar asignación
  const handleExecuteAssignment = useCallback(async () => {
    if (!wizardState.semestreId || !wizardState.alumnosParaAsignar.length) {
      showError("No hay datos para procesar");
      return;
    }

    // Filtrar alumnos excluidos
    const alumnosFinales = wizardState.alumnosParaAsignar.filter(
      alumno => !wizardState.alumnosExcluidos.has(alumno.matricula)
    );

    if (alumnosFinales.length === 0) {
      showError("No hay alumnos para asignar después de las exclusiones");
      return;
    }

    setIsLoading(true);
    try {
      const result = await executeAssignment(wizardState.semestreId, alumnosFinales);
      
      updateWizardState({ executionResult: result });
      setCurrentStep(3);
      
      if (result.status === "OK") {
        success(`Asignación completada: ${result.alumnosAsignados} alumnos asignados`);
      } else if (result.status === "PARTIAL") {
        info(`Asignación parcial: ${result.alumnosAsignados} de ${result.totalAlumnos} alumnos`);
      } else {
        showError(`Error en asignación: ${result.message}`);
      }

      // Invalidar cache de procesos
      queryClient.invalidateQueries({ queryKey: ["assignment-processes"] });
    } catch (err) {
      showError(err instanceof Error ? err.message : "Error al ejecutar asignación");
    } finally {
      setIsLoading(false);
    }
  }, [wizardState.semestreId, wizardState.alumnosParaAsignar, wizardState.alumnosExcluidos, updateWizardState, success, showError, info, queryClient]);

  // Navegación entre pasos
  const handleNext = useCallback(async () => {
    switch (currentStep) {
      case 0:
        await handleValidateExcel();
        break;
      case 1:
        handleContinueWithErrors();
        break;
      case 2:
        await handleExecuteAssignment();
        break;
      default:
        break;
    }
  }, [currentStep, handleValidateExcel, handleContinueWithErrors, handleExecuteAssignment]);

  const handlePrevious = useCallback(() => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  }, [currentStep]);

  const handleConfirm = useCallback(() => {
    onComplete?.();
  }, [onComplete]);

  // Validaciones para habilitar botón siguiente
  const getNextDisabled = () => {
    switch (currentStep) {
      case 0:
        return !wizardState.archivo || !wizardState.semestreId || !wizardState.usuario.trim();
      case 1:
        // Si hay resultado de validación con errores, no permitir avanzar a revisión
        if (!wizardState.validationResult) return true;
        return wizardState.validationResult.status !== "OK";
      case 2:
        return wizardState.alumnosParaAsignar.length === 0;
      default:
        return false;
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 0:
        return (
          <PasoSeleccion
            semestres={semestres}
            semestreId={wizardState.semestreId}
            archivo={wizardState.archivo}
            usuario={wizardState.usuario}
            onSemestreChange={(id) => updateWizardState({ semestreId: id })}
            onArchivoChange={(file) => updateWizardState({ archivo: file })}
            onUsuarioChange={(usuario) => updateWizardState({ usuario })}
          />
        );
      case 1:
        return (
          <PasoValidacion
            validationResult={wizardState.validationResult}
            validationErrors={wizardState.validationErrors}
            onBackToSelection={() => setCurrentStep(0)}
          />
        );
      case 2:
        return (
          <PasoRevision
            alumnos={wizardState.alumnosParaAsignar}
            alumnosExcluidos={wizardState.alumnosExcluidos}
            onToggleExcluir={(matricula) => {
              const newExcluidos = new Set(wizardState.alumnosExcluidos);
              if (newExcluidos.has(matricula)) {
                newExcluidos.delete(matricula);
              } else {
                newExcluidos.add(matricula);
              }
              updateWizardState({ alumnosExcluidos: newExcluidos });
            }}
          />
        );
      case 3:
        return (
          <PasoConfirmacion
            executionResult={wizardState.executionResult}
            totalProcesados={wizardState.alumnosParaAsignar.length - wizardState.alumnosExcluidos.size}
          />
        );
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      {/* Indicador de progreso */}
      <div className="mb-8">
        <Stepper
          steps={stepConfigs}
          currentStep={currentStep}
        />
      </div>

      {/* Contenido del paso actual */}
      <Card className="min-h-[400px]">
        <div className="p-6">
          {renderStepContent()}
        </div>

        {/* Navegación */}
        <div className="px-6 pb-6">
          <StepNavigation
            currentStep={currentStep}
            totalSteps={stepConfigs.length}
            onPrevious={handlePrevious}
            onNext={handleNext}
            onConfirm={handleConfirm}
            onCancel={onCancel}
            nextDisabled={getNextDisabled()}
            isLoading={isLoading}
            nextLabel={currentStep === 0 ? "Validar Excel" : currentStep === 2 ? "Ejecutar Asignación" : "Siguiente"}
            confirmLabel="Finalizar"
          />
        </div>
      </Card>
    </div>
  );
};
