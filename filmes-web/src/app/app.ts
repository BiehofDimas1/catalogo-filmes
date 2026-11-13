import { Component } from '@angular/core';
import { FilmeLista } from './filme-lista/filme-lista';

@Component({
  selector: 'app-root',
  imports: [FilmeLista],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}