import { useCallback, useMemo, useState } from "react";
import { useForm, type Resolver, type SubmitHandler } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";
import { SearchInput } from "@/components/SearchInput";
import {
  createTutor,
  getTutorWithStudents,
  listTutors,
  updateTutor,
  autocompleteTutors,
} from "@/services/tutors-service";
import { useDebounce } from "@/lib/use-debounce";
import { rankTutors } from "@/lib/search-rank";
import type { TutorConAlumnos, TutorResponse } from "@/types";

const tutorSchema = z.object({
  nombre: z.string().min(1, "Ingresa el nombre completo."),
  carrera: z.string().min(1, "Indica la carrera."),
  capacidadMax: z.coerce.number().min(1, "Debe ser al menos 1."),
  areaAtencion: z.string().optional(),
  letraEdificio: z.string().optional(),
  activo: z.boolean(),
});

type TutorForm = z.infer<typeof tutorSchema>;

const columns = (
  onEdit: (tutor: TutorResponse) => void,
  onViewStudents: (tutor: TutorResponse) => void,
): ColumnDef<TutorResponse>[] => [
  { header: "Nombre", accessorKey: "nombre" },
  { header: "Carrera", accessorKey: "carrera" },
  {
    header: "Capacidad",
    accessorKey: "capacidadMax",
    cell: ({ row }) => `${row.original.cargaActual}/${row.original.capacidadMax}`,
  },
  { header: "Disponibles", accessorKey: "capacidadDisponible" },
  {
    header: "Área",
    accessorKey: "areaAtencion",
    cell: ({ getValue }) => getValue() ?? "General",
  },
  {
    header: "Activo",
    accessorKey: "activo",
    cell: ({ getValue }) => (getValue() ? "Sí" : "No"),
  },
  {
    header: "Acciones",
    cell: ({ row }) => (
      <div className="flex flex-wrap gap-2">
        <Button type="button" variant="secondary" onClick={() => onEdit(row.original)}>
          Editar
        </Button>
        <Button type="button" variant="ghost" onClick={() => onViewStudents(row.original)}>
          Ver alumnos
        </Button>
      </div>
    ),
  },
];

