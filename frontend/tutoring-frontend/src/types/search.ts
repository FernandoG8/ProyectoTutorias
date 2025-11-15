import type { AlumnoResponse } from "./alumno";
import type { TutorResponse } from "./tutor";

export type SearchResultType = "student" | "tutor";

export interface StudentSearchResult {
  type: "student";
  item: AlumnoResponse;
}

export interface TutorSearchResult {
  type: "tutor";
  item: TutorResponse;
}

export type SearchResult = StudentSearchResult | TutorSearchResult;

export interface SearchResponse {
  students: AlumnoResponse[];
  tutors: TutorResponse[];
  query: string;
  totalResults: number;
}

export interface AutocompleteResult {
  type: SearchResultType;
  id: number;
  displayName: string;
  detail?: string;
}
