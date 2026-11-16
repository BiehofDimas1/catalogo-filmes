import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { CHAVE_SESSAO, Perfil, Sessao } from './core/auth/auth';

function salvarSessao(perfil: Perfil) {
  const sessao: Sessao = {
    token: 'token-teste',
    expiraEm: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
    usuario: { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil },
  };
  sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(sessao));
}

describe('App', () => {
  afterEach(() => sessionStorage.clear());

  async function renderizar(): Promise<HTMLElement> {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('mostra a marca e os links de visitante sem sessão', async () => {
    const tela = await renderizar();

    expect(tela.querySelector('.marca')?.textContent).toContain('Catálogo de Filmes');
    expect(tela.textContent).toContain('Entrar');
    expect(tela.textContent).toContain('Criar conta');
    expect(tela.textContent).not.toContain('Sair');
  });

  it('mostra o menu do cliente quando há sessão de CLIENTE', async () => {
    salvarSessao('CLIENTE');
    const tela = await renderizar();

    expect(tela.textContent).toContain('Ana');
    expect(tela.textContent).toContain('Meus aluguéis');
    expect(tela.textContent).not.toContain('Novo filme');
  });

  it('mostra o menu do administrador quando há sessão de ADMIN', async () => {
    salvarSessao('ADMIN');
    const tela = await renderizar();

    expect(tela.textContent).toContain('Novo filme');
    expect(tela.textContent).toContain('Aluguéis');
    expect(tela.textContent).not.toContain('Meus aluguéis');
  });
});