export const TutorsPage = () => {
  const queryClient = useQueryClient();
  const [editingTutor, setEditingTutor] = useState<TutorResponse | null>(null);
  const [selectedTutor, setSelectedTutor] = useState<TutorResponse | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [isSearchMode, setIsSearchMode] = useState(false);

  const debouncedSearch = useDebounce(search, 300);

  const {
    data: tutors = [],
    isLoading,
    isFetching,
  } = useQuery<TutorResponse[]>({
    queryKey: ["tutors", isSearchMode, debouncedSearch],
    queryFn: () => listTutors(),
  });

  // Autocomplete suggestions
  const { data: suggestions = [] } = useQuery<TutorResponse[]>({
    queryKey: ["tutors-autocomplete", search],
    queryFn: () => autocompleteTutors(search, 8),
    enabled: search.length >= 2 && !isSearchMode,
  });

  const displayTutors = useMemo(() => {
    if (isSearchMode && debouncedSearch.length >= 2) {
      return rankTutors(tutors, debouncedSearch);
    }
    return tutors;
  }, [tutors, debouncedSearch, isSearchMode]);

  const {
    data: tutorDetail,
    isLoading: loadingStudents,
  } = useQuery<TutorConAlumnos | null>({
    queryKey: ["tutor-students", selectedTutor?.id],
    queryFn: async () => {
      if (!selectedTutor) return null;
      return getTutorWithStudents(selectedTutor.id);
    },
    enabled: Boolean(selectedTutor?.id),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TutorForm>({
    resolver: zodResolver(tutorSchema) as Resolver<TutorForm>,
    defaultValues: {
      nombre: "",
      carrera: "",
      capacidadMax: 1,
      areaAtencion: "",
      letraEdificio: "",
      activo: true,
    },
  });

  const createMutation = useMutation({
    mutationFn: createTutor,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tutors"] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: TutorForm }) => updateTutor(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tutors"] });
    },
  });

  const onSubmit: SubmitHandler<TutorForm> = async (values) => {
    setErrorMessage(null);
    try {
      if (editingTutor) {
        await updateMutation.mutateAsync({ id: editingTutor.id, data: values });
        setFeedback("Tutor actualizado correctamente.");
      } else {
        await createMutation.mutateAsync(values);
        setFeedback("Tutor registrado correctamente.");
      }
      reset();
      setEditingTutor(null);
    } catch (error) {
      console.error(error);
      setErrorMessage("No fue posible guardar la información. Inténtalo nuevamente.");
    }
  };

  const startEdit = useCallback(
    (tutor: TutorResponse) => {
      setEditingTutor(tutor);
      reset({
        nombre: tutor.nombre,
        carrera: tutor.carrera,
        capacidadMax: tutor.capacidadMax,
        areaAtencion: tutor.areaAtencion ?? "",
        letraEdificio: tutor.letraEdificio ?? "",
        activo: tutor.activo,
      });
      setFeedback(null);
    },
    [reset],
  );

  const handleViewStudents = useCallback((tutor: TutorResponse) => {
    setSelectedTutor(tutor);
  }, []);

  const tableColumns = useMemo(
    () => columns(startEdit, handleViewStudents),
    [handleViewStudents, startEdit],
  );

  const clearForm = () => {
    setEditingTutor(null);
    reset({
      nombre: "",
      carrera: "",
      capacidadMax: 1,
      areaAtencion: "",
      letraEdificio: "",
      activo: true,
    });
    setFeedback(null);
  };

  const handleSearchChange = (value: string) => {
    setSearch(value);
    if (value.length >= 2) {
      setIsSearchMode(true);
    } else {
      setIsSearchMode(false);
    }
  };

  const handleSearchClear = () => {
    setSearch("");
    setIsSearchMode(false);
  };

  const handleSelectTutor = (tutor: TutorResponse) => {
    setSearch(tutor.nombre);
    setIsSearchMode(true);
  };

  return (
    <div className="grid gap-6 xl:grid-cols-[2fr_3fr]">
      <Card>
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-semibold text-text">
              {editingTutor ? "Editar tutor" : "Registrar nuevo tutor"}
            </h2>
            <p className="text-sm text-slate-500">
              Gestiona la base de tutores institucionales.
            </p>
          </div>
          {editingTutor && (
            <Button type="button" variant="ghost" onClick={clearForm}>
              Cancelar
            </Button>
          )}
        </div>

        <form className="mt-6 space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="nombre">
              Nombre completo
            </label>
            <Input id="nombre" placeholder="María Pérez" {...register("nombre")} />
            {errors.nombre && (
              <p className="text-sm text-red-600">{errors.nombre.message}</p>
            )}
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="carrera">
                Carrera
              </label>
              <Input id="carrera" placeholder="Ingeniería" {...register("carrera")} />
              {errors.carrera && (
                <p className="text-sm text-red-600">{errors.carrera.message}</p>
              )}
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="capacidadMax">
                Capacidad máxima
              </label>
              <Input id="capacidadMax" type="number" min="1" {...register("capacidadMax")} />
              {errors.capacidadMax && (
                <p className="text-sm text-red-600">{errors.capacidadMax.message}</p>
              )}
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="areaAtencion">
                Área de atención
              </label>
              <Input
                id="areaAtencion"
                placeholder="Ciencias básicas"
                {...register("areaAtencion")}
              />
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="letraEdificio">
                Letra de edificio
              </label>
              <Input id="letraEdificio" placeholder="B" {...register("letraEdificio")} />
            </div>
          </div>

          <div className="flex items-center gap-2">
            <input id="activo" type="checkbox" {...register("activo")} />
            <label className="text-sm font-medium text-text" htmlFor="activo">
              Tutor activo
            </label>
          </div>

          {errorMessage && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{errorMessage}</p>
          )}

          {feedback && (
            <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">{feedback}</p>
          )}

          <Button loading={createMutation.isPending || updateMutation.isPending} type="submit">
            {editingTutor ? "Guardar cambios" : "Registrar tutor"}
          </Button>
        </form>
      </Card>

      <div className="space-y-4">
        <Card>
          <div className="space-y-4">
            <div>
              <h2 className="text-lg font-semibold text-text">Listado de tutores</h2>
              <p className="text-sm text-slate-500">
                Consulta el estado y los alumnos asignados a cada tutor.
              </p>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-text" htmlFor="search-tutor">
                Buscar tutor
              </label>
              <SearchInput
                id="search-tutor"
                placeholder="Ingresa nombre del tutor"
                value={search}
                onChange={handleSearchChange}
                onClear={handleSearchClear}
                onSelect={(item) => handleSelectTutor(item as TutorResponse)}
                suggestions={suggestions}
                suggestionsType="tutor"
                isLoading={search.length >= 2 && !isSearchMode && isFetching}
              />
              {isSearchMode && <Badge variant="info">🔍 Búsqueda global</Badge>}
            </div>
          </div>
        </Card>
        <DataTable
          columns={tableColumns}
          data={displayTutors}
          isLoading={isLoading}
          emptyMessage="No hay tutores registrados aún."
        />

        {selectedTutor && tutorDetail && (
          <Card>
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-text">
                  Alumnos a cargo de {selectedTutor.nombre}
                </h3>
                <p className="text-sm text-slate-500">
                  Total registrados: {tutorDetail.alumnos.length}
                </p>
              </div>
              <Button type="button" variant="ghost" onClick={() => setSelectedTutor(null)}>
                Cerrar
              </Button>
            </div>
            <div className="mt-4 space-y-2 text-sm text-slate-600">
              {loadingStudents && <p>Cargando alumnos asignados...</p>}
              {!loadingStudents && !tutorDetail.alumnos.length && (
                <p>No hay alumnos asignados para este tutor.</p>
              )}
              {!loadingStudents &&
                tutorDetail.alumnos.map((student) => (
                  <div
                    key={student.id}
                    className="rounded-lg border border-border bg-white px-3 py-2"
                  >
                    <p className="font-medium text-text">{student.nombre}</p>
                    <p className="text-xs text-slate-500">
                      Matrícula {student.matricula} · Semestre {student.semestre}
                    </p>
                  </div>
                ))}
            </div>
          </Card>
        )}
      </div>
    </div>
  );
};
