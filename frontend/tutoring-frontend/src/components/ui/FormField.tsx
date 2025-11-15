import type { ReactNode, InputHTMLAttributes } from "react";
import { forwardRef } from "react";
import type { FieldError } from "react-hook-form";
import { colors } from "@/constants/colors";

/**
 * FormField Component
 *
 * Comprehensive form field wrapper combining:
 * - Label with required indicator
 * - Input with error styling
 * - Error messages
 * - Helper text
 * - Icon support
 *
 * Part of Week 2 reusable components for consistent form styling
 *
 * Decision Log:
 * - Wraps native input with consistent styling
 * - Works with React Hook Form's field registration
 * - Supports helper text for additional context
 * - Error state styling uses semantic color system
 * - Accessible with proper labels and ARIA attributes
 */

interface FormFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: FieldError;
  helperText?: string;
  icon?: ReactNode;
  required?: boolean;
}

export const FormField = forwardRef<HTMLInputElement, FormFieldProps>(
  (
    {
      label,
      error,
      helperText,
      icon,
      required,
      className,
      disabled,
      id,
      ...props
    },
    ref
  ) => {
    const inputId = id || `field-${Math.random()}`;
    const hasError = !!error;

    return (
      <div className="space-y-2">
        {label && (
          <label
            htmlFor={inputId}
            className="text-sm font-semibold"
            style={{ color: colors.semantic.text.primary }}
          >
            {label}
            {required && (
              <span
                className="ml-1"
                style={{ color: colors.danger[400] }}
                aria-label="required"
              >
                *
              </span>
            )}
          </label>
        )}

        <div className="relative">
          {icon && (
            <div
              className="absolute left-3 top-1/2 -translate-y-1/2 flex items-center pointer-events-none"
              style={{ color: colors.semantic.text.muted }}
            >
              {icon}
            </div>
          )}

          <input
            ref={ref}
            id={inputId}
            disabled={disabled}
            className={`w-full rounded-lg border px-3 py-2.5 text-sm transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed ${
              icon ? "pl-10" : ""
            } ${className || ""}`}
            style={{
              borderColor: hasError
                ? colors.danger[300]
                : colors.semantic.border,
              backgroundColor: hasError ? colors.danger[50] : "white",
              color: colors.semantic.text.primary,
            }}
            aria-invalid={hasError}
            aria-describedby={
              hasError || helperText
                ? `${inputId}-description`
                : undefined
            }
            {...props}
          />
        </div>

        {/* Error or Helper Text */}
        {(hasError || helperText) && (
          <p
            id={`${inputId}-description`}
            className="text-xs font-medium"
            style={{
              color: hasError ? colors.danger[600] : colors.semantic.text.muted,
            }}
          >
            {hasError ? error.message : helperText}
          </p>
        )}
      </div>
    );
  }
);

FormField.displayName = "FormField";
