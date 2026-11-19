import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AluguelService } from './aluguel';
import { API_URL } from './core/api';

describe('AluguelService', () => {
  let service: AluguelService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AluguelService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('aluga com POST /alugueis enviando o filme', () => {
    service.alugar(3).subscribe();

    const req = http.expectOne(`${API_URL}/alugueis`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ filmeId: 3 });
    req.flush({});
  });

  it('busca o histórico do cliente em /alugueis/meus', () => {
    service.meus().subscribe();

    expect(http.expectOne(`${API_URL}/alugueis/meus`).request.method).toBe('GET');
  });

  it('cancela com POST /alugueis/{id}/cancelar', () => {
    service.cancelar(5).subscribe();

    const req = http.expectOne(`${API_URL}/alugueis/5/cancelar`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('valida o código sem espaços e codificado na URL', () => {
    service.validarAcesso('  abc 123 ').subscribe();

    const req = http.expectOne(`${API_URL}/alugueis/acesso/abc%20123`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });
});
