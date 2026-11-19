import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { mensagemDeErro } from '../core/erro-http';
import { Filme, FilmeDados, FilmeService } from '../filme';
import { Aviso } from '../shared/aviso/aviso';
import { Mensagem } from '../shared/aviso/mensagem';

const ESPERA_ANTES_DE_VOLTAR_MS = 1200;

@Component({
  selector: 'app-filme-form',
  imports: [ReactiveFormsModule, RouterLink, Aviso],
  templateUrl: './filme-form.html',
  styleUrl: './filme-form.css',
})
export class FilmeForm implements OnInit {
  private service = inject(FilmeService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private formBuilder = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);

  id: number | null = null;
  private versao: number | null = null;
  mensagem = new Mensagem();
  enviando = signal(false);

  form = this.formBuilder.nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    duracaoMinutos: [90, [Validators.required, Validators.min(1), Validators.max(600)]],
    licencasDisponiveis: [1, [Validators.required, Validators.min(0)]],
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.carregarFilme(Number(id));
    }
  }

  salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.mensagem.mostrarErro('Corrija os campos destacados antes de salvar.');
      return;
    }

    const dados: FilmeDados = this.form.getRawValue();
    let chamada: Observable<Filme>;
    if (this.id === null) {
      chamada = this.service.cadastrar(dados);
    } else if (this.versao === null) {
      this.mensagem.mostrarErro(
        'O filme ainda não foi carregado. Recarregue a página antes de salvar.',
      );
      return;
    } else {
      chamada = this.service.atualizar(this.id, { ...dados, versao: this.versao });
    }

    this.mensagem.limpar();
    this.enviando.set(true);
    chamada.subscribe({
      next: () => {
        this.enviando.set(false);
        this.mensagem.mostrar('Filme salvo com sucesso!');
        this.voltarParaALista();
      },
      error: (erro) => {
        this.enviando.set(false);
        this.mensagem.mostrarErro(mensagemDeErro(erro, 'Erro ao salvar o filme.'));
      },
    });
  }

  private carregarFilme(id: number) {
    this.id = id;
    this.service.buscar(id).subscribe({
      next: (filme) => {
        this.versao = filme.versao;
        this.form.patchValue(filme);
      },
      error: (erro) =>
        this.mensagem.mostrarErro(mensagemDeErro(erro, 'Não foi possível carregar o filme.')),
    });
  }

  private voltarParaALista() {
    const espera = setTimeout(() => this.router.navigate(['/filmes']), ESPERA_ANTES_DE_VOLTAR_MS);
    this.destroyRef.onDestroy(() => clearTimeout(espera));
  }
}
