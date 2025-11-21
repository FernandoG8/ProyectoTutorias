import { useState } from "react";
import { EyeOff, Users, UserCheck, Search } from "lucide-react";
import { SearchInput } from "@/components/common/SearchInput";
import { Button } from "@/components/ui/Button";
import { Checkbox } from "@/components/ui/Checkbox";
import type { AlumnoValidadoDTO } from "@/types";

interface PasoRevisionProps {
  alumnos: AlumnoValidadoDTO[];
  alumnosExcluidos: Set<string>;
  onToggleExcluir: (matricula: string) => void;
}

/**
 * Paso 3: Revisión y ajustes de asignaciones
 * 
 * Permite:
 * - Ver tabla completa de alumnos a asignar
 * - Excluir alumnos específicos del proceso
 * - Filtrar y buscar en la lista
 * - Ver estadísticas del proceso
 */
export const PasoRevision = ({
  alumnos,
  alumnosExcluidos,
  onToggleExcluir
}: PasoRevisionProps) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [filterCarrera, setFilterCarrera] = useState("");
  const [showExcluidos, setShowExcluidos] = useState(true);

  // Filtrar alumnos
  const alumnosFiltrados = alumnos.filter(alumno => {
    const matchesSearch = !searchTerm || 
      alumno.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
      alumno.matricula.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesCarrera = !filterCarrera || alumno.carrera === filterCarrera;
    
    const matchesVisibility = showExcluidos || !alumnosExcluidos.has(alumno.matricula);
    
    return matchesSearch && matchesCarrera && matchesVisibility;
  });

  // Obtener carreras únicas
  const carreras = Array.from(new Set(alumnos.map(a => a.carrera))).sort();

  // Estadísticas
  const totalAlumnos = alumnos.length;
  const alumnosParaAsignar = totalAlumnos - alumnosExcluidos.size;
  const porcentajeAsignacion = totalAlumnos > 0 ? (alumnosParaAsignar / totalAlumnos) * 100 : 0;

  const handleToggleAll = () => {
    const visibleMatriculas = alumnosFiltrados.map(a => a.matricula);
    const allVisible = visibleMatriculas.every(m => alumnosExcluidos.has(m));
    
    visibleMatriculas.forEach(matricula => {
      if (allVisible && alumnosExcluidos.has(matricula)) {
        onToggleExcluir(matricula); // Incluir
      } else if (!allVisible && !alumnosExcluidos.has(matricula)) {
        onToggleExcluir(matricula); // Excluir
      }
    });
  };

  return (
    <div className="space-y-6">
      {/* Título */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          Revisión de Asignaciones
        </h2>
        <p className="text-gray-600">
          Revisa los datos y excluye alumnos si es necesario antes de ejecutar el proceso.
        </p>
      </div>

      {/* Estadísticas */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center space-x-3">
            <Users className="w-8 h-8 text-blue-600" />
            <div>
              <div className="text-2xl font-bold text-blue-900">{totalAlumnos}</div>
              <div className="text-sm text-blue-700">Total Alumnos</div>
            </div>
          </div>
        </div>
        
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="flex items-center space-x-3">
            <UserCheck className="w-8 h-8 text-green-600" />
            <div>
              <div className="text-2xl font-bold text-green-900">{alumnosParaAsignar}</div>
              <div className="text-sm text-green-700">Para Asignar</div>
            </div>
          </div>
        </div>
        
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
          <div className="flex items-center space-x-3">
            <EyeOff className="w-8 h-8 text-amber-600" />
            <div>
              <div className="text-2xl font-bold text-amber-900">{alumnosExcluidos.size}</div>
              <div className="text-sm text-amber-700">Excluidos</div>
            </div>
          </div>
        </div>
      </div>

      {/* Controles de filtrado */}
      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <div className="flex flex-col sm:flex-row gap-4 flex-1">
          {/* Búsqueda */}
          <div className="flex-1 max-w-md">
            <SearchInput
              placeholder="Buscar por nombre o matrícula..."
              value={searchTerm}
              onChange={setSearchTerm}
              className="w-full"
            />
          </div>
          
          {/* Filtro por carrera */}
          <select
            value={filterCarrera}
            onChange={(e) => setFilterCarrera(e.target.value)}
            className="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="">Todas las carreras</option>
            {carreras.map(carrera => (
              <option key={carrera} value={carrera}>{carrera}</option>
            ))}
          </select>
        </div>

        {/* Controles de visibilidad */}
        <div className="flex items-center space-x-4">
          <label className="flex items-center space-x-2">
            <Checkbox
              checked={showExcluidos}
              onChange={(e) => setShowExcluidos(e.target.checked)}
            />
            <span className="text-sm text-gray-700">Mostrar excluidos</span>
          </label>
          
          <Button
            variant="secondary"
            size="sm"
            onClick={handleToggleAll}
            className="text-xs"
          >
            {alumnosFiltrados.every(a => alumnosExcluidos.has(a.matricula)) 
              ? 'Incluir todos' 
              : 'Excluir todos'
            }
          </Button>
        </div>
      </div>

      {/* Tabla de alumnos */}
      <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  <Checkbox
                    checked={alumnosFiltrados.length > 0 && alumnosFiltrados.every(a => alumnosExcluidos.has(a.matricula))}
                    indeterminate={alumnosFiltrados.some(a => alumnosExcluidos.has(a.matricula)) && !alumnosFiltrados.every(a => alumnosExcluidos.has(a.matricula))}
                    onChange={handleToggleAll}
                  />
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Matrícula
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Nombre
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Carrera
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Semestre
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {alumnosFiltrados.map((alumno, index) => {
                const isExcluido = alumnosExcluidos.has(alumno.matricula);
                
                return (
                  <tr 
                    key={alumno.matricula}
                    className={`
                      ${index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}
                      ${isExcluido ? 'opacity-50' : ''}
                      hover:bg-blue-50 transition-colors
                    `}
                  >
                    <td className="px-6 py-4 whitespace-nowrap">
                      <Checkbox
                        checked={isExcluido}
                        onChange={() => onToggleExcluir(alumno.matricula)}
                      />
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {alumno.matricula}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {alumno.nombre}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {alumno.carrera}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {alumno.semestreNumerico}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`
                        inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                        ${isExcluido 
                          ? 'bg-red-100 text-red-800' 
                          : 'bg-green-100 text-green-800'
                        }
                      `}>
                        {isExcluido ? 'Excluido' : 'Para asignar'}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        {/* Mensaje si no hay resultados */}
        {alumnosFiltrados.length === 0 && (
          <div className="text-center py-12">
            <Search className="w-12 h-12 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500">No se encontraron alumnos con los filtros aplicados</p>
          </div>
        )}
      </div>

      {/* Resumen final */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-blue-900 mb-2">
          Resumen del proceso:
        </h4>
        <div className="text-sm text-blue-800 space-y-1">
          <p>• Se procesarán <strong>{alumnosParaAsignar}</strong> alumnos de un total de <strong>{totalAlumnos}</strong></p>
          <p>• Porcentaje de asignación: <strong>{porcentajeAsignacion.toFixed(1)}%</strong></p>
          {alumnosExcluidos.size > 0 && (
            <p>• <strong>{alumnosExcluidos.size}</strong> alumnos han sido excluidos del proceso</p>
          )}
        </div>
      </div>
    </div>
  );
};
