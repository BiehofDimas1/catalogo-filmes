import { HttpErrorResponse } from '@angular/common/http';

interface Problema {
  title?: string;
  detail?: string;
  erros?: Record<string, string>;
}

export function mensagemDeErro(
  erro: unknown,
  padrao = 'Não foi possível concluir a operação.',
): string {
  if (!(erro instanceof HttpErrorResponse)) {
    return padrao;
  }
  if (erro.status === 0) {
    return 'Não foi possível conectar à API. Verifique se o backend está rodando na porta 8080.';
  }
  const problema = erro.error as Problema | null;
  if (problema && typeof problema === 'object' && problema.detail) {
    const campos = problema.erros ? Object.values(problema.erros) : [];
    return campos.length > 0 ? `${problema.detail} ${campos.join(' ')}` : problema.detail;
  }
  return padrao;
}
