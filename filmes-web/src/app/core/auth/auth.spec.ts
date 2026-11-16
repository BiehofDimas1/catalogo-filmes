import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../api';
import { AuthService, CHAVE_SESSAO } from './auth';

const usuario = { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil: 'CLIENTE' as const };

function horas(n: number): string {
  return new Date(Date.now() + n * 60 * 60 * 1000).toISOString();
}

describe('AuthService', () => {
  let http: HttpTestingController | undefined;

  function criar(): AuthService {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    http = TestBed.inject(HttpTestingController);
    return TestBed.inject(AuthService);
  }

  afterEach(() => {
    http?.verify();
    sessionStorage.clear();
  });

  it('guarda a sessão depois do login', () => {
    const auth = criar();

    auth.entrar({ email: 'ana@teste.com', senha: 'senha123' }).subscribe();
    const req = http!.expectOne(`${API_URL}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ token: 'abc', tipo: 'Bearer', expiraEm: horas(2), usuario });

    expect(auth.token()).toBe('abc');
    expect(auth.ehCliente()).toBe(true);
    expect(auth.ehAdmin()).toBe(false);
    expect(JSON.parse(sessionStorage.getItem(CHAVE_SESSAO) ?? '{}').token).toBe('abc');
  });

  it('sair apaga a sessão', () => {
    sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify({ token: 'abc', expiraEm: horas(2), usuario }));
    const auth = criar();
    expect(auth.estaLogado()).toBe(true);

    auth.sair();

    expect(auth.usuario()).toBeNull();
    expect(auth.estaLogado()).toBe(false);
    expect(sessionStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it('ignora uma sessão salva que já expirou', () => {
    sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify({ token: 'velho', expiraEm: horas(-1), usuario }));

    const auth = criar();

    expect(auth.usuario()).toBeNull();
    expect(auth.token()).toBeNull();
    expect(sessionStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it('cadastra pela rota pública da API', () => {
    const auth = criar();

    auth.cadastrar({ nome: 'Ana', email: 'ana@teste.com', senha: 'senha123' }).subscribe();

    const req = http!.expectOne(`${API_URL}/auth/cadastro`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ nome: 'Ana', email: 'ana@teste.com', senha: 'senha123' });
    req.flush(usuario);
  });
});
