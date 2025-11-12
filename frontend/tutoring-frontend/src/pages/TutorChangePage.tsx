import { useEffect, useMemo, useState } from "react";
import { useForm, type Resolver, type SubmitHandler } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Input } from "@/components/ui/Input";
import { Textarea } from "@/components/ui/Textarea";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Select";
import { Badge } from "@/components/ui/Badge";
import { Skeleton } from "@/components/ui/Skeleton";
import { listTutors } from "@/services/tutors-service";
import { listStudents } from "@/services/alumnos-service";
import { requestTutorChange } from "@/services/asignaciones-service";
import { useDebounce } from "@/lib/use-debounce";
import type { AlumnoPagedResponse, AlumnoResponse, TutorResponse } from "@/types";
import { useAuthStore } from "@/store/auth-store";

const schema = z
  .object({
    alumnoId: z.coerce.number().min(1, "Selecciona un alumno válido."),
    tutorOrigenId: z.coerce.number().min(1, "Selecciona el tutor actual."),
    tutorDestinoId: z.coerce.number().min(1, "Selecciona un tutor."),
    motivo: z.string().min(5, "Describe el motivo de la reasignación."),
    usuario: z.string().min(1, "Ingresa tu usuario."),
    semestreAcademico: z
      .string()
      .regex(/^[0-9]{4}-[12]$/, "Ingresa un semestre válido en formato AAAA-1."),
  })
  .refine((data) => data.tutorDestinoId !== data.tutorOrigenId, {
    message: "El tutor destino debe ser distinto al tutor origen.",
    path: ["tutorDestinoId"],
  });

type FormValues = z.infer<typeof schema>;

