import { useCallback, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { DataTable } from "@/components/ui/DataTable";
import {
  createTutor,
  fetchTutorStudents,
  fetchTutors,
  updateTutor,
} from "@/services/tutors-service";
import type { Tutor } from "@/types";

const tutorSchema = z.object({
  nombre: z.string().min(1, "Ingresa el nombre completo."),
  email: z.string().email("Ingresa un correo válido."),
  telefono: z.string().optional(),
  especialidad: z.string().optional(),
});

type TutorForm = z.infer<typeof tutorSchema>;

const columns = (
  onEdit: (tutor: Tutor) => void,
  onViewStudents: (tutor: Tutor) => void,
): ColumnDef<Tutor>[] => [
  { header: "Nombre", accessorKey: "nombre" },
  { header: "Correo", accessorKey: "email" },
  {
    header: "Teléfono",
    accessorKey: "telefono",
    cell: ({ getValue }) => getValue() ?? "Sin registrar",
  },
  {
    header: "Especialidad",
    accessorKey: "especialidad",
    cell: ({ getValue }) => getValue() ?? "General",
  },
  {
    header: "Alumnos",
    accessorKey: "totalAlumnos",
    cell: ({ getValue }) => getValue() ?? 0,
  },
  {
    header: "Acciones",
    cell: ({ row }) => (
      <div className="flex flex-wrap gap-2">
        <Button
          type="button"
          variant="secondary"
          onClick={() => onEdit(row.original)}
        >
          Editar
        </Button>
        <Button
          type="button"
          variant="ghost"
          onClick={() => onViewStudents(row.original)}
        >
          Ver alumnos
        </Button>
      </div>
    ),
  },
];

export const TutorsPage = () => {
  const queryClient = useQueryClient();
  const [editingTutor, setEditingTutor] = useState<Tutor | null>(null);
  const [selectedTutor, setSelectedTutor] = useState<Tutor | null>(null);
  const [feedback, setFeedback] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const {
    data: tutors = [],
    isLoading,
  } = useQuery({
    queryKey: ["tutors"],
    queryFn: fetchTutors,
  });

  const {
    data: tutorStudents = [],
    isLoading: loadingStudents,
  } = useQuery({
    queryKey: ["tutor-students", selectedTutor?.id],
    queryFn: () => fetchTutorStudents(selectedTutor!.id),
    enabled: Boolean(selectedTutor?.id),
  });

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<TutorForm>({
    resolver: zodResolver(tutorSchema),
    defaultValues: {
      nombre: "",
      email: "",
      telefono: "",
      especialidad: "",
    },
  });

  const createMutation = useMutation({
    mutationFn: createTutor,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tutors"] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: TutorForm }) =>
      updateTutor(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tutors"] });
    },
  });

  const onSubmit = async (values: TutorForm) => {
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
    (tutor: Tutor) => {
      setEditingTutor(tutor);
      reset({
        nombre: tutor.nombre,
        email: tutor.email,
        telefono: tutor.telefono ?? "",
        especialidad: tutor.especialidad ?? "",
      });
      setFeedback(null);
    },
    [reset],
  );

  const handleViewStudents = useCallback((tutor: Tutor) => {
    setSelectedTutor(tutor);
  }, []);

  const tableColumns = useMemo(
    () => columns(startEdit, handleViewStudents),
    [handleViewStudents, startEdit],
  );

  const clearForm = () => {
    setEditingTutor(null);
    reset({ nombre: "", email: "", telefono: "", especialidad: "" });
    setFeedback(null);
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

          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="email">
              Correo institucional
            </label>
            <Input id="email" placeholder="maria.perez@uni.edu" {...register("email")} />
            {errors.email && (
              <p className="text-sm text-red-600">{errors.email.message}</p>
            )}
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="telefono">
                Teléfono de contacto
              </label>
              <Input id="telefono" placeholder="987654321" {...register("telefono")} />
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="especialidad">
                Especialidad
              </label>
              <Input id="especialidad" placeholder="Ingeniería de software" {...register("especialidad")} />
            </div>
          </div>

          {errorMessage && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              {errorMessage}
            </p>
          )}

          {feedback && (
            <p className="rounded-lg bg-green-50 px-3 py-2 text-sm text-green-700">
              {feedback}
            </p>
          )}

          <Button
            loading={createMutation.isPending || updateMutation.isPending}
            type="submit"
          >
            {editingTutor ? "Guardar cambios" : "Registrar tutor"}
          </Button>
        </form>
      </Card>

      <div className="space-y-4">
        <Card>
          <h2 className="text-lg font-semibold text-text">Listado de tutores</h2>
          <p className="text-sm text-slate-500">
            Consulta el estado y los alumnos asignados a cada tutor.
          </p>
        </Card>
        <DataTable
          columns={tableColumns}
          data={tutors}
          isLoading={isLoading}
          emptyMessage="No hay tutores registrados aún."
        />

        {selectedTutor && (
          <Card>
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-semibold text-text">
                  Alumnos a cargo de {selectedTutor.nombre}
                </h3>
                <p className="text-sm text-slate-500">
                  Total registrados: {selectedTutor.totalAlumnos ?? tutorStudents.length}
                </p>
              </div>
              <Button type="button" variant="ghost" onClick={() => setSelectedTutor(null)}>
                Cerrar
              </Button>
            </div>
            <div className="mt-4 space-y-2 text-sm text-slate-600">
              {loadingStudents && <p>Cargando alumnos asignados...</p>}
              {!loadingStudents && tutorStudents.length === 0 && (
                <p>No hay alumnos asignados para este tutor.</p>
              )}
              {!loadingStudents &&
                tutorStudents.map((student) => (
                  <div
                    key={student.id}
                    className="rounded-lg border border-border bg-white px-3 py-2"
                  >
                    <p className="font-medium text-text">{student.nombre}</p>
                    <p className="text-xs text-slate-500">
                      Código {student.codigo} · {student.carrera}
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
