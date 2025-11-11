export interface ApiResponse<T> {
  status: string;
  data: T;
  message: string | null;
  timestamp: string;
}

export interface PagedResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
