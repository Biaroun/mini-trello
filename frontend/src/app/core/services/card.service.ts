import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Card, CardPayload } from '../models/card';

@Service()
export class CardService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  create(columnId: number, payload: CardPayload): Observable<Card> {
    return this.http.post<Card>(`${this.apiUrl}/columns/${columnId}/cards`, payload);
  }

  update(id: number, payload: CardPayload): Observable<Card> {
    return this.http.put<Card>(`${this.apiUrl}/cards/${id}`, payload);
  }

  move(id: number, targetColumnId: number, position: number): Observable<Card> {
    return this.http.patch<Card>(`${this.apiUrl}/cards/${id}/move`, { targetColumnId, position });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/cards/${id}`);
  }
}
