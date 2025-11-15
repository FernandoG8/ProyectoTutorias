import { useEffect, useState, useRef } from "react";

/**
 * Custom Hook: useDebounced
 *
 * Debounces value changes to prevent excessive re-renders/API calls
 *
 * Usage:
 * ```tsx
 * const [searchTerm, setSearchTerm] = useState("");
 * const debouncedSearchTerm = useDebounced(searchTerm, 500);
 *
 * useEffect(() => {
 *   if (debouncedSearchTerm) {
 *     fetchSearchResults(debouncedSearchTerm);
 *   }
 * }, [debouncedSearchTerm]);
 * ```
 *
 * Decision Log:
 * - Cleaner API than importing debounce utility
 * - Automatic cleanup on unmount
 * - Configurable delay
 * - Works with any value type
 */
export const useDebounced = <T,>(value: T, delay: number = 300): T => {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    timeoutRef.current = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, [value, delay]);

  return debouncedValue;
};
