import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Aluguel } from '../aluguel';
import { API_URL } from '../core/api';
import { CHAVE_SESSAO, Perfil } from '../core/auth/auth';
import { Filme } from '../filme';
import { FilmeLista } from './filme-lista';

function filme(id: number, titulo: string, licencas: number, ativo = true): Filme {
  return { id, titulo, duracaoMinutos: 120, licencasDisponiveis: licencas, ativo, versao: 0 };
}

function aluguel(): Aluguel {
  return {
    id: 5,
    filmeId: 1,
    filmeTitulo: 'Matriz',
    usuarioId: 1,
    usuarioNome: 'Ana',
    usuarioEmail: 'ana@teste.com',
    status: 'ATIVO',
    dataAluguel: '2026-11-15T12:00:00Z',
    dataExpiracao: '2026-11-17T12:00:00Z',
    dataCancelamento: null,
    cancelavelAte: '2026-11-15T13:00:00Z',
    cancelavel: true,
    valor: 9.9,
    transacaoPagamento: 'PAG-SIM-1',
    transacaoReembolso: null,
    codigoAcesso: 'codigo-ativo',
  };
}

describe('FilmeLista', () => {
  let http: HttpTestingController;

  async function criar(perfil: Perfil): Promise<ComponentFixture<FilmeLista>> {
    sessionStorage.setItem(
      CHAVE_SESSAO,
      JSON.stringify({
        token: 'token-teste',
        expiraEm: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
        usuario: { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil },
      }),
    );
    await TestBed.configureTestingModule({
      imports: [FilmeLista],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(FilmeLista);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    return fixture;
  }

  function texto(fixture: ComponentFixture<FilmeLista>): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  function botao(fixture: ComponentFixture<FilmeLista>, rotulo: string): HTMLButtonElement {
    const botoes = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button'));
    const encontrado = botoes.find((b) => b.textContent?.includes(rotulo));
    if (!encontrado) {
      throw new Error(`Botão "${rotulo}" não encontrado`);
    }
    return encontrado;
  }

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it('cliente vê as regras do aluguel e aluga um filme', async () => {
    const fixture = await criar('CLIENTE');
    http.expectOne(`${API_URL}/filmes`).flush([filme(1, 'Matriz', 2), filme(2, 'Duna', 0)]);
    http
      .expectOne(`${API_URL}/alugueis/regras`)
      .flush({ valor: 9.9, duracaoHoras: 48, prazoCancelamentoMinutos: 60, limiteAtivos: 5 });
    await fixture.whenStable();

    expect(texto(fixture)).toContain('48 horas');
    expect(texto(fixture)).toContain('Indisponível');
    expect(texto(fixture)).not.toContain('Inativar');

    botao(fixture, 'Alugar').click();
    const req = http.expectOne(`${API_URL}/alugueis`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ filmeId: 1 });
    req.flush(aluguel());
    http.expectOne(`${API_URL}/filmes`).flush([filme(1, 'Matriz', 1), filme(2, 'Duna', 0)]);
    await fixture.whenStable();

    expect(texto(fixture)).toContain('alugado com sucesso');
    expect(texto(fixture)).toContain('codigo-ativo');
  });

  it('mostra a mensagem da API quando o aluguel é recusado', async () => {
    const fixture = await criar('CLIENTE');
    http.expectOne(`${API_URL}/filmes`).flush([filme(1, 'Matriz', 2)]);
    http
      .expectOne(`${API_URL}/alugueis/regras`)
      .flush({ valor: 9.9, duracaoHoras: 48, prazoCancelamentoMinutos: 60, limiteAtivos: 5 });
    await fixture.whenStable();

    botao(fixture, 'Alugar').click();
    http
      .expectOne(`${API_URL}/alugueis`)
      .flush(
        { status: 422, detail: 'Você atingiu o limite de 5 aluguéis ativos.' },
        { status: 422, statusText: 'Unprocessable Entity' },
      );
    await fixture.whenStable();

    expect(texto(fixture)).toContain('Você atingiu o limite de 5 aluguéis ativos.');
  });

  it('administrador vê o status dos filmes e inativa um filme', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = await criar('ADMIN');
    http.expectOne(`${API_URL}/filmes`).flush([filme(1, 'Matriz', 2), filme(2, 'Duna', 1, false)]);
    await fixture.whenStable();

    expect(texto(fixture)).toContain('Inativo');
    expect(texto(fixture)).toContain('Reativar');
    expect(texto(fixture)).not.toContain('Alugar');

    botao(fixture, 'Inativar').click();
    const req = http.expectOne(`${API_URL}/filmes/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
    http.expectOne(`${API_URL}/filmes`).flush([filme(1, 'Matriz', 2, false), filme(2, 'Duna', 1, false)]);
    await fixture.whenStable();

    expect(texto(fixture)).toContain('Filme "Matriz" inativado com sucesso.');
  });

  it('administrador reativa um filme inativo', async () => {
    const fixture = await criar('ADMIN');
    http.expectOne(`${API_URL}/filmes`).flush([filme(2, 'Duna', 1, false)]);
    await fixture.whenStable();

    botao(fixture, 'Reativar').click();
    const req = http.expectOne(`${API_URL}/filmes/2/reativar`);
    expect(req.request.method).toBe('PATCH');
    req.flush(filme(2, 'Duna', 1, true));
    http.expectOne(`${API_URL}/filmes`).flush([filme(2, 'Duna', 1, true)]);
    await fixture.whenStable();

    expect(texto(fixture)).toContain('Filme "Duna" reativado com sucesso.');
  });
});
