import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ReportService } from '../../core/services/report.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { AiService } from '../../core/services/ai.service';
import {
  EmployeeUtilization,
  AvailableResource,
  OverloadedEmployee,
} from '../../core/models/report.model';
import { Dashboard } from '../../core/models/dashboard.model';
import { RecommendedResource } from '../../core/models/ai.model';

@Component({
  selector: 'app-reports-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTabsModule,
    MatCardModule,
    MatTableModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    FormsModule,
  ],
  templateUrl: './reports-dashboard.component.html',
  styleUrl: './reports-dashboard.component.scss',
})
export class ReportsDashboardComponent implements OnInit {
  private reportService = inject(ReportService);
  private dashboardService = inject(DashboardService);
  private aiService = inject(AiService);
  private snackBar = inject(MatSnackBar);

  dashboard: Dashboard | null = null;
  utilization: EmployeeUtilization[] = [];
  available: AvailableResource[] = [];
  overloaded: OverloadedEmployee[] = [];

  utilDisplayed = ['employeeCode', 'fullName', 'totalAllocation'];
  availDisplayed = ['fullName', 'role', 'available'];
  overDisplayed = ['fullName', 'totalAllocation'];

  minAvailable = 0;
  loadingUtil = true;
  loadingAvail = true;
  loadingOver = true;
  loadingStats = true;
  errorUtil: string | null = null;
  errorAvail: string | null = null;
  errorOver: string | null = null;

  // AI
  aiQuery = '';
  aiLoading = false;
  aiRecs: RecommendedResource[] | null = null;
  aiRisks: string[] | null = null;
  aiError: string | null = null;

  ngOnInit(): void {
    this.loadStats();
    this.loadUtilization();
    this.loadOverloaded();
    this.loadAvailable();
  }

  loadStats(): void {
    this.loadingStats = true;
    this.dashboardService.getStats().subscribe({
      next: (d) => {
        this.dashboard = d;
        this.loadingStats = false;
      },
      error: (err) => {
        this.snackBar.open(err.message || 'Failed to load dashboard statistics', 'Close', { duration: 5000 });
        this.loadingStats = false;
      },
    });
  }

  loadUtilization(): void {
    this.loadingUtil = true;
    this.errorUtil = null;
    this.reportService.getUtilization().subscribe({
      next: (data) => {
        this.utilization = data;
        this.loadingUtil = false;
      },
      error: (err) => {
        this.errorUtil = err.message || 'Failed';
        this.loadingUtil = false;
      },
    });
  }

  loadAvailable(): void {
    this.loadingAvail = true;
    this.errorAvail = null;
    this.reportService
      .getAvailable(this.minAvailable > 0 ? this.minAvailable : undefined)
      .subscribe({
        next: (data) => {
          this.available = data;
          this.loadingAvail = false;
        },
        error: (err) => {
          this.errorAvail = err.message || 'Failed';
          this.loadingAvail = false;
        },
      });
  }

  loadOverloaded(): void {
    this.loadingOver = true;
    this.errorOver = null;
    this.reportService.getOverloaded().subscribe({
      next: (data) => {
        this.overloaded = data;
        this.loadingOver = false;
      },
      error: (err) => {
        this.errorOver = err.message || 'Failed';
        this.loadingOver = false;
      },
    });
  }

  getUtilColor(pct: number): string {
    if (pct > 90) return 'overloaded';
    if (pct >= 70) return 'warning';
    return 'normal';
  }

  recommendResource(): void {
    this.aiLoading = true;
    this.aiError = null;
    this.aiRisks = null;
    this.aiService.recommendResource(this.aiQuery).subscribe({
      next: (res) => {
        this.aiRecs = res.recommendedResources;
        this.aiLoading = false;
      },
      error: () => {
        this.aiError = 'Failed to get recommendations';
        this.aiLoading = false;
      },
    });
  }

  detectRisk(): void {
    this.aiLoading = true;
    this.aiError = null;
    this.aiRecs = null;
    this.aiService.detectRisk(this.aiQuery).subscribe({
      next: (res) => {
        this.aiRisks = res.risks;
        this.aiLoading = false;
      },
      error: () => {
        this.aiError = 'Failed to detect risks';
        this.aiLoading = false;
      },
    });
  }
}
