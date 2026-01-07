import { AlertTriangle, CheckCircle, XCircle, ArrowLeft, FileText } from "lucide-react";
import { Button } from "@/components/ui/Button";
import type { ExcelValidacionResponse } from "@/types";

interface PasoValidacionProps {
  validationResult: ExcelValidacionResponse | null;
  validationErrors: any[];
  onBackToSelection: () => void;
}

/**
 * Paso 2: Mostrar resultados de validación
 * 
 * Muestra:
 * - Estado general de la validación
 * - Lista detallada de errores (si los hay)
 * - Resumen de datos válidos
 * - Opciones para continuar o volver
 */
export const PasoValidacion = ({
  validationResult,
  validationErrors,
  onBackToSelection
}: PasoValidacionProps) => {
  if (!validationResult) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <FileText className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-500">No hay resultados de validación</p>
        </div>
      </div>
    );
  }

  const hasErrors = validationResult.status !== "OK" && validationErrors.length > 0;
  const isSuccess = validationResult.status === "OK";

  return (
    <div className="space-y-6">
      {/* Título y estado general */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            Resultados de Validación
          </h2>
          <p className="text-gray-600">
            Revisión de los datos del archivo Excel cargado.
          </p>
        </div>
        
        <Button
          variant="secondary"
          onClick={onBackToSelection}
          className="flex items-center space-x-2"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Cambiar archivo</span>
        </Button>
      </div>

      {/* Estado general */}
      <div className={`
        rounded-lg p-6 border-2
        ${isSuccess 
          ? 'bg-green-50 border-green-200' 
          : 'bg-red-50 border-red-200'
        }
      `}>
        <div className="flex items-center space-x-3">
          {isSuccess ? (
            <CheckCircle className="w-8 h-8 text-green-600" />
          ) : (
            <XCircle className="w-8 h-8 text-red-600" />
          )}
          <div>
            <h3 className={`text-lg font-semibold ${
              isSuccess ? 'text-green-900' : 'text-red-900'
            }`}>
              {isSuccess ? 'Validación Exitosa' : 'Errores Encontrados'}
            </h3>
            <p className={`text-sm ${
              isSuccess ? 'text-green-700' : 'text-red-700'
            }`}>
              {validationResult.message}
            </p>
          </div>
        </div>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-gray-900">
              {validationResult.totalFilas}
            </div>
            <div className="text-sm text-gray-500">Total Filas</div>
          </div>
        </div>
        
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">
              {validationResult.totalValidas}
            </div>
            <div className="text-sm text-gray-500">Válidas</div>
          </div>
        </div>
        
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-red-600">
              {validationResult.totalErrores}
            </div>
            <div className="text-sm text-gray-500">Con Errores</div>
          </div>
        </div>
        
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="text-center">
            <div className="text-2xl font-bold text-blue-600">
              {validationResult.porcentajeExito?.toFixed(1)}%
            </div>
            <div className="text-sm text-gray-500">Éxito</div>
          </div>
        </div>
      </div>

      {/* Lista de errores */}
      {hasErrors && (
        <div className="space-y-4">
          <div className="flex items-center space-x-2">
            <AlertTriangle className="w-5 h-5 text-amber-500" />
            <h3 className="text-lg font-semibold text-gray-900">
              Errores Detectados ({validationErrors.length})
            </h3>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
            <div className="max-h-64 overflow-y-auto">
              {validationErrors.map((error, index) => (
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
                          {error.filaExcel || error.fila || '?'}
                        </span>
                      </div>
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center space-x-2">
                        <span className="text-sm font-medium text-gray-900">
                          {error.campo || 'Campo desconocido'}
                        </span>
                        {error.tipoError && (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-red-100 text-red-800">
                            {error.tipoError}
                          </span>
                        )}
                      </div>
                      <p className="text-sm text-gray-600 mt-1">
                        {error.descripcion || error.error || 'Error desconocido'}
                      </p>
                      {error.valor && (
                        <p className="text-xs text-gray-500 mt-1">
                          <strong>Valor:</strong> {error.valor}
                        </p>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Resumen */}
      {validationResult.resumen && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 className="text-sm font-medium text-blue-900 mb-2">
            Resumen:
          </h4>
          <p className="text-sm text-blue-800">
            {validationResult.resumen}
          </p>
        </div>
      )}

      {/* Acciones recomendadas */}
      {hasErrors && (
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
          <h4 className="text-sm font-medium text-amber-900 mb-2">
            Recomendaciones:
          </h4>
          <ul className="text-sm text-amber-800 space-y-1">
            <li>• Puedes continuar con los datos válidos únicamente</li>
            <li>• O corregir el archivo Excel y volver a cargarlo</li>
            <li>• Los errores mostrados indican problemas específicos por fila</li>
          </ul>
        </div>
      )}
    </div>
  );
};
