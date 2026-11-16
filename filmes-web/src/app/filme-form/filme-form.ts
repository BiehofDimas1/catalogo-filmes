import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { mensagemDeErro } from '../core/erro-http';
import { Filme, FilmeDados, FilmeService } from '../filme';

@Component({
  selector: 'app-filme-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './filme-form.html',
  styleUrl: './filme-form.css',
})
export class FilmeForm implements OnInit {
  private service = inject(FilmeService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  id: number | null = null;
  /** Versão do filme carregado. Volta para a API na edição, que recusa se o filme mudou desde então. */
  private versao: number | null = null;
  mensagem = signal('');
  erro = signal(false);
  enviando = signal(false);

  form = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    duracaoMinutos: [90, [Validators.required, Validators.min(1), Validators.max(600)]],
    licencasDisponiveis: [1, [Validators.required, Validators.min(0)]],
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.id = Number(id);
      this.service.buscar(this.id).subscribe({
        next: (filme) => {
          this.versao = filme.versao;
          this.form.patchValue(filme);
        },
        error: (e) => this.mostrar(mensagemDeErro(e, 'Não foi possível carregar o filme.'), true),
      });
    }
  }

  salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.mostrar('Corrija os campos destacados antes de salvar.', true);
      return;
    }

    const dados: FilmeDados = this.form.getRawValue();
    let chamada: Observable<Filme>;
    if (this.id === null) {
      chamada = this.service.cadastrar(dados);
    } else if (this.versao === null) {
      // Sem o filme carregado, salvar gravaria os valores padrão do formulário por cima dele.
      this.mostrar('O filme ainda não foi carregado. Recarregue a página antes de salvar.', true);
      return;
    } else {
      chamada = this.service.atualizar(this.id, { ...dados, versao: this.versao });
    }

    this.mensagem.set('');
    this.enviando.set(true);
    chamada.subscribe({
      next: () => {
        this.enviando.set(false);
        this.mostrar('Filme salvo com sucesso!', false);
        const espera = setTimeout(() => this.router.navigate(['/filmes']), 1200);
        this.destroyRef.onDestroy(() => clearTimeout(espera));
      },
      error: (e) => {
        this.enviando.set(false);
        this.mostrar(mensagemDeErro(e, 'Erro ao salvar o filme.'), true);
      },
    });
  }

  private mostrar(texto: string, ehErro: boolean) {
    this.mensagem.set(texto);
    this.erro.set(ehErro);
  }
}
