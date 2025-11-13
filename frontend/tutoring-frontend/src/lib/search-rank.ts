/**
 * search-rank.ts
 * Local fallback ranking system for search results when backend doesn't support server-side search
 */

import type { AlumnoResponse, TutorResponse } from "@/types";

/**
 * Score a student based on query match relevance
 * Priority: exact matricula match > name starts with > name contains
 */
export const scoreStudent = (student: AlumnoResponse, query: string): number => {
  const term = query.toLowerCase().trim();
  if (!term) return 0;

  const matricula = student.matricula.toLowerCase();
  const nombre = student.nombre.toLowerCase();

  // Exact matricula match: highest priority
  if (matricula === term) return 1000;

  // Matricula starts with
  if (matricula.startsWith(term)) return 500;

  // Name starts with
  if (nombre.startsWith(term)) return 300;

  // Contains in name
  if (nombre.includes(term)) return 100;

  // Contains in matricula
  if (matricula.includes(term)) return 50;

  return 0;
};

/**
 * Score a tutor based on query match relevance
 * Priority: name starts with > name contains > carrera contains
 */
export const scoreTutor = (tutor: TutorResponse, query: string): number => {
  const term = query.toLowerCase().trim();
  if (!term) return 0;

  const nombre = tutor.nombre.toLowerCase();
  const carrera = tutor.carrera.toLowerCase();

  // Name starts with
  if (nombre.startsWith(term)) return 300;

  // Name contains
  if (nombre.includes(term)) return 150;

  // Carrera contains
  if (carrera.includes(term)) return 50;

  return 0;
};

/**
 * Rank and filter students by query
 */
export const rankStudents = (
  students: AlumnoResponse[],
  query: string,
  limit?: number,
): AlumnoResponse[] => {
  const ranked = students
    .map((student) => ({
      item: student,
      score: scoreStudent(student, query),
    }))
    .filter((r) => r.score > 0)
    .sort((a, b) => b.score - a.score);

  if (limit && ranked.length > limit) {
    return ranked.slice(0, limit).map((r) => r.item);
  }
  return ranked.map((r) => r.item);
};

/**
 * Rank and filter tutors by query
 */
export const rankTutors = (
  tutors: TutorResponse[],
  query: string,
  limit?: number,
): TutorResponse[] => {
  const ranked = tutors
    .map((tutor) => ({
      item: tutor,
      score: scoreTutor(tutor, query),
    }))
    .filter((r) => r.score > 0)
    .sort((a, b) => b.score - a.score);

  if (limit && ranked.length > limit) {
    return ranked.slice(0, limit).map((r) => r.item);
  }
  return ranked.map((r) => r.item);
};
