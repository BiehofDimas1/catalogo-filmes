import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Aluguel, AluguelService, ROTULO_STATUS } from '../aluguel';
import { mensagemDeErro } from '../core/erro-http';
import { Aviso } from '../shared/aviso/aviso';
import { Mensagem } from '../shared/aviso/mensagem';

@Component({
  selector: 'app-meus-alugueis',
  imports: [CurrencyPipe, DatePipe, RouterLink, Aviso],
  templateUrl: './meus-alugueis.html',
  styleUrl: './meus-alugueis.css',
})
export class MeusAlugueis implements OnInit {
  private service = inject(AluguelService);
  protected readonly rotulos = ROTULO_STATUS;

  alugueis = signal<Aluguel[]>([]);
  carregando = signal(true);
  emAndamento = signal<number | null>(null);
  mensagem = new Mensagem();

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
      error: (erro) => {
        this.carregando.set(false);
        this.mensagem.mostrarErro(
          mensagemDeErro(erro, 'Não foi possível carregar os seus aluguéis.'),
        );
      },
    });
  }

  cancelar(aluguel: Aluguel) {
    if (!this.confirmarCancelamento(aluguel)) {
      return;
    }
    this.emAndamento.set(aluguel.id);
    this.service.cancelar(aluguel.id).subscribe({
      next: (cancelado) => {
        this.emAndamento.set(null);
        this.mensagem.mostrar(
          `Aluguel de "${cancelado.filmeTitulo}" cancelado. ` +
            `Reembolso simulado: ${cancelado.transacaoReembolso}.`,
        );
        this.carregar();
      },
      error: (erro) => {
        this.emAndamento.set(null);
        this.mensagem.mostrarErro(mensagemDeErro(erro, 'Não foi possível cancelar o aluguel.'));
        this.carregar();
      },
    });
  }

  private confirmarCancelamento(aluguel: Aluguel): boolean {
    return confirm(
      `Cancelar o aluguel de "${aluguel.filmeTitulo}"? O valor será reembolsado (simulação).`,
    );
  }
}
