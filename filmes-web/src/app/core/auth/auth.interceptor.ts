import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_URL } from '../api';
import { AuthService } from './auth';

const ROTAS_PUBLICAS = [`${API_URL}/auth/login`, `${API_URL}/auth/cadastro`];

/**
 * Envia o token JWT só para a API, nunca para login e cadastro.
 * Se a API responder 401 a uma chamada autenticada, encerra a sessão e volta ao login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const privada = req.url.startsWith(`${API_URL}/`) && !ROTAS_PUBLICAS.includes(req.url);
  const token = privada ? auth.token() : null;
  const requisicao = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(requisicao).pipe(
    catchError((erro: unknown) => {
      if (token && erro instanceof HttpErrorResponse && erro.status === 401) {
        auth.sair();
        void router.navigate(['/login'], { queryParams: { sessao: 'expirada' } });
      }
      return throwError(() => erro);
    }),
  );
};
