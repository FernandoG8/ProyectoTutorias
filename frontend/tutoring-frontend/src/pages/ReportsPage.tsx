import { useMemo, useState, useCallback, useEffect, type FormEvent } from "react";
import dayjs from "dayjs";
import { useForm, type Resolver, type SubmitHandler } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQuery } from "@tanstack/react-query";
import type { ColumnDef } from "@tanstack/react-table";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Select";
import { Input } from "@/components/ui/Input";
import { Badge } from "@/components/ui/Badge";
import { DataTable } from "@/components/ui/DataTable";
import { Skeleton } from "@/components/ui/Skeleton";
import { Modal } from "@/components/ui/Modal";
import { SearchInput } from "@/components/SearchInput";
import {
  exportAlumnosPorTutor,
  exportCarrera,
  exportTodasLasCarreras,
  exportTodosLosTutores,
  fetchReportePorCarrera,
} from "@/services/reports-service";
import { searchTutors } from "@/services/tutors-service";
import { listSemestres } from "@/services/semestres-service";
import { useSemestreStore } from "@/store/semestre-store";
import type { CarreraResumen, ReporteCarrera, TutorResponse } from "@/types";

const tutorReportSchema = z.object({
  tutorId: z.coerce.number().min(1, "Selecciona un tutor."),
  formato: z.enum(["PDF", "EXCEL"]),
  periodo: z.string().min(1, "Indica el período."),
});

const careerReportSchema = z.object({
  codigoCarrera: z.string().min(1, "Ingresa el código."),
  formato: z.enum(["PDF", "EXCEL"]),
  periodo: z.string().min(1, "Selecciona un semestre."),
});

interface CarreraRow extends CarreraResumen {
  fechaGeneracion: string;
  semestre: string;
}

type TutorReportForm = z.infer<typeof tutorReportSchema>;
type CareerReportForm = z.infer<typeof careerReportSchema>;

const saveBlob = (blob: Blob, filename: string) => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", filename);
  document.body.appendChild(link);
  link.click();
  link.parentNode?.removeChild(link);
  window.URL.revokeObjectURL(url);
};

