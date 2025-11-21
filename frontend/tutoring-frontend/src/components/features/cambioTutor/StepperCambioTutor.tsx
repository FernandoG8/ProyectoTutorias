import { useState, useCallback } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Stepper, type StepConfig } from "@/components/common/Stepper/Stepper";
import { StepNavigation } from "@/components/common/Stepper/StepNavigation";
import { Card } from "@/components/ui/Card";
import { useNotification } from "@/hooks/useNotification";
import { changeTutor } from "@/services/asignaciones-service";
import type { AlumnoResponse, TutorResponse } from "@/types";

// Importar los pasos
import { PasoBusquedaAlumno } from "./steps/PasoBusquedaAlumno";
import { PasoSeleccionTutor } from "./steps/PasoSeleccionTutor";
import { PasoConfirmacionCambio } from "./steps/PasoConfirmacionCambio";

interface StepperCambioTutorProps {
  onComplete?: () => void;
  onCancel?: () => void;
}

interface WizardState {
  // Paso 1 - Búsqueda de alumno
  alumnoSeleccionado: AlumnoResponse | null;
  
  // Paso 2 - Selección de tutor
  tutorNuevo: TutorResponse | null;
  motivo: string;
  
  // Paso 3 - Confirmación
  cambioRealizado: boolean;
  resultadoCambio: any | null;
}

const stepConfigs: StepConfig[] = [
  {
    id: "busqueda",
    label: "Búsqueda",
    description: "Seleccionar alumno"
  },
  {
    id: "tutor",
    label: "Nuevo Tutor",
    description: "Seleccionar tutor"
  },
  {
    id: "confirmacion",
    label: "Confirmación",
    description: "Ejecutar cambio"
  }
];

/**
 * Stepper de 3 pasos para cambio de tutor
 * 
 * Flujo:
 * 1. Búsqueda: Buscar y seleccionar alumno (usando /api/alumnos/search)
 * 2. Selección: Buscar y seleccionar nuevo tutor (usando /api/tutores/search)
 * 3. Confirmación: POST /api/asignaciones/cambio-tutor + resultados
 */
export const StepperCambioTutor = ({ 
  onComplete, 
  onCancel 
}: StepperCambioTutorProps) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [wizardState, setWizardState] = useState<WizardState>({
    alumnoSeleccionado: null,
    tutorNuevo: null,
    motivo: "",
    cambioRealizado: false,
    resultadoCambio: null
  });

  const queryClient = useQueryClient();
  const { success, error: showError } = useNotification();

  // Actualizar estado del wizard
  const updateWizardState = useCallback((updates: Partial<WizardState>) => {
    setWizardState(prev => ({ ...prev, ...updates }));
  }, []);

  // Paso 1 -> 2: Alumno seleccionado
  const handleAlumnoSelected = useCallback((alumno: AlumnoResponse) => {
    updateWizardState({ alumnoSeleccionado: alumno });
    setCurrentStep(1);
  }, [updateWizardState]);


  // Paso 3: Ejecutar cambio de tutor
  const handleExecuteChange = useCallback(async () => {
    if (!wizardState.alumnoSeleccionado || !wizardState.tutorNuevo || !wizardState.motivo.trim()) {
      showError("Faltan datos requeridos para el cambio");
      return;
    }

    setIsLoading(true);
    try {
      const result = await changeTutor({
        alumnoId: wizardState.alumnoSeleccionado.id,
        tutorOrigenId: wizardState.alumnoSeleccionado.tutor!.id,
        tutorDestinoId: wizardState.tutorNuevo.id,
        motivo: wizardState.motivo,
        usuarioResponsable: "coordinador" // TODO: Obtener del contexto de usuario
      });

      updateWizardState({ 
        cambioRealizado: true, 
        resultadoCambio: result 
      });

      success(`Cambio de tutor realizado exitosamente`);

      // Invalidar cache relacionado
      queryClient.invalidateQueries({ queryKey: ["alumnos"] });
      queryClient.invalidateQueries({ queryKey: ["tutores"] });
      queryClient.invalidateQueries({ queryKey: ["assignment-processes"] });

    } catch (err) {
      showError(err instanceof Error ? err.message : "Error al realizar el cambio de tutor");
    } finally {
      setIsLoading(false);
    }
  }, [wizardState.alumnoSeleccionado, wizardState.tutorNuevo, wizardState.motivo, updateWizardState, success, showError, queryClient]);

  // Navegación entre pasos
  const handleNext = useCallback(async () => {
    switch (currentStep) {
      case 0:
        if (!wizardState.alumnoSeleccionado) {
          showError("Selecciona un alumno antes de continuar.");
          return;
        }
        setCurrentStep(1);
        break;
      case 1: {
        const alumno = wizardState.alumnoSeleccionado;
        if (!alumno || !wizardState.tutorNuevo || !wizardState.motivo.trim()) {
          showError("Selecciona un tutor y escribe el motivo del cambio.");
          return;
        }
        // Validar límite de cambios de tutor (máximo 2 en el ciclo escolar)
        if ((alumno.cambiosTutor ?? 0) >= 2) {
          showError("No es posible realizar más cambios de tutor para este alumno en el ciclo actual (límite: 2).");
          return;
        }
        setCurrentStep(2);
        break;
      }
      case 2:
        await handleExecuteChange();
        break;
      default:
        break;
    }
  }, [currentStep, handleExecuteChange]);

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
        return !wizardState.alumnoSeleccionado;
      case 1:
        return (
          !wizardState.tutorNuevo ||
          !wizardState.motivo.trim() ||
          (wizardState.alumnoSeleccionado?.cambiosTutor ?? 0) >= 2
        );
      case 2:
        return !wizardState.cambioRealizado;
      default:
        return false;
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 0:
        return (
          <PasoBusquedaAlumno
            alumnoSeleccionado={wizardState.alumnoSeleccionado}
            onAlumnoSelected={handleAlumnoSelected}
          />
        );
      case 1:
        return (
          <PasoSeleccionTutor
            alumno={wizardState.alumnoSeleccionado!}
            tutorSeleccionado={wizardState.tutorNuevo}
            motivo={wizardState.motivo}
            onTutorSelected={(tutor) => updateWizardState({ tutorNuevo: tutor })}
            onMotivoChanged={(motivo) => updateWizardState({ motivo })}
          />
        );
      case 2:
        return (
          <PasoConfirmacionCambio
            alumno={wizardState.alumnoSeleccionado!}
            tutorAnterior={{
              id: wizardState.alumnoSeleccionado!.tutor!.id,
              nombre: wizardState.alumnoSeleccionado!.tutor!.nombre
            }}
            tutorNuevo={wizardState.tutorNuevo!}
            motivo={wizardState.motivo}
            cambioRealizado={wizardState.cambioRealizado}
            resultadoCambio={wizardState.resultadoCambio}
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
      <Card className="min-h-[500px]">
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
            nextLabel={currentStep === 2 ? "Ejecutar Cambio" : "Siguiente"}
            confirmLabel="Finalizar"
          />
        </div>
      </Card>
    </div>
  );
};
