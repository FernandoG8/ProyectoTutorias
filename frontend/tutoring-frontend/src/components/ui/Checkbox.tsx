import { forwardRef } from "react";
import { Check, Minus } from "lucide-react";

interface CheckboxProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'> {
  indeterminate?: boolean;
  label?: string;
}

/**
 * Componente Checkbox personalizado
 * 
 * Características:
 * - Estados: checked, unchecked, indeterminate
 * - Accesible con ARIA
 * - Estilos consistentes con el design system
 */
export const Checkbox = forwardRef<HTMLInputElement, CheckboxProps>(
  ({ className = "", indeterminate = false, label, id, ...props }, ref) => {
    const checkboxId = id || `checkbox-${Math.random().toString(36).substr(2, 9)}`;

    return (
      <div className="flex items-center space-x-2">
        <div className="relative">
          <input
            ref={ref}
            type="checkbox"
            id={checkboxId}
            className="sr-only"
            {...props}
          />
          <label
            htmlFor={checkboxId}
            className={`
              flex items-center justify-center w-4 h-4 border-2 rounded cursor-pointer transition-all duration-200
              ${props.checked || indeterminate
                ? 'bg-blue-600 border-blue-600 text-white'
                : 'bg-white border-gray-300 hover:border-blue-400'
              }
              ${props.disabled ? 'opacity-50 cursor-not-allowed' : ''}
              ${className}
            `}
          >
            {indeterminate ? (
              <Minus className="w-3 h-3" />
            ) : props.checked ? (
              <Check className="w-3 h-3" />
            ) : null}
          </label>
        </div>
        
        {label && (
          <label
            htmlFor={checkboxId}
            className={`text-sm cursor-pointer ${
              props.disabled ? 'text-gray-400' : 'text-gray-700'
            }`}
          >
            {label}
          </label>
        )}
      </div>
    );
  }
);

Checkbox.displayName = "Checkbox";
