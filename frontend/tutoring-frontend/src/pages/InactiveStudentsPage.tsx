import { useCallback, useMemo, useState } from "react";
import dayjs from "dayjs";
import { useMutation, useQuery } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Select } from "@/components/ui/Select";
import { Button } from "@/components/ui/Button";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";
import { Skeleton } from "@/components/ui/Skeleton";
import { Modal } from "@/components/ui/Modal";
import {
  assignInactiveReason,
  listInactiveStudents,
  listPendingInactiveStudents,
} from "@/services/alumnos-inactivos-service";
import { useDebounce } from "@/lib/use-debounce";
import { useAuthStore } from "@/store/auth-store";
import type { AlumnoInactivo, MotivoInactividad } from "@/types";

const motivoLabels: Record<MotivoInactividad, string> = {
  SIN_DEFINIR: "Sin definir",
  BAJA_TEMPORAL: "Baja temporal",
  BAJA_DEFINITIVA: "Baja definitiva",
  MOVILIDAD: "Movilidad académica",
  EGRESADO: "Egresado",
};

const motivoOptions = Object.entries(motivoLabels) as [MotivoInactividad, string][];

export const InactiveStudentsPage = () => {
  const [view, setView] = useState<"pendientes" | "todos">("pendientes");
  const [motivoFilter, setMotivoFilter] = useState<MotivoInactividad | "TODOS">("TODOS");
  const [carrera, setCarrera] = useState("TODAS");
  const [semestre, setSemestre] = useState("TODOS");
  const [search, setSearch] = useState("");

  const [modalOpen, setModalOpen] = useState(false);
  const [selectedAlumno, setSelectedAlumno] = useState<AlumnoInactivo | null>(null);
  const [selectedMotivo, setSelectedMotivo] = useState<MotivoInactividad | "">("");

  const debouncedSearch = useDebounce(search, 400);

  const usuario = useAuthStore((state) => state.user?.username ?? "gestor");

  const query = useQuery<AlumnoInactivo[]>({
    queryKey: ["inactive-students", view, motivoFilter, carrera, semestre],
    queryFn: () => {
      const filters = {
        carrera: carrera !== "TODAS" ? carrera : undefined,
        semestre: semestre !== "TODOS" ? Number(semestre) : undefined,
        motivo: motivoFilter !== "TODOS" ? motivoFilter : undefined,
      };
      return view === "pendientes"
        ? listPendingInactiveStudents(filters)
        : listInactiveStudents(filters);
    },
  });

  const students = query.data ?? [];

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
    const values = new Set<string>();
    students.forEach((student) => {
      if (student.carrera) {
        values.add(student.carrera);
      }
    });
    return Array.from(values).sort((a, b) => a.localeCompare(b));
  }, [students]);

  const semestreOptions = useMemo(() => {
    const values = new Set<number>();
    students.forEach((student) => {
      values.add(student.semestre);
    });
    return Array.from(values).sort((a, b) => a - b);
  }, [students]);

  const motivoMutation = useMutation({
    mutationFn: (motivo: MotivoInactividad) => {
      if (!selectedAlumno) throw new Error("Sin alumno seleccionado");
      return assignInactiveReason({
        alumnoInactivoId: selectedAlumno.id,
        motivo,
        usuario,
      });
    },
    onSuccess: () => {
      query.refetch();
      closeModal();
    },
  });

  const openModal = useCallback((alumno: AlumnoInactivo) => {
    setSelectedAlumno(alumno);
    setSelectedMotivo(alumno.motivo === "SIN_DEFINIR" ? "" : alumno.motivo);
    setModalOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    setModalOpen(false);
    setSelectedAlumno(null);
    setSelectedMotivo("");
  }, []);

  const columns = useMemo<ColumnDef<AlumnoInactivo>[]>(
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
        header: "Motivo",
        accessorKey: "motivo",
        cell: ({ getValue }) => {
          const motivo = getValue<MotivoInactividad>();
          const variant = motivo === "SIN_DEFINIR" ? "warning" : "info";
          return <Badge variant={variant}>{motivoLabels[motivo]}</Badge>;
        },
      },
      {
        header: "Fecha de detección",
        accessorKey: "fechaDeteccion",
        cell: ({ getValue }) => (
          <span className="text-sm text-slate-600">
            {dayjs(getValue<string>()).format("DD/MM/YYYY")}
          </span>
        ),
      },
      {
        header: "Cupo",
        accessorKey: "cupoLiberado",
        cell: ({ getValue }) => {
          const value = getValue<boolean>();
          return (
            <Badge variant={value ? "success" : "danger"}>
              {value ? "Liberado" : "Pendiente"}
            </Badge>
          );
        },
      },
      {
        header: "Acciones",
        cell: ({ row }) => (
          row.original.motivo === "SIN_DEFINIR" ? (
            <Button
              type="button"
              variant="secondary"
              className="text-xs"
              onClick={() => openModal(row.original)}
            >
              Registrar motivo
            </Button>
          ) : (
            <span className="text-xs text-slate-500">Motivo registrado</span>
          )
        ),
      },
    ],
    [openModal],
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3">
        <h2 className="text-2xl font-semibold text-text">Alumnos inactivos</h2>
        <p className="max-w-3xl text-sm text-slate-600">
          Identifica a los alumnos que han quedado sin actividad y gestiona la asignación de un
          motivo oficial para mantener el seguimiento académico actualizado.
        </p>
      </div>

      <Card>
        <div className="flex flex-col gap-4">
          <div className="flex flex-wrap gap-2 rounded-xl bg-slate-100/70 p-1 text-sm font-medium text-primary">
            <button
              type="button"
              onClick={() => setView("pendientes")}
              className={`flex-1 rounded-lg px-3 py-2 transition ${
                view === "pendientes" ? "bg-white shadow" : "hover:bg-white/70"
              }`}
            >
              Pendientes de motivo
            </button>
            <button
              type="button"
              onClick={() => setView("todos")}
              className={`flex-1 rounded-lg px-3 py-2 transition ${
                view === "todos" ? "bg-white shadow" : "hover:bg-white/70"
              }`}
            >
              Historial completo
            </button>
          </div>

          <div className="grid gap-4 md:grid-cols-4">
            <div className="space-y-1 md:col-span-2">
              <label className="text-sm font-medium text-text" htmlFor="search-inactive">
                Buscar alumno
              </label>
              <Input
                id="search-inactive"
                placeholder="Ingresa matrícula o nombre"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
              <p className="text-xs text-slate-500">Filtra rápidamente por matrícula o nombre.</p>
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="filter-motive">
                Motivo
              </label>
              <Select
                id="filter-motive"
                value={motivoFilter}
                onChange={(event) => setMotivoFilter(event.target.value as MotivoInactividad | "TODOS")}
              >
                <option value="TODOS">Todos</option>
                {motivoOptions.map(([value, label]) => (
                  <option key={value} value={value}>
                    {label}
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="filter-career-inactive">
                Carrera
              </label>
              <Select
                id="filter-career-inactive"
                value={carrera}
                onChange={(event) => setCarrera(event.target.value)}
              >
                <option value="TODAS">Todas</option>
                {carreraOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </Select>
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="filter-semester">
                Semestre
              </label>
              <Select
                id="filter-semester"
                value={semestre}
                onChange={(event) => setSemestre(event.target.value)}
              >
                <option value="TODOS">Todos</option>
                {semestreOptions.map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </Select>
            </div>
          </div>
          <div className="flex items-center justify-end">
            <Button variant="secondary" type="button" onClick={() => query.refetch()} disabled={query.isFetching}>
              Actualizar listado
            </Button>
          </div>
        </div>
      </Card>

      {query.isLoading ? (
        <div className="grid gap-4">
          <Skeleton className="h-32" />
          <Skeleton className="h-32" />
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={filteredStudents}
          isLoading={query.isFetching && !query.isLoading}
          emptyMessage="No se encontraron alumnos inactivos con los filtros aplicados."
        />
      )}

      <Modal
        open={modalOpen}
        onClose={closeModal}
        title="Registrar motivo de inactividad"
        description={
          selectedAlumno
            ? `Selecciona el motivo oficial para ${selectedAlumno.nombre} (${selectedAlumno.matricula}).`
            : undefined
        }
        footer={
          <>
            <Button variant="ghost" type="button" onClick={closeModal}>
              Cancelar
            </Button>
            <Button
              type="button"
              onClick={() => selectedMotivo && motivoMutation.mutate(selectedMotivo as MotivoInactividad)}
              loading={motivoMutation.isPending}
              disabled={!selectedMotivo}
            >
              Guardar motivo
            </Button>
          </>
        }
      >
        <div className="space-y-1">
          <label className="text-sm font-medium text-text" htmlFor="motivo-select">
            Motivo oficial
          </label>
          <Select
            id="motivo-select"
            value={selectedMotivo}
            onChange={(event) => setSelectedMotivo(event.target.value as MotivoInactividad | "")}
          >
            <option value="">Selecciona un motivo</option>
            {motivoOptions
              .filter(([value]) => value !== "SIN_DEFINIR")
              .map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
          </Select>
          <p className="text-xs text-slate-500">
            Esta acción quedará registrada en el historial y notificará a los responsables del programa.
          </p>
        </div>
        {motivoMutation.error && (
          <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
            Ocurrió un error al registrar el motivo. Inténtalo nuevamente.
          </p>
        )}
      </Modal>
    </div>
  );
};
