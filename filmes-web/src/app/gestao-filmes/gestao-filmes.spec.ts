import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { API_URL } from '../core/api';
import { Filme } from '../filme';
import { GestaoFilmes } from './gestao-filmes';

function filme(id: number, titulo: string, licencas: number, ativo = true): Filme {
  return { id, titulo, duracaoMinutos: 120, licencasDisponiveis: licencas, ativo, versao: 0 };
}

describe('GestaoFilmes', () => {
  let http: HttpTestingController;

  async function criar(): Promise<ComponentFixture<GestaoFilmes>> {
    await TestBed.configureTestingModule({
      imports: [GestaoFilmes],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    const fixture = TestBed.createComponent(GestaoFilmes);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    return fixture;
  }

  function texto(fixture: ComponentFixture<GestaoFilmes>): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  function botao(fixture: ComponentFixture<GestaoFilmes>, rotulo: string): HTMLButtonElement {
    const botoes = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('button'));
    const encontrado = botoes.find((botao) => botao.textContent?.includes(rotulo));
    if (!encontrado) {
      throw new Error(`Botão "${rotulo}" não encontrado`);
    }
    return encontrado;
  }

  afterEach(() => {
    http.verify();
    vi.restoreAllMocks();
  });

  it('mostra o status dos filmes e inativa um filme', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    const fixture = await criar();
    http.expectOne(`${API_URL}/filmes`).flush([filme(1, 'Matriz', 2), filme(2, 'Duna', 1, false)]);
    await fixture.whenStable();

    expect(texto(fixture)).toContain('Inativo');
    expect(texto(fixture)).toContain('Reativar');
    expect(texto(fixture)).not.toContain('Alugar');

    botao(fixture, 'Inativar').click();
    const req = http.expectOne(`${API_URL}/filmes/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
    http
      .expectOne(`${API_URL}/filmes`)
      .flush([filme(1, 'Matriz', 2, false), filme(2, 'Duna', 1, false)]);
    await fixture.whenStable();

    expect(texto(fixture)).toContain('Filme "Matriz" inativado com sucesso.');
  });

  it('reativa um filme inativo', async () => {
    const fixture = await criar();
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
