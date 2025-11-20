# 📦 Guía de Implementación de Componentes - Frontend Rediseño

**Documento Técnico Detallado**
**Fecha:** 2025-11-20

---

## 📋 Tabla de Contenidos

1. [Componentes Base - Implementación](#componentes-base---implementación)
2. [Componentes Compuestos](#componentes-compuestos)
3. [Patrones de Estado](#patrones-de-estado)
4. [Ejemplos Prácticos](#ejemplos-prácticos)
5. [Checklist de Implementación](#checklist-de-implementación)

---

## 🧩 Componentes Base - Implementación

### 1. Button Component (Base)

```typescript
// src/components/ui/Button.tsx

import React from "react";
import { Loader2 } from "lucide-react";

type ButtonVariant = "primary" | "secondary" | "tertiary" | "danger";
type ButtonSize = "sm" | "md" | "lg";
type IconPosition = "left" | "right" | "only";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  size?: ButtonSize;
  loading?: boolean;
  icon?: React.ReactNode;
  iconPosition?: IconPosition;
}

const variantStyles: Record<ButtonVariant, string> = {
  primary:
    "bg-primary-600 text-white hover:bg-primary-700 active:bg-primary-800 disabled:bg-gray-200 disabled:text-gray-500",
  secondary:
    "bg-white border border-gray-300 text-gray-900 hover:bg-gray-50 active:bg-gray-100 disabled:bg-gray-50 disabled:text-gray-500",
  tertiary:
    "bg-transparent text-primary-600 hover:bg-primary-50 active:bg-primary-100 disabled:text-gray-400",
  danger:
    "bg-red-600 text-white hover:bg-red-700 active:bg-red-800 disabled:bg-gray-200 disabled:text-gray-500",
};

const sizeStyles: Record<ButtonSize, string> = {
  sm: "px-3 py-2 text-sm",
  md: "px-4 py-2.5 text-base",
  lg: "px-6 py-3 text-lg",
};

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  (
    {
      children,
      variant = "primary",
      size = "md",
      loading = false,
      icon,
      iconPosition = "left",
      disabled,
      className = "",
      ...props
    },
    ref
  ) => {
    const isIconOnly = iconPosition === "only";

    return (
      <button
        ref={ref}
        disabled={disabled || loading}
        className={`
          inline-flex items-center justify-center gap-2
          rounded-lg font-medium
          transition-all duration-150 ease-out
          focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-400 focus-visible:ring-offset-2
          disabled:cursor-not-allowed
          ${variantStyles[variant]}
          ${sizeStyles[size]}
          ${isIconOnly && (size === "sm" ? "w-8 h-8" : size === "md" ? "w-10 h-10" : "w-12 h-12")}
          ${className}
        `}
        {...props}
      >
        {loading ? (
          <Loader2 className={`animate-spin ${isIconOnly ? "" : "w-4 h-4"}`} />
        ) : (
          <>
            {icon && iconPosition === "left" && icon}
            {!isIconOnly && children}
            {icon && iconPosition === "right" && icon}
            {isIconOnly && icon}
          </>
        )}
      </button>
    );
  }
);

Button.displayName = "Button";
```

**Uso:**

```typescript
// Variaciones
<Button variant="primary">Guardar</Button>
<Button variant="secondary">Cancelar</Button>
<Button variant="tertiary">Link</Button>
<Button variant="danger">Eliminar</Button>

// Con iconos
<Button icon={<SaveIcon />} iconPosition="left">Guardar</Button>
<Button icon={<TrashIcon />} iconPosition="only" />

// Estados
<Button loading>Guardando...</Button>
<Button disabled>Deshabilitado</Button>

// Tamaños
<Button size="sm">Small</Button>
<Button size="md">Medium</Button>
<Button size="lg">Large</Button>
```

---

### 2. Input Component

```typescript
// src/components/ui/Input.tsx

import React from "react";
import { AlertCircle, CheckCircle } from "lucide-react";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  success?: boolean;
  hint?: string;
  leadingIcon?: React.ReactNode;
  trailingIcon?: React.ReactNode;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  (
    {
      label,
      error,
      success,
      hint,
      leadingIcon,
      trailingIcon,
      className = "",
      ...props
    },
    ref
  ) => {
    const hasError = !!error;
    const baseClasses = `
      w-full px-3 py-2.5 rounded-lg
      border text-base
      transition-all duration-150
      focus:outline-none focus:ring-2 focus:ring-offset-2
      disabled:bg-gray-50 disabled:text-gray-500 disabled:cursor-not-allowed
    `;

    const borderClasses = hasError
      ? "border-red-500 bg-red-50 focus:border-red-500 focus:ring-red-100"
      : success
        ? "border-green-500 bg-green-50 focus:border-green-500 focus:ring-green-100"
        : "border-gray-200 bg-white focus:border-primary-500 focus:ring-primary-100";

    return (
      <div className="w-full">
        {label && (
          <label className="block text-sm font-medium text-gray-700 mb-1.5">
            {label}
          </label>
        )}

        <div className="relative">
          {leadingIcon && (
            <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
              {leadingIcon}
            </div>
          )}

          <input
            ref={ref}
            className={`
              ${baseClasses}
              ${borderClasses}
              ${leadingIcon ? "pl-10" : ""}
              ${trailingIcon ? "pr-10" : ""}
              ${className}
            `}
            {...props}
          />

          {trailingIcon && (
            <div className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">
              {trailingIcon}
            </div>
          )}

          {hasError && (
            <div className="absolute right-3 top-1/2 -translate-y-1/2 text-red-500">
              <AlertCircle className="w-4 h-4" />
            </div>
          )}

          {success && (
            <div className="absolute right-3 top-1/2 -translate-y-1/2 text-green-500">
              <CheckCircle className="w-4 h-4" />
            </div>
          )}
        </div>

        {error && <p className="text-xs text-red-600 mt-1">{error}</p>}
        {hint && !error && <p className="text-xs text-gray-500 mt-1">{hint}</p>}
      </div>
    );
  }
);

Input.displayName = "Input";
```

---

### 3. Select Component (Dropdown)

```typescript
// src/components/ui/Select.tsx

import React, { useState, useRef, useEffect } from "react";
import { ChevronDown, X } from "lucide-react";

interface Option {
  value: string | number;
  label: string;
  disabled?: boolean;
}

interface SelectProps {
  label?: string;
  value?: string | number;
  onChange: (value: string | number) => void;
  options: Option[];
  placeholder?: string;
  error?: string;
  clearable?: boolean;
  searchable?: boolean;
  disabled?: boolean;
}

export const Select = ({
  label,
  value,
  onChange,
  options,
  placeholder = "Selecciona una opción",
  error,
  clearable = false,
  searchable = false,
  disabled = false,
}: SelectProps) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const containerRef = useRef<HTMLDivElement>(null);

  const selectedOption = options.find((opt) => opt.value === value);
  const filteredOptions = searchable
    ? options.filter((opt) =>
        opt.label.toLowerCase().includes(searchTerm.toLowerCase())
      )
    : options;

  // Close on outside click
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(e.target as Node)
      ) {
        setIsOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="w-full" ref={containerRef}>
      {label && (
        <label className="block text-sm font-medium text-gray-700 mb-1.5">
          {label}
        </label>
      )}

      <div className="relative">
        <button
          type="button"
          disabled={disabled}
          onClick={() => setIsOpen(!isOpen)}
          className={`
            w-full px-3 py-2.5 rounded-lg border text-base text-left
            transition-all duration-150
            flex items-center justify-between
            focus:outline-none focus-visible:ring-2 focus-visible:ring-primary-400
            ${
              error
                ? "border-red-500 bg-red-50"
                : "border-gray-200 bg-white hover:bg-gray-50"
            }
            ${disabled ? "bg-gray-50 text-gray-500 cursor-not-allowed" : ""}
            ${isOpen ? "border-primary-500 ring-2 ring-primary-100" : ""}
          `}
        >
          <div className="flex items-center gap-2 flex-1">
            <span className={!value ? "text-gray-500" : ""}>
              {selectedOption?.label || placeholder}
            </span>
          </div>

          <div className="flex items-center gap-1">
            {clearable && value && (
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  onChange("");
                  setSearchTerm("");
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-4 h-4" />
              </button>
            )}
            <ChevronDown
              className={`w-5 h-5 text-gray-400 transition-transform ${
                isOpen ? "rotate-180" : ""
              }`}
            />
          </div>
        </button>

        {isOpen && (
          <div
            className={`
              absolute top-full left-0 right-0 mt-1
              bg-white border border-gray-200 rounded-lg shadow-lg z-50
              max-h-[300px] overflow-y-auto
            `}
          >
            {searchable && (
              <div className="sticky top-0 p-2 border-b border-gray-200 bg-gray-50">
                <input
                  type="text"
                  placeholder="Buscar..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-2 py-1.5 rounded border border-gray-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary-100"
                  autoFocus
                />
              </div>
            )}

            {filteredOptions.length === 0 ? (
              <div className="p-3 text-center text-sm text-gray-500">
                Sin resultados
              </div>
            ) : (
              filteredOptions.map((option) => (
                <button
                  key={option.value}
                  type="button"
                  disabled={option.disabled}
                  onClick={() => {
                    onChange(option.value);
                    setIsOpen(false);
                    setSearchTerm("");
                  }}
                  className={`
                    w-full text-left px-3 py-2 text-sm
                    transition-colors
                    ${option.value === value ? "bg-primary-50 text-primary-900" : "hover:bg-gray-50"}
                    ${option.disabled ? "text-gray-400 cursor-not-allowed" : ""}
                  `}
                >
                  {option.label}
                </button>
              ))
            )}
          </div>
        )}
      </div>

      {error && <p className="text-xs text-red-600 mt-1">{error}</p>}
    </div>
  );
};
```

---

### 4. Card Component

```typescript
// src/components/ui/Card.tsx

import React from "react";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "default" | "elevated" | "outlined";
  hoverable?: boolean;
}

export const Card = React.forwardRef<HTMLDivElement, CardProps>(
  (
    {
      children,
      variant = "default",
      hoverable = false,
      className = "",
      ...props
    },
    ref
  ) => {
    const variantStyles = {
      default: "bg-white border border-gray-200 shadow-xs",
      elevated: "bg-white border border-gray-200 shadow-md",
      outlined: "bg-white border-2 border-gray-300",
    };

    return (
      <div
        ref={ref}
        className={`
          rounded-lg
          transition-all duration-150
          ${variantStyles[variant]}
          ${hoverable ? "hover:shadow-md cursor-pointer" : ""}
          ${className}
        `}
        {...props}
      >
        {children}
      </div>
    );
  }
);

Card.displayName = "Card";
```

---

### 5. Badge Component

```typescript
// src/components/ui/Badge.tsx

import React from "react";

type BadgeVariant = "default" | "success" | "warning" | "danger" | "info";
type BadgeSize = "sm" | "md";

interface BadgeProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: BadgeVariant;
  size?: BadgeSize;
  icon?: React.ReactNode;
}

const variantStyles: Record<BadgeVariant, string> = {
  default: "bg-gray-100 text-gray-900",
  success: "bg-green-100 text-green-900",
  warning: "bg-amber-100 text-amber-900",
  danger: "bg-red-100 text-red-900",
  info: "bg-blue-100 text-blue-900",
};

const sizeStyles: Record<BadgeSize, string> = {
  sm: "px-2 py-1 text-xs",
  md: "px-3 py-1.5 text-sm",
};

export const Badge = React.forwardRef<HTMLDivElement, BadgeProps>(
  (
    {
      children,
      variant = "default",
      size = "md",
      icon,
      className = "",
      ...props
    },
    ref
  ) => {
    return (
      <div
        ref={ref}
        className={`
          inline-flex items-center gap-1.5
          rounded-full font-medium
          ${variantStyles[variant]}
          ${sizeStyles[size]}
          ${className}
        `}
        {...props}
      >
        {icon && <span className="flex-shrink-0">{icon}</span>}
        {children}
      </div>
    );
  }
);

Badge.displayName = "Badge";
```

---

## 🔧 Componentes Compuestos

### 1. PageHeader Component

```typescript
// src/components/composed/PageHeader.tsx

import React from "react";
import { ChevronRight } from "lucide-react";

interface Breadcrumb {
  label: string;
  href?: string;
}

interface Action {
  label: string;
  onClick?: () => void;
  variant?: "primary" | "secondary";
  icon?: React.ReactNode;
}

interface PageHeaderProps {
  title: string;
  description?: string;
  breadcrumbs?: Breadcrumb[];
  actions?: Action[];
  stats?: Array<{ label: string; value: string | number }>;
}

export const PageHeader: React.FC<PageHeaderProps> = ({
  title,
  description,
  breadcrumbs,
  actions,
  stats,
}) => {
  return (
    <div className="space-y-4">
      {/* Breadcrumbs */}
      {breadcrumbs && breadcrumbs.length > 0 && (
        <div className="flex items-center gap-2 text-sm">
          {breadcrumbs.map((crumb, idx) => (
            <React.Fragment key={idx}>
              {crumb.href ? (
                <a href={crumb.href} className="text-primary-600 hover:text-primary-700">
                  {crumb.label}
                </a>
              ) : (
                <span className="text-gray-600">{crumb.label}</span>
              )}
              {idx < breadcrumbs.length - 1 && (
                <ChevronRight className="w-4 h-4 text-gray-400" />
              )}
            </React.Fragment>
          ))}
        </div>
      )}

      {/* Title + Description */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">{title}</h1>
        {description && (
          <p className="text-gray-600 mt-1">{description}</p>
        )}
      </div>

      {/* Stats */}
      {stats && stats.length > 0 && (
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {stats.map((stat) => (
            <div key={stat.label} className="bg-gray-50 rounded-lg p-4">
              <p className="text-xs font-medium text-gray-600 uppercase tracking-wide">
                {stat.label}
              </p>
              <p className="text-2xl font-bold text-gray-900 mt-1">
                {stat.value}
              </p>
            </div>
          ))}
        </div>
      )}

      {/* Actions */}
      {actions && actions.length > 0 && (
        <div className="flex gap-2">
          {actions.map((action) => (
            <button
              key={action.label}
              onClick={action.onClick}
              className={`
                px-4 py-2 rounded-lg font-medium transition-all
                ${
                  action.variant === "secondary"
                    ? "bg-gray-100 text-gray-900 hover:bg-gray-200"
                    : "bg-primary-600 text-white hover:bg-primary-700"
                }
              `}
            >
              {action.icon && <span className="mr-2">{action.icon}</span>}
              {action.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
};
```

---

### 2. DataTable Component (Completo)

```typescript
// src/components/composed/DataTable.tsx

import React, { useState } from "react";
import { ChevronUp, ChevronDown } from "lucide-react";

export interface Column<T> {
  accessor: keyof T;
  header: string;
  width?: string;
  sortable?: boolean;
  cell?: (value: any, row: T) => React.ReactNode;
}

interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  isLoading?: boolean;
  pagination?: {
    current: number;
    pageSize: number;
    total: number;
    onPageChange: (page: number) => void;
  };
  sorting?: {
    field: string;
    order: "asc" | "desc";
    onSort: (field: string, order: "asc" | "desc") => void;
  };
  rowSelection?: boolean;
  onRowClick?: (row: T) => void;
}

export const DataTable = <T extends { id?: string | number },>({
  columns,
  data,
  isLoading,
  pagination,
  sorting,
  rowSelection,
  onRowClick,
}: DataTableProps<T>) => {
  const [selectedRows, setSelectedRows] = useState<Set<string | number>>(
    new Set()
  );

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
      </div>
    );
  }

  if (data.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-gray-500">Sin resultados</p>
      </div>
    );
  }

  const handleSelectAll = () => {
    if (selectedRows.size === data.length) {
      setSelectedRows(new Set());
    } else {
      setSelectedRows(
        new Set(data.map((row) => row.id || Math.random()))
      );
    }
  };

  return (
    <div className="overflow-x-auto border border-gray-200 rounded-lg">
      <table className="w-full border-collapse">
        <thead className="bg-gray-50 border-b border-gray-200">
          <tr>
            {rowSelection && (
              <th className="w-12 px-4 py-3">
                <input
                  type="checkbox"
                  checked={selectedRows.size === data.length}
                  onChange={handleSelectAll}
                  className="w-4 h-4"
                />
              </th>
            )}
            {columns.map((col) => (
              <th
                key={String(col.accessor)}
                style={{ width: col.width }}
                className="px-4 py-3 text-left text-xs font-semibold text-gray-700 uppercase tracking-wider"
              >
                {col.sortable ? (
                  <button
                    onClick={() => {
                      const field = String(col.accessor);
                      const order =
                        sorting?.field === field && sorting.order === "asc"
                          ? "desc"
                          : "asc";
                      sorting?.onSort(field, order);
                    }}
                    className="flex items-center gap-1 hover:text-gray-900 transition-colors"
                  >
                    {col.header}
                    {sorting?.field === String(col.accessor) && (
                      <>
                        {sorting.order === "asc" ? (
                          <ChevronUp className="w-4 h-4" />
                        ) : (
                          <ChevronDown className="w-4 h-4" />
                        )}
                      </>
                    )}
                  </button>
                ) : (
                  col.header
                )}
              </th>
            ))}
          </tr>
        </thead>

        <tbody>
          {data.map((row, idx) => (
            <tr
              key={row.id || idx}
              className={`
                border-b border-gray-200
                transition-colors
                ${idx % 2 === 0 ? "bg-white" : "bg-gray-50"}
                ${onRowClick ? "hover:bg-blue-50 cursor-pointer" : ""}
              `}
              onClick={() => onRowClick?.(row)}
            >
              {rowSelection && (
                <td className="w-12 px-4 py-3">
                  <input
                    type="checkbox"
                    checked={selectedRows.has(row.id || idx)}
                    onChange={(e) => {
                      const newSet = new Set(selectedRows);
                      if (e.target.checked) {
                        newSet.add(row.id || idx);
                      } else {
                        newSet.delete(row.id || idx);
                      }
                      setSelectedRows(newSet);
                    }}
                    onClick={(e) => e.stopPropagation()}
                    className="w-4 h-4"
                  />
                </td>
              )}
              {columns.map((col) => (
                <td
                  key={String(col.accessor)}
                  style={{ width: col.width }}
                  className="px-4 py-3 text-sm text-gray-900"
                >
                  {col.cell
                    ? col.cell((row as any)[col.accessor], row)
                    : (row as any)[col.accessor]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>

      {pagination && (
        <div className="px-4 py-3 bg-gray-50 border-t border-gray-200 flex items-center justify-between text-sm">
          <span className="text-gray-600">
            Mostrando{" "}
            {(pagination.current - 1) * pagination.pageSize + 1} a{" "}
            {Math.min(
              pagination.current * pagination.pageSize,
              pagination.total
            )}{" "}
            de {pagination.total}
          </span>

          <div className="flex gap-2">
            <button
              onClick={() =>
                pagination.onPageChange(
                  Math.max(1, pagination.current - 1)
                )
              }
              disabled={pagination.current === 1}
              className="px-3 py-1.5 border border-gray-200 rounded text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              ← Anterior
            </button>

            <span className="px-2 py-1.5 text-gray-600">
              {pagination.current} /{" "}
              {Math.ceil(pagination.total / pagination.pageSize)}
            </span>

            <button
              onClick={() =>
                pagination.onPageChange(
                  Math.min(
                    Math.ceil(pagination.total / pagination.pageSize),
                    pagination.current + 1
                  )
                )
              }
              disabled={
                pagination.current >=
                Math.ceil(pagination.total / pagination.pageSize)
              }
              className="px-3 py-1.5 border border-gray-200 rounded text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Siguiente →
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
```

---

## 📊 Patrones de Estado

### Loading State

```typescript
export const LoadingState = () => (
  <div className="flex flex-col items-center justify-center h-64 gap-4">
    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
    <p className="text-gray-600">Cargando...</p>
  </div>
);
```

### Empty State

```typescript
export const EmptyState = ({
  title,
  description,
  action,
}: {
  title: string;
  description?: string;
  action?: { label: string; onClick: () => void };
}) => (
  <div className="flex flex-col items-center justify-center h-64 gap-4">
    <div className="text-center space-y-2">
      <p className="text-lg font-medium text-gray-900">{title}</p>
      {description && (
        <p className="text-sm text-gray-600">{description}</p>
      )}
    </div>
    {action && (
      <Button onClick={action.onClick}>{action.label}</Button>
    )}
  </div>
);
```

### Error State

```typescript
export const ErrorState = ({
  title = "Error",
  message,
  retry,
}: {
  title?: string;
  message: string;
  retry?: () => void;
}) => (
  <div className="flex flex-col items-center justify-center h-64 gap-4 p-4 bg-red-50 border border-red-200 rounded-lg">
    <AlertCircle className="w-8 h-8 text-red-600" />
    <div className="text-center space-y-2">
      <p className="text-lg font-medium text-red-900">{title}</p>
      <p className="text-sm text-red-700">{message}</p>
    </div>
    {retry && (
      <Button variant="primary" onClick={retry}>
        Reintentar
      </Button>
    )}
  </div>
);
```

---

## 💡 Ejemplos Prácticos

### Ejemplo 1: Página de Alumnos Completa

```typescript
// src/pages/AlumnosPage.tsx

import React, { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Plus, Download } from "lucide-react";

import { PageHeader } from "@/components/composed/PageHeader";
import { DataTable } from "@/components/composed/DataTable";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { CarreraBadge } from "@/components/features/CarreraBadge";

export const AlumnosPage = () => {
  const [filters, setFilters] = useState({
    search: "",
    carrera: "",
    semestre: "",
  });

  const [pagination, setPagination] = useState({
    page: 1,
    pageSize: 10,
  });

  const [sorting, setSorting] = useState({
    field: "matricula",
    order: "asc" as const,
  });

  // Query
  const { data, isLoading } = useQuery({
    queryKey: ["alumnos", filters, pagination, sorting],
    queryFn: async () => {
      const response = await fetch(
        `/api/alumnos?` +
          new URLSearchParams({
            search: filters.search,
            carrera: filters.carrera,
            semestre: filters.semestre,
            page: pagination.page.toString(),
            pageSize: pagination.pageSize.toString(),
            sortBy: sorting.field,
            order: sorting.order,
          })
      );
      return response.json();
    },
  });

  const columns = [
    { accessor: "id" as const, header: "#", width: "60px" },
    { accessor: "matricula" as const, header: "Matrícula", width: "120px", sortable: true },
    { accessor: "nombre" as const, header: "Nombre", width: "200px", sortable: true },
    {
      accessor: "carrera" as const,
      header: "Carrera",
      width: "100px",
      cell: (value: string) => <CarreraBadge carrera={value} />,
    },
    { accessor: "semestreNumerico" as const, header: "Semestre", width: "100px" },
    {
      accessor: "estado" as const,
      header: "Estado",
      width: "100px",
      cell: (value: string) => (
        <Badge variant={value === "ACTIVO" ? "success" : "danger"}>
          {value}
        </Badge>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <PageHeader
        title="Alumnos"
        description="Gestiona el registro de estudiantes"
        breadcrumbs={[{ label: "Inicio", href: "/" }, { label: "Alumnos" }]}
        stats={[
          { label: "Total", value: data?.total || 0 },
          { label: "Activos", value: data?.activos || 0 },
          { label: "Inactivos", value: data?.inactivos || 0 },
        ]}
        actions={[
          {
            label: "Exportar",
            icon: <Download className="w-4 h-4" />,
            variant: "secondary",
          },
          { label: "+ Nuevo", icon: <Plus className="w-4 h-4" /> },
        ]}
      />

      {/* Filters */}
      <Card className="p-4">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Input
            placeholder="Buscar por matrícula, nombre..."
            value={filters.search}
            onChange={(e) =>
              setFilters({ ...filters, search: e.target.value })
            }
          />
          <Select
            label="Carrera"
            value={filters.carrera}
            onChange={(val) => setFilters({ ...filters, carrera: String(val) })}
            options={[
              { value: "", label: "Todas" },
              { value: "ITS", label: "ITS" },
              { value: "ISC", label: "ISC" },
            ]}
          />
          <Select
            label="Semestre"
            value={filters.semestre}
            onChange={(val) => setFilters({ ...filters, semestre: String(val) })}
            options={[
              { value: "", label: "Todos" },
              { value: "1", label: "Semestre 1" },
              { value: "2", label: "Semestre 2" },
            ]}
          />
          <div className="flex items-end gap-2">
            <Button variant="secondary" className="flex-1">
              Filtrar
            </Button>
            <Button
              variant="tertiary"
              onClick={() =>
                setFilters({ search: "", carrera: "", semestre: "" })
              }
            >
              Limpiar
            </Button>
          </div>
        </div>
      </Card>

      {/* Table */}
      <Card>
        <DataTable
          columns={columns}
          data={data?.alumnos || []}
          isLoading={isLoading}
          pagination={{
            current: pagination.page,
            pageSize: pagination.pageSize,
            total: data?.total || 0,
            onPageChange: (page) => setPagination({ ...pagination, page }),
          }}
          sorting={{
            field: sorting.field,
            order: sorting.order,
            onSort: (field, order) => setSorting({ field, order }),
          }}
        />
      </Card>
    </div>
  );
};
```

---

## ✅ Checklist de Implementación

### Componentes Base
- [ ] Button.tsx (todos los variants)
- [ ] Input.tsx (con error state)
- [ ] Select.tsx (searchable)
- [ ] Card.tsx
- [ ] Badge.tsx
- [ ] Modal.tsx
- [ ] Tabs.tsx

### Componentes Compuestos
- [ ] PageHeader.tsx
- [ ] DataTable.tsx (completo)
- [ ] WizardContainer.tsx
- [ ] EmptyState.tsx
- [ ] LoadingState.tsx
- [ ] ErrorState.tsx

### Patrones de Estado
- [ ] Loading screens
- [ ] Empty states
- [ ] Error handling
- [ ] Success feedback

### Módulos
- [ ] AlumnosPage (refactorizado)
- [ ] TutoresPage (refactorizado)
- [ ] CambioTutorWizard (nuevo)
- [ ] ReportesPage (mejorado)
- [ ] AlumnosInactivosPage (mejorado)

### Accesibilidad
- [ ] Contraste de colores (4.5:1)
- [ ] Focus visible en todos los elementos
- [ ] ARIA labels donde corresponde
- [ ] Navegación con teclado (Tab, Enter, Esc)
- [ ] Semantic HTML

### Testing
- [ ] Tests unitarios de componentes
- [ ] Tests de integración de módulos
- [ ] Testing de accesibilidad (axe)
- [ ] Testing de responsividad

---

**Este documento sirve como referencia técnica para la implementación del rediseño completo.**
