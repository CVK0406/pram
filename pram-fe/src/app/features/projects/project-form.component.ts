import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProjectService } from '../../core/services/project.service';
import { ProjectRequest } from '../../core/models/project.model';

@Component({
  selector: 'app-project-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './project-form.component.html',
  styleUrl: './project-form.component.scss',
})
export class ProjectFormComponent {
  private fb = inject(FormBuilder);
  private projectService = inject(ProjectService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    projectCode: ['', [Validators.required, Validators.maxLength(20)]],
    projectName: ['', [Validators.required, Validators.maxLength(200)]],
    customer: ['', [Validators.required, Validators.maxLength(100)]],
    startDate: ['', Validators.required],
    endDate: ['', Validators.required],
    status: ['PLANNING' as string],
  }, { validators: this.dateRangeValidator });

  submitting = false;
  serverErrors: Record<string, string> = {};

  get f() {
    return this.form.controls;
  }

  dateRangeValidator(group: { get: (k: string) => any }) {
    const start = group.get('startDate')?.value;
    const end = group.get('endDate')?.value;
    if (start && end && new Date(end) < new Date(start)) {
      return { endBeforeStart: true };
    }
    return null;
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.submitting = true;
    this.serverErrors = {};
    const payload = this.form.value as ProjectRequest;

    this.projectService.create(payload).subscribe({
      next: () => {
        this.router.navigate(['/projects']);
      },
      error: (err) => {
        this.submitting = false;
        const body = err.error;

        if (err.status === 409) {
          const msg = body?.message || 'Duplicate project code';
          this.serverErrors['projectCode'] = msg;
          this.form.controls.projectCode.setErrors({ duplicate: true });
        } else if (err.status === 400 && body?.details) {
          Object.entries(body.details as Record<string, string>).forEach(([k, v]) => {
            if (k in this.f) {
              this.serverErrors[k] = v;
              this.form.get(k)?.setErrors({ serverError: true });
            }
          });
        } else {
          this.serverErrors['general'] = body?.message || 'Failed to create project';
        }
      },
    });
  }
}
