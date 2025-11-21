import { useState } from "react";
import { Card } from "@/components/ui/Card";
import { Button } from "@/components/ui/Button";
import { StepperCambioTutor } from "@/components/features/cambioTutor/StepperCambioTutor";
import { useNotification } from "@/hooks/useNotification";

/**
 * PÁGINA DE CAMBIO DE TUTOR - NUEVA IMPLEMENTACIÓN CON STEPPER
 * 
 * Flujo de 3 pasos:
 * 1. Búsqueda y selección de alumno
 * 2. Selección de nuevo tutor y motivo
 * 3. Confirmación y ejecución del cambio
 * 
 * Usa endpoints correctos:
 * - GET /api/alumnos/search para buscar alumnos
 * - GET /api/tutores/search para buscar tutores
 * - POST /api/asignaciones/cambio-tutor para ejecutar cambio
 */
export const TutorChangePage = () => {
  const [stepperOpen, setStepperOpen] = useState(false);
  const { success } = useNotification();

  const handleStepperComplete = () => {
    success("Cambio de tutor completado exitosamente");
    setStepperOpen(false);
  };

  const handleStepperCancel = () => {
    setStepperOpen(false);
  };

  return (
    <div className="space-y-6">
      {/* Panel principal: Iniciar cambio de tutor */}
      <Card>
        <div className="space-y-4">
          <div>
            <h1 className="text-2xl font-semibold text-gray-900">Cambio de Tutor</h1>
            <p className="mt-1 text-sm text-gray-600">
              Proceso guiado para cambiar la asignación de tutor de un alumno
            </p>
          </div>

          <div className="flex items-center justify-between rounded-lg border border-blue-200 bg-blue-50/50 p-4">
            <div>
              <h3 className="font-medium text-blue-900">Proceso guiado</h3>
              <p className="mt-1 text-sm text-blue-700">
                Busca al alumno, selecciona el nuevo tutor y especifica el motivo del cambio.
              </p>
            </div>
            <Button
              onClick={() => setStepperOpen(true)}
              className="bg-blue-600 hover:bg-blue-700"
            >
              Iniciar cambio
            </Button>
          </div>

          {/* Info sobre el nuevo flujo */}
          <div className="grid gap-4 md:grid-cols-3">
            <div className="rounded-lg border border-gray-200 p-3">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-600">
                  1
                </div>
                <span className="font-medium text-gray-900">Buscar Alumno</span>
              </div>
              <p className="mt-2 text-xs text-gray-600">
                Buscar y seleccionar el alumno que necesita cambio de tutor
              </p>
            </div>
            <div className="rounded-lg border border-gray-200 p-3">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-600">
                  2
                </div>
                <span className="font-medium text-gray-900">Seleccionar Tutor</span>
              </div>
              <p className="mt-2 text-xs text-gray-600">
                Elegir nuevo tutor disponible y especificar motivo del cambio
              </p>
            </div>
            <div className="rounded-lg border border-gray-200 p-3">
              <div className="flex items-center gap-2">
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-blue-100 text-sm font-semibold text-blue-600">
                  3
                </div>
                <span className="font-medium text-gray-900">Confirmar</span>
              </div>
              <p className="mt-2 text-xs text-gray-600">
                Revisar detalles y ejecutar el cambio de tutor
              </p>
            </div>
          </div>
        </div>
      </Card>

      {/* Guía de uso */}
      <Card>
        <div className="space-y-4">
          <h2 className="text-lg font-semibold text-gray-900">Guía para el cambio de tutor</h2>
          <ul className="space-y-3 text-sm text-gray-600">
            <li>• Confirma que el estudiante esté activo en el período académico vigente</li>
            <li>• Verifica que el nuevo tutor tenga capacidad disponible</li>
            <li>• Documenta claramente el motivo del cambio</li>
            <li>• Notifica a ambos tutores (actual y nuevo) sobre el cambio</li>
            <li>• Los cambios quedan registrados en el historial del sistema</li>
          </ul>
        </div>
      </Card>

      {/* Stepper embebido */}
      {stepperOpen && (
        <div className="mt-6">
          <StepperCambioTutor
            onComplete={handleStepperComplete}
            onCancel={handleStepperCancel}
          />
        </div>
      )}
    </div>
  );
};
