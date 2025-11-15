import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { Card } from "@/components/ui/Card";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";
import { Skeleton } from "@/components/ui/Skeleton";
import { SearchInput } from "@/components/SearchInput";
import { listStudents, searchStudents, autocompleteStudents } from "@/services/alumnos-service";
import { useDebounce } from "@/lib/use-debounce";
import { rankStudents } from "@/lib/search-rank";
import type { AlumnoPagedResponse, AlumnoResponse, EstadoAlumno, TutorResponse } from "@/types";

const columns: ColumnDef<AlumnoResponse>[] = [
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
];

const pageSize = 20;

export const StudentsPage = () => {
  const [page, setPage] = useState(1);
  const [estado, setEstado] = useState<EstadoAlumno | "TODOS">("TODOS");
  const [carrera, setCarrera] = useState("TODAS");
  const [search, setSearch] = useState("");
  const [isSearchMode, setIsSearchMode] = useState(false);

  const debouncedSearch = useDebounce(search, 300);

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
          limit: pageSize,
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
    queryFn: () => autocompleteStudents(search, estado, carrera, 8),
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
      <div className="flex flex-col gap-3">
        <h2 className="text-2xl font-semibold text-text">Alumnos activos</h2>
        <p className="max-w-3xl text-sm text-slate-600">
          Consulta y filtra a los alumnos inscritos en el programa de tutorías. Usa la
          búsqueda por matrícula o nombre y acota los resultados por estado y carrera.
        </p>
      </div>

      <Card>
        <div className="flex flex-col gap-4">
          <div className="grid gap-4 md:grid-cols-4">
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="search-student">
                Buscar alumno
              </label>
              <SearchInput
                id="search-student"
                placeholder="Ingresa matrícula o nombre"
                value={search}
                onChange={handleSearchChange}
                onClear={handleSearchClear}
                onSelect={handleSelectStudent}
                suggestions={suggestions}
                suggestionsType="student"
                isLoading={search.length >= 2 && !isSearchMode && isFetching}
              />
              <p className="text-xs text-slate-500">
                {isSearchMode ? (
                  <>Búsqueda global habilitada. Presiona <kbd className="bg-slate-100 px-1 rounded text-xs">Escape</kbd> para cancelar.</>
                ) : (
                  <>Escribe 2+ caracteres para búsqueda automática.</>
                )}
              </p>
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="filter-status">
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
                <option value="TODOS">Todos</option>
                <option value="ACTIVO">Activos</option>
                <option value="INACTIVO">Inactivos</option>
              </Select>
              <p className="text-xs text-slate-500">Selecciona el estado académico reportado por el sistema.</p>
            </div>
            <div className="space-y-1 md:col-span-2">
              <label className="text-sm font-medium text-text" htmlFor="filter-career">
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
                <option value="TODAS">Todas</option>
                {carreraOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </Select>
              <p className="text-xs text-slate-500">
                Las carreras listadas corresponden a los resultados obtenidos en la consulta actual.
              </p>
            </div>
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-xs text-slate-500">
                Página {page} de {totalPages} • {pagedStudents?.totalElements ?? 0} registros totales
              </span>
              {isSearchMode && <Badge variant="info">🔍 Búsqueda global</Badge>}
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="secondary"
                onClick={() => refetch()}
                className="hidden sm:inline-flex"
                type="button"
                disabled={isFetching}
              >
                Actualizar
              </Button>
              <Button
                variant="ghost"
                onClick={handlePrev}
                type="button"
                disabled={page === 1 || isFetching}
              >
                Anterior
              </Button>
              <Button
                variant="ghost"
                onClick={handleNext}
                type="button"
                disabled={page === totalPages || isFetching}
              >
                Siguiente
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
        <DataTable
          columns={columns}
          data={filteredStudents}
          isLoading={isFetching && !isLoading}
          emptyMessage="No se encontraron alumnos con los filtros aplicados."
        />
      )}
    </div>
  );
};
