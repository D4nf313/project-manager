import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { ProjectService } from '../project.service';
import { Project, canCreateProjects } from '../../core/models/models';
import { CreateProjectComponent } from '../create-project/create-project.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, CreateProjectComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  projects = signal<Project[]>([]);
  loading = signal(true);
  errorMsg = signal('');
  showModal = signal(false);

  // Datos de sesión expuestos al template
session       = computed(() => this.authService.session());
workspaceName = computed(() => this.authService.session()?.workspaceName ?? '');
userName      = computed(() => this.authService.session()?.userName ?? '');

  canCreate = computed(() => {
    const role = this.authService.role();
    return role ? canCreateProjects(role) : false;
  });

  constructor(
    private authService: AuthService,
    private projectService: ProjectService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading.set(true);
    this.errorMsg.set('');

    this.projectService.getProjects().subscribe({
      next: (data) => {
        this.projects.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 401 || err.status === 403) {
          this.errorMsg.set(
            'Tu sesión expiró. Por favor inicia sesión nuevamente.',
          );
          setTimeout(() => this.logout(), 2000);
        } else {
          this.errorMsg.set(
            'No se pudo cargar los proyectos. Intenta nuevamente.',
          );
        }
      },
    });
  }

  openCreateModal(): void {
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
  }

  onProjectCreated(project: Project): void {
    this.projects.update((list) => [project, ...list]);
    this.closeModal();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

changeWorkspace(): void {
  this.authService.clearSession();
  this.router.navigate(['/workspaces']);
}

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  roleBadgeClass(role: string): string {
    return `role-badge role-badge--${role?.toLowerCase()}`;
  }

initials = computed(() => {
  const name = this.authService.session()?.userName ?? '';
  return name.split(' ').map((n: string) => n[0]).slice(0, 2).join('').toUpperCase();
});
}
