import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AllocationService } from '../../core/services/allocation.service';
import { EmployeeService } from '../../core/services/employee.service';
import { Allocation } from '../../core/models/allocation.model';
import { Employee } from '../../core/models/employee.model';

@Component({
  selector: 'app-allocation-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatSnackBarModule,
  ],
  templateUrl: './allocation-list.component.html',
  styleUrl: './allocation-list.component.scss',
})
export class AllocationListComponent implements OnInit {
  private allocationService = inject(AllocationService);
  private employeeService = inject(EmployeeService);
  private snackBar = inject(MatSnackBar);

  displayedColumns = ['projectCode', 'allocationPercent', 'roleInProject', 'startDate', 'endDate', 'actions'];
  allocations: Allocation[] = [];
  employees: Employee[] = [];
  selectedEmployeeId: number | null = null;
  loading = false;
  error: string | null = null;

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.employeeService.getAll(0, 200).subscribe({
      next: (page) => (this.employees = page.content),
    });
  }

  onEmployeeChange(id: number | null): void {
    this.selectedEmployeeId = id;
    if (id === null) {
      this.allocations = [];
      return;
    }
    this.loading = true;
    this.error = null;
    this.allocationService.getAll(id).subscribe({
      next: (data) => {
        this.allocations = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Failed to load allocations';
        this.loading = false;
      },
    });
  }

  deleteAllocation(id: number): void {
    if (!confirm('Delete this allocation?')) return;
    this.allocationService.delete(id).subscribe({
      next: () => {
        this.allocations = this.allocations.filter((a) => a.allocationId !== id);
        this.snackBar.open('Allocation deleted', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.snackBar.open(err.error?.message || 'Delete failed', 'Close', { duration: 5000 });
      },
    });
  }
}
