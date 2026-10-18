import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models/api-error';

// Transforme toute erreur HTTP en Error portant un message lisible par l'utilisateur
export const errorInterceptor: HttpInterceptorFn = (req, next) =>
  next(req).pipe(catchError((error: HttpErrorResponse) => throwError(() => new Error(toMessage(error)))));

function toMessage(error: HttpErrorResponse): string {
  if (error.status === 0) {
    return "Impossible de joindre le serveur. Vérifiez que l'API est démarrée.";
  }

  const body = error.error as ApiError | null;
  if (body?.errors) {
    return Object.values(body.errors).join(' ');
  }
  return body?.message ?? 'Une erreur est survenue.';
}
