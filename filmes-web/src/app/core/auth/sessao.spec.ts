import { TestBed } from '@angular/core/testing';
import { CHAVE_SESSAO, RepositorioSessao } from './repositorio-sessao';
import { Sessao, SessaoService } from './sessao';

const usuario = { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil: 'CLIENTE' as const };

function horas(n: number): string {
  return new Date(Date.now() + n * 60 * 60 * 1000).toISOString();
}

class RepositorioEmMemoria extends RepositorioSessao {
  salva: Sessao | null = null;

  ler(): Sessao | null {
    return this.salva;
  }

  gravar(sessao: Sessao): void {
    this.salva = sessao;
  }

  apagar(): void {
    this.salva = null;
  }
}

describe('SessaoService', () => {
  afterEach(() => sessionStorage.clear());

  it('encerrar apaga a sessão', () => {
    sessionStorage.setItem(
      CHAVE_SESSAO,
      JSON.stringify({ token: 'abc', expiraEm: horas(2), usuario }),
    );
    const sessao = TestBed.inject(SessaoService);
    expect(sessao.estaLogado()).toBe(true);

    sessao.encerrar();

    expect(sessao.usuario()).toBeNull();
    expect(sessao.estaLogado()).toBe(false);
    expect(sessionStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it('ignora uma sessão salva que já expirou', () => {
    sessionStorage.setItem(
      CHAVE_SESSAO,
      JSON.stringify({ token: 'velho', expiraEm: horas(-1), usuario }),
    );

    const sessao = TestBed.inject(SessaoService);

    expect(sessao.usuario()).toBeNull();
    expect(sessao.token()).toBeNull();
    expect(sessionStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it('funciona com qualquer repositório de sessão', () => {
    const repositorio = new RepositorioEmMemoria();
    TestBed.configureTestingModule({
      providers: [{ provide: RepositorioSessao, useValue: repositorio }],
    });
    const sessao = TestBed.inject(SessaoService);

    sessao.iniciar({ token: 'abc', expiraEm: horas(2), usuario });

    expect(repositorio.salva?.token).toBe('abc');
    expect(sessao.ehCliente()).toBe(true);
    expect(sessionStorage.getItem(CHAVE_SESSAO)).toBeNull();

    sessao.encerrar();

    expect(repositorio.salva).toBeNull();
  });
});
