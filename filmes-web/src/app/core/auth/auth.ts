import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { API_URL } from '../api';
import { Sessao, SessaoService, Usuario } from './sessao';

export interface DadosLogin {
  email: string;
  senha: string;
}

export interface DadosCadastro extends DadosLogin {
  nome: string;
}

interface RespostaLogin extends Sessao {
  tipo: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private sessao = inject(SessaoService);

  entrar(dados: DadosLogin): Observable<Sessao> {
    return this.http.post<RespostaLogin>(`${API_URL}/auth/login`, dados).pipe(
      map(({ token, expiraEm, usuario }) => ({ token, expiraEm, usuario })),
      tap((sessao) => this.sessao.iniciar(sessao)),
    );
  }

  cadastrar(dados: DadosCadastro): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_URL}/auth/cadastro`, dados);
  }
}
