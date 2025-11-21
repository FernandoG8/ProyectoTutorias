import { useState, useEffect, useCallback } from "react";
import { Search, X, Loader2 } from "lucide-react";
import { colors } from "@/constants/colors";

interface SearchInputProps {
  placeholder?: string;
  value?: string;
  onChange?: (value: string) => void;
  onSearch?: (value: string) => void;
  debounceMs?: number;
  minLength?: number;
  isLoading?: boolean;
  disabled?: boolean;
  className?: string;
  showClearButton?: boolean;
  autoFocus?: boolean;
}

/**
 * Componente de búsqueda con debounce
 * 
 * Características:
 * - Debounce configurable para evitar búsquedas excesivas
 * - Longitud mínima de búsqueda
 * - Estados de carga y deshabilitado
 * - Botón de limpiar
 * - Icono de búsqueda
 */
export const SearchInput = ({
  placeholder = "Buscar...",
  value = "",
  onChange,
  onSearch,
  debounceMs = 300,
  minLength = 2,
  isLoading = false,
  disabled = false,
  className = "",
  showClearButton = true,
  autoFocus = false
}: SearchInputProps) => {
  const [internalValue, setInternalValue] = useState(value);
  const [debouncedValue, setDebouncedValue] = useState(value);

  // Debounce effect
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedValue(internalValue);
    }, debounceMs);

    return () => clearTimeout(timer);
  }, [internalValue, debounceMs]);

  // Trigger search when debounced value changes
  // NOTE: onSearch NO está en dependencies para evitar bucles infinitos
  // si el componente padre no usa useCallback
  useEffect(() => {
    if (debouncedValue.length >= minLength || debouncedValue.length === 0) {
      onSearch?.(debouncedValue);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedValue, minLength]);

  // Sync with external value changes
  useEffect(() => {
    setInternalValue(value);
  }, [value]);

  const handleInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setInternalValue(newValue);
    onChange?.(newValue);
  }, [onChange]);

  const handleClear = useCallback(() => {
    setInternalValue("");
    onChange?.("");
    onSearch?.("");
  }, [onChange, onSearch]);

  const handleKeyDown = useCallback((e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      onSearch?.(internalValue);
    }
    if (e.key === 'Escape') {
      handleClear();
    }
  }, [internalValue, onSearch, handleClear]);

  return (
    <div className={`relative ${className}`}>
      {/* Icono de búsqueda */}
      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
        {isLoading ? (
          <Loader2 className="h-4 w-4 text-gray-400 animate-spin" />
        ) : (
          <Search className="h-4 w-4 text-gray-400" />
        )}
      </div>

      {/* Input */}
      <input
        type="text"
        value={internalValue}
        onChange={handleInputChange}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        disabled={disabled}
        autoFocus={autoFocus}
        className={`
          w-full pl-10 pr-10 py-2 border border-gray-300 rounded-lg
          focus:ring-2 focus:ring-blue-500 focus:border-blue-500
          disabled:bg-gray-50 disabled:text-gray-500 disabled:cursor-not-allowed
          transition-colors duration-200
          ${disabled ? 'bg-gray-50' : 'bg-white'}
        `}
        style={{
          borderColor: colors.semantic.border,
        }}
      />

      {/* Botón de limpiar */}
      {showClearButton && internalValue && !disabled && (
        <button
          onClick={handleClear}
          className="absolute inset-y-0 right-0 pr-3 flex items-center hover:text-gray-600 transition-colors"
          title="Limpiar búsqueda"
        >
          <X className="h-4 w-4 text-gray-400" />
        </button>
      )}

      {/* Indicador de longitud mínima */}
      {internalValue.length > 0 && internalValue.length < minLength && (
        <div className="absolute top-full left-0 mt-1 text-xs text-gray-500">
          Mínimo {minLength} caracteres para buscar
        </div>
      )}
    </div>
  );
};
