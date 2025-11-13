import { useEffect, useRef, useState, type InputHTMLAttributes } from "react";
import type { AlumnoResponse, TutorResponse } from "@/types";
import { Badge } from "@/components/ui/Badge";

interface SearchInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "onSelect"> {
  value: string;
  onChange: (value: string) => void;
  onSelect?: (item: AlumnoResponse | TutorResponse, type: "student" | "tutor") => void;
  suggestions?: (AlumnoResponse | TutorResponse)[];
  suggestionsType?: "student" | "tutor" | "mixed";
  isLoading?: boolean;
  onClear?: () => void;
}

export const SearchInput = ({
  value,
  onChange,
  onSelect,
  suggestions = [],
  suggestionsType = "mixed",
  isLoading = false,
  onClear,
  className = "",
  ...props
}: SearchInputProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  const isStudent = (item: any): item is AlumnoResponse => "matricula" in item;
  const isTutor = (item: any): item is TutorResponse => "capacidadMax" in item;

  // Filter suggestions based on type
  const displaySuggestions = suggestions.filter((item) => {
    if (suggestionsType === "student") return isStudent(item);
    if (suggestionsType === "tutor") return isTutor(item);
    return true;
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e.target.value);
    setHighlightedIndex(-1);
    if (e.target.value.length >= 2) {
      setIsOpen(true);
    } else {
      setIsOpen(false);
    }
  };

  const handleSelect = (item: AlumnoResponse | TutorResponse) => {
    if (isStudent(item)) {
      onChange(item.nombre);
      onSelect?.(item, "student");
    } else if (isTutor(item)) {
      onChange(item.nombre);
      onSelect?.(item, "tutor");
    }
    setIsOpen(false);
    setHighlightedIndex(-1);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (!isOpen) return;

    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setHighlightedIndex((prev) =>
          prev < displaySuggestions.length - 1 ? prev + 1 : prev,
        );
        break;
      case "ArrowUp":
        e.preventDefault();
        setHighlightedIndex((prev) => (prev > 0 ? prev - 1 : -1));
        break;
      case "Enter":
        e.preventDefault();
        if (highlightedIndex >= 0 && displaySuggestions[highlightedIndex]) {
          handleSelect(displaySuggestions[highlightedIndex]);
        }
        break;
      case "Escape":
        e.preventDefault();
        setIsOpen(false);
        setHighlightedIndex(-1);
        break;
    }
  };

  const handleClear = () => {
    onChange("");
    onClear?.();
    setIsOpen(false);
    setHighlightedIndex(-1);
    inputRef.current?.focus();
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="relative" ref={containerRef}>
      <div className="relative">
        <input
          ref={inputRef}
          type="text"
          value={value}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          onFocus={() => {
            if (value.length >= 2 && displaySuggestions.length > 0) {
              setIsOpen(true);
            }
          }}
          className={`w-full rounded-lg border border-border bg-white px-3 py-2 text-sm text-text placeholder:text-slate-400 focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/40 ${className}`}
          {...props}
        />

        {/* Clear button */}
        {value && (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition"
            aria-label="Clear search"
          >
            ✕
          </button>
        )}

        {/* Loading indicator */}
        {isLoading && value && (
          <div className="absolute right-3 top-1/2 -translate-y-1/2">
            <div className="w-4 h-4 border-2 border-primary/30 border-t-primary rounded-full animate-spin" />
          </div>
        )}
      </div>

      {/* Autocomplete dropdown */}
      {isOpen && displaySuggestions.length > 0 && (
        <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-border rounded-lg shadow-lg z-50 max-h-80 overflow-y-auto">
          {displaySuggestions.map((item, index) => {
            const isHighlighted = index === highlightedIndex;
            const isStudent = "matricula" in item;

            return (
              <button
                key={isStudent ? `student-${item.id}` : `tutor-${item.id}`}
                type="button"
                onClick={() => handleSelect(item)}
                className={`w-full text-left px-3 py-2 transition ${
                  isHighlighted
                    ? "bg-primary/10 border-l-2 border-primary"
                    : "hover:bg-slate-50 border-l-2 border-transparent"
                }`}
              >
                <div className="flex items-center justify-between gap-2">
                  <div>
                    <p className="font-medium text-sm text-text">
                      {isStudent ? item.nombre : item.nombre}
                    </p>
                    <p className="text-xs text-slate-500">
                      {isStudent ? (
                        <>
                          {item.matricula} • {item.carrera} • S{item.semestre}
                        </>
                      ) : (
                        <>
                          {item.carrera} • {item.cargaActual}/{item.capacidadMax}
                        </>
                      )}
                    </p>
                  </div>
                  <Badge variant={isStudent ? "info" : "default"} className="text-xs">
                    {isStudent ? "Alumno" : "Tutor"}
                  </Badge>
                </div>
              </button>
            );
          })}
        </div>
      )}

      {/* No results message */}
      {isOpen && value && displaySuggestions.length === 0 && !isLoading && (
        <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-border rounded-lg shadow-lg p-3 z-50">
          <p className="text-sm text-slate-500 text-center">
            No se encontraron resultados para "{value}"
          </p>
        </div>
      )}
    </div>
  );
};
