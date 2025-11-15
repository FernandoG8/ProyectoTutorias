import type { TutorConAlumnos } from "./tutor";

export interface CarreraResumen {
  carrera: string;
  totalAlumnos: number;
  totalTutores: number;
  tutores: TutorConAlumnos[];
}

export interface ReporteCarrera {
  semestre: string;
  fechaGeneracion: string;
  carreras: CarreraResumen[];
}

export interface ReporteArchivo {
  fileName: string;
  mediaType: string;
  contenido: ArrayBuffer;
}
