import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from './core/api';

export interface Filme {
  id: number;
  titulo: string;
  duracaoMinutos: number;
  licencasDisponiveis: number;
  ativo: boolean;
  versao: number;
}

export interface FilmeDados {
  titulo: string;
  duracaoMinutos: number;
  licencasDisponiveis: number;
  versao?: number;
}

@Injectable({ providedIn: 'root' })
export class FilmeService {
  private http = inject(HttpClient);
  private url = `${API_URL}/filmes`;

  listar(): Observable<Filme[]> {
    return this.http.get<Filme[]>(this.url);
  }

  buscar(id: number): Observable<Filme> {
    return this.http.get<Filme>(`${this.url}/${id}`);
  }

  cadastrar(filme: FilmeDados): Observable<Filme> {
    return this.http.post<Filme>(this.url, filme);
  }

  atualizar(id: number, filme: FilmeDados): Observable<Filme> {
    return this.http.put<Filme>(`${this.url}/${id}`, filme);
  }

  inativar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  reativar(id: number): Observable<Filme> {
    return this.http.patch<Filme>(`${this.url}/${id}/reativar`, {});
  }
}
