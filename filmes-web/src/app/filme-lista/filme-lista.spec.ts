import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { API_URL } from '../core/api';
import { CHAVE_SESSAO } from '../core/auth/repositorio-sessao';
import { Perfil } from '../core/auth/sessao';
import { FilmeLista } from './filme-lista';

describe('FilmeLista', () => {
  let http: HttpTestingController;

  async function renderizar(perfil: Perfil): Promise<HTMLElement> {
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
    return fixture.nativeElement as HTMLElement;
  }

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  it('mostra o catálogo de aluguel para o CLIENTE', async () => {
    const tela = await renderizar('CLIENTE');
    http.expectOne(`${API_URL}/filmes`).flush([]);
    http
      .expectOne(`${API_URL}/alugueis/regras`)
      .flush({ valor: 9.9, duracaoHoras: 48, prazoCancelamentoMinutos: 60, limiteAtivos: 5 });

    expect(tela.querySelector('app-catalogo-cliente')).not.toBeNull();
    expect(tela.querySelector('app-gestao-filmes')).toBeNull();
  });

  it('mostra a gestão de filmes para o ADMIN', async () => {
    const tela = await renderizar('ADMIN');
    http.expectOne(`${API_URL}/filmes`).flush([]);

    expect(tela.querySelector('app-gestao-filmes')).not.toBeNull();
    expect(tela.querySelector('app-catalogo-cliente')).toBeNull();
  });
});
