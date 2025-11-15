import { useEffect, useRef, useState } from "react";
import { Search, X } from "lucide-react";
import Fuse from "fuse.js";
import { colors } from "@/constants/colors";
import { useDebounced } from "@/hooks/useDebounced";

/**
 * AutocompleteSearch Component
 *
 * Advanced search component with:
 * - Debounced input for performance
 * - Fuzzy search using Fuse.js
 * - Category grouping support
 * - Keyboard navigation (up/down arrows, Enter to select, Esc to close)
 * - Loading state
 * - No results feedback
 * - Accessibility features
 *
 * Part of Week 2 reusable components
 *
 * Decision Log:
 * - Fuse.js for fuzzy matching (better UX than exact match)
 * - Debounced input to prevent excessive filtering
 * - Keyboard navigation for accessibility
 * - Custom dropdown instead of cmdk to maintain simplicity
 * - Works with any data structure via searchKeys prop
 */

interface SearchOption {
  id: string | number;
  label: string;
  [key: string]: any;
}

interface AutocompleteSearchProps {
  options: SearchOption[];
  onSelect: (option: SearchOption) => void;
  placeholder?: string;
  searchKeys?: string[];
  isLoading?: boolean;
  noResultsText?: string;
  maxResults?: number;
  onSearchChange?: (query: string) => void;
}

export const AutocompleteSearch = ({
  options,
  onSelect,
  placeholder = "Buscar...",
  searchKeys = ["label"],
  isLoading = false,
  noResultsText = "No se encontraron resultados",
  maxResults = 8,
  onSearchChange,
}: AutocompleteSearchProps) => {
  const [query, setQuery] = useState("");
  const [isOpen, setIsOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const debouncedQuery = useDebounced(query, 300);
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Initialize Fuse.js for fuzzy search
  const fuse = new Fuse(options, {
    keys: searchKeys,
    threshold: 0.3,
    minMatchCharLength: 1,
  });

  const filteredOptions = debouncedQuery
    ? fuse.search(debouncedQuery).map((result: any) => result.item)
    : options;

  const displayedOptions = filteredOptions.slice(0, maxResults);

  // Notify parent of search change
  useEffect(() => {
    onSearchChange?.(debouncedQuery);
  }, [debouncedQuery, onSearchChange]);

  // Keyboard navigation
  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (!isOpen) {
      if (e.key === "ArrowDown" || e.key === "ArrowUp") {
        setIsOpen(true);
        setSelectedIndex(0);
      }
      return;
    }

    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setSelectedIndex((prev) =>
          prev < displayedOptions.length - 1 ? prev + 1 : prev
        );
        break;
      case "ArrowUp":
        e.preventDefault();
        setSelectedIndex((prev) => (prev > 0 ? prev - 1 : -1));
        break;
      case "Enter":
        e.preventDefault();
        if (selectedIndex >= 0 && displayedOptions[selectedIndex]) {
          handleSelect(displayedOptions[selectedIndex]);
        }
        break;
      case "Escape":
        e.preventDefault();
        setIsOpen(false);
        setSelectedIndex(-1);
        break;
      default:
        break;
    }
  };

  // Handle selection
  const handleSelect = (option: SearchOption) => {
    onSelect(option);
    setQuery("");
    setIsOpen(false);
    setSelectedIndex(-1);
    inputRef.current?.blur();
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div ref={containerRef} className="relative">
      {/* Search Input */}
      <div className="relative">
        <Search
          className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5"
          style={{ color: colors.semantic.text.muted }}
        />
        <input
          ref={inputRef}
          type="text"
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            if (e.target.value) {
              setIsOpen(true);
              setSelectedIndex(0);
            }
          }}
          onKeyDown={handleKeyDown}
          onFocus={() => query && setIsOpen(true)}
          placeholder={placeholder}
          className="w-full rounded-lg border px-3 py-2.5 pl-10 pr-10 text-sm transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2"
          style={{
            borderColor: colors.semantic.border,
            color: colors.semantic.text.primary,
          }}
          aria-autocomplete="list"
          aria-controls="autocomplete-dropdown"
          aria-expanded={isOpen}
        />
        {query && (
          <button
            onClick={() => setQuery("")}
            className="absolute right-3 top-1/2 -translate-y-1/2 p-0.5 hover:bg-gray-100 rounded transition-colors"
            type="button"
            aria-label="Limpiar búsqueda"
          >
            <X className="h-4 w-4" style={{ color: colors.semantic.text.muted }} />
          </button>
        )}
      </div>

      {/* Dropdown Results */}
      {isOpen && (
        <div
          id="autocomplete-dropdown"
          className="absolute top-full left-0 right-0 mt-1 rounded-lg border bg-white shadow-lg z-50"
          style={{
            borderColor: colors.semantic.border,
            maxHeight: "300px",
            overflowY: "auto",
          }}
        >
          {isLoading ? (
            <div className="px-4 py-8 text-center">
              <div
                className="inline-block h-5 w-5 animate-spin rounded-full border-2 border-current border-r-transparent"
                style={{ borderColor: colors.primary[400] }}
              />
            </div>
          ) : displayedOptions.length === 0 ? (
            <div
              className="px-4 py-6 text-center text-sm"
              style={{ color: colors.semantic.text.secondary }}
            >
              {noResultsText}
            </div>
          ) : (
            <ul className="divide-y" style={{ borderColor: colors.semantic.border }}>
              {displayedOptions.map((option: any, index: any) => (
                <li key={option.id}>
                  <button
                    onClick={() => handleSelect(option)}
                    className="w-full text-left px-4 py-2.5 text-sm transition-colors hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-inset"
                    style={{
                      backgroundColor:
                        selectedIndex === index
                          ? colors.primary[50]
                          : undefined,
                      color:
                        selectedIndex === index
                          ? colors.primary[700]
                          : colors.semantic.text.primary,
                    }}
                  >
                    <div className="font-medium">{option.label}</div>
                    {option.description && (
                      <div
                        className="text-xs mt-0.5"
                        style={{ color: colors.semantic.text.secondary }}
                      >
                        {option.description}
                      </div>
                    )}
                  </button>
                </li>
              ))}
            </ul>
          )}

          {filteredOptions.length > maxResults && (
            <div
              className="px-4 py-2 text-center text-xs"
              style={{ color: colors.semantic.text.muted }}
            >
              +{filteredOptions.length - maxResults} más resultados
            </div>
          )}
        </div>
      )}
    </div>
  );
};
