import { HttpErrorResponse } from '@angular/common/http';
import { mensagemDeErro } from './erro-http';

describe('mensagemDeErro', () => {
  it('junta o detail e os erros por campo enviados pela API', () => {
    const erro = new HttpErrorResponse({
      status: 400,
      error: {
        detail: 'Verifique os campos informados.',
        erros: { duracaoMinutos: 'A duração máxima é 600 minutos.' },
      },
    });

    expect(mensagemDeErro(erro)).toBe(
      'Verifique os campos informados. A duração máxima é 600 minutos.',
    );
  });

  it('usa só o detail quando não há erros por campo', () => {
    const erro = new HttpErrorResponse({
      status: 422,
      error: { detail: 'Não há licenças disponíveis.' },
    });

    expect(mensagemDeErro(erro)).toBe('Não há licenças disponíveis.');
  });

  it('avisa quando a API não responde', () => {
    expect(mensagemDeErro(new HttpErrorResponse({ status: 0 }))).toContain(
      'Não foi possível conectar à API',
    );
  });

  it('usa a mensagem padrão quando a resposta não tem detalhe', () => {
    const erro = new HttpErrorResponse({ status: 500, error: 'falha' });

    expect(mensagemDeErro(erro, 'Erro ao salvar.')).toBe('Erro ao salvar.');
    expect(mensagemDeErro(new Error('qualquer'), 'Padrão.')).toBe('Padrão.');
  });
});
