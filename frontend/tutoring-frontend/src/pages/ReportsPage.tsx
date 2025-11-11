import { useForm, type Resolver, type SubmitHandler } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { Select } from "@/components/ui/Select";
import { Input } from "@/components/ui/Input";
import { downloadCareerReport, downloadTutorReport } from "@/services/reports-service";
import { fetchTutors } from "@/services/tutors-service";

const tutorReportSchema = z.object({
  tutorId: z.coerce.number().min(1, "Selecciona un tutor."),
  formato: z.enum(["PDF", "EXCEL"]),
  periodo: z.string().min(1, "Indica el período."),
});

const careerReportSchema = z.object({
  codigoCarrera: z.string().min(1, "Ingresa el código."),
  formato: z.enum(["PDF", "EXCEL"]),
  periodo: z.string().min(1, "Indica el período."),
});

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
  const {
    register: registerTutor,
    handleSubmit: handleSubmitTutor,
    formState: { errors: tutorErrors },
    reset: resetTutor,
  } = useForm<TutorReportForm>({
    resolver: zodResolver(tutorReportSchema) as Resolver<TutorReportForm>,
    defaultValues: {
      tutorId: 0,
      formato: "PDF",
      periodo: "",
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
      periodo: "",
    },
  });

  const {
    data: tutors = [],
    isLoading: tutorsLoading,
  } = useQuery({
    queryKey: ["tutors"],
    queryFn: fetchTutors,
  });

  const tutorReportMutation = useMutation({
    mutationFn: downloadTutorReport,
  });

  const careerReportMutation = useMutation({
    mutationFn: downloadCareerReport,
  });

  const onSubmitTutor: SubmitHandler<TutorReportForm> = async (values) => {
    const blob = await tutorReportMutation.mutateAsync(values);
    saveBlob(blob, `reporte-tutor-${values.tutorId}.${values.formato === "PDF" ? "pdf" : "xlsx"}`);
    resetTutor({ tutorId: 0, formato: values.formato, periodo: "" });
  };

  const onSubmitCareer: SubmitHandler<CareerReportForm> = async (values) => {
    const blob = await careerReportMutation.mutateAsync(values);
    saveBlob(
      blob,
      `reporte-carrera-${values.codigoCarrera}.${values.formato === "PDF" ? "pdf" : "xlsx"}`,
    );
    resetCareer({ codigoCarrera: "", formato: values.formato, periodo: "" });
  };

  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <Card>
        <h2 className="text-lg font-semibold text-text">Reporte por tutor</h2>
        <p className="text-sm text-slate-500">
          Descarga la relación de alumnos acompañados por un tutor en un período determinado.
        </p>

        <form className="mt-6 space-y-4" onSubmit={handleSubmitTutor(onSubmitTutor)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="tutorId">
              Tutor
            </label>
            <Select id="tutorId" defaultValue="0" {...registerTutor("tutorId")}
              disabled={tutorsLoading}
            >
              <option value="0" disabled>
                Selecciona un tutor
              </option>
              {tutors.map((tutor) => (
                <option key={tutor.id} value={tutor.id}>
                  {tutor.nombre}
                </option>
              ))}
            </Select>
            {tutorErrors.tutorId && (
              <p className="text-sm text-red-600">{tutorErrors.tutorId.message}</p>
            )}
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
              <Input id="periodoTutor" placeholder="2025" {...registerTutor("periodo")} />
              {tutorErrors.periodo && (
                <p className="text-sm text-red-600">{tutorErrors.periodo.message}</p>
              )}
            </div>
          </div>

          <Button loading={tutorReportMutation.isPending} type="submit">
            Descargar reporte
          </Button>

          {tutorReportMutation.error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              No se pudo generar el reporte. Intenta nuevamente más tarde.
            </p>
          )}
        </form>
      </Card>

      <Card>
        <h2 className="text-lg font-semibold text-text">Reporte por carrera</h2>
        <p className="text-sm text-slate-500">
          Obtén el consolidado de tutorías por programa académico.
        </p>

        <form className="mt-6 space-y-4" onSubmit={handleSubmitCareer(onSubmitCareer)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-text" htmlFor="codigoCarrera">
              Código de carrera
            </label>
            <Input
              id="codigoCarrera"
              placeholder="ING-SOFT"
              {...registerCareer("codigoCarrera")}
            />
            {careerErrors.codigoCarrera && (
              <p className="text-sm text-red-600">{careerErrors.codigoCarrera.message}</p>
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
                Período
              </label>
              <Input id="periodoCarrera" placeholder="2025" {...registerCareer("periodo")} />
              {careerErrors.periodo && (
                <p className="text-sm text-red-600">{careerErrors.periodo.message}</p>
              )}
            </div>
          </div>

          <Button loading={careerReportMutation.isPending} type="submit">
            Descargar consolidado
          </Button>

          {careerReportMutation.error && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
              No se pudo generar el reporte solicitado.
            </p>
          )}
        </form>
      </Card>
    </div>
  );
};
