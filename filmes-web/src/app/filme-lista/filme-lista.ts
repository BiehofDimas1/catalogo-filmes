import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Filme, FilmeService } from '../filme';

@Component({
  selector: 'app-filme-lista',
  imports: [RouterLink],
  templateUrl: './filme-lista.html',
  styleUrl: './filme-lista.css',
})
export class FilmeLista implements OnInit {
  private service = inject(FilmeService);

  filmes = signal<Filme[]>([]);
  mensagem = signal('');
  erro = signal(false);

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.service.listar().subscribe({
      next: (lista) => this.filmes.set(lista),
      error: () => this.mostrar('Não foi possível carregar os filmes. O backend está rodando?', true),
    });
  }

  inativar(filme: Filme) {
    this.service.inativar(filme.id!).subscribe({
      next: () => {
        this.mostrar(`Filme "${filme.titulo}" inativado com sucesso.`, false);
        this.carregar();
      },
      error: () => this.mostrar('Erro ao inativar o filme.', true),
    });
  }

  private mostrar(texto: string, ehErro: boolean) {
    this.mensagem.set(texto);
    this.erro.set(ehErro);
  }
}