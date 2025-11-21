import { ChevronLeft, ChevronRight, Check } from "lucide-react";
import { Button } from "@/components/ui/Button";

interface StepNavigationProps {
  currentStep: number;
  totalSteps: number;
  onPrevious?: () => void;
  onNext?: () => void;
  onConfirm?: () => void;
  nextDisabled?: boolean;
  confirmDisabled?: boolean;
  isLoading?: boolean;
  nextLabel?: string;
  confirmLabel?: string;
  showCancel?: boolean;
  onCancel?: () => void;
}

/**
 * Componente de navegación para Stepper
 * 
 * Características:
 * - Botones Anterior/Siguiente
 * - Botón de confirmación en el último paso
 * - Estados de carga y deshabilitado
 * - Etiquetas personalizables
 */
const StepNavigation = ({
  currentStep,
  totalSteps,
  onPrevious,
  onNext,
  onConfirm,
  nextDisabled = false,
  confirmDisabled = false,
  isLoading = false,
  nextLabel = "Siguiente",
  confirmLabel = "Confirmar",
  showCancel = true,
  onCancel
}: StepNavigationProps) => {
  const isFirstStep = currentStep === 0;
  const isLastStep = currentStep === totalSteps - 1;

  return (
    <div className="flex items-center justify-between pt-6 border-t border-gray-200">
      {/* Botón Anterior / Cancelar */}
      <div>
        {isFirstStep ? (
          showCancel && onCancel && (
            <Button
              variant="secondary"
              onClick={onCancel}
              disabled={isLoading}
            >
              Cancelar
            </Button>
          )
        ) : (
          <Button
            variant="secondary"
            onClick={onPrevious}
            disabled={isLoading}
            className="flex items-center space-x-2"
          >
            <ChevronLeft className="w-4 h-4" />
            <span>Anterior</span>
          </Button>
        )}
      </div>

      {/* Indicador de progreso */}
      <div className="text-sm text-gray-500">
        Paso {currentStep + 1} de {totalSteps}
      </div>

      {/* Botón Siguiente / Confirmar */}
      <div>
        {isLastStep ? (
          <Button
            onClick={onConfirm}
            disabled={confirmDisabled || isLoading}
            loading={isLoading}
            className="flex items-center space-x-2 bg-green-600 hover:bg-green-700"
          >
            <Check className="w-4 h-4" />
            <span>{confirmLabel}</span>
          </Button>
        ) : (
          <Button
            onClick={onNext}
            disabled={nextDisabled || isLoading}
            loading={isLoading}
            className="flex items-center space-x-2"
          >
            <span>{nextLabel}</span>
            <ChevronRight className="w-4 h-4" />
          </Button>
        )}
      </div>
    </div>
  );
};

export { StepNavigation };
