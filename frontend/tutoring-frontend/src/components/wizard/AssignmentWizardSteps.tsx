/**
 * Assignment Wizard - Step Configuration
 *
 * Configuración de los 5 pasos del wizard de cambio de tutor
 * Tipo checkout profesional SaaS
 */

export const WIZARD_STEPS = [
  {
    id: 1,
    label: "Seleccionar Alumnos",
    description: "Elige los alumnos a reasignar",
    icon: "👥",
  },
  {
    id: 2,
    label: "Validar Datos",
    description: "Verifica la información de los alumnos",
    icon: "✓",
  },
  {
    id: 3,
    label: "Buscar Tutor",
    description: "Encuentra el tutor destino",
    icon: "🔍",
  },
  {
    id: 4,
    label: "Confirmar Asignación",
    description: "Revisa los cambios a realizar",
    icon: "📋",
  },
  {
    id: 5,
    label: "Completado",
    description: "Asignación realizada",
    icon: "✅",
  },
] as const;

export type WizardStep = typeof WIZARD_STEPS[number]["id"];

export interface WizardState {
  step: WizardStep;
  selectedStudents: string[]; // Alumno IDs
  selectedTutor: string | null; // Tutor ID
  validationErrors: string[];
  completed: boolean;
}

export const initialWizardState: WizardState = {
  step: 1,
  selectedStudents: [],
  selectedTutor: null,
  validationErrors: [],
  completed: false,
};
