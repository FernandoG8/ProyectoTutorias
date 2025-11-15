import { Check } from "lucide-react";
import { colors } from "@/constants/colors";

/**
 * Stepper Component
 *
 * Multi-step wizard component with:
 * - Step indicators with numbers
 * - Completion checkmarks
 * - Progress visualization
 * - Disabled/completed step styling
 * - Custom step labels
 *
 * Part of Week 2 components
 *
 * Usage:
 * ```tsx
 * <Stepper
 *   steps={[
 *     { label: "Carga de archivo", description: "Sube la lista de alumnos" },
 *     { label: "Validación", description: "Verifica los datos" },
 *     { label: "Asignación", description: "Asigna tutores" },
 *   ]}
 *   currentStep={1}
 * />
 * ```
 */

interface Step {
  label: string;
  description?: string;
  disabled?: boolean;
}

interface StepperProps {
  steps: Step[];
  currentStep: number;
  onStepClick?: (stepIndex: number) => void;
  orientation?: "horizontal" | "vertical";
}

export const Stepper = ({
  steps,
  currentStep,
  onStepClick,
  orientation = "horizontal",
}: StepperProps) => {
  return (
    <div className={orientation === "horizontal" ? "w-full" : ""}>
      {orientation === "horizontal" ? (
        // Horizontal Stepper
        <div>
          {/* Step Indicators */}
          <div className="flex items-center justify-between mb-6">
            {steps.map((step, index) => {
              const isCompleted = index < currentStep;
              const isCurrent = index === currentStep;
              const isDisabled = step.disabled;

              return (
                <div key={index} className="flex flex-col items-center flex-1">
                  {/* Step Circle */}
                  <button
                    onClick={() => onStepClick?.(index)}
                    disabled={isDisabled}
                    className={`relative flex h-10 w-10 items-center justify-center rounded-full border-2 font-semibold transition-all mb-2 ${
                      isDisabled ? "cursor-not-allowed opacity-50" : "cursor-pointer"
                    }`}
                    style={{
                      borderColor: isCompleted
                        ? colors.success[400]
                        : isCurrent
                          ? colors.primary[400]
                          : colors.semantic.border,
                      backgroundColor: isCompleted
                        ? colors.success[400]
                        : isCurrent
                          ? colors.primary[50]
                          : "white",
                      color: isCompleted
                        ? "white"
                        : isCurrent
                          ? colors.primary[700]
                          : colors.semantic.text.muted,
                    }}
                  >
                    {isCompleted ? (
                      <Check className="h-5 w-5" />
                    ) : (
                      <span>{index + 1}</span>
                    )}
                  </button>

                  {/* Label */}
                  <p
                    className="text-xs font-medium text-center whitespace-nowrap"
                    style={{
                      color: isCurrent
                        ? colors.semantic.text.primary
                        : colors.semantic.text.secondary,
                    }}
                  >
                    {step.label}
                  </p>
                </div>
              );
            })}
          </div>

          {/* Progress Line */}
          <div className="flex items-center justify-between gap-2 mb-4">
            {steps.map((_, index) => {
              const isCompleted = index < currentStep;
              return (
                <div
                  key={index}
                  className="flex-1 h-1 rounded-full transition-colors"
                  style={{
                    backgroundColor: isCompleted
                      ? colors.success[400]
                      : colors.semantic.border,
                  }}
                />
              );
            })}
          </div>

          {/* Descriptions */}
          <div className="flex items-center justify-between">
            {steps.map((step, index) => (
              <p
                key={index}
                className="text-xs text-center flex-1 px-2"
                style={{ color: colors.semantic.text.muted }}
              >
                {step.description}
              </p>
            ))}
          </div>
        </div>
      ) : (
        // Vertical Stepper
        <div className="space-y-4">
          {steps.map((step, index) => {
            const isCompleted = index < currentStep;
            const isCurrent = index === currentStep;
            const isDisabled = step.disabled;

            return (
              <div key={index} className="flex gap-4">
                {/* Step Indicator */}
                <div className="flex flex-col items-center">
                  <button
                    onClick={() => onStepClick?.(index)}
                    disabled={isDisabled}
                    className="flex h-10 w-10 items-center justify-center rounded-full border-2 font-semibold transition-all mb-2"
                    style={{
                      borderColor: isCompleted
                        ? colors.success[400]
                        : isCurrent
                          ? colors.primary[400]
                          : colors.semantic.border,
                      backgroundColor: isCompleted
                        ? colors.success[400]
                        : isCurrent
                          ? colors.primary[50]
                          : "white",
                      color: isCompleted
                        ? "white"
                        : isCurrent
                          ? colors.primary[700]
                          : colors.semantic.text.muted,
                    }}
                  >
                    {isCompleted ? (
                      <Check className="h-5 w-5" />
                    ) : (
                      <span>{index + 1}</span>
                    )}
                  </button>

                  {/* Vertical Line to Next Step */}
                  {index < steps.length - 1 && (
                    <div
                      className="w-0.5 h-12"
                      style={{
                        backgroundColor: isCompleted
                          ? colors.success[400]
                          : colors.semantic.border,
                      }}
                    />
                  )}
                </div>

                {/* Step Content */}
                <div className="flex-1 pt-1">
                  <p
                    className="font-medium"
                    style={{
                      color: isCurrent
                        ? colors.semantic.text.primary
                        : colors.semantic.text.secondary,
                    }}
                  >
                    {step.label}
                  </p>
                  {step.description && (
                    <p
                      className="text-xs"
                      style={{ color: colors.semantic.text.muted }}
                    >
                      {step.description}
                    </p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
