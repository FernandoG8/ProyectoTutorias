import { useState, useCallback, useEffect } from "react";
import { Search, UserCheck, Users, ArrowRight, MessageSquare } from "lucide-react";
import { SearchInput } from "@/components/common/SearchInput";
import { Card } from "@/components/ui/Card";
import { Textarea } from "@/components/ui/Textarea";
import { searchTutors, getTutor } from "@/services/tutors-service";
import { useNotification } from "@/hooks/useNotification";
import type { AlumnoResponse, TutorResponse } from "@/types";

interface PasoSeleccionTutorProps {
  alumno: AlumnoResponse;
  tutorSeleccionado: TutorResponse | null;
  motivo: string;
  onTutorSelected: (tutor: TutorResponse) => void;
  onMotivoChanged: (motivo: string) => void;
}

/**
 * Paso 2: Selección de nuevo tutor y motivo
 * 
 * Permite:
 * - Buscar tutores usando /api/tutores/search
 * - Ver comparación entre tutor actual y nuevo
 * - Especificar motivo del cambio
 */
export const PasoSeleccionTutor = ({
  alumno,
  tutorSeleccionado,
  motivo,
  onTutorSelected,
  onMotivoChanged
}: PasoSeleccionTutorProps) => {
  const [searchTerm, setSearchTerm] = useState("");
  const [searchResults, setSearchResults] = useState<TutorResponse[]>([]);
  const [tutorActual, setTutorActual] = useState<TutorResponse | null>(
    alumno.tutor
      ? {
          id: alumno.tutor.id,
          nombre: alumno.tutor.nombre,
          carrera: alumno.tutor.carrera,
          capacidadMax: 0,
          cargaActual: 0,
          capacidadDisponible: 0,
          areaAtencion: null,
          letraEdificio: null,
          activo: true,
        }
      : null,
  );
  const [isSearching, setIsSearching] = useState(false);
  const { error: showError } = useNotification();

  // Refrescar datos del tutor actual para tener capacidad/carga al día
  useEffect(() => {
    let mounted = true;
    const fetchTutorActual = async () => {
      if (!alumno.tutor?.id) return;
      try {
        const actualizado = await getTutor(alumno.tutor.id);
        if (mounted) {
          setTutorActual({
            ...alumno.tutor,
            ...actualizado,
            capacidadDisponible:
              actualizado.capacidadDisponible ??
              Math.max((actualizado.capacidadMax ?? 0) - (actualizado.cargaActual ?? 0), 0),
          });
        }
      } catch {
        // Si falla, mantenemos la info existente
        if (mounted) {
          setTutorActual(
            alumno.tutor
              ? {
                  id: alumno.tutor.id,
                  nombre: alumno.tutor.nombre,
                  carrera: alumno.tutor.carrera,
                  capacidadMax: 0,
                  cargaActual: 0,
                  capacidadDisponible: 0,
                  areaAtencion: null,
                  letraEdificio: null,
                  activo: true,
                }
              : null,
          );
        }
      }
    };
    fetchTutorActual();
    return () => {
      mounted = false;
    };
  }, [alumno.tutor]);

  const handleSearch = useCallback(async (term: string) => {
    if (!term.trim() || term.length < 2) {
      setSearchResults([]);
      return;
    }

    setIsSearching(true);
    try {
      const results = await searchTutors({
        q: term,
        carrera: alumno.carrera, // Filtrar por la misma carrera del alumno
        limit: 10
      });

      const tutoresDisponibles = results
        .map((tutor) => {
          const capacidadMax = tutor.capacidadMax ?? (tutor as any)?.capacidad_max ?? 0;
          const cargaActual = tutor.cargaActual ?? (tutor as any)?.carga_actual ?? 0;
          const defaultDisponible = Math.max(capacidadMax - cargaActual, 0);

          return {
            ...tutor,
            activo: tutor.activo ?? true, // si backend no envía "activo", asumimos que sí lo está
            capacidadMax,
            cargaActual,
            capacidadDisponible:
              tutor.capacidadDisponible ?? defaultDisponible,
            areaAtencion: tutor.areaAtencion ?? (tutor as any)?.area_atencion ?? null,
          };
        })
        // Enriquecer cada tutor con datos actualizados del backend
        .map(async (tutor) => {
          try {
            const detalle = await getTutor(tutor.id);
            const max = detalle.capacidadMax ?? tutor.capacidadMax ?? 0;
            const actual = detalle.cargaActual ?? tutor.cargaActual ?? 0;
            return {
              ...tutor,
              ...detalle,
              capacidadMax: max,
              cargaActual: actual,
              capacidadDisponible:
                detalle.capacidadDisponible ??
                Math.max(max - actual, 0),
            };
          } catch {
            return tutor;
          }
        });

      const enriquecidos = await Promise.all(tutoresDisponibles);

      const ordenados = enriquecidos
        .filter(
          (tutor) =>
            (tutor.activo ?? true) &&
            tutor.id !== alumno.tutor?.id,
        )
        .sort((a, b) => {
          // Prioriza tutores de la misma carrera que el alumno
          const aIsSame = a.carrera === alumno.carrera;
          const bIsSame = b.carrera === alumno.carrera;
          if (aIsSame !== bIsSame) return aIsSame ? -1 : 1;
          return a.nombre.localeCompare(b.nombre);
        });

      setSearchResults(ordenados);
    } catch (err) {
      showError(err instanceof Error ? err.message : "Error al buscar tutores");
      setSearchResults([]);
    } finally {
      setIsSearching(false);
    }
  }, [showError, alumno.carrera, alumno.tutor?.id]);

  const handleSelectTutor = (tutor: TutorResponse) => {
    // Si el tutor seleccionado no tiene datos de capacidad actualizados, intento obtenerlos
    const ensureTutor = async () => {
      if (tutor.capacidadMax && tutor.cargaActual !== undefined) {
        onTutorSelected(tutor);
        return;
      }
      try {
        const detalle = await getTutor(tutor.id);
        const max = detalle.capacidadMax ?? tutor.capacidadMax ?? 0;
        const actual = detalle.cargaActual ?? tutor.cargaActual ?? 0;
        onTutorSelected({
          ...tutor,
          ...detalle,
          capacidadMax: max,
          cargaActual: actual,
          capacidadDisponible:
            detalle.capacidadDisponible ??
            Math.max(max - actual, 0),
        });
      } catch {
        onTutorSelected(tutor);
      }
    };
    void ensureTutor();
  };


  return (
    <div className="space-y-6">
      {/* Título */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          Selección de Nuevo Tutor
        </h2>
        <p className="text-gray-600">
          Busca y selecciona el nuevo tutor para <strong>{alumno.nombre}</strong>.
        </p>
      </div>

      {/* Información del alumno */}
      <Card className="p-4 bg-blue-50 border-blue-200">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
            <Users className="w-5 h-5 text-blue-600" />
          </div>
          <div>
            <h3 className="font-medium text-blue-900">{alumno.nombre}</h3>
            <p className="text-sm text-blue-700">
              {alumno.matricula} • {alumno.carrera} • Tutor actual: {tutorActual?.nombre ?? alumno.tutor?.nombre}
            </p>
            {tutorActual && (
              <p className="text-xs text-blue-700">
                Carga: {tutorActual.cargaActual ?? 0}/{tutorActual.capacidadMax ?? 0} • Disponible:{" "}
                {Math.max((tutorActual.capacidadMax ?? 0) - (tutorActual.cargaActual ?? 0), 0)}
              </p>
            )}
          </div>
        </div>
      </Card>

      {/* Búsqueda de tutores */}
      <div className="space-y-4">
        <div className="flex items-center space-x-2">
          <Search className="w-5 h-5 text-gray-400" />
          <span className="text-sm font-medium text-gray-700">Buscar nuevo tutor</span>
        </div>
        
        <SearchInput
          placeholder="Buscar por nombre del tutor..."
          value={searchTerm}
          onChange={setSearchTerm}
          onSearch={handleSearch}
          isLoading={isSearching}
          className="w-full"
        />
      </div>

      {/* Resultados de búsqueda */}
      {searchResults.length > 0 && (
        <div className="space-y-3">
          <h3 className="text-sm font-medium text-gray-700">
            Tutores disponibles ({searchResults.length})
          </h3>
          
          <div className="grid gap-3 max-h-64 overflow-y-auto">
            {searchResults.map((tutor) => (
              <Card 
                key={tutor.id}
                className={`p-4 cursor-pointer transition-all hover:shadow-md ${
                  tutorSeleccionado?.id === tutor.id 
                    ? 'ring-2 ring-green-500 bg-green-50' 
                    : 'hover:bg-gray-50'
                }`}
                onClick={() => handleSelectTutor(tutor)}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center space-x-3">
                    <div className="flex-shrink-0">
                      <div className="w-10 h-10 bg-green-100 rounded-full flex items-center justify-center">
                        <UserCheck className="w-5 h-5 text-green-600" />
                      </div>
                    </div>
                    <div>
                      <h4 className="text-sm font-medium text-gray-900">
                        {tutor.nombre}
                      </h4>
                      <p className="text-sm text-gray-500">
                        {tutor.carrera} • {tutor.areaAtencion}
                      </p>
                    </div>
                  </div>
                  
                  <div className="text-right">
                    <div className="text-sm text-gray-600">
                      Carga: {tutor.cargaActual}/{tutor.capacidadMax}
                    </div>
                    <div className={`text-xs px-2 py-1 rounded-full ${
                      tutor.capacidadDisponible > 0 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-amber-100 text-amber-800'
                    }`}>
                      {tutor.capacidadDisponible > 0 ? 'Disponible' : 'Ocupado'}
                    </div>
                  </div>
                </div>
              </Card>
            ))}
          </div>
        </div>
      )}

      {/* Comparación de tutores */}
      {tutorSeleccionado && (
        <div className="space-y-4">
          <h3 className="text-sm font-medium text-gray-700">
            Comparación de tutores
          </h3>
          
          <div className="grid md:grid-cols-3 gap-4 items-center">
            {/* Tutor actual */}
            <Card className="p-4 bg-red-50 border-red-200">
              <div className="text-center">
                <h4 className="font-medium text-red-900 mb-2">Tutor Actual</h4>
                <div className="space-y-1 text-sm">
                  <p className="font-medium">{tutorActual?.nombre ?? alumno.tutor?.nombre}</p>
                  <p className="text-red-700">{alumno.carrera}</p>
                  {tutorActual && (
                    <p className="text-xs text-red-700">
                      Carga: {tutorActual.cargaActual ?? 0}/{tutorActual.capacidadMax ?? 0}
                    </p>
                  )}
                </div>
              </div>
            </Card>

            {/* Flecha */}
            <div className="flex justify-center">
              <ArrowRight className="w-8 h-8 text-gray-400" />
            </div>

            {/* Tutor nuevo */}
            <Card className="p-4 bg-green-50 border-green-200">
              <div className="text-center">
                <h4 className="font-medium text-green-900 mb-2">Tutor Nuevo</h4>
                <div className="space-y-1 text-sm">
                  <p className="font-medium">{tutorSeleccionado.nombre}</p>
                  <p className="text-green-700">{tutorSeleccionado.carrera}</p>
                  <p className="text-xs text-green-600">
                    Carga: {tutorSeleccionado.cargaActual}/{tutorSeleccionado.capacidadMax}
                  </p>
                </div>
              </div>
            </Card>
          </div>
        </div>
      )}

      {/* Motivo del cambio */}
      <div className="space-y-2">
        <label className="text-sm font-medium text-gray-700 flex items-center space-x-2">
          <MessageSquare className="w-4 h-4" />
          <span>Motivo del cambio *</span>
        </label>
        <Textarea
          placeholder="Describe el motivo del cambio de tutor (mínimo 10 caracteres)..."
          value={motivo}
          onChange={(e) => onMotivoChanged(e.target.value)}
          rows={3}
          maxLength={200}
          className="w-full"
        />
        <p className="text-xs text-gray-500">
          {motivo.length}/200 caracteres{motivo.length < 10 ? ' (mínimo 10)' : ''}
        </p>
      </div>

      {/* Mensaje si no hay resultados */}
      {searchTerm.length >= 2 && searchResults.length === 0 && !isSearching && (
        <div className="text-center py-8">
          <Search className="w-12 h-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-500">
            No se encontraron tutores disponibles para {alumno.carrera}
          </p>
          <p className="text-sm text-gray-400 mt-1">
            Intenta con otro término de búsqueda
          </p>
        </div>
      )}

      {/* Instrucciones */}
      {!searchTerm && (
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
          <h4 className="text-sm font-medium text-amber-900 mb-2">
            Consideraciones:
          </h4>
          <ul className="text-sm text-amber-800 space-y-1">
            <li>• Solo se muestran tutores activos y disponibles</li>
            <li>• Se filtran automáticamente por la carrera del alumno ({alumno.carrera})</li>
            <li>• Se excluye el tutor actual del alumno</li>
            <li>• El motivo debe ser específico y justificado</li>
          </ul>
        </div>
      )}
    </div>
  );
};
