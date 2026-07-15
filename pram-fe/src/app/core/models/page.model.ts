export interface PageMetadata {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface Page<T> {
  content: T[];
  page?: PageMetadata;
  totalPages?: number;
  totalElements?: number;
  size?: number;
  number?: number;
  first?: boolean;
  last?: boolean;
}
