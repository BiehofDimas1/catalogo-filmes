import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Aviso } from './aviso';
import { Mensagem } from './mensagem';

@Component({
  imports: [Aviso],
  template: '<app-aviso [mensagem]="mensagem" />',
})
class TelaComAviso {
  mensagem = new Mensagem();
}

describe('Aviso', () => {
  let fixture: ComponentFixture<TelaComAviso>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [TelaComAviso] }).compileComponents();

    fixture = TestBed.createComponent(TelaComAviso);
    fixture.detectChanges();
  });

  function paragrafo(): HTMLElement | null {
    return (fixture.nativeElement as HTMLElement).querySelector('p');
  }

  it('não mostra nada enquanto não há mensagem', () => {
    expect(paragrafo()).toBeNull();
  });

  it('mostra sucesso como status, erro como alerta e some ao limpar', async () => {
    const mensagem = fixture.componentInstance.mensagem;

    mensagem.mostrar('Filme salvo com sucesso!');
    await fixture.whenStable();
    expect(paragrafo()?.textContent).toContain('Filme salvo com sucesso!');
    expect(paragrafo()?.getAttribute('role')).toBe('status');
    expect(paragrafo()?.classList.contains('erro')).toBe(false);

    mensagem.mostrarErro('Erro ao salvar o filme.');
    await fixture.whenStable();
    expect(paragrafo()?.textContent).toContain('Erro ao salvar o filme.');
    expect(paragrafo()?.getAttribute('role')).toBe('alert');
    expect(paragrafo()?.classList.contains('erro')).toBe(true);

    mensagem.limpar();
    await fixture.whenStable();
    expect(paragrafo()).toBeNull();
  });
});
