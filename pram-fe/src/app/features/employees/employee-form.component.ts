import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeRequest } from '../../core/models/employee.model';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.scss',
})
export class EmployeeFormComponent {
  private fb = inject(FormBuilder);
  private employeeService = inject(EmployeeService);
  private router = inject(Router);

  form = this.fb.nonNullable.group({
    employeeCode: ['', [Validators.required, Validators.maxLength(20)]],
    fullName: ['', [Validators.required, Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(100)]],
    role: ['', [Validators.required, Validators.maxLength(50)]],
    department: ['', [Validators.required, Validators.maxLength(50)]],
  });

  submitting = false;
  serverErrors: Record<string, string> = {};

  get f() {
    return this.form.controls;
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.submitting = true;
    this.serverErrors = {};
    const payload = this.form.value as EmployeeRequest;

    this.employeeService.create(payload).subscribe({
      next: () => {
        this.router.navigate(['/employees']);
      },
      error: (err) => {
        this.submitting = false;
        const body = err.error;

        if (err.status === 409) {
          // employeeCode or email duplicate
          const msg: string = body?.message || '';
          if (msg.toLowerCase().includes('code')) {
            this.serverErrors['employeeCode'] = msg;
            this.form.controls.employeeCode.setErrors({ duplicate: true });
          } else if (msg.toLowerCase().includes('email')) {
            this.serverErrors['email'] = msg;
            this.form.controls.email.setErrors({ duplicate: true });
          } else {
            this.serverErrors['general'] = msg || 'Duplicate employee code or email';
          }
        } else if (err.status === 400 && body?.details) {
          Object.entries(body.details as Record<string, string>).forEach(([k, v]) => {
            if (k in this.f) {
              this.serverErrors[k] = v;
              this.form.get(k)?.setErrors({ serverError: true });
            }
          });
        } else {
          this.serverErrors['general'] = body?.message || 'Failed to create employee';
        }
      },
    });
  }
}
