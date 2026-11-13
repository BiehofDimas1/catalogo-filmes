import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Filme, FilmeService } from '../filme';

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

  id: number | null = null;
  mensagem = signal('');
  erro = signal(false);

  form = this.fb.group({
    titulo: ['', Validators.required],
    duracaoMinutos: [90, [Validators.required, Validators.min(1), Validators.max(600)]],
    licencasDisponiveis: [1, [Validators.required, Validators.min(0)]],
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.id = Number(id);
      this.service.buscar(this.id).subscribe({
        next: (filme) => this.form.patchValue(filme),
        error: () => this.mostrar('Não foi possível carregar o filme.', true),
      });
    }
  }

  salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.mostrar('Corrija os campos destacados antes de salvar.', true);
      return;
    }

    const dados = this.form.getRawValue() as Filme;
    const chamada =
      this.id === null
        ? this.service.cadastrar(dados)
        : this.service.atualizar(this.id, dados);

    chamada.subscribe({
      next: () => {
        this.mostrar('Filme salvo com sucesso!', false);
        setTimeout(() => this.router.navigate(['/']), 1200);
      },
      error: (e) =>
        this.mostrar(
          e.status === 400
            ? 'Dados inválidos: a duração deve ser de 1 a 600 minutos e o título é obrigatório.'
            : 'Erro ao salvar o filme. O backend está rodando?',
          true,
        ),
    });
  }

  private mostrar(texto: string, ehErro: boolean) {
    this.mensagem.set(texto);
    this.erro.set(ehErro);
  }
}