import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AluguelService, RegrasAluguel } from '../aluguel';
import { AuthService } from '../core/auth/auth';
import { mensagemDeErro } from '../core/erro-http';
import { Filme, FilmeService } from '../filme';

@Component({
  selector: 'app-filme-lista',
  imports: [RouterLink, CurrencyPipe],
  templateUrl: './filme-lista.html',
  styleUrl: './filme-lista.css',
})
export class FilmeLista implements OnInit {
  private filmeService = inject(FilmeService);
  private aluguelService = inject(AluguelService);
  private route = inject(ActivatedRoute);
  protected auth = inject(AuthService);

  filmes = signal<Filme[]>([]);
  regras = signal<RegrasAluguel | null>(null);
  carregando = signal(true);
  /** Filme com operação em andamento, para desabilitar o botão e evitar clique duplo. */
  emAndamento = signal<number | null>(null);
  mensagem = signal('');
  erro = signal(false);

  ngOnInit() {
    if (this.route.snapshot.queryParamMap.get('bemvindo')) {
      this.mostrar(`Conta criada com sucesso. Bem-vindo(a), ${this.auth.usuario()?.nome ?? ''}!`, false);
    }
    this.carregar();
    if (this.auth.ehCliente()) {
      this.aluguelService.regras().subscribe({
        next: (regras) => this.regras.set(regras),
        error: () => this.regras.set(null),
      });
    }
  }

  carregar() {
    this.carregando.set(true);
    this.filmeService.listar().subscribe({
      next: (lista) => {
        this.filmes.set(lista);
        this.carregando.set(false);
      },
      error: (e) => {
        this.carregando.set(false);
        this.mostrar(mensagemDeErro(e, 'Não foi possível carregar os filmes.'), true);
      },
    });
  }

  alugar(filme: Filme) {
    this.emAndamento.set(filme.id);
    this.aluguelService.alugar(filme.id).subscribe({
      next: (aluguel) => {
        this.emAndamento.set(null);
        this.mostrar(
          `"${aluguel.filmeTitulo}" alugado com sucesso! Código de acesso: ${aluguel.codigoAcesso}. ` +
            'Acompanhe em "Meus aluguéis".',
          false,
        );
        this.carregar();
      },
      error: (e) => {
        this.emAndamento.set(null);
        this.mostrar(mensagemDeErro(e, 'Não foi possível alugar o filme.'), true);
      },
    });
  }

  inativar(filme: Filme) {
    const confirmado = confirm(
      `Inativar "${filme.titulo}"? Ele sai do catálogo dos clientes, mas os aluguéis ativos continuam valendo.`,
    );
    if (!confirmado) {
      return;
    }
    this.emAndamento.set(filme.id);
    this.filmeService.inativar(filme.id).subscribe({
      next: () => {
        this.emAndamento.set(null);
        this.mostrar(`Filme "${filme.titulo}" inativado com sucesso.`, false);
        this.carregar();
      },
      error: (e) => {
        this.emAndamento.set(null);
        this.mostrar(mensagemDeErro(e, 'Erro ao inativar o filme.'), true);
      },
    });
  }

  reativar(filme: Filme) {
    this.emAndamento.set(filme.id);
    this.filmeService.reativar(filme.id).subscribe({
      next: () => {
        this.emAndamento.set(null);
        this.mostrar(`Filme "${filme.titulo}" reativado com sucesso.`, false);
        this.carregar();
      },
      error: (e) => {
        this.emAndamento.set(null);
        this.mostrar(mensagemDeErro(e, 'Erro ao reativar o filme.'), true);
      },
    });
  }

  private mostrar(texto: string, ehErro: boolean) {
    this.mensagem.set(texto);
    this.erro.set(ehErro);
  }
}
