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
  /** Muda a cada alteração do filme, inclusive quando um aluguel retira ou devolve uma licença. */
  versao: number;
}

/** Dados que o ADMIN envia ao cadastrar ou editar um filme. */
export interface FilmeDados {
  titulo: string;
  duracaoMinutos: number;
  licencasDisponiveis: number;
  /** Só na edição: a versão do filme carregada no formulário. A API recusa (409) se ele mudou desde então. */
  versao?: number;
}

@Injectable({ providedIn: 'root' })
export class FilmeService {
  private http = inject(HttpClient);
  private url = `${API_URL}/filmes`;

  /** O ADMIN recebe todos os filmes; o CLIENTE, só os ativos. */
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
