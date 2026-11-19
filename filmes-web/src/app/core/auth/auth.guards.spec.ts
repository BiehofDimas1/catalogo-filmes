import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
  UrlTree,
  provideRouter,
} from '@angular/router';
import { autenticadoGuard, perfilGuard, visitanteGuard } from './auth.guards';
import { CHAVE_SESSAO } from './repositorio-sessao';
import { Perfil } from './sessao';

function configurar(perfil?: Perfil) {
  if (perfil) {
    sessionStorage.setItem(
      CHAVE_SESSAO,
      JSON.stringify({
        token: 'token-teste',
        expiraEm: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
        usuario: { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil },
      }),
    );
  }
  TestBed.configureTestingModule({
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
}

function executar(guard: CanActivateFn, url = '/alvo') {
  return TestBed.runInInjectionContext(() =>
    guard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
  );
}

function destino(resultado: unknown): string {
  expect(resultado).toBeInstanceOf(UrlTree);
  return TestBed.inject(Router).serializeUrl(resultado as UrlTree);
}

describe('guards de navegação', () => {
  afterEach(() => sessionStorage.clear());

  it('autenticadoGuard manda o visitante para o login guardando a página pedida', () => {
    configurar();

    const resultado = executar(autenticadoGuard, '/meus-alugueis');

    expect(destino(resultado).startsWith('/login')).toBe(true);
    expect((resultado as UrlTree).queryParams['voltar']).toBe('/meus-alugueis');
  });

  it('autenticadoGuard deixa o usuário logado passar', () => {
    configurar('CLIENTE');

    expect(executar(autenticadoGuard)).toBe(true);
  });

  it('perfilGuard manda o cliente de volta ao catálogo nas telas do ADMIN', () => {
    configurar('CLIENTE');

    expect(destino(executar(perfilGuard('ADMIN')))).toBe('/filmes');
  });

  it('perfilGuard libera o perfil certo', () => {
    configurar('ADMIN');

    expect(executar(perfilGuard('ADMIN'))).toBe(true);
  });

  it('visitanteGuard tira o usuário logado das telas de login e cadastro', () => {
    configurar('ADMIN');

    expect(destino(executar(visitanteGuard))).toBe('/filmes');
  });

  it('visitanteGuard deixa o visitante entrar no login', () => {
    configurar();

    expect(executar(visitanteGuard)).toBe(true);
  });
});
