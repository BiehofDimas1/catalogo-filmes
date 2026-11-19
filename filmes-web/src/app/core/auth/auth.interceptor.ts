import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../api';
import { SessaoService } from './sessao';

const ROTAS_PUBLICAS = [`${API_URL}/auth/login`, `${API_URL}/auth/cadastro`];

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const sessao = inject(SessaoService);
  const router = inject(Router);
  const privada = req.url.startsWith(`${API_URL}/`) && !ROTAS_PUBLICAS.includes(req.url);
  const token = privada ? sessao.token() : null;
  const requisicao = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(requisicao).pipe(
    catchError((erro: unknown) => {
      if (token && erro instanceof HttpErrorResponse && erro.status === 401) {
        sessao.encerrar();
        void router.navigate(['/login'], { queryParams: { sessao: 'expirada' } });
      }
      return throwError(() => erro);
    }),
  );
};
