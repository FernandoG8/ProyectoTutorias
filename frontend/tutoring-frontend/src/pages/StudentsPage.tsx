import { useMemo, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";
import { Skeleton } from "@/components/ui/Skeleton";
import { listStudents } from "@/services/alumnos-service";
import { useDebounce } from "@/lib/use-debounce";
import type { AlumnoPagedResponse, AlumnoResponse, EstadoAlumno } from "@/types";

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

  const debouncedSearch = useDebounce(search, 400);

  const {
    data: pagedStudents,
    isLoading,
    isFetching,
    refetch,
  } = useQuery<AlumnoPagedResponse>({
    queryKey: ["students", page, estado, carrera],
    queryFn: () =>
      listStudents({
        page,
        limit: pageSize,
        estado: estado === "TODOS" ? undefined : estado,
        carrera: carrera === "TODAS" ? undefined : carrera,
      }),
  });

  const students: AlumnoResponse[] = pagedStudents?.items ?? [];

  const filteredStudents = useMemo(() => {
    if (!debouncedSearch) return students;
    const term = debouncedSearch.trim().toLowerCase();
    return students.filter((student) =>
      [student.matricula, student.nombre]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(term)),
    );
  }, [students, debouncedSearch]);

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
              <Input
                id="search-student"
                placeholder="Ingresa matrícula o nombre"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
              <p className="text-xs text-slate-500">La búsqueda se aplica sobre los resultados de la página actual.</p>
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
            <span className="text-xs text-slate-500">
              Página {page} de {totalPages} • {pagedStudents?.totalElements ?? 0} registros totales
            </span>
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
