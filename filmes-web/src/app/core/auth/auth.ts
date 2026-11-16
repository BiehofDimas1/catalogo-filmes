import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';
import { API_URL } from '../api';

export type Perfil = 'ADMIN' | 'CLIENTE';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
}

/** Sessão guardada no navegador. Serve só para a interface: quem autoriza é a API. */
export interface Sessao {
  token: string;
  expiraEm: string;
  usuario: Usuario;
}

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

export const CHAVE_SESSAO = 'filmes.sessao';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private sessaoAtual = signal<Sessao | null>(lerSessaoSalva());

  readonly usuario = computed(() => this.sessaoAtual()?.usuario ?? null);
  readonly ehAdmin = computed(() => this.usuario()?.perfil === 'ADMIN');
  readonly ehCliente = computed(() => this.usuario()?.perfil === 'CLIENTE');

  entrar(dados: DadosLogin): Observable<Sessao> {
    return this.http.post<RespostaLogin>(`${API_URL}/auth/login`, dados).pipe(
      map(({ token, expiraEm, usuario }) => ({ token, expiraEm, usuario })),
      tap((sessao) => this.salvar(sessao)),
    );
  }

  cadastrar(dados: DadosCadastro): Observable<Usuario> {
    return this.http.post<Usuario>(`${API_URL}/auth/cadastro`, dados);
  }

  /** Há sessão e o token ainda vale? (Encerra a sessão vencida.) Não use em templates. */
  estaLogado(): boolean {
    return this.token() !== null;
  }

  /** Token da sessão, ou null se não houver sessão válida. Não use em templates. */
  token(): string | null {
    const sessao = this.sessaoAtual();
    if (!sessao) {
      return null;
    }
    if (expirou(sessao)) {
      this.sair();
      return null;
    }
    return sessao.token;
  }

  sair(): void {
    this.sessaoAtual.set(null);
    try {
      sessionStorage.removeItem(CHAVE_SESSAO);
    } catch {
      // Armazenamento indisponível: a sessão já saiu da memória.
    }
  }

  private salvar(sessao: Sessao): void {
    this.sessaoAtual.set(sessao);
    try {
      sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
    } catch {
      // Armazenamento indisponível: a sessão fica só em memória até recarregar a página.
    }
  }
}

function expirou(sessao: Sessao): boolean {
  const limite = Date.parse(sessao.expiraEm);
  return Number.isNaN(limite) || limite <= Date.now();
}

function lerSessaoSalva(): Sessao | null {
  try {
    const salva = sessionStorage.getItem(CHAVE_SESSAO);
    if (!salva) {
      return null;
    }
    const sessao = JSON.parse(salva) as Sessao;
    if (!sessao?.token || !sessao.usuario || expirou(sessao)) {
      sessionStorage.removeItem(CHAVE_SESSAO);
      return null;
    }
    return sessao;
  } catch {
    return null;
  }
}
