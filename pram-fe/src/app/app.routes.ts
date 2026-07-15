import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'employees',
    loadChildren: () => import('./features/employees/employees.routes'),
  },
  {
    path: 'projects',
    loadChildren: () => import('./features/projects/projects.routes'),
  },
  {
    path: 'allocations',
    loadChildren: () => import('./features/allocations/allocations.routes'),
  },
  {
    path: 'reports',
    loadChildren: () => import('./features/reports/reports.routes'),
  },
  { path: '', redirectTo: '/employees', pathMatch: 'full' },
];
