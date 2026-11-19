import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessaoService } from './core/auth/sessao';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected sessao = inject(SessaoService);
  private router = inject(Router);

  sair() {
    this.sessao.encerrar();
    void this.router.navigate(['/login']);
  }
}
