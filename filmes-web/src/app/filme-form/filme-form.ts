import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { mensagemDeErro } from '../core/erro-http';
import { FilmeDados, FilmeService } from '../filme';

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
        next: (filme) => this.form.patchValue(filme),
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
    const chamada =
      this.id === null ? this.service.cadastrar(dados) : this.service.atualizar(this.id, dados);

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
