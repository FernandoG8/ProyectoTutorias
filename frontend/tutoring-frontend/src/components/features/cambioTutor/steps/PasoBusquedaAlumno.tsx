import { useState, useCallback } from "react";
import { Search, User, UserCheck, GraduationCap } from "lucide-react";
import { SearchInput } from "@/components/common/SearchInput";
import { Card } from "@/components/ui/Card";
import { searchStudents } from "@/services/alumnos-service";
import { useNotification } from "@/hooks/useNotification";
import type { AlumnoResponse } from "@/types";

interface PasoBusquedaAlumnoProps {
  alumnoSeleccionado: AlumnoResponse | null;
  onAlumnoSelected: (alumno: AlumnoResponse) => void;
}

/**
 * Paso 1: Búsqueda y selección de alumno
 * 
 * Permite:
 * - Buscar alumnos usando /api/alumnos/search
 * - Ver información actual del alumno y su tutor
 * - Seleccionar alumno para cambio de tutor
 */
export const PasoBusquedaAlumno = ({
  alumnoSeleccionado,
  onAlumnoSelected
}: PasoBusquedaAlumnoProps) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState<AlumnoResponse[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const { error: showError } = useNotification();

  const handleSearch = useCallback(async (term: string) => {
    if (!term.trim() || term.length < 2) {
      setSearchResults([]);
      return;
    }

    setIsSearching(true);
    try {
      const results = await searchStudents({
        q: term,
        estado: "ACTIVO",
        size: 10,
        page: 1,
      });
      
      // Filtrar solo alumnos que tienen tutor asignado
      const alumnosConTutor = results.items.filter(alumno => alumno.tutor && alumno.tutor.nombre);
      setSearchResults(alumnosConTutor);
    } catch (err) {
      showError(err instanceof Error ? err.message : "Error al buscar alumnos");
      setSearchResults([]);
    } finally {
      setIsSearching(false);
    }
  }, [showError]);

  const handleSelectAlumno = (alumno: AlumnoResponse) => {
    onAlumnoSelected(alumno);
  };

  return (
    <div className="space-y-6">
      {/* Título */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          Búsqueda de Alumno
        </h2>
        <p className="text-gray-600">
          Busca y selecciona el alumno al que deseas cambiar de tutor.
        </p>
      </div>

      {/* Búsqueda */}
      <div className="space-y-4">
        <div className="flex items-center space-x-2">
          <Search className="w-5 h-5 text-gray-400" />
          <span className="text-sm font-medium text-gray-700">Buscar alumno</span>
        </div>
        
        <SearchInput
          placeholder="Buscar por nombre o matrícula..."
          value={searchTerm}
          onChange={setSearchTerm}
          onSearch={handleSearch}
          isLoading={isSearching}
          className="w-full"
          autoFocus
        />
      </div>

      {/* Resultados de búsqueda */}
      {searchResults.length > 0 && (
        <div className="space-y-3">
          <h3 className="text-sm font-medium text-gray-700">
            Resultados de búsqueda ({searchResults.length})
          </h3>
          
          <div className="grid gap-3 max-h-64 overflow-y-auto">
            {searchResults.map((alumno) => (
              <Card 
                key={alumno.id}
                className={`p-4 cursor-pointer transition-all hover:shadow-md ${
                  alumnoSeleccionado?.id === alumno.id 
                    ? 'ring-2 ring-blue-500 bg-blue-50' 
                    : 'hover:bg-gray-50'
                }`}
                onClick={() => handleSelectAlumno(alumno)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="flex-shrink-0">
                      <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                        <User className="w-5 h-5 text-blue-600" />
                      </div>
                    </div>
                    <div>
                      <h4 className="text-sm font-medium text-gray-900">
                        {alumno.nombre}
                      </h4>
                      <p className="text-sm text-gray-500">
                        {alumno.matricula} • {alumno.carrera} • Semestre {alumno.semestre}
                      </p>
                    </div>
                  </div>
                  
                  <div className="text-right">
                    <div className="flex items-center space-x-1 text-sm text-gray-600">
                      <UserCheck className="w-4 h-4" />
                      <span>Tutor actual:</span>
                    </div>
                    <p className="text-sm font-medium text-gray-900">
                      {alumno.tutor?.nombre}
                    </p>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* Alumno seleccionado */}
      {alumnoSeleccionado && (
        <div className="space-y-3">
          <h3 className="text-sm font-medium text-gray-700">
            Alumno seleccionado
          </h3>
          
          <Card className="p-6 bg-green-50 border-green-200">
            <div className="flex items-start space-x-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                  <GraduationCap className="w-6 h-6 text-green-600" />
                </div>
              </div>
              
              <div className="flex-1">
                <h4 className="text-lg font-semibold text-green-900">
                  {alumnoSeleccionado.nombre}
                </h4>
                
                <div className="mt-2 grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-green-700 font-medium">Matrícula:</span>
                    <p className="text-green-800">{alumnoSeleccionado.matricula}</p>
                  </div>
                  <div>
                    <span className="text-green-700 font-medium">Carrera:</span>
                    <p className="text-green-800">{alumnoSeleccionado.carrera}</p>
                  </div>
                  <div>
                    <span className="text-green-700 font-medium">Semestre:</span>
                    <p className="text-green-800">{alumnoSeleccionado.semestre}</p>
                  </div>
                  <div>
                    <span className="text-green-700 font-medium">Estado:</span>
                    <p className="text-green-800">{alumnoSeleccionado.estado}</p>
                  </div>
                </div>
                
                <div className="mt-4 p-3 bg-green-100 rounded-lg">
                  <div className="flex items-center space-x-2">
                    <UserCheck className="w-4 h-4 text-green-600" />
                    <span className="text-sm font-medium text-green-700">Tutor actual:</span>
                  </div>
                  <p className="text-sm text-green-800 mt-1">
                    {alumnoSeleccionado.tutor?.nombre}
                  </p>
                </div>
              </div>
            </div>
          </Card>
        </div>
      )}

      {/* Mensaje si no hay resultados */}
      {searchTerm.length >= 2 && searchResults.length === 0 && !isSearching && (
        <div className="text-center py-8">
          <Search className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-500">
            No se encontraron alumnos activos con tutor asignado
          </p>
          <p className="text-sm text-gray-400 mt-1">
            Intenta con otro término de búsqueda
          </p>
        </div>
      )}

      {/* Instrucciones */}
      {!searchTerm && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 className="text-sm font-medium text-blue-900 mb-2">
            Instrucciones:
          </h4>
          <ul className="text-sm text-blue-800 space-y-1">
            <li>• Busca por nombre completo o matrícula del alumno</li>
            <li>• Solo se muestran alumnos activos con tutor asignado</li>
            <li>• Haz clic en un resultado para seleccionarlo</li>
            <li>• Verifica que sea el alumno correcto antes de continuar</li>
          </ul>
        </div>
      )}
    </div>
  );
};
