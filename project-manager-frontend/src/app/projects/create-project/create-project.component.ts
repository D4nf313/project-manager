import { Component, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProjectService } from '../project.service';
import { Project } from '../../core/models/models';

@Component({
  selector: 'app-create-project',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-project.component.html',
  styleUrl: './create-project.component.scss'
})
export class CreateProjectComponent {

  @Output() created   = new EventEmitter<Project>();
  @Output() cancelled = new EventEmitter<void>();

  form: FormGroup;
  loading  = signal(false);
  errorMsg = signal('');

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService
  ) {
    this.form = this.fb.group({
      name:        ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(500)]]
    });
  }

  get name()        { return this.form.get('name')!; }
  get description() { return this.form.get('description')!; }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMsg.set('');

    this.projectService.createProject(this.form.value).subscribe({
      next: (project) => {
        this.loading.set(false);
        this.created.emit(project);
      },
      error: (err) => {
        this.loading.set(false);
        if (err.status === 403) {
          this.errorMsg.set('No tienes permisos para crear proyectos en este workspace.');
        } else if (err.status === 400) {
          this.errorMsg.set('Datos inválidos. Revisa los campos e intenta nuevamente.');
        } else {
          this.errorMsg.set('No se pudo crear el proyecto. Intenta nuevamente.');
        }
      }
    });
  }

  cancel(): void {
    this.cancelled.emit();
  }

  onOverlayClick(event: MouseEvent): void {
    // Cerrar solo si se hace clic en el overlay, no en el modal
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.cancel();
    }
  }

  get nameLength(): number {
    return this.name.value?.length ?? 0;
  }

  get descLength(): number {
    return this.description.value?.length ?? 0;
  }
}
