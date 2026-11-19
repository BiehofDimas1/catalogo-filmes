import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { mensagemDeErro } from '../core/erro-http';
import { Filme, FilmeService } from '../filme';
import { Aviso } from '../shared/aviso/aviso';
import { Mensagem } from '../shared/aviso/mensagem';

@Component({
  selector: 'app-gestao-filmes',
  imports: [RouterLink, Aviso],
  templateUrl: './gestao-filmes.html',
  styleUrl: './gestao-filmes.css',
})
export class GestaoFilmes implements OnInit {
  private filmeService = inject(FilmeService);

  filmes = signal<Filme[]>([]);
  carregando = signal(true);
  emAndamento = signal<number | null>(null);
  mensagem = new Mensagem();

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.carregando.set(true);
    this.filmeService.listar().subscribe({
      next: (lista) => {
        this.filmes.set(lista);
        this.carregando.set(false);
      },
      error: (erro) => {
        this.carregando.set(false);
        this.mensagem.mostrarErro(mensagemDeErro(erro, 'Não foi possível carregar os filmes.'));
      },
    });
  }

  inativar(filme: Filme) {
    if (!this.confirmarInativacao(filme)) {
      return;
    }
    this.emAndamento.set(filme.id);
    this.filmeService.inativar(filme.id).subscribe({
      next: () => this.concluirComSucesso(`Filme "${filme.titulo}" inativado com sucesso.`),
      error: (erro) => this.concluirComErro(mensagemDeErro(erro, 'Erro ao inativar o filme.')),
    });
  }

  reativar(filme: Filme) {
    this.emAndamento.set(filme.id);
    this.filmeService.reativar(filme.id).subscribe({
      next: () => this.concluirComSucesso(`Filme "${filme.titulo}" reativado com sucesso.`),
      error: (erro) => this.concluirComErro(mensagemDeErro(erro, 'Erro ao reativar o filme.')),
    });
  }

  private confirmarInativacao(filme: Filme): boolean {
    return confirm(
      `Inativar "${filme.titulo}"? Ele sai do catálogo dos clientes, ` +
        'mas os aluguéis ativos continuam valendo.',
    );
  }

  private concluirComSucesso(texto: string) {
    this.emAndamento.set(null);
    this.mensagem.mostrar(texto);
    this.carregar();
  }

  private concluirComErro(texto: string) {
    this.emAndamento.set(null);
    this.mensagem.mostrarErro(texto);
  }
}
