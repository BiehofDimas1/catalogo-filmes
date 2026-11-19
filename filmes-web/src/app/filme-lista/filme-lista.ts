import { Component, inject } from '@angular/core';
import { CatalogoCliente } from '../catalogo-cliente/catalogo-cliente';
import { SessaoService } from '../core/auth/sessao';
import { GestaoFilmes } from '../gestao-filmes/gestao-filmes';

@Component({
  selector: 'app-filme-lista',
  imports: [CatalogoCliente, GestaoFilmes],
  templateUrl: './filme-lista.html',
})
export class FilmeLista {
  protected sessao = inject(SessaoService);
}
