import { Routes } from '@angular/router';
import { Cadastro } from './cadastro/cadastro';
import { FilmeForm } from './filme-form/filme-form';
import { FilmeLista } from './filme-lista/filme-lista';
import { Login } from './login/login';

export const routes: Routes = [
  { path: '', component: FilmeLista },
  { path: 'filmes/novo', component: FilmeForm },
  { path: 'filmes/:id/editar', component: FilmeForm },
  { path: 'login', component: Login },
  { path: 'cadastro', component: Cadastro },
];