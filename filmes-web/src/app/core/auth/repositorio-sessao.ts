import { Injectable } from '@angular/core';
import type { Sessao } from './sessao';

export const CHAVE_SESSAO = 'filmes.sessao';

@Injectable({ providedIn: 'root', useFactory: () => new RepositorioSessaoNavegador() })
export abstract class RepositorioSessao {
  abstract ler(): Sessao | null;
  abstract gravar(sessao: Sessao): void;
  abstract apagar(): void;
}

export class RepositorioSessaoNavegador extends RepositorioSessao {
  ler(): Sessao | null {
    return ouPadraoSeFalhar(() => {
      const salva = sessionStorage.getItem(CHAVE_SESSAO);
      return salva ? (JSON.parse(salva) as Sessao) : null;
    }, null);
  }

  gravar(sessao: Sessao): void {
    ouPadraoSeFalhar(() => sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao)), undefined);
  }

  apagar(): void {
    ouPadraoSeFalhar(() => sessionStorage.removeItem(CHAVE_SESSAO), undefined);
  }
}

function ouPadraoSeFalhar<T>(acao: () => T, padrao: T): T {
  try {
    return acao();
  } catch {
    return padrao;
  }
}
