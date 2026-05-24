import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { WorkspaceInfo } from '../../core/models/models';

@Component({
  selector: 'app-workspace-selector',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './workspace-selector.component.html',
  styleUrl: './workspace-selector.component.scss'
})
export class WorkspaceSelectorComponent implements OnInit {

  workspaces   = signal<WorkspaceInfo[]>([]);
  selected     = signal<WorkspaceInfo | null>(null);
  loading      = signal(false);
  errorMsg     = signal('');

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Recuperar workspaces (del signal o del sessionStorage tras recarga)
    const ws = this.authService.workspaces().length
      ? this.authService.workspaces()
      : this.authService.loadWorkspacesFromSession();

    if (!ws.length) {
      // No hay datos de login previo, volver al login
      this.router.navigate(['/login']);
      return;
    }

    this.workspaces.set(ws);
  }

  selectWorkspace(ws: WorkspaceInfo): void {
    this.selected.set(ws);
  }

  isSelected(ws: WorkspaceInfo): boolean {
    return this.selected()?.id === ws.id;
  }

  confirm(): void {
    const ws = this.selected();
    if (!ws) return;

    this.loading.set(true);
    this.errorMsg.set('');

    this.authService.selectWorkspace(ws.id).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 401) {
          this.errorMsg.set('Sesión expirada. Por favor vuelve a iniciar sesión.');
          setTimeout(() => this.router.navigate(['/login']), 2000);
        } else {
          this.errorMsg.set('No se pudo seleccionar el workspace. Intenta nuevamente.');
        }
      }
    });
  }

  goBack(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  roleBadgeClass(role: string): string {
    return `badge badge--${role.toLowerCase()}`;
  }

  roleLabel(role: string): string {
    const labels: Record<string, string> = {
      ADMIN: 'Administrador',
      EDITOR: 'Editor',
      LECTOR: 'Lector'
    };
    return labels[role] ?? role;
  }

  workspaceInitial(name: string): string {
    return name.charAt(0).toUpperCase();
  }
}
