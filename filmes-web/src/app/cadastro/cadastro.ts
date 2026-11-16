import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { AuthService } from '../core/auth/auth';
import { mensagemDeErro } from '../core/erro-http';

@Component({
  selector: 'app-cadastro',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro.html',
  styleUrl: './cadastro.css',
})
export class Cadastro {
  private auth = inject(AuthService);
  private router = inject(Router);

  mensagem = signal('');
  erro = signal('');
  enviando = signal(false);

  form = new FormGroup({
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.maxLength(100)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6), Validators.maxLength(72)],
    }),
  });

  /** Cria a conta (sempre como CLIENTE), entra automaticamente e abre o catálogo. */
  cadastrar() {
    this.mensagem.set('');
    this.erro.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erro.set('Corrija os campos destacados antes de cadastrar.');
      return;
    }

    const dados = this.form.getRawValue();
    this.enviando.set(true);
    this.auth
      .cadastrar(dados)
      .pipe(switchMap(() => this.auth.entrar({ email: dados.email, senha: dados.senha })))
      .subscribe({
        next: () => {
          this.enviando.set(false);
          void this.router.navigate(['/filmes'], { queryParams: { bemvindo: 1 } });
        },
        error: (e) => {
          this.enviando.set(false);
          this.erro.set(mensagemDeErro(e, 'Não foi possível criar a conta.'));
        },
      });
  }
}
