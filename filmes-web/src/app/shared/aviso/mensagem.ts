import { computed, signal } from '@angular/core';

interface Estado {
  texto: string;
  ehErro: boolean;
}

const VAZIO: Estado = { texto: '', ehErro: false };

export class Mensagem {
  private readonly estado = signal<Estado>(VAZIO);

  readonly texto = computed(() => this.estado().texto);
  readonly ehErro = computed(() => this.estado().ehErro);

  mostrar(texto: string) {
    this.estado.set({ texto, ehErro: false });
  }

  mostrarErro(texto: string) {
    this.estado.set({ texto, ehErro: true });
  }

  limpar() {
    this.estado.set(VAZIO);
  }
}
