import { useState } from "react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { WizardStepper } from "./WizardStepper";
import {
  WIZARD_STEPS,
  initialWizardState,
  type WizardState,
  type WizardStep,
} from "./AssignmentWizardSteps";
import { colors } from "@/constants/colors";
import { ChevronLeft, ChevronRight } from "lucide-react";

interface AssignmentWizardContainerProps {
  onComplete?: (state: WizardState) => void;
  onCancel?: () => void;
}

/**
 * AssignmentWizardContainer Component
 *
 * Contenedor del wizard profesional de cambio de tutor
 * Gestiona:
 * - Estado del wizard
 * - Navegación entre pasos
 * - Validaciones
 * - Composición de contenido por paso
 */
export const AssignmentWizardContainer = ({
  onComplete,
  onCancel,
}: AssignmentWizardContainerProps) => {
  const [wizardState, setWizardState] = useState<WizardState>(initialWizardState);

  const currentStepConfig = WIZARD_STEPS.find((s) => s.id === wizardState.step);

  const canGoBack = wizardState.step > 1;
  const canGoForward = wizardState.step < WIZARD_STEPS.length;
  const isCompleted = wizardState.step === WIZARD_STEPS.length;

  const handleNextStep = () => {
    if (wizardState.step < WIZARD_STEPS.length) {
      setWizardState((prev) => ({
        ...prev,
        step: (prev.step + 1) as WizardStep,
      }));
    }
  };

  const handlePrevStep = () => {
    if (wizardState.step > 1) {
      setWizardState((prev) => ({
        ...prev,
        step: (prev.step - 1) as WizardStep,
      }));
    }
  };

  const handleComplete = () => {
    setWizardState((prev) => ({
      ...prev,
      completed: true,
    }));
    onComplete?.(wizardState);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1
          className="text-3xl font-bold"
          style={{ color: colors.semantic.text.primary }}
        >
          Cambio de Tutor
        </h1>
        <p
          className="text-sm mt-1"
          style={{ color: colors.semantic.text.secondary }}
        >
          Reasigna alumnos a diferentes tutores de forma segura y organizada
        </p>
      </div>

      {/* Stepper */}
      <WizardStepper currentStep={wizardState.step} />

      {/* Step Content */}
      <Card className="min-h-[400px]">
        <div className="space-y-6">
          {/* Step Header */}
          <div>
            <h2
              className="text-xl font-semibold"
              style={{ color: colors.semantic.text.primary }}
            >
              {currentStepConfig?.icon} {currentStepConfig?.label}
            </h2>
            <p
              className="text-sm mt-1"
              style={{ color: colors.semantic.text.secondary }}
            >
              {currentStepConfig?.description}
            </p>
          </div>

          {/* Placeholder content */}
          <div
            className="rounded-lg border-2 border-dashed p-8 text-center"
            style={{ borderColor: colors.semantic.border }}
          >
            <p style={{ color: colors.semantic.text.muted }}>
              Contenido del paso {wizardState.step} (será implementado con los pasos específicos)
            </p>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-between pt-6 border-t" style={{ borderColor: colors.semantic.border }}>
            <Button
              variant="ghost"
              onClick={onCancel}
            >
              Cancelar
            </Button>

            <div className="flex gap-2">
              <Button
                variant="secondary"
                disabled={!canGoBack}
                onClick={handlePrevStep}
                icon={<ChevronLeft className="h-4 w-4" />}
              >
                Atrás
              </Button>

              {isCompleted ? (
                <Button
                  variant="primary"
                  onClick={handleComplete}
                >
                  Finalizar
                </Button>
              ) : (
                <Button
                  variant="primary"
                  disabled={!canGoForward}
                  onClick={handleNextStep}
                  icon={<ChevronRight className="h-4 w-4" />}
                  iconPosition="right"
                >
                  Siguiente
                </Button>
              )}
            </div>
          </div>
        </div>
      </Card>

      {/* Debug info (development only) */}
      {typeof window !== "undefined" &&
        (window as any).__DEV__ && (
          <Card className="bg-gray-50">
            <details className="text-xs">
              <summary className="cursor-pointer font-semibold">Estado del Wizard (debug)</summary>
              <pre className="mt-2 overflow-auto text-xs">{JSON.stringify(wizardState, null, 2)}</pre>
            </details>
          </Card>
        )}
    </div>
  );
};
