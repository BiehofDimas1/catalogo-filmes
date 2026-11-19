import { CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AluguelService, RegrasAluguel } from '../aluguel';
import { SessaoService } from '../core/auth/sessao';
import { mensagemDeErro } from '../core/erro-http';
import { Filme, FilmeService } from '../filme';
import { Aviso } from '../shared/aviso/aviso';
import { Mensagem } from '../shared/aviso/mensagem';

@Component({
  selector: 'app-catalogo-cliente',
  imports: [CurrencyPipe, Aviso],
  templateUrl: './catalogo-cliente.html',
  styleUrl: './catalogo-cliente.css',
})
export class CatalogoCliente implements OnInit {
  private filmeService = inject(FilmeService);
  private aluguelService = inject(AluguelService);
  private route = inject(ActivatedRoute);
  private sessao = inject(SessaoService);

  filmes = signal<Filme[]>([]);
  regras = signal<RegrasAluguel | null>(null);
  carregando = signal(true);
  emAndamento = signal<number | null>(null);
  mensagem = new Mensagem();

  ngOnInit() {
    if (this.route.snapshot.queryParamMap.get('bemvindo')) {
      this.darBoasVindas();
    }
    this.carregar();
    this.carregarRegras();
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

  alugar(filme: Filme) {
    this.emAndamento.set(filme.id);
    this.aluguelService.alugar(filme.id).subscribe({
      next: (aluguel) => {
        this.emAndamento.set(null);
        this.mensagem.mostrar(
          `"${aluguel.filmeTitulo}" alugado com sucesso! ` +
            `Código de acesso: ${aluguel.codigoAcesso}. Acompanhe em "Meus aluguéis".`,
        );
        this.carregar();
      },
      error: (erro) => {
        this.emAndamento.set(null);
        this.mensagem.mostrarErro(mensagemDeErro(erro, 'Não foi possível alugar o filme.'));
      },
    });
  }

  private darBoasVindas() {
    const nome = this.sessao.usuario()?.nome ?? '';
    this.mensagem.mostrar(`Conta criada com sucesso. Bem-vindo(a), ${nome}!`);
  }

  private carregarRegras() {
    this.aluguelService.regras().subscribe({
      next: (regras) => this.regras.set(regras),
      error: () => this.regras.set(null),
    });
  }
}
