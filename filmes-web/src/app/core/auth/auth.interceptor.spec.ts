import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { API_URL } from '../api';
import { authInterceptor } from './auth.interceptor';
import { CHAVE_SESSAO } from './repositorio-sessao';
import { SessaoService } from './sessao';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controle: HttpTestingController;

  beforeEach(() => {
    sessionStorage.setItem(
      CHAVE_SESSAO,
      JSON.stringify({
        token: 'token-teste',
        expiraEm: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
        usuario: { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil: 'CLIENTE' },
      }),
    );
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controle = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controle.verify();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it('envia o token nas chamadas privadas da API', () => {
    http.get(`${API_URL}/filmes`).subscribe();

    const req = controle.expectOne(`${API_URL}/filmes`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-teste');
    req.flush([]);
  });

  it('não envia o token no login nem no cadastro', () => {
    http.post(`${API_URL}/auth/login`, {}).subscribe();
    http.post(`${API_URL}/auth/cadastro`, {}).subscribe();

    expect(controle.expectOne(`${API_URL}/auth/login`).request.headers.has('Authorization')).toBe(
      false,
    );
    expect(
      controle.expectOne(`${API_URL}/auth/cadastro`).request.headers.has('Authorization'),
    ).toBe(false);
  });

  it('não envia o token para outros endereços', () => {
    http.get('https://exemplo.com/dados').subscribe();

    expect(
      controle.expectOne('https://exemplo.com/dados').request.headers.has('Authorization'),
    ).toBe(false);
  });

  it('encerra a sessão e volta ao login quando a API responde 401', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    let falhou = false;

    http.get(`${API_URL}/filmes`).subscribe({ error: () => (falhou = true) });
    controle
      .expectOne(`${API_URL}/filmes`)
      .flush(
        { detail: 'Faça login para acessar este recurso.' },
        { status: 401, statusText: 'Unauthorized' },
      );

    expect(falhou).toBe(true);
    expect(TestBed.inject(SessaoService).usuario()).toBeNull();
    expect(navegar).toHaveBeenCalledWith(['/login'], { queryParams: { sessao: 'expirada' } });
  });
});
