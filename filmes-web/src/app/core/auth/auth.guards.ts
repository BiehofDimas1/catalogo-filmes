import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Perfil, SessaoService } from './sessao';

export const autenticadoGuard: CanActivateFn = (_rota, estado) => {
  const sessao = inject(SessaoService);
  const router = inject(Router);
  return sessao.estaLogado()
    ? true
    : router.createUrlTree(['/login'], { queryParams: { voltar: estado.url } });
};

export function perfilGuard(perfil: Perfil): CanActivateFn {
  return (_rota, estado) => {
    const sessao = inject(SessaoService);
    const router = inject(Router);
    if (!sessao.estaLogado()) {
      return router.createUrlTree(['/login'], { queryParams: { voltar: estado.url } });
    }
    return sessao.usuario()?.perfil === perfil ? true : router.createUrlTree(['/filmes']);
  };
}

export const visitanteGuard: CanActivateFn = () => {
  const sessao = inject(SessaoService);
  const router = inject(Router);
  return sessao.estaLogado() ? router.createUrlTree(['/filmes']) : true;
};
