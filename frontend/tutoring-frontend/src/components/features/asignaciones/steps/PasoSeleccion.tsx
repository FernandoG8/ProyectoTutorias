import { Upload, FileSpreadsheet, Calendar, User } from "lucide-react";
import { Select } from "@/components/ui/Select";
import { FormField } from "@/components/ui/FormField";
import type { Semestre } from "@/types";

interface PasoSeleccionProps {
  semestres: Semestre[];
  semestreId: number | null;
  archivo: File | null;
  usuario: string;
  onSemestreChange: (id: number) => void;
  onArchivoChange: (file: File | null) => void;
  onUsuarioChange: (usuario: string) => void;
}

/**
 * Paso 1: Selección de archivo y semestre
 * 
 * Permite al usuario:
 * - Seleccionar el semestre académico
 * - Subir archivo Excel
 * - Especificar usuario responsable
 * - Ver previsualización básica del archivo
 */
export const PasoSeleccion = ({
  semestres,
  semestreId,
  archivo,
  usuario,
  onSemestreChange,
  onArchivoChange,
  onUsuarioChange
}: PasoSeleccionProps) => {
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] || null;
    onArchivoChange(file);
  };

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="space-y-6">
      {/* Título */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">
          Selección de Archivo y Semestre
        </h2>
        <p className="text-gray-600">
          Proporciona los datos necesarios para iniciar el proceso de asignación de tutores.
        </p>
      </div>

      {/* Formulario */}
      <div className="grid gap-6 md:grid-cols-2">
        {/* Selección de semestre */}
        <div className="space-y-2">
          <label className="text-sm font-semibold text-gray-700 flex items-center space-x-2">
            <Calendar className="w-4 h-4" />
            <span>Semestre Académico *</span>
          </label>
          <Select
            value={semestreId?.toString() || ""}
            onChange={(e) => onSemestreChange(parseInt(e.target.value))}
            className="w-full"
          >
            <option value="">Selecciona un semestre</option>
            {semestres.map((semestre) => (
              <option key={semestre.id} value={semestre.id}>
                {semestre.codigo} - {semestre.nombre}
              </option>
            ))}
          </Select>
        </div>

        {/* Usuario responsable */}
        <div className="space-y-2">
          <FormField
            label="Usuario Responsable"
            placeholder="coord_tutorias"
            value={usuario}
            onChange={(e) => onUsuarioChange(e.target.value)}
            icon={<User className="w-4 h-4" />}
            required
          />
        </div>
      </div>

      {/* Carga de archivo */}
      <div className="space-y-4">
        <label className="text-sm font-semibold text-gray-700 flex items-center space-x-2">
          <FileSpreadsheet className="w-4 h-4" />
          <span>Archivo Excel *</span>
        </label>

        {/* Área de drop */}
        <div className="relative">
          <input
            type="file"
            accept=".xlsx,.xls,.csv"
            onChange={handleFileChange}
            className="sr-only"
            id="archivo-input"
          />
          <label
            htmlFor="archivo-input"
            className={`
              flex flex-col items-center justify-center w-full h-32 
              border-2 border-dashed rounded-lg cursor-pointer
              transition-colors duration-200
              ${archivo 
                ? 'border-green-300 bg-green-50' 
                : 'border-gray-300 bg-gray-50 hover:bg-gray-100'
              }
            `}
          >
            {archivo ? (
              <div className="flex flex-col items-center space-y-2">
                <FileSpreadsheet className="w-8 h-8 text-green-600" />
                <div className="text-center">
                  <p className="text-sm font-medium text-green-700">
                    {archivo.name}
                  </p>
                  <p className="text-xs text-green-600">
                    {formatFileSize(archivo.size)}
                  </p>
                </div>
              </div>
            ) : (
              <div className="flex flex-col items-center space-y-2">
                <Upload className="w-8 h-8 text-gray-400" />
                <div className="text-center">
                  <p className="text-sm font-medium text-gray-700">
                    Arrastra o haz clic para seleccionar
                  </p>
                  <p className="text-xs text-gray-500">
                    Excel (.xlsx, .xls) o CSV (máx. 10 MB)
                  </p>
                </div>
              </div>
            )}
          </label>
        </div>

        {/* Información del archivo seleccionado */}
        {archivo && (
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-start space-x-3">
              <FileSpreadsheet className="w-5 h-5 text-blue-600 mt-0.5" />
              <div className="flex-1">
                <h4 className="text-sm font-medium text-blue-900">
                  Archivo seleccionado
                </h4>
                <div className="mt-1 text-sm text-blue-700">
                  <p><strong>Nombre:</strong> {archivo.name}</p>
                  <p><strong>Tamaño:</strong> {formatFileSize(archivo.size)}</p>
                  <p><strong>Tipo:</strong> {archivo.type || 'Desconocido'}</p>
                  <p><strong>Última modificación:</strong> {new Date(archivo.lastModified).toLocaleString()}</p>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Recomendaciones */}
      <div className="bg-amber-50 border border-amber-200 rounded-lg p-4">
        <h4 className="text-sm font-medium text-amber-900 mb-2">
          Recomendaciones:
        </h4>
        <ul className="text-sm text-amber-800 space-y-1">
          <li>• Usa las plantillas institucionales para garantizar el orden de columnas</li>
          <li>• Verifica que el archivo contenga: Matrícula, Nombre, Carrera, Semestre</li>
          <li>• El tamaño máximo del archivo es de 10 MB</li>
          <li>• Asegúrate de que el período académico sea correcto</li>
        </ul>
      </div>
    </div>
  );
};
