import { useState, useCallback } from "react";

interface FieldError {
  message: string;
  type: string;
}

interface FieldState {
  value: string | number | boolean;
  error?: FieldError;
  touched: boolean;
}

/**
 * Custom Hook: useFormField
 *
 * Manages individual form field state with validation
 *
 * Usage:
 * ```tsx
 * const username = useFormField("", (val) => {
 *   if (!val) return { type: "required", message: "Username required" };
 *   if (val.length < 3) return { type: "minLength", message: "Min 3 chars" };
 * });
 *
 * <input
 *   value={username.value}
 *   onChange={username.setValue}
 *   onBlur={username.touch}
 * />
 * {username.error && <span>{username.error.message}</span>}
 * ```
 *
 * Decision Log:
 * - Complements React Hook Form for individual field control
 * - Lightweight alternative to full form library for simple fields
 * - Tracks touched state for better UX
 */
export const useFormField = (
  initialValue: string | number | boolean = "",
  validator?: (value: any) => FieldError | undefined
) => {
  const [state, setState] = useState<FieldState>({
    value: initialValue,
    touched: false,
  });

  const validate = useCallback((value: any): FieldError | undefined => {
    if (validator) {
      return validator(value);
    }
    return undefined;
  }, [validator]);

  const setValue = useCallback(
    (newValue: string | number | boolean | React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
      const value =
        (newValue instanceof Event) || (newValue && typeof newValue === "object" && "target" in newValue)
          ? (newValue as any).target.value
          : newValue;

      setState((prev) => ({
        ...prev,
        value,
        error: validate(value),
      }));
    },
    [validate]
  );

  const touch = useCallback(() => {
    setState((prev) => ({ ...prev, touched: true }));
  }, []);

  const reset = useCallback(() => {
    setState({ value: initialValue, touched: false });
  }, [initialValue]);

  const setError = useCallback((error?: FieldError) => {
    setState((prev) => ({ ...prev, error }));
  }, []);

  return {
    value: state.value,
    error: state.error,
    touched: state.touched,
    setValue,
    touch,
    reset,
    setError,
    isDirty: state.value !== initialValue,
    isValid: !state.error,
    isInvalid: state.touched && !!state.error,
  };
};
