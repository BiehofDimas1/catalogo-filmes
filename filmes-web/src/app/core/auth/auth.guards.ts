import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService, Perfil } from './auth';

// Os guards só controlam a navegação. Quem autoriza de verdade é a API.

/** Exige login. Sem sessão, manda para o login guardando a página pedida. */
export const autenticadoGuard: CanActivateFn = (_rota, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estaLogado() ? true : router.createUrlTree(['/login'], { queryParams: { voltar: estado.url } });
};

/** Exige um perfil. Outro perfil volta para o catálogo. */
export function perfilGuard(perfil: Perfil): CanActivateFn {
  return (_rota, estado) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (!auth.estaLogado()) {
      return router.createUrlTree(['/login'], { queryParams: { voltar: estado.url } });
    }
    return auth.usuario()?.perfil === perfil ? true : router.createUrlTree(['/filmes']);
  };
}

/** Login e cadastro são só para quem ainda não entrou. */
export const visitanteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.estaLogado() ? router.createUrlTree(['/filmes']) : true;
};
