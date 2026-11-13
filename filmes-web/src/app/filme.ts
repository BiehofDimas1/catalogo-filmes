import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface Filme {
  id?: number;
  titulo: string;
  duracaoMinutos: number;
  ativo?: boolean;
  licencasDisponiveis: number;
}

@Injectable({ providedIn: 'root' })
export class FilmeService {
  private http = inject(HttpClient);
  private url = 'http://localhost:8080/filmes';

  listar(): Observable<Filme[]> {
    return this.http.get<Filme[]>(this.url);
  }

  buscar(id: number): Observable<Filme> {
    return this.http.get<Filme>(`${this.url}/${id}`);
  }

  cadastrar(filme: Filme): Observable<Filme> {
    return this.http.post<Filme>(this.url, filme);
  }

  atualizar(id: number, filme: Filme): Observable<Filme> {
    return this.http.put<Filme>(`${this.url}/${id}`, filme);
  }

  inativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}