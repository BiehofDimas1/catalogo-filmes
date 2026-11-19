import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth/auth';
import { mensagemDeErro } from '../core/erro-http';
import { Aviso } from '../shared/aviso/aviso';
import { Mensagem } from '../shared/aviso/mensagem';

const DESTINO_PADRAO = '/filmes';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, Aviso],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private auth = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  mensagem = new Mensagem();
  enviando = signal(false);

  form = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email],
    }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6)],
    }),
  });

  constructor() {
    const parametros = this.route.snapshot.queryParamMap;
    if (parametros.get('sessao') === 'expirada') {
      this.mensagem.mostrarErro('Sua sessão expirou. Entre novamente.');
    } else if (parametros.get('voltar')) {
      this.mensagem.mostrar('Entre para continuar.');
    }
  }

  entrar() {
    this.mensagem.limpar();
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.mensagem.mostrarErro('Corrija os campos destacados antes de entrar.');
      return;
    }

    this.enviando.set(true);
    this.auth.entrar(this.form.getRawValue()).subscribe({
      next: () => {
        this.enviando.set(false);
        void this.router.navigateByUrl(this.destinoDepoisDoLogin());
      },
      error: (erro) => {
        this.enviando.set(false);
        this.mensagem.mostrarErro(mensagemDeErro(erro, 'Não foi possível entrar.'));
      },
    });
  }

  private destinoDepoisDoLogin(): string {
    const destino = this.route.snapshot.queryParamMap.get('voltar');
    return ehRotaInterna(destino) ? destino : DESTINO_PADRAO;
  }
}

function ehRotaInterna(destino: string | null): destino is string {
  return !!destino && destino.startsWith('/') && !destino.startsWith('//');
}
