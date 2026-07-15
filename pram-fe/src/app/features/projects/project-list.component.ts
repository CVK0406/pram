import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProjectService } from '../../core/services/project.service';
import { Project, ProjectStatus } from '../../core/models/project.model';

@Component({
  selector: 'app-project-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './project-list.component.html',
  styleUrl: './project-list.component.scss',
})
export class ProjectListComponent implements OnInit {
  private projectService = inject(ProjectService);
  private snackBar = inject(MatSnackBar);

  displayedColumns = ['projectCode', 'projectName', 'customer', 'startDate', 'endDate', 'status', 'actions'];
  projects: Project[] = [];
  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;
  statusFilter: ProjectStatus | '' = '';
  loading = true;
  error: string | null = null;
  transitioningId: number | null = null;

  readonly statusOptions: { value: string; label: string }[] = [
    { value: '', label: 'All' },
    { value: 'PLANNING', label: 'PLANNING' },
    { value: 'ACTIVE', label: 'ACTIVE' },
    { value: 'COMPLETED', label: 'COMPLETED' },
  ];

  ngOnInit(): void {
    this.loadProjects();
  }

  loadProjects(): void {
    this.loading = true;
    this.error = null;
    this.projectService.getAll(this.statusFilter, this.pageIndex, this.pageSize).subscribe({
      next: (page) => {
        this.projects = page.content;
        this.totalElements = page.page?.totalElements ?? page.totalElements ?? 0;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.message || 'Failed to load projects';
        this.loading = false;
      },
    });
  }

  onStatusFilterChange(status: string): void {
    this.statusFilter = status as ProjectStatus | '';
    this.pageIndex = 0;
    this.loadProjects();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProjects();
  }

  getStatusClass(status: ProjectStatus): string {
    switch (status) {
      case 'PLANNING': return 'planning';
      case 'ACTIVE': return 'active';
      case 'COMPLETED': return 'completed';
      default: return '';
    }
  }

  getNextStatus(status: ProjectStatus): ProjectStatus | null {
    if (status === 'PLANNING') return 'ACTIVE';
    if (status === 'ACTIVE') return 'COMPLETED';
    return null;
  }

  transitionStatus(project: Project): void {
    const next = this.getNextStatus(project.status);
    if (!next || this.transitioningId !== null) return;

    this.transitioningId = project.projectId!;
    this.projectService.updateStatus(project.projectId!, next).subscribe({
      next: (updated) => {
        const idx = this.projects.findIndex((p) => p.projectId === updated.projectId);
        if (idx >= 0) this.projects[idx] = updated;
        this.transitioningId = null;
        this.snackBar.open(`Status changed to ${next}`, 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.transitioningId = null;
        const msg = err.error?.message || 'Failed to update status';
        this.snackBar.open(msg, 'Close', { duration: 5000 });
      },
    });
  }
}
