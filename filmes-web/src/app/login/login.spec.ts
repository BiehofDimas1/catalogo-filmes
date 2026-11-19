import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { API_URL } from '../core/api';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    http = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  it('não chama a API com o formulário inválido', async () => {
    fixture.componentInstance.entrar();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Corrija os campos destacados');
    http.expectNone(`${API_URL}/auth/login`);
  });

  it('entra e abre o catálogo', () => {
    const navegar = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    fixture.componentInstance.form.setValue({ email: 'ana@teste.com', senha: 'senha123' });

    fixture.componentInstance.entrar();
    http.expectOne(`${API_URL}/auth/login`).flush({
      token: 'abc',
      tipo: 'Bearer',
      expiraEm: new Date(Date.now() + 60 * 60 * 1000).toISOString(),
      usuario: { id: 1, nome: 'Ana', email: 'ana@teste.com', perfil: 'CLIENTE' },
    });

    expect(navegar).toHaveBeenCalledWith('/filmes');
  });

  it('mostra a mensagem da API quando o e-mail ou a senha estão errados', async () => {
    fixture.componentInstance.form.setValue({ email: 'ana@teste.com', senha: 'errada1' });

    fixture.componentInstance.entrar();
    http
      .expectOne(`${API_URL}/auth/login`)
      .flush(
        { status: 401, detail: 'E-mail ou senha inválidos.' },
        { status: 401, statusText: 'Unauthorized' },
      );
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('E-mail ou senha inválidos.');
  });
});