export const TutorChangePage = () => {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);
  const canReassign = user?.roles.includes("ROLE_COORDINADOR_TUTORIAS") ?? false;

  const {
    data: tutors = [],
    isLoading: tutorsLoading,
  } = useQuery<TutorResponse[]>({
    queryKey: ["tutors"],
    queryFn: () => listTutors({ activos: true }),
  });

  const [search, setSearch] = useState("");
  const [selectedStudent, setSelectedStudent] = useState<AlumnoResponse | null>(null);
  const debouncedSearch = useDebounce(search, 400);

  const studentsQuery = useQuery<AlumnoPagedResponse>({
    queryKey: ["students-for-change"],
    queryFn: () => listStudents({ estado: "ACTIVO", limit: 150, page: 1 }),
  });

  const students = studentsQuery.data?.items ?? [];

  const filteredStudents = useMemo(() => {
    if (!debouncedSearch) {
      return students.slice(0, 8);
    }
    const term = debouncedSearch.trim().toLowerCase();
    return students.filter((student) =>
      [student.matricula, student.nombre]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(term)),
    );
  }, [students, debouncedSearch]);

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema) as Resolver<FormValues>,
    defaultValues: {
      alumnoId: 0,
      tutorOrigenId: 0,
      tutorDestinoId: 0,
      motivo: "",
      usuario: user?.username ?? "",
      semestreAcademico: "",
    },
  });

  useEffect(() => {
    if (user?.username) {
      setValue("usuario", user.username);
    }
  }, [user?.username, setValue]);

  useEffect(() => {
    if (selectedStudent) {
      setValue("alumnoId", selectedStudent.id);
      setValue("tutorOrigenId", selectedStudent.tutor?.id ?? 0);
    } else {
      setValue("alumnoId", 0);
      setValue("tutorOrigenId", 0);
    }
  }, [selectedStudent, setValue]);

  const mutation = useMutation({
    mutationFn: requestTutorChange,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["students-for-change"] });
      reset({
        alumnoId: 0,
        tutorOrigenId: 0,
        tutorDestinoId: 0,
        motivo: "",
        usuario: user?.username ?? "",
        semestreAcademico: "",
      });
      setSelectedStudent(null);
    },
  });

  const onSubmit: SubmitHandler<FormValues> = async (values) => {
    await mutation.mutateAsync(values);
  };

  const tutorOptions = useMemo(() => {
    if (!selectedStudent) {
      return tutors.map((tutor) => ({ value: tutor.id, label: tutor.nombre, carrera: tutor.carrera }));
    }
    return tutors
      .filter((tutor) => tutor.carrera === selectedStudent.carrera)
      .map((tutor) => ({ value: tutor.id, label: tutor.nombre, carrera: tutor.carrera }));
  }, [tutors, selectedStudent]);

  const selectedTutorOrigen = selectedStudent?.tutor;

  return (
    <div className="space-y-6">
      <div className="grid gap-6 xl:grid-cols-[3fr,2fr]">
        <Card>
          <div className="space-y-2">
            <h2 className="text-lg font-semibold text-text">Reasignación manual de tutor</h2>
            <p className="text-sm text-slate-600">
              Busca al alumno por matrícula o nombre, confirma al tutor actual y selecciona al nuevo tutor dentro de la misma carrera.
            </p>
          </div>

          <div className="mt-6 space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium text-text" htmlFor="search-student-change">
                Buscar alumno
              </label>
              <Input
                id="search-student-change"
                placeholder="Ej. 202501234"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
              <p className="text-xs text-slate-500">La búsqueda aplica sobre los alumnos activos disponibles para reasignación.</p>
            </div>

            {studentsQuery.isLoading ? (
              <Skeleton className="h-32" />
            ) : (
              <div className="grid gap-2">
                {filteredStudents.length === 0 && (
                  <p className="rounded-lg border border-dashed border-primary/40 bg-primary/5 px-3 py-2 text-sm text-primary">
                    No se encontraron alumnos con los criterios ingresados.
                  </p>
                )}
                {filteredStudents.map((student) => (
                  <button
                    type="button"
                    key={student.id}
                    className={`flex flex-col rounded-xl border px-3 py-2 text-left transition ${
                      selectedStudent?.id === student.id
                        ? "border-primary bg-primary/5"
                        : "border-border hover:border-primary/40"
                    }`}
                    onClick={() => setSelectedStudent(student)}
                  >
                    <div className="flex items-center justify-between text-sm">
                      <span className="font-semibold text-text">{student.nombre}</span>
                      <span className="text-xs text-slate-500">{student.matricula}</span>
                    </div>
                    <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-slate-500">
                      <Badge variant="info">{student.carrera}</Badge>
                      <span>Semestre {student.semestre}</span>
                      <span>
                        Tutor: {student.tutor?.nombre ?? "Sin asignar"}
                      </span>
                    </div>
                  </button>
                ))}
              </div>
            )}

            <form className="space-y-5" onSubmit={handleSubmit(onSubmit)}>
              <input type="hidden" {...register("alumnoId", { valueAsNumber: true })} />
              <input type="hidden" {...register("tutorOrigenId", { valueAsNumber: true })} />

              <div className="rounded-xl border border-border bg-slate-50 px-4 py-3 text-sm text-slate-600">
                {selectedStudent ? (
                  <div className="space-y-1">
                    <p className="font-semibold text-text">Alumno seleccionado</p>
                    <p>{selectedStudent.nombre} ({selectedStudent.matricula})</p>
                    <p className="text-xs text-slate-500">
                      Carrera: {selectedStudent.carrera} • Semestre {selectedStudent.semestre}
                    </p>
                    {selectedTutorOrigen ? (
                      <p className="text-xs text-slate-500">Tutor actual: {selectedTutorOrigen.nombre}</p>
                    ) : (
                      <p className="text-xs text-amber-600">El alumno no tiene tutor asignado actualmente.</p>
                    )}
                  </div>
                ) : (
                  <p>Selecciona un alumno para habilitar la reasignación.</p>
                )}
              </div>

              {selectedStudent && !selectedTutorOrigen && (
                <p className="rounded-lg bg-amber-50 px-3 py-2 text-xs text-amber-700">
                  Debes asignar un tutor inicial al alumno antes de registrar una reasignación manual.
                </p>
              )}

              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-1">
                  <label className="text-sm font-medium text-text" htmlFor="tutorDestinoId">
                    Tutor destino
                  </label>
                  <Select
                    id="tutorDestinoId"
                    defaultValue="0"
                    {...register("tutorDestinoId")}
                    disabled={tutorsLoading || !selectedStudent}
                  >
                    <option value="0" disabled>
                      Selecciona un tutor disponible
                    </option>
                    {tutorOptions.map((option) => (
                      <option key={option.value} value={option.value}>
                        {option.label}
                      </option>
                    ))}
                  </Select>
                  {selectedStudent && tutorOptions.length === 0 && (
                    <p className="text-xs text-amber-600">
                      No hay tutores disponibles en {selectedStudent.carrera}. Verifica la carga de tutores antes de continuar.
                    </p>
                  )}
                  {errors.tutorDestinoId && (
                    <p className="text-sm text-rose-600">{errors.tutorDestinoId.message}</p>
                  )}
                </div>
                <div className="space-y-1">
                  <label className="text-sm font-medium text-text" htmlFor="semestreAcademico">
                    Semestre académico
                  </label>
                  <Input
                    id="semestreAcademico"
                    placeholder="2025-1"
                    {...register("semestreAcademico")}
                  />
                  <p className="text-xs text-slate-500">Este dato se registra en el historial del alumno y habilita los reportes del período.</p>
                  {errors.semestreAcademico && (
                    <p className="text-sm text-rose-600">{errors.semestreAcademico.message}</p>
                  )}
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-sm font-medium text-text" htmlFor="motivo">
                  Motivo de la reasignación
                </label>
                <Textarea
                  id="motivo"
                  rows={4}
                  placeholder="Describe la razón de la solicitud por parte del alumno, tutor o programa."
                  {...register("motivo")}
                />
                {errors.motivo && (
                  <p className="text-sm text-rose-600">{errors.motivo.message}</p>
                )}
              </div>

              <div className="space-y-1">
                <label className="text-sm font-medium text-text" htmlFor="usuario">
                  Usuario responsable
                </label>
                <Input id="usuario" placeholder="coord_tutorias" {...register("usuario")} />
                {errors.usuario && (
                  <p className="text-sm text-rose-600">{errors.usuario.message}</p>
                )}
              </div>

              {!canReassign && (
                <p className="rounded-lg bg-amber-50 px-3 py-2 text-sm text-amber-700">
                  No tienes permisos para realizar la reasignación manual.
                </p>
              )}

              {mutation.error && (
                <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
                  Ocurrió un problema al registrar la reasignación. Inténtalo nuevamente.
                </p>
              )}

              {mutation.isSuccess && (
                <p className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-700">
                  La reasignación se registró correctamente.
                </p>
              )}

              <Button
                disabled={!canReassign || !selectedStudent || !selectedTutorOrigen}
                loading={mutation.isPending}
                type="submit"
              >
                Registrar cambio
              </Button>
            </form>
          </div>
        </Card>

        <Card>
          <h2 className="text-lg font-semibold text-text">Guía para la reasignación</h2>
          <ul className="mt-4 space-y-3 text-sm text-slate-600">
            <li>• Confirma que el estudiante esté activo en el período académico vigente.</li>
            <li>• Notifica al tutor actual y destino sobre la solicitud y la fecha de cambio.</li>
            <li>• Documenta en el expediente del alumno el motivo y la evidencia asociada.</li>
            <li>• Los cambios quedan registrados en el historial del módulo de reportes.</li>
          </ul>
        </Card>
      </div>
    </div>
  );
};
