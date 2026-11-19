import { Injectable, computed, inject, signal } from '@angular/core';
import { RepositorioSessao } from './repositorio-sessao';

export type Perfil = 'ADMIN' | 'CLIENTE';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
}

export interface Sessao {
  token: string;
  expiraEm: string;
  usuario: Usuario;
}

@Injectable({ providedIn: 'root' })
export class SessaoService {
  private repositorio = inject(RepositorioSessao);
  private atual = signal<Sessao | null>(this.restaurar());

  readonly usuario = computed(() => this.atual()?.usuario ?? null);
  readonly ehAdmin = computed(() => this.usuario()?.perfil === 'ADMIN');
  readonly ehCliente = computed(() => this.usuario()?.perfil === 'CLIENTE');

  iniciar(sessao: Sessao): void {
    this.atual.set(sessao);
    this.repositorio.gravar(sessao);
  }

  encerrar(): void {
    this.atual.set(null);
    this.repositorio.apagar();
  }

  estaLogado(): boolean {
    return this.token() !== null;
  }

  token(): string | null {
    const sessao = this.atual();
    if (!sessao) {
      return null;
    }
    if (expirou(sessao)) {
      this.encerrar();
      return null;
    }
    return sessao.token;
  }

  private restaurar(): Sessao | null {
    const salva = this.repositorio.ler();
    if (salva && !valida(salva)) {
      this.repositorio.apagar();
      return null;
    }
    return salva;
  }
}

function expirou(sessao: Sessao): boolean {
  const limite = Date.parse(sessao.expiraEm);
  return Number.isNaN(limite) || limite <= Date.now();
}

function valida(sessao: Sessao): boolean {
  return Boolean(sessao.token && sessao.usuario) && !expirou(sessao);
}
