import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { API_URL } from '../core/api';
import { FilmeForm } from './filme-form';

describe('FilmeForm', () => {
  let fixture: ComponentFixture<FilmeForm>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FilmeForm],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(FilmeForm);
    http = TestBed.inject(HttpTestingController);
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  function texto(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  it('abre como cadastro de filme novo', () => {
    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain('Novo filme');
  });

  it('não envia o formulário inválido', async () => {
    fixture.componentInstance.form.setValue({ titulo: '', duracaoMinutos: 700, licencasDisponiveis: -1 });

    fixture.componentInstance.salvar();
    await fixture.whenStable();

    expect(texto()).toContain('Corrija os campos destacados');
    expect(texto()).toContain('A duração deve ser de 1 a 600 minutos.');
    http.expectNone(`${API_URL}/filmes`);
  });

  it('mostra os erros de validação enviados pela API', async () => {
    fixture.componentInstance.form.setValue({ titulo: 'Filme longo', duracaoMinutos: 600, licencasDisponiveis: 1 });

    fixture.componentInstance.salvar();
    http.expectOne(`${API_URL}/filmes`).flush(
      { status: 400, detail: 'Verifique os campos informados.', erros: { duracaoMinutos: 'A duração máxima é 600 minutos.' } },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(texto()).toContain('A duração máxima é 600 minutos.');
  });

  it('mostra a mensagem de título duplicado', async () => {
    fixture.componentInstance.form.setValue({ titulo: 'Matriz', duracaoMinutos: 136, licencasDisponiveis: 1 });

    fixture.componentInstance.salvar();
    http
      .expectOne(`${API_URL}/filmes`)
      .flush({ status: 409, detail: 'Já existe um filme com o título "Matriz".' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(texto()).toContain('Já existe um filme com o título "Matriz".');
  });

  it('mostra sucesso ao salvar', async () => {
    fixture.componentInstance.form.setValue({ titulo: 'Duna', duracaoMinutos: 155, licencasDisponiveis: 2 });

    fixture.componentInstance.salvar();
    const req = http.expectOne(`${API_URL}/filmes`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ titulo: 'Duna', duracaoMinutos: 155, licencasDisponiveis: 2 });
    req.flush({ id: 9, titulo: 'Duna', duracaoMinutos: 155, licencasDisponiveis: 2, ativo: true }, { status: 201, statusText: 'Created' });
    await fixture.whenStable();

    expect(texto()).toContain('Filme salvo com sucesso!');
  });
});
