import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../api';
import { AuthService } from './auth';
import { CHAVE_SESSAO } from './repositorio-sessao';
import { SessaoService } from './sessao';

const usuario = { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil: 'CLIENTE' as const };

function horas(n: number): string {
  return new Date(Date.now() + n * 60 * 60 * 1000).toISOString();
}

describe('AuthService', () => {
  let http: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it('inicia a sessão depois do login', () => {
    auth.entrar({ email: 'ana@teste.com', senha: 'senha123' }).subscribe();
    const req = http.expectOne(`${API_URL}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'abc', tipo: 'Bearer', expiraEm: horas(2), usuario });

    const sessao = TestBed.inject(SessaoService);
    expect(sessao.token()).toBe('abc');
    expect(sessao.ehCliente()).toBe(true);
    expect(sessao.ehAdmin()).toBe(false);
    expect(JSON.parse(sessionStorage.getItem(CHAVE_SESSAO) ?? '{}').token).toBe('abc');
  });

  it('cadastra pela rota pública da API', () => {
    auth.cadastrar({ nome: 'Ana', email: 'ana@teste.com', senha: 'senha123' }).subscribe();

    const req = http.expectOne(`${API_URL}/auth/cadastro`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nome: 'Ana', email: 'ana@teste.com', senha: 'senha123' });
    req.flush(usuario);
  });
});
