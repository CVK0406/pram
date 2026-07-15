import { Component, OnInit, inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { map, Observable, startWith } from 'rxjs';
import { AllocationService } from '../../core/services/allocation.service';
import { EmployeeService } from '../../core/services/employee.service';
import { ProjectService } from '../../core/services/project.service';
import { Employee } from '../../core/models/employee.model';
import { Project, ProjectStatus } from '../../core/models/project.model';
import { AllocationRequest } from '../../core/models/allocation.model';

@Component({
  selector: 'app-allocation-form',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatAutocompleteModule,
    MatSnackBarModule,
  ],
  templateUrl: './allocation-form.component.html',
  styleUrl: './allocation-form.component.scss',
})
export class AllocationFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private allocationService = inject(AllocationService);
  private employeeService = inject(EmployeeService);
  private projectService = inject(ProjectService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  form = this.fb.nonNullable.group({
    employee: ['', Validators.required],
    projectId: [0 as number, Validators.required],
    allocationPercent: [0 as number, [Validators.required, Validators.min(1), Validators.max(100)]],
    roleInProject: ['', Validators.required],
    startDate: ['', Validators.required],
    endDate: [''],
  });

  employees: Employee[] = [];
  projects: Project[] = [];
  filteredEmployees: Observable<Employee[]>;
  submitting = false;
  serverErrors: Record<string, string> = {};
  employeeWorkloadMsg = '';

  employeeCtrl = this.fb.nonNullable.control<string | Employee>('');

  constructor() {
    this.filteredEmployees = this.employeeCtrl.valueChanges.pipe(
      startWith(''),
      map((v) => {
        const name = typeof v === 'string' ? v : v?.fullName || '';
        return this.filterEmployees(name);
      }),
    );
  }

  ngOnInit(): void {
    this.employeeService.getAll(0, 200).subscribe({
      next: (page) => (this.employees = page.content),
      error: (err) => {
        this.serverErrors['general'] = err.message || 'Failed to load employees';
      },
    });
    this.projectService.getAll('', 0, 200).subscribe({
      next: (page) => {
        this.projects = page.content.filter((p) => p.status !== 'COMPLETED');
      },
      error: (err) => {
        this.serverErrors['general'] = err.message || 'Failed to load projects';
      },
    });
  }

  private filterEmployees(value: string): Employee[] {
    const filter = value.toLowerCase();
    return this.employees.filter(
      (e) =>
        e.fullName.toLowerCase().includes(filter) ||
        e.employeeCode.toLowerCase().includes(filter),
    );
  }

  displayEmployee(e: Employee): string {
    return e ? `${e.employeeCode} — ${e.fullName}` : '';
  }

  onEmployeeSelected(employee: Employee): void {
    this.form.patchValue({ employee: employee.fullName });
    this.clearFieldError('employee');

    // Fetch workload
    this.employeeWorkloadMsg = 'Loading...';
    this.employeeService.getWorkload(employee.employeeId!).subscribe({
      next: (wl) => {
        if (wl.available <= 0) {
          this.employeeWorkloadMsg = 'This employee is fully allocated (0% available)';
        } else {
          this.employeeWorkloadMsg = `Available: ${wl.available}% (currently allocated ${wl.totalAllocation}%)`;
        }
      },
      error: () => {
        this.employeeWorkloadMsg = '';
      },
    });
  }

  get f() {
    return this.form.controls;
  }

  onProjectIdChange(projectId: number): void {
    this.clearFieldError('projectId');
  }

  private clearFieldError(field: string): void {
    delete this.serverErrors[field];
  }

  private getSelectedEmployee(): Employee | undefined {
    const name = this.form.value.employee;
    return this.employees.find((e) => e.fullName === name);
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const employee = this.getSelectedEmployee();
    if (!employee) {
      this.serverErrors['employee'] = 'Please select a valid employee from the list';
      return;
    }

    this.submitting = true;
    this.serverErrors = {};

    const payload: AllocationRequest = {
      employeeId: employee.employeeId!,
      projectId: this.form.value.projectId!,
      allocationPercent: this.form.value.allocationPercent!,
      roleInProject: this.form.value.roleInProject!,
      startDate: this.form.value.startDate!,
      endDate: this.form.value.endDate || undefined,
    };

    this.allocationService.create(payload).subscribe({
      next: () => {
        this.snackBar.open('Allocation created successfully', 'Close', { duration: 3000 });
        this.router.navigate(['/allocations']);
      },
      error: (err) => {
        this.submitting = false;
        const body = err.error;

        if (err.status === 400) {
          const msg: string = body?.message || '';
          if (msg.includes('100%') || msg.includes('exceed')) {
            this.serverErrors['allocationPercent'] = msg;
          } else if (msg.includes('COMPLETED')) {
            this.serverErrors['projectId'] = 'Cannot allocate to a COMPLETED project';
          } else if (body?.details) {
            Object.entries(body.details as Record<string, string>).forEach(([k, v]) => {
              if (k in this.f) this.serverErrors[k] = v;
            });
          } else {
            this.serverErrors['general'] = msg;
          }
        } else if (err.status === 404) {
          this.serverErrors['general'] = body?.message || 'Employee or Project not found';
        } else {
          this.serverErrors['general'] = body?.message || 'Failed to create allocation';
        }
      },
    });
  }
}
