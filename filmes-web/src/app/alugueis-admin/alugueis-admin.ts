import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Aluguel, AluguelService, ROTULO_STATUS, ValidacaoAcesso } from '../aluguel';
import { mensagemDeErro } from '../core/erro-http';

/** Tela do ADMIN: todos os aluguéis e validação de códigos de acesso. */
@Component({
  selector: 'app-alugueis-admin',
  imports: [CurrencyPipe, DatePipe, ReactiveFormsModule],
  templateUrl: './alugueis-admin.html',
  styleUrl: './alugueis-admin.css',
})
export class AlugueisAdmin implements OnInit {
  private service = inject(AluguelService);
  protected readonly rotulos = ROTULO_STATUS;

  alugueis = signal<Aluguel[]>([]);
  carregando = signal(true);
  mensagem = signal('');

  validacao = signal<ValidacaoAcesso | null>(null);
  erroValidacao = signal('');
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
      error: (e) => {
        this.carregando.set(false);
        this.mensagem.set(mensagemDeErro(e, 'Não foi possível carregar os aluguéis.'));
      },
    });
  }

  /** Confere o código sem consumi-lo: o mesmo código pode ser validado várias vezes enquanto vale. */
  validar() {
    this.validacao.set(null);
    this.erroValidacao.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erroValidacao.set('Informe o código de acesso.');
      return;
    }

    this.validando.set(true);
    this.service.validarAcesso(this.form.controls.codigo.value).subscribe({
      next: (resultado) => {
        this.validando.set(false);
        this.validacao.set(resultado);
        // Recarrega a lista para ela mostrar o mesmo status da validação.
        this.carregar();
      },
      error: (e) => {
        this.validando.set(false);
        this.erroValidacao.set(mensagemDeErro(e, 'Não foi possível validar o código.'));
      },
    });
  }
}
