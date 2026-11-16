import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Aluguel, AluguelService, ROTULO_STATUS } from '../aluguel';
import { mensagemDeErro } from '../core/erro-http';

/** Histórico do cliente: aluguéis ativos, expirados e cancelados, com cancelamento dentro do prazo. */
@Component({
  selector: 'app-meus-alugueis',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './meus-alugueis.html',
  styleUrl: './meus-alugueis.css',
})
export class MeusAlugueis implements OnInit {
  private service = inject(AluguelService);
  protected readonly rotulos = ROTULO_STATUS;

  alugueis = signal<Aluguel[]>([]);
  carregando = signal(true);
  emAndamento = signal<number | null>(null);
  mensagem = signal('');
  erro = signal(false);

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.carregando.set(true);
    this.service.meus().subscribe({
      next: (lista) => {
        this.alugueis.set(lista);
        this.carregando.set(false);
      },
      error: (e) => {
        this.carregando.set(false);
        this.mostrar(mensagemDeErro(e, 'Não foi possível carregar os seus aluguéis.'), true);
      },
    });
  }

  cancelar(aluguel: Aluguel) {
    const confirmado = confirm(
      `Cancelar o aluguel de "${aluguel.filmeTitulo}"? O valor será reembolsado (simulação).`,
    );
    if (!confirmado) {
      return;
    }
    this.emAndamento.set(aluguel.id);
    this.service.cancelar(aluguel.id).subscribe({
      next: (cancelado) => {
        this.emAndamento.set(null);
        this.mostrar(
          `Aluguel de "${cancelado.filmeTitulo}" cancelado. Reembolso simulado: ${cancelado.transacaoReembolso}.`,
          false,
        );
        this.carregar();
      },
      error: (e) => {
        this.emAndamento.set(null);
        this.mostrar(mensagemDeErro(e, 'Não foi possível cancelar o aluguel.'), true);
        this.carregar();
      },
    });
  }

  private mostrar(texto: string, ehErro: boolean) {
    this.mensagem.set(texto);
    this.erro.set(ehErro);
  }
}
