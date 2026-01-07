import { CheckCircle, AlertTriangle, XCircle, Download, Eye, Clock, AlertCircle } from "lucide-react";
import { Button } from "@/components/ui/Button";
import type { EjecucionAsignacionResponse } from "@/types";

interface PasoConfirmacionProps {
  executionResult: EjecucionAsignacionResponse | null;
  totalProcesados?: number;
}

/**
 * Paso 4: Confirmación y resultados
 * 
 * Muestra:
 * - Resultados de la ejecución
 * - Estadísticas detalladas
 * - Errores si los hay
 * - Opciones para descargar reportes
 */
export const PasoConfirmacion = ({
  executionResult
}: PasoConfirmacionProps) => {
  if (!executionResult) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <Clock className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-500">Esperando resultados de ejecución...</p>
        </div>
      </div>
    );
  }

  const getStatusIcon = () => {
    switch (executionResult.status) {
      case "OK":
        return <CheckCircle className="w-12 h-12 text-green-600" />;
      case "PARTIAL":
        return <AlertTriangle className="w-12 h-12 text-amber-600" />;
      case "ERROR":
        return <XCircle className="w-12 h-12 text-red-600" />;
      default:
        return <AlertCircle className="w-12 h-12 text-gray-600" />;
    }
  };

  const getStatusColor = () => {
    switch (executionResult.status) {
      case "OK":
        return "green";
      case "PARTIAL":
        return "amber";
      case "ERROR":
        return "red";
      default:
        return "gray";
    }
  };

  const getStatusMessage = () => {
    switch (executionResult.status) {
      case "OK":
        return "Proceso Completado Exitosamente";
      case "PARTIAL":
        return "Proceso Completado Parcialmente";
      case "ERROR":
        return "Error en el Proceso";
      default:
        return "Estado Desconocido";
    }
  };

  const statusColor = getStatusColor();
  const formatDuration = (ms: number) => {
    const seconds = Math.floor(ms / 1000);
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    
    if (minutes > 0) {
      return `${minutes}m ${remainingSeconds}s`;
    }
    return `${remainingSeconds}s`;
  };

  return (
    <div className="space-y-6">
      {/* Título */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          Resultados del Proceso
        </h2>
        <p className="text-gray-600">
          El proceso de asignación de tutores ha finalizado.
        </p>
      </div>

      {/* Estado general */}
      <div className={`
        rounded-lg p-6 border-2 text-center
        ${statusColor === 'green' ? 'bg-green-50 border-green-200' : ''}
        ${statusColor === 'amber' ? 'bg-amber-50 border-amber-200' : ''}
        ${statusColor === 'red' ? 'bg-red-50 border-red-200' : ''}
        ${statusColor === 'gray' ? 'bg-gray-50 border-gray-200' : ''}
      `}>
        <div className="flex flex-col items-center space-y-4">
          {getStatusIcon()}
          <div>
            <h3 className={`text-xl font-semibold ${
              statusColor === 'green' ? 'text-green-900' : ''
            }${
              statusColor === 'amber' ? 'text-amber-900' : ''
            }${
              statusColor === 'red' ? 'text-red-900' : ''
            }${
              statusColor === 'gray' ? 'text-gray-900' : ''
            }`}>
              {getStatusMessage()}
            </h3>
            <p className={`text-sm mt-1 ${
              statusColor === 'green' ? 'text-green-700' : ''
            }${
              statusColor === 'amber' ? 'text-amber-700' : ''
            }${
              statusColor === 'red' ? 'text-red-700' : ''
            }${
              statusColor === 'gray' ? 'text-gray-700' : ''
            }`}>
              {executionResult.message}
            </p>
          </div>
        </div>
      </div>

      {/* Estadísticas principales */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-gray-900">
              {executionResult.totalAlumnos}
            </div>
            <div className="text-sm text-gray-500">Total Procesados</div>
          </div>
        </div>
        
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {executionResult.alumnosAsignados}
            </div>
            <div className="text-sm text-gray-500">Asignados</div>
          </div>
        </div>
        
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-red-600">
              {executionResult.alumnosConError}
            </div>
            <div className="text-sm text-gray-500">Con Errores</div>
          </div>
        </div>
        
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">
              {executionResult.porcentajeExito?.toFixed(1)}%
            </div>
            <div className="text-sm text-gray-500">Éxito</div>
          </div>
        </div>
      </div>

      {/* Información adicional */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Detalles del proceso */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <h4 className="text-sm font-semibold text-gray-900 mb-3">
            Detalles del Proceso
          </h4>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Duración:</span>
              <span className="font-medium">{formatDuration(executionResult.duracionMs)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Timestamp:</span>
              <span className="font-medium">
                {new Date(executionResult.timestamp).toLocaleString()}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Tasa de éxito:</span>
              <span className="font-medium">{executionResult.porcentajeExito?.toFixed(2)}%</span>
            </div>
          </div>
        </div>

        {/* Resumen */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <h4 className="text-sm font-semibold text-gray-900 mb-3">
            Resumen
          </h4>
          <div className="text-sm text-gray-700">
            {executionResult.detalles || "Proceso completado sin detalles adicionales."}
          </div>
        </div>
      </div>

      {/* Errores detallados */}
      {executionResult.erroresDetalle && executionResult.erroresDetalle.length > 0 && (
        <div className="space-y-4">
          <div className="flex items-center space-x-2">
            <AlertTriangle className="w-5 h-5 text-red-500" />
            <h3 className="text-lg font-semibold text-gray-900">
              Errores Detallados ({executionResult.erroresDetalle.length})
            </h3>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
            <div className="max-h-64 overflow-y-auto">
              {executionResult.erroresDetalle.map((error, index) => (
                <div 
                  key={index}
                  className={`p-4 border-b border-gray-100 ${
                    index % 2 === 0 ? 'bg-gray-50' : 'bg-white'
                  }`}
                >
                  <div className="flex items-start space-x-3">
                    <div className="flex-shrink-0">
                      <div className="w-6 h-6 bg-red-100 rounded-full flex items-center justify-center">
                        <span className="text-xs font-medium text-red-600">
                          {index + 1}
                        </span>
                      </div>
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center space-x-2">
                        <span className="text-sm font-medium text-gray-900">
                          {error.matricula || 'Matrícula desconocida'}
                        </span>
                        <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800">
                          ERROR
                        </span>
                      </div>
                      <p className="text-sm text-gray-600 mt-1">
                        {error.descripcion || 'Error sin descripción'}
                      </p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Acciones */}
      <div className="flex flex-col sm:flex-row gap-4 justify-center">
        <Button
          variant="secondary"
          className="flex items-center space-x-2"
          onClick={() => {
            // TODO: Implementar descarga de reporte
            console.log("Descargar reporte");
          }}
        >
          <Download className="w-4 h-4" />
          <span>Descargar Reporte</span>
        </Button>
        
        <Button
          variant="secondary"
          className="flex items-center space-x-2"
          onClick={() => {
            // TODO: Implementar vista de detalles
            console.log("Ver detalles");
          }}
        >
          <Eye className="w-4 h-4" />
          <span>Ver Detalles Completos</span>
        </Button>
      </div>

      {/* Mensaje final */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 text-center">
        <p className="text-sm text-blue-800">
          El proceso de asignación ha finalizado. Puedes revisar los resultados en el dashboard 
          o generar reportes para análisis adicional.
        </p>
      </div>
    </div>
  );
};
