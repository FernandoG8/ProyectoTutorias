import { Check } from "lucide-react";
import { colors } from "@/constants/colors";

export interface StepConfig {
  id: string;
  label: string;
  description?: string;
  disabled?: boolean;
}

interface StepperProps {
  steps: StepConfig[];
  currentStep: number;
  onStepClick?: (stepIndex: number) => void;
  className?: string;
}

/**
 * Componente Stepper/Wizard reutilizable
 * 
 * Características:
 * - Indicador visual de progreso
 * - Navegación entre pasos (opcional)
 * - Estados: completado, activo, pendiente, deshabilitado
 * - Responsive design
 */
export const Stepper = ({ 
  steps, 
  currentStep, 
  onStepClick, 
  className = "" 
}: StepperProps) => {
  const getStepStatus = (stepIndex: number) => {
    if (stepIndex < currentStep) return "completed";
    if (stepIndex === currentStep) return "active";
    if (steps[stepIndex]?.disabled) return "disabled";
    return "pending";
  };

  const getStepStyles = (status: string) => {
    switch (status) {
      case "completed":
        return {
          circle: `bg-green-500 text-white border-green-500`,
          label: "text-green-700 font-medium",
          description: "text-green-600"
        };
      case "active":
        return {
          circle: `bg-blue-500 text-white border-blue-500`,
          label: "text-blue-700 font-medium",
          description: "text-blue-600"
        };
      case "disabled":
        return {
          circle: "bg-gray-300 text-gray-500 border-gray-300",
          label: "text-gray-400",
          description: "text-gray-400"
        };
      default: // pending
        return {
          circle: "bg-white text-gray-500 border-gray-300",
          label: "text-gray-500",
          description: "text-gray-400"
        };
    }
  };

  return (
    <div className={`w-full ${className}`}>
      <nav aria-label="Progress">
        <ol className="flex items-center justify-between">
          {steps.map((step, stepIndex) => {
            const status = getStepStatus(stepIndex);
            const styles = getStepStyles(status);
            const isClickable = onStepClick && !step.disabled && stepIndex <= currentStep;

            return (
              <li key={step.id} className="flex-1 relative">
                <div className="flex flex-col items-center">
                  {/* Círculo del paso */}
                  <button
                    onClick={() => isClickable && onStepClick(stepIndex)}
                    disabled={!isClickable}
                    className={`
                      relative z-10 flex items-center justify-center w-10 h-10 rounded-full border-2 transition-all duration-200
                      ${styles.circle}
                      ${isClickable ? 'hover:scale-105 cursor-pointer' : 'cursor-default'}
                      ${status === 'active' ? 'ring-4 ring-blue-100' : ''}
                    `}
                  >
                    {status === "completed" ? (
                      <Check className="w-5 h-5" />
                    ) : (
                      <span className="text-sm font-semibold">
                        {stepIndex + 1}
                      </span>
                    )}
                  </button>

                  {/* Etiqueta y descripción */}
                  <div className="mt-3 text-center">
                    <p className={`text-sm ${styles.label}`}>
                      {step.label}
                    </p>
                    {step.description && (
                      <p className={`text-xs mt-1 ${styles.description}`}>
                        {step.description}
                      </p>
                    )}
                  </div>
                </div>

                {/* Línea conectora */}
                {stepIndex < steps.length - 1 && (
                  <div 
                    className="absolute top-5 left-1/2 w-full h-0.5 -ml-5 z-0"
                    style={{
                      backgroundColor: stepIndex < currentStep 
                        ? colors.success[500] 
                        : colors.neutral[300]
                    }}
                  />
                )}
              </li>
            );
          })}
        </ol>
      </nav>
    </div>
  );
};
