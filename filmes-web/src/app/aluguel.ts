import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from './core/api';

export type StatusAluguel = 'ATIVO' | 'EXPIRADO' | 'CANCELADO';

export interface Aluguel {
  id: number;
  filmeId: number;
  filmeTitulo: string;
  usuarioId: number;
  usuarioNome: string;
  usuarioEmail: string;
  status: StatusAluguel;
  dataAluguel: string;
  dataExpiracao: string;
  dataCancelamento: string | null;
  cancelavelAte: string;
  cancelavel: boolean;
  valor: number;
  transacaoPagamento: string;
  transacaoReembolso: string | null;
  codigoAcesso: string;
}

export interface RegrasAluguel {
  valor: number;
  duracaoHoras: number;
  prazoCancelamentoMinutos: number;
  limiteAtivos: number;
}

export interface ValidacaoAcesso {
  valido: boolean;
  mensagem: string;
  status: StatusAluguel;
  filmeTitulo: string;
  usuarioNome: string;
  dataExpiracao: string;
}

export const ROTULO_STATUS: Record<StatusAluguel, string> = {
  ATIVO: 'Ativo',
  EXPIRADO: 'Expirado',
  CANCELADO: 'Cancelado',
};

@Injectable({ providedIn: 'root' })
export class AluguelService {
  private http = inject(HttpClient);
  private url = `${API_URL}/alugueis`;

  regras(): Observable<RegrasAluguel> {
    return this.http.get<RegrasAluguel>(`${this.url}/regras`);
  }

  alugar(filmeId: number): Observable<Aluguel> {
    return this.http.post<Aluguel>(this.url, { filmeId });
  }

  meus(): Observable<Aluguel[]> {
    return this.http.get<Aluguel[]>(`${this.url}/meus`);
  }

  todos(): Observable<Aluguel[]> {
    return this.http.get<Aluguel[]>(this.url);
  }

  cancelar(id: number): Observable<Aluguel> {
    return this.http.post<Aluguel>(`${this.url}/${id}/cancelar`, {});
  }

  validarAcesso(codigo: string): Observable<ValidacaoAcesso> {
    return this.http.get<ValidacaoAcesso>(`${this.url}/acesso/${encodeURIComponent(codigo.trim())}`);
  }
}
