import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Aluguel } from '../aluguel';
import { API_URL } from '../core/api';
import { Filme } from '../filme';
import { CatalogoCliente } from './catalogo-cliente';

const REGRAS = { valor: 9.9, duracaoHoras: 48, prazoCancelamentoMinutos: 60, limiteAtivos: 5 };

function filme(id: number, titulo: string, licencas: number): Filme {
  return { id, titulo, duracaoMinutos: 120, licencasDisponiveis: licencas, ativo: true, versao: 0 };
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

describe('CatalogoCliente', () => {
  let http: HttpTestingController;

  async function criar(): Promise<ComponentFixture<CatalogoCliente>> {
    await TestBed.configureTestingModule({
      imports: [CatalogoCliente],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(CatalogoCliente);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    return fixture;
  }

  function texto(fixture: ComponentFixture<CatalogoCliente>): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  function botao(fixture: ComponentFixture<CatalogoCliente>, rotulo: string): HTMLButtonElement {
    const botoes = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button'));
    const encontrado = botoes.find((botao) => botao.textContent?.includes(rotulo));
    if (!encontrado) {
      throw new Error(`Botão "${rotulo}" não encontrado`);
    }
    return encontrado;
  }

  afterEach(() => http.verify());

  it('mostra as regras do aluguel e aluga um filme', async () => {
    const fixture = await criar();
    http.expectOne(`${API_URL}/filmes`).flush([filme(1, 'Matriz', 2), filme(2, 'Duna', 0)]);
    http.expectOne(`${API_URL}/alugueis/regras`).flush(REGRAS);
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
    const fixture = await criar();
    http.expectOne(`${API_URL}/filmes`).flush([filme(1, 'Matriz', 2)]);
    http.expectOne(`${API_URL}/alugueis/regras`).flush(REGRAS);
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
});
