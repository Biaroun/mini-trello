// Format d'erreur renvoyé par le GlobalExceptionHandler du backend
export interface ApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  errors: Record<string, string> | null;
}
