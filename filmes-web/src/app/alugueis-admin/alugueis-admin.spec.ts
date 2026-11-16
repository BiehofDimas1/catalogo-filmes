import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Aluguel } from '../aluguel';
import { API_URL } from '../core/api';
import { AlugueisAdmin } from './alugueis-admin';

const aluguel: Aluguel = {
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

describe('AlugueisAdmin', () => {
  let fixture: ComponentFixture<AlugueisAdmin>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AlugueisAdmin],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AlugueisAdmin);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function texto(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('lista os aluguéis de todos os clientes', async () => {
    http.expectOne(`${API_URL}/alugueis`).flush([aluguel]);
    await fixture.whenStable();

    expect(texto()).toContain('Ana');
    expect(texto()).toContain('ana@teste.com');
    expect(texto()).toContain('codigo-ativo');
  });

  it('valida um código e mostra que o acesso está liberado', async () => {
    http.expectOne(`${API_URL}/alugueis`).flush([]);
    await fixture.whenStable();

    fixture.componentInstance.form.setValue({ codigo: ' codigo-ativo ' });
    fixture.componentInstance.validar();
    http.expectOne(`${API_URL}/alugueis/acesso/codigo-ativo`).flush({
      valido: true,
      mensagem: 'Acesso liberado: o aluguel está ativo.',
      status: 'ATIVO',
      filmeTitulo: 'Matriz',
      usuarioNome: 'Ana',
      dataExpiracao: '2026-11-17T12:00:00Z',
    });
    // Depois de validar, a lista é recarregada para mostrar o status atual.
    http.expectOne(`${API_URL}/alugueis`).flush([aluguel]);
    await fixture.whenStable();

    expect(texto()).toContain('Acesso liberado: o aluguel está ativo.');
  });

  it('mostra o erro da API para código inexistente', async () => {
    http.expectOne(`${API_URL}/alugueis`).flush([]);
    await fixture.whenStable();

    fixture.componentInstance.form.setValue({ codigo: 'nao-existe' });
    fixture.componentInstance.validar();
    http
      .expectOne(`${API_URL}/alugueis/acesso/nao-existe`)
      .flush({ status: 404, detail: 'Código de acesso não encontrado.' }, { status: 404, statusText: 'Not Found' });
    await fixture.whenStable();

    expect(texto()).toContain('Código de acesso não encontrado.');
  });

  it('pede o código antes de chamar a API', async () => {
    http.expectOne(`${API_URL}/alugueis`).flush([]);
    await fixture.whenStable();

    fixture.componentInstance.validar();
    await fixture.whenStable();

    expect(texto()).toContain('Informe o código de acesso.');
    http.expectNone((req) => req.url.includes('/alugueis/acesso/'));
  });
});
