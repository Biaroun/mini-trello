import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Board, BoardDetail } from '../models/board';

@Service()
export class BoardService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/boards`;

  getAll(): Observable<Board[]> {
    return this.http.get<Board[]>(this.url);
  }

  getById(id: number): Observable<BoardDetail> {
    return this.http.get<BoardDetail>(`${this.url}/${id}`);
  }

  create(name: string): Observable<Board> {
    return this.http.post<Board>(this.url, { name });
  }

  rename(id: number, name: string): Observable<Board> {
    return this.http.put<Board>(`${this.url}/${id}`, { name });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
