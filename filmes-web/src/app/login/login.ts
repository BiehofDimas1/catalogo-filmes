import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth/auth';
import { mensagemDeErro } from '../core/erro-http';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  mensagem = signal('');
  erro = signal('');
  enviando = signal(false);

  form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    senha: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(6)] }),
  });

  constructor() {
    const parametros = this.route.snapshot.queryParamMap;
    if (parametros.get('sessao') === 'expirada') {
      this.erro.set('Sua sessão expirou. Entre novamente.');
    } else if (parametros.get('voltar')) {
      this.mensagem.set('Entre para continuar.');
    }
  }

  entrar() {
    this.mensagem.set('');
    this.erro.set('');
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.erro.set('Corrija os campos destacados antes de entrar.');
      return;
    }

    this.enviando.set(true);
    this.auth.entrar(this.form.getRawValue()).subscribe({
      next: () => {
        this.enviando.set(false);
        void this.router.navigateByUrl(destinoSeguro(this.route.snapshot.queryParamMap.get('voltar')));
      },
      error: (e) => {
        this.enviando.set(false);
        this.erro.set(mensagemDeErro(e, 'Não foi possível entrar.'));
      },
    });
  }
}

/** Depois do login, só volta para caminhos internos do app. */
function destinoSeguro(destino: string | null): string {
  return destino && destino.startsWith('/') && !destino.startsWith('//') ? destino : '/filmes';
}
