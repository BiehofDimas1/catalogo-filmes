import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Aluguel, AluguelService, ROTULO_STATUS, ValidacaoAcesso } from '../aluguel';
import { mensagemDeErro } from '../core/erro-http';
import { Aviso } from '../shared/aviso/aviso';
import { Mensagem } from '../shared/aviso/mensagem';

@Component({
  selector: 'app-alugueis-admin',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule, Aviso],
  templateUrl: './alugueis-admin.html',
  styleUrl: './alugueis-admin.css',
})
export class AlugueisAdmin implements OnInit {
  private service = inject(AluguelService);
  protected readonly rotulos = ROTULO_STATUS;

  alugueis = signal<Aluguel[]>([]);
  carregando = signal(true);
  mensagem = new Mensagem();

  validacao = signal<ValidacaoAcesso | null>(null);
  avisoValidacao = new Mensagem();
  validando = signal(false);

  form = new FormGroup({
    codigo: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.carregando.set(true);
    this.service.todos().subscribe({
      next: (lista) => {
        this.alugueis.set(lista);
        this.carregando.set(false);
      },
      error: (erro) => {
        this.carregando.set(false);
        this.mensagem.mostrarErro(mensagemDeErro(erro, 'Não foi possível carregar os aluguéis.'));
      },
    });
  }

  validar() {
    this.validacao.set(null);
    this.avisoValidacao.limpar();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.avisoValidacao.mostrarErro('Informe o código de acesso.');
      return;
    }

    this.validando.set(true);
    this.service.validarAcesso(this.form.controls.codigo.value).subscribe({
      next: (resultado) => {
        this.validando.set(false);
        this.validacao.set(resultado);
        this.carregar();
      },
      error: (erro) => {
        this.validando.set(false);
        this.avisoValidacao.mostrarErro(mensagemDeErro(erro, 'Não foi possível validar o código.'));
      },
    });
  }
}
