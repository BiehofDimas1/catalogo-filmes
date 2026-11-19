import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from './core/api';
import { Filme, FilmeService } from './filme';

describe('FilmeService', () => {
  let service: FilmeService;
  let http: HttpTestingController;

  const matriz: Filme = {
    id: 1,
    titulo: 'Matriz',
    duracaoMinutos: 136,
    licencasDisponiveis: 2,
    ativo: true,
    versao: 0,
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(FilmeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista os filmes com GET /filmes', () => {
    let quantidade = 0;
    service.listar().subscribe((filmes) => (quantidade = filmes.length));

    const req = http.expectOne(`${API_URL}/filmes`);
    expect(req.request.method).toBe('GET');
    req.flush([matriz]);

    expect(quantidade).toBe(1);
  });

  it('cadastra com POST enviando só os campos do formulário', () => {
    service.cadastrar({ titulo: 'Duna', duracaoMinutos: 155, licencasDisponiveis: 3 }).subscribe();

    const req = http.expectOne(`${API_URL}/filmes`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      titulo: 'Duna',
      duracaoMinutos: 155,
      licencasDisponiveis: 3,
    });
    req.flush({ ...matriz, id: 2, titulo: 'Duna' });
  });

  it('atualiza com PUT enviando a versão carregada', () => {
    service
      .atualizar(1, { titulo: 'Matriz', duracaoMinutos: 140, licencasDisponiveis: 2, versao: 3 })
      .subscribe();

    const req = http.expectOne(`${API_URL}/filmes/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({
      titulo: 'Matriz',
      duracaoMinutos: 140,
      licencasDisponiveis: 2,
      versao: 3,
    });
    req.flush({ ...matriz, duracaoMinutos: 140, versao: 4 });
  });

  it('inativa com DELETE /filmes/{id}', () => {
    service.inativar(1).subscribe();

    const req = http.expectOne(`${API_URL}/filmes/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('reativa com PATCH /filmes/{id}/reativar', () => {
    service.reativar(1).subscribe();

    const req = http.expectOne(`${API_URL}/filmes/1/reativar`);
    expect(req.request.method).toBe('PATCH');
    req.flush(matriz);
  });
});