export const ReportsPage = () => {
  const { semestreActivo } = useSemestreStore();
  const [filters, setFilters] = useState({ semestre: "", carrera: "" });
  const [resumenFormato, setResumenFormato] = useState<"PDF" | "EXCEL">("EXCEL");
  const [submittedFilters, setSubmittedFilters] = useState<{
    semestreAcademico: string;
    carrera?: string;
  } | null>(null);
  const [filtersError, setFiltersError] = useState<string | null>(null);
  const [searchCarrera, setSearchCarrera] = useState("");
  const [selectedCareer, setSelectedCareer] = useState<CarreraResumen | null>(null);
  const [tutorSearch, setTutorSearch] = useState("");
  const [tutorSuggestions, setTutorSuggestions] = useState<TutorResponse[]>([]);

  const {
    register: registerTutor,
    handleSubmit: handleSubmitTutor,
    formState: { errors: tutorErrors },
    reset: resetTutor,
    setValue: setTutorValue,
  } = useForm<TutorReportForm>({
    resolver: zodResolver(tutorReportSchema) as Resolver<TutorReportForm>,
    defaultValues: {
      tutorId: 0,
      formato: "PDF",
      periodo: semestreActivo?.codigo || "",
    },
  });

  const {
    register: registerCareer,
    handleSubmit: handleSubmitCareer,
    formState: { errors: careerErrors },
    reset: resetCareer,
  } = useForm<CareerReportForm>({
    resolver: zodResolver(careerReportSchema) as Resolver<CareerReportForm>,
    defaultValues: {
      codigoCarrera: "",
      formato: "EXCEL",
      periodo: semestreActivo?.codigo || "",
    },
  });

  // Query para obtener semestres dinámicamente
  const { data: semestres = [] } = useQuery({
    queryKey: ["semestres"],
    queryFn: () => listSemestres(),
  });

  const tutorSearchQuery = useQuery({
    queryKey: ["search-tutors-report", tutorSearch],
    queryFn: () => searchTutors({ q: tutorSearch, limit: 10 }),
    enabled: tutorSearch.length >= 2,
  });

  // Update suggestions when autocomplete query results change
  const handleTutorSearchChange = useCallback((value: string) => {
    setTutorSearch(value);
    if (value.length < 2) {
      setTutorSuggestions([]);
    }
  }, []);

  useEffect(() => {
    if (tutorSearch.length >= 2 && tutorSearchQuery.data) {
      setTutorSuggestions(tutorSearchQuery.data);
    }
  }, [tutorSearchQuery.data, tutorSearch]);

  const handleTutorSelect = useCallback(
    (item: TutorResponse | { id?: number; nombre: string }) => {
      if ("id" in item && item.id) {
        setTutorValue("tutorId", item.id);
        setTutorSearch(item.nombre);
        setTutorSuggestions([]);
      }
    },
    [setTutorValue],
  );

  const reportQuery = useQuery<ReporteCarrera>({
    queryKey: ["reporte-por-carrera", submittedFilters],
    queryFn: () => fetchReportePorCarrera(submittedFilters!),
    enabled: submittedFilters !== null,
  });

  const carreraRows = useMemo<CarreraRow[]>(() => {
    if (!reportQuery.data) return [];
    return reportQuery.data.carreras.map((carrera) => ({
      ...carrera,
      fechaGeneracion: reportQuery.data!.fechaGeneracion,
      semestre: reportQuery.data!.semestre,
    }));
  }, [reportQuery.data]);

  const filteredRows = useMemo(() => {
    if (!searchCarrera.trim()) return carreraRows;
    const term = searchCarrera.trim().toLowerCase();
    return carreraRows.filter((row) => row.carrera.toLowerCase().includes(term));
  }, [carreraRows, searchCarrera]);

  const tutorReportMutation = useMutation({
    mutationFn: ({ tutorId, formato, periodo }: TutorReportForm) =>
      exportAlumnosPorTutor(tutorId, { formato, periodo }),
  });

  const careerReportMutation = useMutation({
    mutationFn: ({ codigoCarrera, formato, periodo }: CareerReportForm) =>
      exportCarrera(codigoCarrera, { formato, periodo }),
  });

  const downloadAllMutation = useMutation({
    mutationFn: (payload: { periodo?: string; formato: "PDF" | "EXCEL" }) =>
      exportTodasLasCarreras({ formato: payload.formato, periodo: payload.periodo }),
  });

  const downloadCarreraMutation = useMutation({
    mutationFn: (payload: { codigo: string; formato: "PDF" | "EXCEL"; periodo?: string }) =>
      exportCarrera(payload.codigo, {
        formato: payload.formato,
        periodo: payload.periodo,
      }),
  });

  const handleDownloadCarrera = useCallback(
    async (codigo: string) => {
      if (!submittedFilters) return;
      const blob = await downloadCarreraMutation.mutateAsync({
        codigo,
        formato: resumenFormato,
        periodo: submittedFilters.semestreAcademico,
      });
      saveBlob(blob, `reporte-carrera-${codigo}.${resumenFormato === "PDF" ? "pdf" : "xlsx"}`);
    },
    [downloadCarreraMutation, submittedFilters, resumenFormato],
  );

  const downloadTutoresMutation = useMutation({
    mutationFn: (payload: { periodo?: string; formato: "PDF" | "EXCEL" }) =>
      exportTodosLosTutores({ formato: payload.formato, periodo: payload.periodo }),
  });

  const columns: ColumnDef<CarreraRow>[] = useMemo(
    () => [
      {
        header: "Carrera",
        accessorKey: "carrera",
        cell: ({ getValue }) => (
          <span className="font-semibold text-text">{getValue<string>()}</span>
        ),
      },
      {
        header: "Alumnos",
        accessorKey: "totalAlumnos",
      },
      {
        header: "Tutores",
        accessorKey: "totalTutores",
      },
      {
        header: "Estado",
        cell: ({ row }) => (
          <Badge variant={row.original.totalTutores > 0 ? "success" : "warning"}>
            {row.original.totalTutores > 0 ? "Con tutores" : "Sin tutores"}
          </Badge>
        ),
      },
      {
        header: "Generado",
        accessorKey: "fechaGeneracion",
        cell: ({ getValue }) => dayjs(getValue<string>()).format("DD/MM/YYYY HH:mm"),
      },
      {
        header: "Acciones",
        cell: ({ row }) => (
          <div className="flex flex-wrap gap-2">
            <Button
              type="button"
              variant="ghost"
              className="text-xs"
              onClick={() => setSelectedCareer(row.original)}
            >
              Ver tutores
            </Button>
            <Button
              type="button"
              variant="secondary"
              className="text-xs"
              onClick={() => handleDownloadCarrera(row.original.carrera)}
              disabled={downloadCarreraMutation.isPending}
            >
              Descargar
            </Button>
          </div>
        ),
      },
    ],
    [handleDownloadCarrera, downloadCarreraMutation.isPending],
  );

  const onSubmitTutor: SubmitHandler<TutorReportForm> = async (values) => {
    const blob = await tutorReportMutation.mutateAsync(values);
    saveBlob(blob, `reporte-tutor-${values.tutorId}.${values.formato === "PDF" ? "pdf" : "xlsx"}`);
    resetTutor({ tutorId: 0, formato: values.formato, periodo: "" });
    setTutorSearch("");
    setTutorSuggestions([]);
  };

  const onSubmitCareer: SubmitHandler<CareerReportForm> = async (values) => {
    const blob = await careerReportMutation.mutateAsync(values);
    saveBlob(
      blob,
      `reporte-carrera-${values.codigoCarrera}.${values.formato === "PDF" ? "pdf" : "xlsx"}`,
    );
    resetCareer({ codigoCarrera: "", formato: values.formato, periodo: values.periodo });
  };

  const handleFiltersSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!filters.semestre.trim()) {
      setFiltersError("Ingresa el semestre académico que deseas consultar.");
      return;
    }
    setFiltersError(null);
    setSubmittedFilters({
      semestreAcademico: filters.semestre.trim(),
      carrera: filters.carrera.trim() ? filters.carrera.trim() : undefined,
    });
  };

  const handleDownloadAll = async () => {
    const periodo = filters.semestre.trim() || undefined;
    const blob = await downloadAllMutation.mutateAsync({ periodo, formato: resumenFormato });
    const extension = resumenFormato === "PDF" ? "zip" : "zip";
    saveBlob(blob, `reportes-carreras-${filters.semestre.trim() || "todos"}.${extension}`);
  };

  const handleDownloadAllTutors = async () => {
    const periodo = filters.semestre.trim() || undefined;
    const blob = await downloadTutoresMutation.mutateAsync({ periodo, formato: resumenFormato });
    saveBlob(blob, `reportes-tutores-${filters.semestre.trim() || "todos"}.zip`);
  };

  return (
    <div className="space-y-6">
      <Card>
        <div className="space-y-4">
          <div className="flex flex-col gap-2">
            <h2 className="text-lg font-semibold text-text">Resumen por carrera</h2>
            <p className="text-sm text-slate-600">
              Consulta el panorama general de tutorías por programa académico y descarga reportes al instante.
            </p>
          </div>

          <form className="grid gap-4 md:grid-cols-[2fr,2fr,1.2fr,1fr]" onSubmit={handleFiltersSubmit}>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="semestre-consulta">
                Semestre académico
              </label>
              <Select
                id="semestre-consulta"
                value={filters.semestre}
                onChange={(event) => setFilters((prev) => ({ ...prev, semestre: event.target.value }))}
              >
                <option value="">Selecciona un semestre</option>
                {semestres.map((sem) => (
                  <option key={sem.id} value={sem.codigo}>
                    {sem.codigo} - {sem.nombre}
                  </option>
                ))}
              </Select>
              <p className="text-xs text-slate-500">Selecciona el semestre académico a consultar.</p>
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="carrera-consulta">
                Carrera (opcional)
              </label>
              <Input
                id="carrera-consulta"
                placeholder="ISC, ADM, ..."
                value={filters.carrera}
                onChange={(event) => setFilters((prev) => ({ ...prev, carrera: event.target.value }))}
              />
              <p className="text-xs text-slate-500">Ingresa el código de carrera si deseas limitar la consulta.</p>
            </div>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="formato-resumen">
                Formato
              </label>
              <Select
                id="formato-resumen"
                value={resumenFormato}
                onChange={(event) => setResumenFormato(event.target.value as "PDF" | "EXCEL")}
              >
                <option value="PDF">PDF</option>
                <option value="EXCEL">Excel</option>
              </Select>
              <p className="text-xs text-slate-500">Elige el formato para exportar el resumen.</p>
            </div>
            <div className="flex items-end gap-2">
              <Button type="submit" className="flex-1">
                Consultar
              </Button>
              <Button
                type="button"
                variant="secondary"
                className="hidden sm:flex"
                onClick={handleDownloadAll}
                disabled={downloadAllMutation.isPending}
              >
                Descargar ZIP
              </Button>
              <Button
                type="button"
                variant="secondary"
                className="hidden sm:flex"
                onClick={handleDownloadAllTutors}
                disabled={downloadTutoresMutation.isPending}
              >
                ZIP tutores
              </Button>
            </div>
          </form>
          {filtersError && (
            <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">{filtersError}</p>
          )}

          <div className="space-y-3">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="text-sm text-slate-500">
                {submittedFilters ? (
                  <span>
                    Resultados para {submittedFilters.carrera ?? "todas las carreras"} en el semestre {submittedFilters.semestreAcademico}.
                  </span>
                ) : (
                  <span>Ingresa un semestre académico para visualizar el resumen.</span>
                )}
              </div>
              <div className="w-full sm:w-64">
                <Input
                  placeholder="Buscar carrera"
                  value={searchCarrera}
                  onChange={(event) => setSearchCarrera(event.target.value)}
                />
              </div>
            </div>

            {reportQuery.isLoading ? (
              <div className="grid gap-3">
                <Skeleton className="h-24" />
                <Skeleton className="h-24" />
              </div>
            ) : (
              <div className="overflow-x-auto">
                <DataTable
                  columns={columns}
                  data={filteredRows}
                  isLoading={reportQuery.isFetching && !reportQuery.isLoading}
                  emptyMessage="Consulta un semestre para obtener información consolidada."
                />
              </div>
            )}
          </div>
        </div>
      </Card>

      <div className="grid gap-6 xl:grid-cols-2">
        <Card>
          <h3 className="text-lg font-semibold text-text">Reporte por tutor</h3>
          <p className="text-sm text-slate-600">Descarga la relación de alumnos acompañados por un tutor en un período determinado.</p>

          <form className="mt-6 space-y-4" onSubmit={handleSubmitTutor(onSubmitTutor)}>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="tutorSearch">
                Tutor
              </label>
              <SearchInput
                id="tutorSearch"
                placeholder="Ej. Juan Pérez"
                value={tutorSearch}
                onChange={handleTutorSearchChange}
                onSelect={(item) => handleTutorSelect(item as TutorResponse)}
                suggestions={tutorSuggestions}
                suggestionsType="tutor"
                isLoading={tutorSearch.length >= 2 && tutorSearchQuery.isFetching}
              />
              <input type="hidden" {...registerTutor("tutorId")} />
              {tutorErrors.tutorId && (
                <p className="text-sm text-rose-600">{tutorErrors.tutorId.message}</p>
              )}
              <p className="text-xs text-slate-500">Escribe 2+ caracteres para buscar por nombre.</p>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-1">
                <label className="text-sm font-medium text-text" htmlFor="formatoTutor">
                  Formato
                </label>
                <Select id="formatoTutor" defaultValue="PDF" {...registerTutor("formato")}>
                  <option value="PDF">PDF</option>
                  <option value="EXCEL">Excel</option>
                </Select>
              </div>
              <div className="space-y-1">
                <label className="text-sm font-medium text-text" htmlFor="periodoTutor">
                  Período
                </label>
                <Select id="periodoTutor" defaultValue={semestreActivo?.codigo || ""} {...registerTutor("periodo")}>
                  <option value="" disabled>
                    Selecciona un semestre
                  </option>
                  {semestres.map((sem) => (
                    <option key={sem.id} value={sem.codigo}>
                      {sem.codigo} - {sem.nombre}
                    </option>
                  ))}
                </Select>
                {tutorErrors.periodo && (
                  <p className="text-sm text-rose-600">{tutorErrors.periodo.message}</p>
                )}
              </div>
            </div>

            <Button loading={tutorReportMutation.isPending} type="submit">
              Descargar reporte
            </Button>

            {tutorReportMutation.error && (
              <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
                No se pudo generar el reporte. Intenta nuevamente más tarde.
              </p>
            )}
          </form>
        </Card>

        <Card>
          <h3 className="text-lg font-semibold text-text">Reporte por carrera</h3>
          <p className="text-sm text-slate-600">Obtén el consolidado de tutorías por programa académico.</p>

          <form className="mt-6 space-y-4" onSubmit={handleSubmitCareer(onSubmitCareer)}>
            <div className="space-y-1">
              <label className="text-sm font-medium text-text" htmlFor="codigoCarrera">
                Código de carrera
              </label>
              <Input id="codigoCarrera" placeholder="ING-SOFT" {...registerCareer("codigoCarrera")} />
              {careerErrors.codigoCarrera && (
                <p className="text-sm text-rose-600">{careerErrors.codigoCarrera.message}</p>
              )}
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <div className="space-y-1">
                <label className="text-sm font-medium text-text" htmlFor="formatoCarrera">
                  Formato
                </label>
                <Select id="formatoCarrera" defaultValue="EXCEL" {...registerCareer("formato")}>
                  <option value="PDF">PDF</option>
                  <option value="EXCEL">Excel</option>
                </Select>
              </div>
              <div className="space-y-1">
                <label className="text-sm font-medium text-text" htmlFor="periodoCarrera">
                  Semestre académico
                </label>
                <Select id="periodoCarrera" defaultValue={semestreActivo?.codigo || ""} {...registerCareer("periodo")}>
                  <option value="" disabled>
                    Selecciona un semestre
                  </option>
                  {semestres.map((sem) => (
                    <option key={sem.id} value={sem.codigo}>
                      {sem.codigo} - {sem.nombre}
                    </option>
                  ))}
                </Select>
                {careerErrors.periodo && (
                  <p className="text-sm text-rose-600">{careerErrors.periodo.message}</p>
                )}
              </div>
            </div>

            <Button loading={careerReportMutation.isPending} type="submit">
              Descargar consolidado
            </Button>

            {careerReportMutation.error && (
              <p className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-600">
                No se pudo generar el reporte solicitado.
              </p>
            )}
          </form>
        </Card>
      </div>

      <Modal
        open={Boolean(selectedCareer)}
        onClose={() => setSelectedCareer(null)}
        title={selectedCareer ? `Tutores de ${selectedCareer.carrera}` : ""}
        description={selectedCareer ? `Alumnos asignados en el semestre ${submittedFilters?.semestreAcademico ?? "consultado"}.` : undefined}
      >
        {selectedCareer && selectedCareer.tutores.length > 0 ? (
          <div className="space-y-3">
            {selectedCareer.tutores.map((tutor) => (
              <div key={tutor.id} className="rounded-lg border border-border px-3 py-2">
                <div className="flex items-center justify-between text-sm font-semibold text-text">
                  <span>{tutor.nombre}</span>
                  <span>
                    {tutor.cargaActual}/{tutor.capacidadMax}
                  </span>
                </div>
                <p className="text-xs text-slate-500">
                  Área: {tutor.areaAtencion ?? "-"} • Edificio: {tutor.letraEdificio ?? "-"}
                </p>
                <p className="text-xs text-slate-500">Alumnos asignados: {tutor.alumnos.length}</p>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-slate-600">No se encontraron tutores para esta carrera en el período consultado.</p>
        )}
      </Modal>
    </div>
  );
};
