import { Component, computed, input } from '@angular/core';
import { Mensagem } from './mensagem';

@Component({
  selector: 'app-aviso',
  templateUrl: './aviso.html',
  styleUrl: './aviso.css',
})
export class Aviso {
  readonly mensagem = input.required<Mensagem>();

  protected readonly papel = computed(() => (this.mensagem().ehErro() ? 'alert' : 'status'));
}
