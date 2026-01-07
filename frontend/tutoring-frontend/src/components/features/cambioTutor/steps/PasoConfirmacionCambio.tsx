import { CheckCircle, AlertTriangle, User, UserCheck, ArrowRight, MessageSquare, Clock } from "lucide-react";
import { Card } from "@/components/ui/Card";
import type { AlumnoResponse, TutorResponse } from "@/types";

interface PasoConfirmacionCambioProps {
  alumno: AlumnoResponse;
  tutorAnterior: { id: number; nombre: string };
  tutorNuevo: TutorResponse;
  motivo: string;
  cambioRealizado: boolean;
  resultadoCambio: any | null;
}

/**
 * Paso 3: Confirmación y resultados del cambio
 * 
 * Muestra:
 * - Resumen del cambio a realizar
 * - Confirmación antes de ejecutar
 * - Resultados del cambio ejecutado
 */
export const PasoConfirmacionCambio = ({
  alumno,
  tutorAnterior,
  tutorNuevo,
  motivo,
  cambioRealizado,
  resultadoCambio
}: PasoConfirmacionCambioProps) => {
  if (cambioRealizado && resultadoCambio) {
    // Mostrar resultados del cambio
    return (
      <div className="space-y-6">
        {/* Título */}
        <div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            Cambio Realizado Exitosamente
          </h2>
          <p className="text-gray-600">
            El cambio de tutor se ha completado correctamente.
          </p>
        </div>

        {/* Estado del cambio */}
        <Card className="p-6 bg-green-50 border-green-200 text-center">
          <div className="flex flex-col items-center space-y-4">
            <CheckCircle className="w-16 h-16 text-green-600" />
            <div>
              <h3 className="text-xl font-semibold text-green-900">
                Cambio Completado
              </h3>
              <p className="text-sm text-green-700 mt-1">
                El tutor ha sido reasignado exitosamente
              </p>
            </div>
          </div>
        </Card>

        {/* Detalles del cambio realizado */}
        <div className="grid gap-6">
          {/* Información del alumno */}
          <Card className="p-4">
            <h4 className="font-medium text-gray-900 mb-3 flex items-center space-x-2">
              <User className="w-4 h-4" />
              <span>Alumno</span>
            </h4>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-gray-600">Nombre:</span>
                <p className="font-medium">{resultadoCambio.alumnoNombre}</p>
              </div>
              <div>
                <span className="text-gray-600">Matrícula:</span>
                <p className="font-medium">{alumno.matricula}</p>
              </div>
            </div>
          </Card>

          {/* Cambio de tutor */}
          <Card className="p-4">
            <h4 className="font-medium text-gray-900 mb-3 flex items-center space-x-2">
              <UserCheck className="w-4 h-4" />
              <span>Cambio de Tutor</span>
            </h4>
            
            <div className="grid md:grid-cols-3 gap-4 items-center">
              {/* Tutor anterior */}
              <div className="text-center p-3 bg-red-50 rounded-lg">
                <p className="text-xs text-red-600 font-medium mb-1">ANTERIOR</p>
                <p className="font-medium text-red-900">
                  {resultadoCambio.tutorOrigenNombre}
                </p>
              </div>

              {/* Flecha */}
              <div className="flex justify-center">
                <ArrowRight className="w-6 h-6 text-gray-400" />
              </div>

              {/* Tutor nuevo */}
              <div className="text-center p-3 bg-green-50 rounded-lg">
                <p className="text-xs text-green-600 font-medium mb-1">NUEVO</p>
                <p className="font-medium text-green-900">
                  {resultadoCambio.tutorDestinoNombre}
                </p>
              </div>
            </div>
          </Card>

          {/* Motivo */}
          <Card className="p-4">
            <h4 className="font-medium text-gray-900 mb-3 flex items-center space-x-2">
              <MessageSquare className="w-4 h-4" />
              <span>Motivo del Cambio</span>
            </h4>
            <p className="text-sm text-gray-700 bg-gray-50 p-3 rounded-lg">
              {resultadoCambio.motivo}
            </p>
          </Card>

          {/* Información adicional */}
          <Card className="p-4">
            <h4 className="font-medium text-gray-900 mb-3 flex items-center space-x-2">
              <Clock className="w-4 h-4" />
              <span>Detalles del Proceso</span>
            </h4>
            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <span className="text-gray-600">Fecha del cambio:</span>
                <p className="font-medium">
                  {new Date(resultadoCambio.fechaCambio).toLocaleString()}
                </p>
              </div>
              <div>
                <span className="text-gray-600">Usuario responsable:</span>
                <p className="font-medium">{resultadoCambio.usuarioResponsable}</p>
              </div>
            </div>
          </Card>
        </div>

        {/* Mensaje final */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 text-center">
          <p className="text-sm text-blue-800">
            El cambio ha sido registrado en el sistema. El alumno ahora está asignado al nuevo tutor.
            Puedes revisar los cambios en el dashboard o generar reportes actualizados.
          </p>
        </div>
      </div>
    );
  }

  // Mostrar confirmación antes del cambio
  return (
    <div className="space-y-6">
      {/* Título */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          Confirmación de Cambio
        </h2>
        <p className="text-gray-600">
          Revisa los detalles antes de ejecutar el cambio de tutor.
        </p>
      </div>

      {/* Advertencia */}
      <Card className="p-4 bg-amber-50 border-amber-200">
        <div className="flex items-start space-x-3">
          <AlertTriangle className="w-5 h-5 text-amber-600 mt-0.5" />
          <div>
            <h4 className="font-medium text-amber-900">Importante</h4>
            <p className="text-sm text-amber-800 mt-1">
              Esta acción modificará la asignación del alumno en la base de datos. 
              Asegúrate de que todos los datos sean correctos antes de continuar.
            </p>
          </div>
        </div>
      </Card>

      {/* Resumen del cambio */}
      <div className="grid gap-6">
        {/* Información del alumno */}
        <Card className="p-4">
          <h4 className="font-medium text-gray-900 mb-3 flex items-center space-x-2">
            <User className="w-4 h-4" />
            <span>Alumno</span>
          </h4>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <span className="text-gray-600">Nombre:</span>
              <p className="font-medium">{alumno.nombre}</p>
            </div>
            <div>
              <span className="text-gray-600">Matrícula:</span>
              <p className="font-medium">{alumno.matricula}</p>
            </div>
            <div>
              <span className="text-gray-600">Carrera:</span>
              <p className="font-medium">{alumno.carrera}</p>
            </div>
            <div>
              <span className="text-gray-600">Semestre:</span>
              <p className="font-medium">{alumno.semestre}</p>
            </div>
          </div>
        </Card>

        {/* Cambio de tutor */}
        <Card className="p-4">
          <h4 className="font-medium text-gray-900 mb-3 flex items-center space-x-2">
            <UserCheck className="w-4 h-4" />
            <span>Cambio de Tutor</span>
          </h4>
          
          <div className="grid md:grid-cols-3 gap-4 items-center">
            {/* Tutor actual */}
            <div className="text-center p-4 bg-red-50 rounded-lg border border-red-200">
              <p className="text-xs text-red-600 font-medium mb-2">TUTOR ACTUAL</p>
              <p className="font-medium text-red-900">{tutorAnterior.nombre}</p>
              <p className="text-xs text-red-700 mt-1">{alumno.carrera}</p>
            </div>

            {/* Flecha */}
            <div className="flex justify-center">
              <div className="flex flex-col items-center">
                <ArrowRight className="w-8 h-8 text-gray-400" />
                <span className="text-xs text-gray-500 mt-1">Cambiar a</span>
              </div>
            </div>

            {/* Tutor nuevo */}
            <div className="text-center p-4 bg-green-50 rounded-lg border border-green-200">
              <p className="text-xs text-green-600 font-medium mb-2">TUTOR NUEVO</p>
              <p className="font-medium text-green-900">{tutorNuevo.nombre}</p>
              <p className="text-xs text-green-700 mt-1">
                {tutorNuevo.carrera} • Carga: {tutorNuevo.cargaActual + 1}/{tutorNuevo.capacidadMax}
              </p>
            </div>
          </div>
        </Card>

        {/* Motivo */}
        <Card className="p-4">
          <h4 className="font-medium text-gray-900 mb-3 flex items-center space-x-2">
            <MessageSquare className="w-4 h-4" />
            <span>Motivo del Cambio</span>
          </h4>
          <p className="text-sm text-gray-700 bg-gray-50 p-3 rounded-lg">
            {motivo}
          </p>
        </Card>
      </div>

      {/* Confirmación final */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-blue-900 mb-2">
          ¿Confirmas el cambio de tutor?
        </h4>
        <p className="text-sm text-blue-800">
          Al hacer clic en "Ejecutar Cambio", se realizará la reasignación inmediatamente.
          Esta acción quedará registrada en el historial del sistema.
        </p>
      </div>
    </div>
  );
};
