import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Aluguel } from '../aluguel';
import { API_URL } from '../core/api';
import { MeusAlugueis } from './meus-alugueis';

function aluguel(dados: Partial<Aluguel> = {}): Aluguel {
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
    ...dados,
  };
}

const cancelado = (id: number) =>
  aluguel({ id, status: 'CANCELADO', cancelavel: false, transacaoReembolso: 'REE-SIM-1', codigoAcesso: 'codigo-antigo' });

describe('MeusAlugueis', () => {
  let fixture: ComponentFixture<MeusAlugueis>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MeusAlugueis],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MeusAlugueis);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
    vi.restoreAllMocks();
  });

  function texto(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  function botaoCancelar(): HTMLButtonElement {
    const botao = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button')).find((b) =>
      b.textContent?.includes('Cancelar'),
    );
    if (!botao) {
      throw new Error('Botão Cancelar não encontrado');
    }
    return botao;
  }

  it('mostra o histórico, com o código só do aluguel ativo', async () => {
    http.expectOne(`${API_URL}/alugueis/meus`).flush([aluguel(), cancelado(4)]);
    await fixture.whenStable();

    expect(texto()).toContain('codigo-ativo');
    expect(texto()).toContain('Cancelado');
    expect(texto()).toContain('(reembolsado)');
    expect(texto()).not.toContain('codigo-antigo');
  });

  it('avisa quando ainda não há aluguéis', async () => {
    http.expectOne(`${API_URL}/alugueis/meus`).flush([]);
    await fixture.whenStable();

    expect(texto()).toContain('Você ainda não alugou nenhum filme.');
  });

  it('cancela um aluguel dentro do prazo', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    http.expectOne(`${API_URL}/alugueis/meus`).flush([aluguel()]);
    await fixture.whenStable();

    botaoCancelar().click();
    const req = http.expectOne(`${API_URL}/alugueis/5/cancelar`);
    expect(req.request.method).toBe('POST');
    req.flush(cancelado(5));
    http.expectOne(`${API_URL}/alugueis/meus`).flush([cancelado(5)]);
    await fixture.whenStable();

    expect(texto()).toContain('Aluguel de "Matriz" cancelado. Reembolso simulado: REE-SIM-1.');
  });

  it('mostra o erro da API quando o prazo de cancelamento terminou', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    http.expectOne(`${API_URL}/alugueis/meus`).flush([aluguel()]);
    await fixture.whenStable();

    botaoCancelar().click();
    http
      .expectOne(`${API_URL}/alugueis/5/cancelar`)
      .flush(
        { status: 422, detail: 'O prazo para cancelar (1 hora após o aluguel) já terminou.' },
        { status: 422, statusText: 'Unprocessable Entity' },
      );
    http.expectOne(`${API_URL}/alugueis/meus`).flush([aluguel({ cancelavel: false })]);
    await fixture.whenStable();

    expect(texto()).toContain('O prazo para cancelar (1 hora após o aluguel) já terminou.');
  });

  it('não cancela quando o cliente desiste na confirmação', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(false);
    http.expectOne(`${API_URL}/alugueis/meus`).flush([aluguel()]);
    await fixture.whenStable();

    botaoCancelar().click();

    http.expectNone(`${API_URL}/alugueis/5/cancelar`);
  });
});
