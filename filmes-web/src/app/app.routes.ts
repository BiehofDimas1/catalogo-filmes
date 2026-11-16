import { Routes } from '@angular/router';
import { AlugueisAdmin } from './alugueis-admin/alugueis-admin';
import { Cadastro } from './cadastro/cadastro';
import { autenticadoGuard, perfilGuard, visitanteGuard } from './core/auth/auth.guards';
import { FilmeForm } from './filme-form/filme-form';
import { FilmeLista } from './filme-lista/filme-lista';
import { Login } from './login/login';
import { MeusAlugueis } from './meus-alugueis/meus-alugueis';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'filmes' },
  { path: 'login', component: Login, canActivate: [visitanteGuard], title: 'Entrar | Catálogo de Filmes' },
  { path: 'cadastro', component: Cadastro, canActivate: [visitanteGuard], title: 'Criar conta | Catálogo de Filmes' },
  { path: 'filmes', component: FilmeLista, canActivate: [autenticadoGuard], title: 'Catálogo | Catálogo de Filmes' },
  {
    path: 'filmes/novo',
    component: FilmeForm,
    canActivate: [perfilGuard('ADMIN')],
    title: 'Novo filme | Catálogo de Filmes',
  },
  {
    path: 'filmes/:id/editar',
    component: FilmeForm,
    canActivate: [perfilGuard('ADMIN')],
    title: 'Editar filme | Catálogo de Filmes',
  },
  {
    path: 'meus-alugueis',
    component: MeusAlugueis,
    canActivate: [perfilGuard('CLIENTE')],
    title: 'Meus aluguéis | Catálogo de Filmes',
  },
  {
    path: 'admin/alugueis',
    component: AlugueisAdmin,
    canActivate: [perfilGuard('ADMIN')],
    title: 'Aluguéis | Catálogo de Filmes',
  },
  { path: '**', redirectTo: 'filmes' },
];
