import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { API_URL } from '../core/api';
import { Cadastro } from './cadastro';

describe('Cadastro', () => {
  let fixture: ComponentFixture<Cadastro>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Cadastro],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Cadastro);
    http = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it('não chama a API com o formulário inválido', async () => {
    fixture.componentInstance.form.setValue({ nome: '', email: 'nao-e-email', senha: '123' });

    fixture.componentInstance.cadastrar();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Corrija os campos destacados');
    http.expectNone(`${API_URL}/auth/cadastro`);
  });

  it('cria a conta, entra e abre o catálogo com boas-vindas', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    fixture.componentInstance.form.setValue({ nome: 'Ana', email: 'ana@teste.com', senha: 'senha123' });

    fixture.componentInstance.cadastrar();
    const cadastro = http.expectOne(`${API_URL}/auth/cadastro`);
    expect(cadastro.request.body).toEqual({ nome: 'Ana', email: 'ana@teste.com', senha: 'senha123' });
    cadastro.flush({ id: 1, nome: 'Ana', email: 'ana@teste.com', perfil: 'CLIENTE' }, { status: 201, statusText: 'Created' });
    http.expectOne(`${API_URL}/auth/login`).flush({
      token: 'abc',
      tipo: 'Bearer',
      expiraEm: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
      usuario: { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil: 'CLIENTE' },
    });

    expect(navegar).toHaveBeenCalledWith(['/filmes'], { queryParams: { bemvindo: 1 } });
  });

  it('mostra a mensagem da API quando o e-mail já está em uso', async () => {
    fixture.componentInstance.form.setValue({ nome: 'Ana', email: 'ana@teste.com', senha: 'senha123' });

    fixture.componentInstance.cadastrar();
    http
      .expectOne(`${API_URL}/auth/cadastro`)
      .flush({ status: 409, detail: 'Já existe uma conta com este e-mail.' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Já existe uma conta com este e-mail.');
  });
});
