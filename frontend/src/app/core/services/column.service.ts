import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Column } from '../models/column';

@Service()
export class ColumnService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  create(boardId: number, name: string): Observable<Column> {
    return this.http.post<Column>(`${this.apiUrl}/boards/${boardId}/columns`, { name });
  }

  rename(id: number, name: string): Observable<Column> {
    return this.http.put<Column>(`${this.apiUrl}/columns/${id}`, { name });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/columns/${id}`);
  }
}
