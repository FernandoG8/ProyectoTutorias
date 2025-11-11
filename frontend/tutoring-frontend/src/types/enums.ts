export type EstadoAlumno = "ACTIVO" | "INACTIVO";

export type MotivoInactividad =
  | "SIN_DEFINIR"
  | "BAJA_TEMPORAL"
  | "BAJA_DEFINITIVA"
  | "MOVILIDAD"
  | "EGRESADO";

export type EstadoProceso =
  | "INICIADO"
  | "COMPARANDO"
  | "LIBERANDO_CUPOS"
  | "ASIGNANDO"
  | "COMPLETADO"
  | "FALLIDO";

export type SeveridadAlerta = "CRITICO" | "ERROR" | "WARNING" | "INFO";

export type TipoAlerta =
  | "ERROR_FORMATO"
  | "ASIGNACION_CRUZADA"
  | "CAPACIDAD_EXCEDIDA"
  | "SIN_TUTOR_DISPONIBLE";

export type RolUsuario =
  | "ROLE_COORDINADOR_TUTORIAS"
  | "ROLE_SECRETARIO_ACADEMICO";
