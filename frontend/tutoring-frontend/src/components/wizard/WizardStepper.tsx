import { colors } from "@/constants/colors";
import { WIZARD_STEPS, type WizardStep } from "./AssignmentWizardSteps";

interface WizardStepperProps {
  currentStep: WizardStep;
  onStepClick?: (step: WizardStep) => void;
}

/**
 * WizardStepper Component
 *
 * Visualización de los pasos del wizard tipo checkout
 * - Paso actual resaltado
 * - Pasos completados marcados
 * - Descripción de cada paso
 * - Indicador visual de progreso
 */
export const WizardStepper = ({
  currentStep,
  onStepClick,
}: WizardStepperProps) => {
  return (
    <div className="space-y-4">
      {/* Progress bar */}
      <div className="h-2 rounded-full overflow-hidden" style={{ backgroundColor: colors.semantic.border }}>
        <div
          className="h-full transition-all duration-300"
          style={{
            width: `${((currentStep - 1) / (WIZARD_STEPS.length - 1)) * 100}%`,
            backgroundColor: colors.primary[600],
          }}
        />
      </div>

      {/* Steps */}
      <div className="grid grid-cols-5 gap-2 md:gap-4">
        {WIZARD_STEPS.map((stepConfig) => {
          const isCompleted = currentStep > stepConfig.id;
          const isCurrent = currentStep === stepConfig.id;
          const isDisabled = currentStep < stepConfig.id;

          return (
            <button
              key={stepConfig.id}
              onClick={() => !isDisabled && onStepClick?.(stepConfig.id)}
              disabled={isDisabled || !onStepClick}
              className={`
                flex flex-col items-center gap-2 p-3 rounded-lg transition-all
                ${isDisabled ? "opacity-50 cursor-not-allowed" : "cursor-pointer hover:bg-gray-50"}
              `}
              style={{
                backgroundColor: isCurrent ? colors.primary[50] : isCompleted ? colors.success[50] : "transparent",
                ...(isCurrent && { outline: `2px solid ${colors.primary[600]}`, outlineOffset: "2px" }),
              }}
            >
              {/* Icon/Badge */}
              <div
                className={`
                  flex items-center justify-center w-8 h-8 rounded-full text-sm font-semibold
                  ${isCompleted || isCurrent ? "text-white" : "text-gray-600"}
                `}
                style={{
                  backgroundColor: isCompleted
                    ? colors.success[600]
                    : isCurrent
                      ? colors.primary[600]
                      : colors.semantic.border,
                }}
              >
                {isCompleted ? "✓" : stepConfig.id}
              </div>

              {/* Label */}
              <div className="text-center min-h-[40px] flex flex-col justify-center">
                <p
                  className="text-xs font-semibold hidden sm:block"
                  style={{
                    color: isCurrent ? colors.primary[600] : colors.semantic.text.primary,
                  }}
                >
                  {stepConfig.label}
                </p>
                <p
                  className="text-xs sm:text-xs hidden sm:block"
                  style={{ color: colors.semantic.text.muted }}
                >
                  {stepConfig.description}
                </p>
                <p className="text-xs sm:hidden">{stepConfig.id}</p>
              </div>
            </button>
          );
        })}
      </div>
    </div>
  );
};
