import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { RotateCw, Users } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";
import { Skeleton } from "@/components/ui/Skeleton";
import { PageHeader } from "@/components/layout/PageHeader";
import { SearchInput } from "@/components/SearchInput";
import { listStudents, searchStudents, autocompleteStudents } from "@/services/alumnos-service";
import { useDebounce } from "@/lib/use-debounce";
import { rankStudents } from "@/lib/search-rank";
import { colors } from "@/constants/colors";
import type { AlumnoPagedResponse, AlumnoResponse, EstadoAlumno, TutorResponse } from "@/types";

const pageSize = 20;

export const StudentsPage = () => {
  const [page, setPage] = useState(1);
  const [estado, setEstado] = useState<EstadoAlumno | "TODOS">("TODOS");
  const [carrera, setCarrera] = useState("TODAS");
  const [search, setSearch] = useState("");
  const [isSearchMode, setIsSearchMode] = useState(false);

  const debouncedSearch = useDebounce(search, 300);

  // Memoized column definitions to prevent unnecessary re-renders
  const columns = useMemo<ColumnDef<AlumnoResponse>[]>(
    () => [
      {
        header: "Matrícula",
        accessorKey: "matricula",
        cell: ({ getValue }) => (
          <span className="font-semibold text-text">{getValue<string>()}</span>
        ),
      },
      {
        header: "Nombre",
        accessorKey: "nombre",
      },
      {
        header: "Carrera",
        accessorKey: "carrera",
      },
      {
        header: "Semestre",
        accessorKey: "semestre",
        cell: ({ getValue }) => <span className="text-slate-600">{getValue<number>()}</span>,
      },
      {
        header: "Estado",
        accessorKey: "estado",
        cell: ({ getValue }) => {
          const value = getValue<EstadoAlumno>();
          return (
            <Badge variant={value === "ACTIVO" ? "success" : "warning"}>
              {value === "ACTIVO" ? "Activo" : "Inactivo"}
            </Badge>
          );
        },
      },
      {
        header: "Tutor asignado",
        cell: ({ row }) => (
          <div className="flex flex-col text-sm">
            <span className="font-medium text-text">
              {row.original.tutor?.nombre ?? "Sin asignar"}
            </span>
            {row.original.tutor ? (
              <span className="text-xs text-slate-500">
                {row.original.tutor.carrera}
              </span>
            ) : (
              <span className="text-xs text-amber-600">Pendiente de asignación</span>
            )}
          </div>
        ),
      },
    ],
    []
  );

  // Fetch list or search results
  const {
    data: pagedStudents,
    isLoading,
    isFetching,
    refetch,
  } = useQuery<AlumnoPagedResponse>({
    queryKey: ["students", page, estado, carrera, isSearchMode, debouncedSearch],
    queryFn: async () => {
      if (isSearchMode && debouncedSearch.length >= 2) {
        return searchStudents({
          q: debouncedSearch,
          page,
          size: pageSize,
          estado: estado === "TODOS" ? undefined : estado,
          carrera: carrera === "TODAS" ? undefined : carrera,
        });
      }
      return listStudents({
        page,
        limit: pageSize,
        estado: estado === "TODOS" ? undefined : estado,
        carrera: carrera === "TODAS" ? undefined : carrera,
      });
    },
  });

  // Autocomplete suggestions (respects estado and carrera filters)
  const { data: suggestions = [] } = useQuery<AlumnoResponse[]>({
    queryKey: ["students-autocomplete", search, estado, carrera],
    queryFn: () => autocompleteStudents(search, estado, carrera),
    enabled: search.length >= 2 && !isSearchMode,
  });

  const students: AlumnoResponse[] = pagedStudents?.items ?? [];

  const filteredStudents = useMemo(() => {
    if (isSearchMode || debouncedSearch.length < 2) return students;
    // Client-side filtering for non-search mode (filtering within current page)
    const term = debouncedSearch.trim().toLowerCase();
    return rankStudents(students, term);
  }, [students, debouncedSearch, isSearchMode]);

  const carreraOptions = useMemo(() => {
    const items = new Set<string>();
    students.forEach((student) => {
      if (student.carrera) {
        items.add(student.carrera);
      }
    });
    return Array.from(items).sort((a, b) => a.localeCompare(b));
  }, [students]);

  const totalPages = pagedStudents?.totalPages ?? 1;

  const handlePrev = () => {
    setPage((current) => Math.max(1, current - 1));
  };

  const handleNext = () => {
    setPage((current) => Math.min(totalPages, current + 1));
  };

  const handleSearchChange = (value: string) => {
    setSearch(value);
    setPage(1);
    if (value.length >= 2) {
      setIsSearchMode(true);
    } else {
      setIsSearchMode(false);
    }
  };

  const handleSearchClear = () => {
    setSearch("");
    setIsSearchMode(false);
    setPage(1);
  };

  const handleSelectStudent = (item: AlumnoResponse | TutorResponse, type: "student" | "tutor") => {
    if (type === "student") {
      setSearch((item as AlumnoResponse).nombre);
      setIsSearchMode(true);
      setPage(1);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <PageHeader
        icon={<Users className="h-8 w-8" style={{ color: colors.primary[600] }} />}
        title="Gestión de Alumnos"
        description="Consulta y administra los alumnos inscritos. Usa filtros para encontrar alumnos específicos por matrícula, estado o carrera."
        stats={[
          {
            label: "Total Alumnos",
            value: pagedStudents?.totalElements ?? 0,
          },
          {
            label: "Activos",
            value: pagedStudents?.totalElements ?? 0,
          },
          {
            label: "Inactivos",
            value: 0,
          },
          {
            label: "Sin tutor",
            value: 0,
          },
        ]}
      />

      {/* Filters Card */}
      <Card>
        <div className="space-y-4">
          {/* Search Bar */}
          <div>
            <label
              className="text-sm font-semibold mb-2 block"
              htmlFor="search-student"
              style={{ color: colors.semantic.text.primary }}
            >
              Buscar alumno
            </label>
            <SearchInput
              id="search-student"
              placeholder="Matrícula, nombre o carrera..."
              value={search}
              onChange={handleSearchChange}
              onClear={handleSearchClear}
              onSelect={handleSelectStudent}
              suggestions={suggestions}
              suggestionsType="student"
              isLoading={search.length >= 2 && !isSearchMode && isFetching}
            />
            <p
              className="text-xs mt-2"
              style={{ color: colors.semantic.text.muted }}
            >
              {isSearchMode ? (
                <>Búsqueda global activa. Presiona Escape para cancelar</>
              ) : (
                <>Escribe 2+ caracteres para búsqueda automática</>
              )}
            </p>
          </div>

          {/* Filters Grid */}
          <div className="grid gap-4 md:grid-cols-3">
            <div>
              <label
                className="text-sm font-semibold mb-2 block"
                htmlFor="filter-status"
                style={{ color: colors.semantic.text.primary }}
              >
                Estado
              </label>
              <Select
                id="filter-status"
                value={estado}
                onChange={(event) => {
                  setPage(1);
                  setEstado(event.target.value as EstadoAlumno | "TODOS");
                }}
              >
                <option value="TODOS">Todos los estados</option>
                <option value="ACTIVO">Activos</option>
                <option value="INACTIVO">Inactivos</option>
              </Select>
              <p
                className="text-xs mt-1"
                style={{ color: colors.semantic.text.muted }}
              >
                Filtra por estado académico
              </p>
            </div>

            <div className="md:col-span-2">
              <label
                className="text-sm font-semibold mb-2 block"
                htmlFor="filter-career"
                style={{ color: colors.semantic.text.primary }}
              >
                Carrera
              </label>
              <Select
                id="filter-career"
                value={carrera}
                onChange={(event) => {
                  setPage(1);
                  setCarrera(event.target.value);
                }}
              >
                <option value="TODAS">Todas las carreras</option>
                {carreraOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </Select>
              <p
                className="text-xs mt-1"
                style={{ color: colors.semantic.text.muted }}
              >
                Filtra por programa académico
              </p>
            </div>
          </div>

          {/* Pagination & Actions */}
          <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-4 border-t" style={{ borderColor: colors.semantic.border }}>
            <div className="flex items-center gap-2">
              <span
                className="text-sm font-medium"
                style={{ color: colors.semantic.text.secondary }}
              >
                {pagedStudents?.totalElements ?? 0} registros
              </span>
              {isSearchMode && (
                <Badge variant="info">🔍 Búsqueda activa</Badge>
              )}
              <span
                className="text-xs"
                style={{ color: colors.semantic.text.muted }}
              >
                Página {page} de {totalPages}
              </span>
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="ghost"
                onClick={() => refetch()}
                disabled={isFetching}
                title="Actualizar datos"
                className="hidden sm:inline-flex gap-2"
              >
                <RotateCw className="h-4 w-4" />
                Actualizar
              </Button>
              <Button
                variant="ghost"
                onClick={handlePrev}
                type="button"
                disabled={page === 1 || isFetching}
              >
                ← Anterior
              </Button>
              <Button
                variant="ghost"
                onClick={handleNext}
                type="button"
                disabled={page === totalPages || isFetching}
              >
                Siguiente →
              </Button>
            </div>
          </div>
        </div>
      </Card>

      {isLoading ? (
        <div className="grid gap-4">
          <Skeleton className="h-32" />
          <Skeleton className="h-32" />
        </div>
      ) : (
        <div className="overflow-x-auto">
          <DataTable
            columns={columns}
            data={filteredStudents}
            isLoading={isFetching && !isLoading}
            emptyMessage="No se encontraron alumnos con los filtros aplicados."
          />
        </div>
      )}
    </div>
  );
};
