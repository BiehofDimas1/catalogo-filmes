import { Routes } from '@angular/router';
import { FilmeForm } from './filme-form/filme-form';
import { FilmeLista } from './filme-lista/filme-lista';

export const routes: Routes = [
  { path: '', component: FilmeLista },
  { path: 'filmes/novo', component: FilmeForm },
  { path: 'filmes/:id/editar', component: FilmeForm },
];