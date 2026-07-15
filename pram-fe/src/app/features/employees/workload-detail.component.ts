import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeWorkload, WorkloadAllocation } from '../../core/models/workload.model';

@Component({
  selector: 'app-workload-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatProgressBarModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './workload-detail.component.html',
  styleUrl: './workload-detail.component.scss',
})
export class WorkloadDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private employeeService = inject(EmployeeService);

  workload: EmployeeWorkload | null = null;
  displayedColumns = ['projectCode', 'allocationPercent', 'roleInProject'];
  loading = true;
  error: string | null = null;

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.employeeService.getWorkload(id).subscribe({
      next: (data) => {
        this.workload = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load workload';
        this.loading = false;
      },
    });
  }
}
