# Frontend UI Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restyle entire PRAMS frontend from default Angular Material to Studio Minimal design — dark sidebar, clean bordered tables/cards, indigo accent.

**Architecture:** No structural HTML refactoring. Add sidebar layout, override M3 theme tokens via `mat.theme()`, apply shared CSS patterns to all 8 feature SCSS files. Keep all components standalone, all routing intact.

**Tech Stack:** Angular 21, Angular Material 21, Sass with M3 `mat.theme()` overrides.

## Global Constraints

- No new npm dependencies
- No NgRx or state management additions
- No HTML structure changes to feature component templates — only class additions and back-nav links
- All existing CSS variable references (`var(--mat-sys-*)`) kept functional
- English UI labels throughout
- Theme override via `@use '@angular/material' as mat` in `styles.scss`
- Sidebar uses `#1e1e2e` background, indigo-600 (`#4f46e5`) accent
- Tables: no elevation, `1px solid #e5e7eb` border, `border-radius: 8px`
- Cards: border-style (no shadow), `border-radius: 8px`
- Status: replaced mat-chip with inline badge style
- Shared state patterns: loading (spinner + muted text), error (red text + retry button), empty (centered muted text)

---

### Task 1: Create Sidebar Component

**Files:**
- Create: `src/app/layout/sidebar.component.ts`
- Create: `src/app/layout/sidebar.component.html`
- Create: `src/app/layout/sidebar.component.scss`

**Interfaces:**
- Consumes: Angular Router (`routerLink`, `routerLinkActive`) + Material icons
- Produces: `<app-sidebar>` — emits no events, receives no inputs. Routes defined inline.

- [ ] **Create sidebar TS**

```typescript
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

interface NavItem {
  path: string;
  label: string;
  icon: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="sidebar">
      <div class="brand">
        <span class="brand-icon">◈</span>
        <span class="brand-text">PRAMS</span>
      </div>
      <nav class="nav">
        <a
          *ngFor="let item of navItems"
          class="nav-item"
          [routerLink]="item.path"
          routerLinkActive="active"
          [routerLinkActiveOptions]="{ exact: item.path === '/employees' }"
        >
          <span class="nav-icon material-icons">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </a>
      </nav>
      <div class="sidebar-footer">
        <span class="version">v1.0</span>
      </div>
    </div>
  `,
  styles: [`
    .sidebar {
      width: 240px;
      height: 100dvh;
      background: #1e1e2e;
      display: flex;
      flex-direction: column;
      color: #c8c8d0;
      user-select: none;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 1.25rem 1.25rem 1.5rem;
      border-bottom: 1px solid rgba(255,255,255,0.06);
    }
    .brand-icon {
      font-size: 1.5rem;
      color: #4f46e5;
    }
    .brand-text {
      font-size: 1.125rem;
      font-weight: 600;
      color: #fff;
      letter-spacing: 0.01em;
    }
    .nav {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 2px;
      padding: 0.75rem 0.5rem;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.625rem 0.75rem;
      border-radius: 6px;
      color: #9ca3af;
      text-decoration: none;
      font-size: 0.875rem;
      font-weight: 500;
      transition: all 0.15s ease;
    }
    .nav-item:hover {
      background: rgba(255,255,255,0.06);
      color: #e5e7eb;
    }
    .nav-item.active {
      background: rgba(79,70,229,0.15);
      color: #818cf8;
    }
    .nav-icon {
      font-size: 1.25rem;
      width: 1.25rem;
      height: 1.25rem;
    }
    .sidebar-footer {
      padding: 0.75rem 1.25rem;
      border-top: 1px solid rgba(255,255,255,0.06);
    }
    .version {
      font-size: 0.75rem;
      color: #6b7280;
    }
  `],
})
export class SidebarComponent {
  navItems: NavItem[] = [
    { path: '/employees', label: 'Employees', icon: 'people' },
    { path: '/projects', label: 'Projects', icon: 'folder' },
    { path: '/allocations', label: 'Allocations', icon: 'link' },
    { path: '/reports', label: 'Reports', icon: 'bar_chart' },
  ];
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/layout/
git commit -m "feat: add sidebar layout component"
```

---

### Task 2: Wire Sidebar into App Shell

**Files:**
- Modify: `src/app/app.ts`
- Modify: `src/app/app.html`
- Modify: `src/app/app.scss`

- [ ] **Update `app.ts`** — import SidebarComponent

```typescript
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './layout/sidebar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {}
```

- [ ] **Replace `app.html`** — sidebar + content layout

```html
<div class="app-shell">
  <app-sidebar />
  <main class="content">
    <router-outlet />
  </main>
</div>
```

- [ ] **Replace `app.scss`** — horizontal flex layout

```scss
:host {
  display: block;
  height: 100dvh;
}

.app-shell {
  display: flex;
  height: 100%;
}

.content {
  flex: 1;
  overflow-y: auto;
  background: #fafafa;
  padding: 1.5rem 2rem;
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/app.ts pram-fe/src/app/app.html pram-fe/src/app/app.scss
git commit -m "feat: wire sidebar into app shell"
```

---

### Task 3: Override Material Theme (styles.scss)

**Files:**
- Modify: `src/styles.scss`

**Interfaces:**
- Consumes: Task 1-2 (sidebar + app shell)
- Produces: Global CSS variables consumed by all Material components

- [ ] **Replace `styles.scss`** — custom theme tokens + typography + resets

```scss
@use '@angular/material' as mat;

html {
  height: 100%;

  @include mat.theme((
    color: (
      primary: (
        0: #000000,
        10: #1a1a2e,
        20: #2e2e4a,
        25: #3a3a56,
        30: #464662,
        35: #52526e,
        40: #5e5e7a,
        50: #777793,
        60: #9191ae,
        70: #ababc9,
        80: #c7c7e5,
        90: #e3e3f1,
        95: #f1f1f9,
        98: #fafafc,
        99: #fdfdfe,
        100: #ffffff,
        primary: #4f46e5,
        surface: #fafafa,
      ),
      tertiary: mat.$blue-palette,
    ),
    typography: (
      plain-family: 'Inter, Roboto, system-ui, sans-serif',
      brand-family: 'Inter, Roboto, system-ui, sans-serif',
    ),
    density: -1,
  ));
}

body {
  color-scheme: light;
  background: #fafafa;
  color: #1a1a2e;
  font-family: 'Inter', Roboto, system-ui, sans-serif;
  margin: 0;
  height: 100%;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* --- Table overrides (all mat-table instances) --- */
.mat-mdc-table {
  background: transparent !important;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;

  .mdc-data-table__header-row {
    background: #f9fafb;
    height: 48px;
  }

  .mdc-data-table__header-cell {
    color: #6b7280;
    font-weight: 600;
    font-size: 0.75rem;
    letter-spacing: 0.04em;
    text-transform: uppercase;
    border-bottom: none;
    padding: 0 1rem;
  }

  .mdc-data-table__row {
    height: 48px;
    transition: background 0.1s ease;

    &:hover {
      background: #f9fafb;
    }
  }

  .mdc-data-table__cell {
    border-bottom-color: #f3f4f6;
    padding: 0 1rem;
    font-size: 0.875rem;
    color: #374151;
  }
}

.mat-mdc-paginator {
  background: transparent !important;
  font-size: 0.8125rem;
}

/* --- Card overrides --- */
.mat-mdc-card {
  border: 1px solid #e5e7eb !important;
  border-radius: 8px !important;
  box-shadow: none !important;
}

/* --- Form field overrides --- */
.mdc-text-field--outlined {
  --mdc-outlined-text-field-outline-width: 1px;
  --mdc-outlined-text-field-focus-outline-width: 2px;

  .mdc-notched-outline__leading,
  .mdc-notched-outline__notch,
  .mdc-notched-outline__trailing {
    border-color: #e5e7eb !important;
  }

  &.mdc-text-field--focused {
    .mdc-notched-outline__leading,
    .mdc-notched-outline__notch,
    .mdc-notched-outline__trailing {
      border-color: #4f46e5 !important;
    }
  }
}

.mat-mdc-form-field-error {
  font-size: 0.75rem;
}

/* --- Button overrides --- */
.mat-mdc-raised-button.mat-primary {
  --mdc-protected-button-container-color: #4f46e5;
  --mdc-protected-button-label-text-color: #ffffff;
}

.mat-mdc-stroked-button {
  --mdc-outlined-button-outline-color: #d1d5db;
}

/* --- Chips replaced by status badge pattern --- */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  padding: 0.125rem 0.5rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.01em;

  &::before {
    content: '';
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  &.planning {
    color: #6b7280;
    &::before { background: #9ca3af; }
  }

  &.active {
    color: #059669;
    &::before { background: #10b981; }
  }

  &.completed {
    color: #2563eb;
    &::before { background: #3b82f6; }
  }
}

/* --- Stat card for dashboard --- */
.stat-card {
  .mdc-card__media { display: none; }
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1.2;
  color: #1a1a2e;
  margin-bottom: 0.25rem;
}

.stat-label {
  font-size: 0.8125rem;
  color: #6b7280;
  font-weight: 500;
}

/* --- Shared state message pattern --- */
.state-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 2rem;
  color: #6b7280;
  text-align: center;

  &.error {
    color: #ef4444;
  }
}

/* --- Back nav link --- */
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  color: #6b7280;
  text-decoration: none;
  font-size: 0.8125rem;
  font-weight: 500;
  margin-bottom: 1rem;
  transition: color 0.1s ease;

  &:hover {
    color: #4f46e5;
  }

  .material-icons {
    font-size: 1rem;
    width: 1rem;
    height: 1rem;
  }
}

/* --- General error block --- */
.general-error {
  color: #ef4444;
  font-size: 0.875rem;
  padding: 0.5rem 0;
}

/* --- Loading pattern --- */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 3rem 2rem;
  color: #6b7280;
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/styles.scss
git commit -m "feat: override Material theme tokens for Studio Minimal"
```

---

### Task 4: Restyle Employee List + Add Status Badge Pattern

**Files:**
- Modify: `src/app/features/employees/employee-list.component.html`
- Modify: `src/app/features/employees/employee-list.component.scss`

- [ ] **Update `employee-list.component.html`** — remove `mat-elevation-z1`, wrap table in card-style container, apply shared class pattern

```html
<div class="page-header">
  <h2>Employees</h2>
  <button mat-raised-button color="primary" routerLink="/employees/create">
    <mat-icon>add</mat-icon>
    Add Employee
  </button>
</div>

<ng-container *ngIf="loading; else loaded">
  <div class="loading-state">
    <mat-spinner diameter="28" />
    <span>Loading employees…</span>
  </div>
</ng-container>

<ng-template #loaded>
  <ng-container *ngIf="error; else data">
    <div class="state-message error">
      <p>{{ error }}</p>
      <button mat-stroked-button (click)="loadEmployees()">Retry</button>
    </div>
  </ng-container>

  <ng-template #data>
    <div class="table-container">
    <table mat-table [dataSource]="employees">
      <ng-container matColumnDef="employeeCode">
        <th mat-header-cell *matHeaderCellDef>Code</th>
        <td mat-cell *matCellDef="let e">{{ e.employeeCode }}</td>
      </ng-container>

      <ng-container matColumnDef="fullName">
        <th mat-header-cell *matHeaderCellDef>Full Name</th>
        <td mat-cell *matCellDef="let e">{{ e.fullName }}</td>
      </ng-container>

      <ng-container matColumnDef="email">
        <th mat-header-cell *matHeaderCellDef>Email</th>
        <td mat-cell *matCellDef="let e">{{ e.email }}</td>
      </ng-container>

      <ng-container matColumnDef="role">
        <th mat-header-cell *matHeaderCellDef>Role</th>
        <td mat-cell *matCellDef="let e">{{ e.role }}</td>
      </ng-container>

      <ng-container matColumnDef="department">
        <th mat-header-cell *matHeaderCellDef>Department</th>
        <td mat-cell *matCellDef="let e">{{ e.department }}</td>
      </ng-container>

      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef></th>
        <td mat-cell *matCellDef="let e">
          <a mat-stroked-button [routerLink]="['/employees', e.employeeId, 'workload']">
            <mat-icon>bar_chart</mat-icon>
            Workload
          </a>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>

      <tr class="mat-row" *matNoDataRow>
        <td class="mat-cell" [colSpan]="displayedColumns.length" style="text-align:center;padding:2rem;color:#6b7280;">
          No employees found
        </td>
      </tr>
    </table>

    <mat-paginator
      [length]="totalElements"
      [pageSize]="pageSize"
      [pageIndex]="pageIndex"
      [pageSizeOptions]="[10, 20, 50]"
      (page)="onPageChange($event)"
      showFirstLastButtons
    />
    </div>
  </ng-template>
</ng-template>
```

- [ ] **Replace `employee-list.component.scss`**

```scss
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;

  h2 {
    margin: 0;
    font-size: 1.375rem;
    font-weight: 600;
    color: #1a1a2e;
  }
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/features/employees/employee-list.component.html pram-fe/src/app/features/employees/employee-list.component.scss
git commit -m "style: restyle employee list — bordered table, shared states"
```

---

### Task 5: Restyle Employee Form + Add Back Nav

**Files:**
- Modify: `src/app/features/employees/employee-form.component.html`
- Modify: `src/app/features/employees/employee-form.component.scss`

- [ ] **Update `employee-form.component.html`** — add back link, clean up

Search for `<mat-card>` tag and replace the entire content:

```html
<a routerLink="/employees" class="back-link">
  <span class="material-icons">arrow_back</span>
  Back to Employees
</a>

<div class="form-card">
  <h3 class="form-title">Add Employee</h3>

  <form [formGroup]="form" (ngSubmit)="onSubmit()">
    <div class="form-grid">
      <mat-form-field appearance="outline">
        <mat-label>Employee Code</mat-label>
        <input matInput formControlName="employeeCode" placeholder="EMP001" maxlength="20" />
        <mat-error *ngIf="f.employeeCode.errors?.['required']">Required</mat-error>
        <mat-error *ngIf="serverErrors['employeeCode']">{{ serverErrors['employeeCode'] }}</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Full Name</mat-label>
        <input matInput formControlName="fullName" placeholder="John Doe" maxlength="100" />
        <mat-error *ngIf="f.fullName.errors?.['required']">Required</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Email</mat-label>
        <input matInput formControlName="email" placeholder="john@company.com" type="email" maxlength="100" />
        <mat-error *ngIf="f.email.errors?.['required']">Required</mat-error>
        <mat-error *ngIf="f.email.errors?.['email']">Invalid email format</mat-error>
        <mat-error *ngIf="serverErrors['email']">{{ serverErrors['email'] }}</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Role</mat-label>
        <input matInput formControlName="role" placeholder="Developer" maxlength="50" />
        <mat-error *ngIf="f.role.errors?.['required']">Required</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Department</mat-label>
        <input matInput formControlName="department" placeholder="Engineering" maxlength="50" />
        <mat-error *ngIf="f.department.errors?.['required']">Required</mat-error>
      </mat-form-field>
    </div>

    <div class="general-error" *ngIf="serverErrors['general']">
      {{ serverErrors['general'] }}
    </div>

    <div class="actions">
      <button mat-stroked-button type="button" routerLink="/employees">Cancel</button>
      <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || submitting">
        @if (submitting) {
          <mat-spinner diameter="18" class="btn-spinner" />
        }
        <span>Create</span>
      </button>
    </div>
  </form>
</div>
```

- [ ] **Replace `employee-form.component.scss`**

```scss
.form-card {
  max-width: 560px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1.5rem 1.5rem 1rem;
}

.form-title {
  margin: 0 0 0.25rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a1a2e;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 0.5rem;
}

.general-error {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-spinner {
  display: inline-block;
  margin-right: 0.25rem;
}
```

Note: The `@if` syntax requires Angular 17+. The project is Angular 21, so this is fine. This replaces the `*ngIf` pattern for the spinner.

- [ ] **Commit**

```bash
git add pram-fe/src/app/features/employees/employee-form.component.html pram-fe/src/app/features/employees/employee-form.component.scss
git commit -m "style: restyle employee form — bordered card, back nav"
```

---

### Task 6: Restyle Project List — Status Badge + Filter

**Files:**
- Modify: `src/app/features/projects/project-list.component.html`
- Modify: `src/app/features/projects/project-list.component.scss`
- Modify: `src/app/features/projects/project-list.component.ts`

- [ ] **Update HTML**: Remove `mat-elevation-z1`, replace `mat-chip` status with `span.status-badge`, use card container pattern

```html
<div class="page-header">
  <h2>Projects</h2>
  <button mat-raised-button color="primary" routerLink="/projects/create">
    <mat-icon>add</mat-icon>
    Add Project
  </button>
</div>

<div class="filter-row">
  <mat-form-field appearance="outline" subscriptSizing="dynamic">
    <mat-label>Status</mat-label>
    <mat-select [value]="statusFilter" (selectionChange)="onStatusFilterChange($event.value)">
      <mat-option *ngFor="let opt of statusOptions" [value]="opt.value">{{ opt.label }}</mat-option>
    </mat-select>
  </mat-form-field>
</div>

<ng-container *ngIf="loading; else loaded">
  <div class="loading-state">
    <mat-spinner diameter="28" />
    <span>Loading projects…</span>
  </div>
</ng-container>

<ng-template #loaded>
  <ng-container *ngIf="error; else data">
    <div class="state-message error">
      <p>{{ error }}</p>
      <button mat-stroked-button (click)="loadProjects()">Retry</button>
    </div>
  </ng-container>

  <ng-template #data>
    <div class="table-container">
    <table mat-table [dataSource]="projects">
      <ng-container matColumnDef="projectCode">
        <th mat-header-cell *matHeaderCellDef>Code</th>
        <td mat-cell *matCellDef="let p">{{ p.projectCode }}</td>
      </ng-container>

      <ng-container matColumnDef="projectName">
        <th mat-header-cell *matHeaderCellDef>Name</th>
        <td mat-cell *matCellDef="let p">{{ p.projectName }}</td>
      </ng-container>

      <ng-container matColumnDef="customer">
        <th mat-header-cell *matHeaderCellDef>Customer</th>
        <td mat-cell *matCellDef="let p">{{ p.customer }}</td>
      </ng-container>

      <ng-container matColumnDef="startDate">
        <th mat-header-cell *matHeaderCellDef>Start</th>
        <td mat-cell *matCellDef="let p">{{ p.startDate }}</td>
      </ng-container>

      <ng-container matColumnDef="endDate">
        <th mat-header-cell *matHeaderCellDef>End</th>
        <td mat-cell *matCellDef="let p">{{ p.endDate }}</td>
      </ng-container>

      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef>Status</th>
        <td mat-cell *matCellDef="let p">
          <span class="status-badge" [ngClass]="getStatusClass(p.status)">{{ p.status }}</span>
        </td>
      </ng-container>

      <ng-container matColumnDef="actions">
        <th mat-header-cell *matHeaderCellDef></th>
        <td mat-cell *matCellDef="let p">
          <button
            mat-stroked-button
            *ngIf="getNextStatus(p.status)"
            [disabled]="transitioningId === p.projectId"
            (click)="transitionStatus(p)"
          >
            {{ transitioningId === p.projectId ? '…' : '→ ' + getNextStatus(p.status) }}
          </button>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>

      <tr class="mat-row" *matNoDataRow>
        <td class="mat-cell" [colSpan]="displayedColumns.length" style="text-align:center;padding:2rem;color:#6b7280;">
          No projects found
        </td>
      </tr>
    </table>

    <mat-paginator
      [length]="totalElements"
      [pageSize]="pageSize"
      [pageIndex]="pageIndex"
      [pageSizeOptions]="[10, 20, 50]"
      (page)="onPageChange($event)"
      showFirstLastButtons
    />
    </div>
  </ng-template>
</ng-template>
```

- [ ] **Update `project-list.component.ts`** — change `getStatusClass` to return new class names

```typescript
getStatusClass(status: ProjectStatus): string {
  switch (status) {
    case 'PLANNING': return 'planning';
    case 'ACTIVE': return 'active';
    case 'COMPLETED': return 'completed';
    default: return '';
  }
}
```

Remove the `STATUS_COLORS` const (line 14-18) since it's no longer used. Also remove `MatChipsModule` import since we no longer use mat-chip.

- [ ] **Replace `project-list.component.scss`**

```scss
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;

  h2 {
    margin: 0;
    font-size: 1.375rem;
    font-weight: 600;
    color: #1a1a2e;
  }
}

.filter-row {
  margin-bottom: 1rem;

  mat-form-field {
    width: 200px;
  }
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/features/projects/project-list.component.html pram-fe/src/app/features/projects/project-list.component.scss pram-fe/src/app/features/projects/project-list.component.ts
git commit -m "style: restyle project list — status badges, bordered table"
```

---

### Task 7: Restyle Project Form

**Files:**
- Modify: `src/app/features/projects/project-form.component.html`
- Modify: `src/app/features/projects/project-form.component.scss`

- [ ] **Update HTML**: Replace mat-card with form-card + back link (same pattern as employee form)

```html
<a routerLink="/projects" class="back-link">
  <span class="material-icons">arrow_back</span>
  Back to Projects
</a>

<div class="form-card">
  <h3 class="form-title">Add Project</h3>

  <form [formGroup]="form" (ngSubmit)="onSubmit()">
    <div class="form-grid">
      <mat-form-field appearance="outline">
        <mat-label>Project Code</mat-label>
        <input matInput formControlName="projectCode" placeholder="PRJ001" maxlength="20" />
        <mat-error *ngIf="f.projectCode.errors?.['required']">Required</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Project Name</mat-label>
        <input matInput formControlName="projectName" placeholder="E-Commerce Platform" maxlength="200" />
        <mat-error *ngIf="f.projectName.errors?.['required']">Required</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Customer</mat-label>
        <input matInput formControlName="customer" placeholder="Acme Corp" maxlength="100" />
        <mat-error *ngIf="f.customer.errors?.['required']">Required</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Start Date</mat-label>
        <input matInput [matDatepicker]="startPicker" formControlName="startDate" />
        <mat-datepicker-toggle matSuffix [for]="startPicker" />
        <mat-datepicker #startPicker />
        <mat-error *ngIf="f.startDate.errors?.['required']">Required</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>End Date</mat-label>
        <input matInput [matDatepicker]="endPicker" formControlName="endDate" />
        <mat-datepicker-toggle matSuffix [for]="endPicker" />
        <mat-datepicker #endPicker />
        <mat-error *ngIf="f.endDate.errors?.['required']">Required</mat-error>
        <mat-error *ngIf="form.errors?.['endBeforeStart']">End date must be after start date</mat-error>
      </mat-form-field>

      <mat-form-field appearance="outline">
        <mat-label>Status</mat-label>
        <mat-select formControlName="status">
          <mat-option value="PLANNING">PLANNING</mat-option>
        </mat-select>
      </mat-form-field>
    </div>

    <div class="general-error" *ngIf="serverErrors['general']">
      {{ serverErrors['general'] }}
    </div>

    <div class="actions">
      <button mat-stroked-button type="button" routerLink="/projects">Cancel</button>
      <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || submitting">
        @if (submitting) {
          <mat-spinner diameter="18" class="btn-spinner" />
        }
        <span>Create</span>
      </button>
    </div>
  </form>
</div>
```

- [ ] **Replace `project-form.component.scss`** (same pattern as employee form)

```scss
.form-card {
  max-width: 600px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1.5rem 1.5rem 1rem;
}

.form-title {
  margin: 0 0 0.25rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a1a2e;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 0.5rem;
}

.general-error {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-spinner {
  display: inline-block;
  margin-right: 0.25rem;
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/features/projects/project-form.component.html pram-fe/src/app/features/projects/project-form.component.scss
git commit -m "style: restyle project form — bordered card, back nav"
```

---

### Task 8: Restyle Allocation List

**Files:**
- Modify: `src/app/features/allocations/allocation-list.component.html`
- Modify: `src/app/features/allocations/allocation-list.component.scss`

- [ ] **Update HTML**: Use page-header pattern, shared state classes, bordered table

```html
<div class="page-header">
  <h2>Allocations</h2>
  <button mat-raised-button color="primary" routerLink="/allocations/create">
    <mat-icon>add</mat-icon>
    Add Allocation
  </button>
</div>

<div class="filter-row">
  <mat-form-field appearance="outline" subscriptSizing="dynamic">
    <mat-label>Employee</mat-label>
    <mat-select
      [value]="selectedEmployeeId"
      (selectionChange)="onEmployeeChange($event.value)"
    >
      <mat-option [value]="null">-- Select --</mat-option>
      <mat-option *ngFor="let e of employees" [value]="e.employeeId">
        {{ e.employeeCode }} — {{ e.fullName }}
      </mat-option>
    </mat-select>
  </mat-form-field>
</div>

<ng-container *ngIf="!selectedEmployeeId">
  <div class="state-message">Select an employee to view allocations</div>
</ng-container>

<ng-container *ngIf="selectedEmployeeId && loading">
  <div class="loading-state">
    <mat-spinner diameter="28" />
    <span>Loading allocations…</span>
  </div>
</ng-container>

<ng-container *ngIf="error">
  <div class="state-message error">
    <p>{{ error }}</p>
    <button mat-stroked-button (click)="selectedEmployeeId && onEmployeeChange(selectedEmployeeId)">Retry</button>
  </div>
</ng-container>

<ng-container *ngIf="selectedEmployeeId && !loading && !error">
  <div class="table-container">
  <table mat-table [dataSource]="allocations">
    <ng-container matColumnDef="projectCode">
      <th mat-header-cell *matHeaderCellDef>Project</th>
      <td mat-cell *matCellDef="let a">{{ a.projectCode }}</td>
    </ng-container>

    <ng-container matColumnDef="allocationPercent">
      <th mat-header-cell *matHeaderCellDef>%</th>
      <td mat-cell *matCellDef="let a">{{ a.allocationPercent }}%</td>
    </ng-container>

    <ng-container matColumnDef="roleInProject">
      <th mat-header-cell *matHeaderCellDef>Role</th>
      <td mat-cell *matCellDef="let a">{{ a.roleInProject }}</td>
    </ng-container>

    <ng-container matColumnDef="startDate">
      <th mat-header-cell *matHeaderCellDef>Start</th>
      <td mat-cell *matCellDef="let a">{{ a.startDate }}</td>
    </ng-container>

    <ng-container matColumnDef="endDate">
      <th mat-header-cell *matHeaderCellDef>End</th>
      <td mat-cell *matCellDef="let a">{{ a.endDate || '-' }}</td>
    </ng-container>

    <ng-container matColumnDef="actions">
      <th mat-header-cell *matHeaderCellDef></th>
      <td mat-cell *matCellDef="let a">
        <button mat-icon-button color="warn" (click)="deleteAllocation(a.allocationId!)" matTooltip="Delete">
          <mat-icon>delete</mat-icon>
        </button>
      </td>
    </ng-container>

    <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
    <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>

    <tr class="mat-row" *matNoDataRow>
      <td class="mat-cell" [colSpan]="displayedColumns.length" style="text-align:center;padding:2rem;color:#6b7280;">
        No allocations found
      </td>
    </tr>
  </table>
  </div>
</ng-container>
```

- [ ] **Replace `allocation-list.component.scss`**

```scss
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;

  h2 {
    margin: 0;
    font-size: 1.375rem;
    font-weight: 600;
    color: #1a1a2e;
  }
}

.filter-row {
  margin-bottom: 1rem;

  mat-form-field { width: 320px; }
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/features/allocations/allocation-list.component.html pram-fe/src/app/features/allocations/allocation-list.component.scss
git commit -m "style: restyle allocation list — bordered table, shared states"
```

---

### Task 9: Restyle Allocation Form

**Files:**
- Modify: `src/app/features/allocations/allocation-form.component.html`
- Modify: `src/app/features/allocations/allocation-form.component.scss`

- [ ] **Update HTML**: Replace mat-card with form-card + back link

```html
<a routerLink="/allocations" class="back-link">
  <span class="material-icons">arrow_back</span>
  Back to Allocations
</a>

<div class="form-card">
  <h3 class="form-title">Add Allocation</h3>

  <form [formGroup]="form" (ngSubmit)="onSubmit()">
    <div class="form-grid">
      <!-- Employee autocomplete -->
      <mat-form-field appearance="outline">
        <mat-label>Employee</mat-label>
        <input
          matInput
          [matAutocomplete]="auto"
          [formControl]="employeeCtrl"
          placeholder="Search by name or code"
        />
        <mat-autocomplete #auto="matAutocomplete" [displayWith]="displayEmployee" (optionSelected)="onEmployeeSelected($event.option.value)">
          <mat-option *ngFor="let e of filteredEmployees | async" [value]="e">
            {{ e.employeeCode }} — {{ e.fullName }}
          </mat-option>
        </mat-autocomplete>
        <mat-error *ngIf="serverErrors['employee']">{{ serverErrors['employee'] }}</mat-error>
      </mat-form-field>

      <!-- Workload info -->
      <div class="workload-info" *ngIf="employeeWorkloadMsg">{{ employeeWorkloadMsg }}</div>

      <!-- Project -->
      <mat-form-field appearance="outline">
        <mat-label>Project</mat-label>
        <mat-select formControlName="projectId" (selectionChange)="onProjectIdChange($event.value)">
          <mat-option *ngFor="let p of projects" [value]="p.projectId">
            {{ p.projectCode }} — {{ p.projectName }}
          </mat-option>
        </mat-select>
        <mat-error *ngIf="f.projectId.errors?.['required']">Required</mat-error>
        <mat-error *ngIf="serverErrors['projectId']">{{ serverErrors['projectId'] }}</mat-error>
      </mat-form-field>

      <!-- Allocation percent -->
      <mat-form-field appearance="outline">
        <mat-label>Allocation %</mat-label>
        <input matInput formControlName="allocationPercent" type="number" min="1" max="100" placeholder="50" />
        <span matSuffix>%</span>
        <mat-error *ngIf="f.allocationPercent.errors?.['required']">Required</mat-error>
        <mat-error *ngIf="f.allocationPercent.errors?.['min']">Min 1%</mat-error>
        <mat-error *ngIf="f.allocationPercent.errors?.['max']">Max 100%</mat-error>
        <mat-error *ngIf="serverErrors['allocationPercent']">{{ serverErrors['allocationPercent'] }}</mat-error>
      </mat-form-field>

      <!-- Role -->
      <mat-form-field appearance="outline">
        <mat-label>Role in Project</mat-label>
        <input matInput formControlName="roleInProject" placeholder="Developer" />
        <mat-error *ngIf="f.roleInProject.errors?.['required']">Required</mat-error>
      </mat-form-field>

      <!-- Start date -->
      <mat-form-field appearance="outline">
        <mat-label>Start Date</mat-label>
        <input matInput [matDatepicker]="startPicker" formControlName="startDate" />
        <mat-datepicker-toggle matSuffix [for]="startPicker" />
        <mat-datepicker #startPicker />
        <mat-error *ngIf="f.startDate.errors?.['required']">Required</mat-error>
      </mat-form-field>

      <!-- End date -->
      <mat-form-field appearance="outline">
        <mat-label>End Date</mat-label>
        <input matInput [matDatepicker]="endPicker" formControlName="endDate" />
        <mat-datepicker-toggle matSuffix [for]="endPicker" />
        <mat-datepicker #endPicker />
      </mat-form-field>
    </div>

    <div class="general-error" *ngIf="serverErrors['general']">
      {{ serverErrors['general'] }}
    </div>

    <div class="actions">
      <button mat-stroked-button type="button" routerLink="/allocations">Cancel</button>
      <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid || submitting">
        @if (submitting) {
          <mat-spinner diameter="18" class="btn-spinner" />
        }
        <span>Create</span>
      </button>
    </div>
  </form>
</div>
```

- [ ] **Replace `allocation-form.component.scss`**

```scss
.form-card {
  max-width: 600px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1.5rem 1.5rem 1rem;
}

.form-title {
  margin: 0 0 0.25rem;
  font-size: 1.125rem;
  font-weight: 600;
  color: #1a1a2e;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 0.5rem;
}

.workload-info {
  font-size: 0.8125rem;
  color: #4f46e5;
  margin: -0.5rem 0 0;
  padding: 0 0.25rem;
}

.general-error {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.5rem;
}

.actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-spinner {
  display: inline-block;
  margin-right: 0.25rem;
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/features/allocations/allocation-form.component.html pram-fe/src/app/features/allocations/allocation-form.component.scss
git commit -m "style: restyle allocation form — bordered card, back nav"
```

---

### Task 10: Restyle Workload Detail

**Files:**
- Modify: `src/app/features/employees/workload-detail.component.html`
- Modify: `src/app/features/employees/workload-detail.component.scss`

- [ ] **Update HTML**: Use back-link, summary card with border style

```html
<a routerLink="/employees" class="back-link">
  <span class="material-icons">arrow_back</span>
  Back to Employees
</a>

<ng-container *ngIf="loading; else loaded">
  <div class="loading-state">
    <mat-spinner diameter="28" />
    <span>Loading workload…</span>
  </div>
</ng-container>

<ng-template #loaded>
  <ng-container *ngIf="error; else data">
    <div class="state-message error">{{ error }}</div>
  </ng-container>

  <ng-template #data>
    <div *ngIf="workload" class="summary-card">
      <div class="card-header">
        <h3>{{ workload.employeeName }}</h3>
        <span class="card-subtitle">Workload Detail</span>
      </div>
      <div class="card-body">
        <div class="stats-row">
          <div class="stat">
            <span class="stat-value" [class.over-allocated]="workload.totalAllocation > 100">{{ workload.totalAllocation }}%</span>
            <span class="stat-label">Total Allocation</span>
          </div>
          <div class="stat">
            <span class="stat-value" [class.over-allocated]="workload.available < 0">{{ workload.available }}%</span>
            <span class="stat-label">Available</span>
          </div>
        </div>

        <mat-progress-bar
          mode="determinate"
          [value]="workload.totalAllocation"
          [color]="workload.totalAllocation > 90 ? 'warn' : workload.totalAllocation > 70 ? 'accent' : 'primary'"
        />

        <div class="section" *ngIf="workload.allocations.length > 0">
          <h4>Allocations</h4>
          <div class="table-container">
          <table mat-table [dataSource]="workload.allocations">
            <ng-container matColumnDef="projectCode">
              <th mat-header-cell *matHeaderCellDef>Project</th>
              <td mat-cell *matCellDef="let a">{{ a.projectCode }}</td>
            </ng-container>

            <ng-container matColumnDef="allocationPercent">
              <th mat-header-cell *matHeaderCellDef>%</th>
              <td mat-cell *matCellDef="let a">{{ a.allocationPercent }}%</td>
            </ng-container>

            <ng-container matColumnDef="roleInProject">
              <th mat-header-cell *matHeaderCellDef>Role</th>
              <td mat-cell *matCellDef="let a">{{ a.roleInProject }}</td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
          </table>
          </div>
        </div>

        <div class="state-message" *ngIf="workload.allocations.length === 0">
          No active allocations for this employee.
        </div>
      </div>
    </div>
  </ng-template>
</ng-template>
```

- [ ] **Replace `workload-detail.component.scss`**

```scss
.summary-card {
  max-width: 640px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.card-header {
  padding: 1.25rem 1.5rem 0;
}

.card-header h3 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a1a2e;
}

.card-subtitle {
  font-size: 0.8125rem;
  color: #6b7280;
}

.card-body {
  padding: 1rem 1.5rem 1.5rem;
}

.stats-row {
  display: flex;
  gap: 2.5rem;
  margin-bottom: 1rem;
}

.stat {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 0.8125rem;
  color: #6b7280;
  font-weight: 500;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1a1a2e;
}

.over-allocated {
  color: #ef4444;
}

mat-progress-bar {
  margin-bottom: 1.5rem;
}

.section {
  h4 {
    margin: 0 0 0.75rem;
    font-size: 0.9375rem;
    font-weight: 600;
    color: #374151;
  }
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/features/employees/workload-detail.component.html pram-fe/src/app/features/employees/workload-detail.component.scss
git commit -m "style: restyle workload detail — bordered card, stat layout"
```

---

### Task 11: Restyle Reports Dashboard

**Files:**
- Modify: `src/app/features/reports/reports-dashboard.component.html`
- Modify: `src/app/features/reports/reports-dashboard.component.scss`

- [ ] **Update HTML**: Page header, stat cards as border-style, clean up state messages

```html
<div class="page-header">
  <h2>Reports Dashboard</h2>
</div>

<!-- Summary Cards -->
<div class="stats-row" *ngIf="!loadingStats && dashboard">
  <div class="summary-card stat-card">
    <div class="card-body">
      <div class="stat-value">{{ dashboard.totalEmployees }}</div>
      <div class="stat-label">Employees</div>
    </div>
  </div>
  <div class="summary-card stat-card">
    <div class="card-body">
      <div class="stat-value">{{ dashboard.totalProjects }}</div>
      <div class="stat-label">Projects</div>
    </div>
  </div>
  <div class="summary-card stat-card">
    <div class="card-body">
      <div class="stat-value">{{ dashboard.activeAllocations }}</div>
      <div class="stat-label">Active Allocations</div>
    </div>
  </div>
</div>

<!-- Tabbed Reports -->
<mat-tab-group dynamicHeight>
  <!-- Utilization -->
  <mat-tab label="Utilization">
    <div class="tab-content">
      <ng-container *ngIf="loadingUtil; else utilLoaded">
        <div class="loading-state">
          <mat-spinner diameter="28" />
          <span>Loading…</span>
        </div>
      </ng-container>
      <ng-template #utilLoaded>
        <ng-container *ngIf="errorUtil; else utilData">
          <div class="state-message error">
            <p>{{ errorUtil }}</p>
            <button mat-stroked-button (click)="loadUtilization()">Retry</button>
          </div>
        </ng-container>
        <ng-template #utilData>
          <div class="table-container">
          <table mat-table [dataSource]="utilization">
            <ng-container matColumnDef="employeeCode">
              <th mat-header-cell *matHeaderCellDef>Code</th>
              <td mat-cell *matCellDef="let r">{{ r.employeeCode }}</td>
            </ng-container>
            <ng-container matColumnDef="fullName">
              <th mat-header-cell *matHeaderCellDef>Name</th>
              <td mat-cell *matCellDef="let r">{{ r.fullName }}</td>
            </ng-container>
            <ng-container matColumnDef="totalAllocation">
              <th mat-header-cell *matHeaderCellDef>Allocation</th>
              <td mat-cell *matCellDef="let r" [class.overloaded]="r.totalAllocation > 90" [class.warning]="r.totalAllocation >= 70 && r.totalAllocation <= 90">
                {{ r.totalAllocation }}%
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="utilDisplayed"></tr>
            <tr mat-row *matRowDef="let row; columns: utilDisplayed"></tr>
            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell" [colSpan]="utilDisplayed.length" style="text-align:center;padding:2rem;color:#6b7280;">No data</td>
            </tr>
          </table>
          </div>
        </ng-template>
      </ng-template>
    </div>
  </mat-tab>

  <!-- Available -->
  <mat-tab label="Available Resources">
    <div class="tab-content">
      <div class="filter-row">
        <mat-form-field appearance="outline" subscriptSizing="dynamic">
          <mat-label>Min Available %</mat-label>
          <input matInput type="number" [(ngModel)]="minAvailable" min="0" max="100" />
        </mat-form-field>
        <button mat-raised-button (click)="loadAvailable()">Filter</button>
      </div>

      <ng-container *ngIf="loadingAvail; else availLoaded">
        <div class="loading-state">
          <mat-spinner diameter="28" />
          <span>Loading…</span>
        </div>
      </ng-container>
      <ng-template #availLoaded>
        <ng-container *ngIf="errorAvail; else availData">
          <div class="state-message error">
            <p>{{ errorAvail }}</p>
            <button mat-stroked-button (click)="loadAvailable()">Retry</button>
          </div>
        </ng-container>
        <ng-template #availData>
          <div class="table-container">
          <table mat-table [dataSource]="available">
            <ng-container matColumnDef="fullName">
              <th mat-header-cell *matHeaderCellDef>Name</th>
              <td mat-cell *matCellDef="let r">{{ r.fullName }}</td>
            </ng-container>
            <ng-container matColumnDef="role">
              <th mat-header-cell *matHeaderCellDef>Role</th>
              <td mat-cell *matCellDef="let r">{{ r.role }}</td>
            </ng-container>
            <ng-container matColumnDef="available">
              <th mat-header-cell *matHeaderCellDef>Available</th>
              <td mat-cell *matCellDef="let r">{{ r.available }}%</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="availDisplayed"></tr>
            <tr mat-row *matRowDef="let row; columns: availDisplayed"></tr>
            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell" [colSpan]="availDisplayed.length" style="text-align:center;padding:2rem;color:#6b7280;">No data</td>
            </tr>
          </table>
          </div>
        </ng-template>
      </ng-template>
    </div>
  </mat-tab>

  <!-- Overloaded -->
  <mat-tab label="Overloaded">
    <div class="tab-content">
      <ng-container *ngIf="loadingOver; else overLoaded">
        <div class="loading-state">
          <mat-spinner diameter="28" />
          <span>Loading…</span>
        </div>
      </ng-container>
      <ng-template #overLoaded>
        <ng-container *ngIf="errorOver; else overData">
          <div class="state-message error">
            <p>{{ errorOver }}</p>
            <button mat-stroked-button (click)="loadOverloaded()">Retry</button>
          </div>
        </ng-container>
        <ng-template #overData>
          <div class="table-container">
          <table mat-table [dataSource]="overloaded">
            <ng-container matColumnDef="fullName">
              <th mat-header-cell *matHeaderCellDef>Name</th>
              <td mat-cell *matCellDef="let r">{{ r.fullName }}</td>
            </ng-container>
            <ng-container matColumnDef="totalAllocation">
              <th mat-header-cell *matHeaderCellDef>Allocation</th>
              <td mat-cell *matCellDef="let r" class="overloaded">{{ r.totalAllocation }}%</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="overDisplayed"></tr>
            <tr mat-row *matRowDef="let row; columns: overDisplayed"></tr>
            <tr class="mat-row" *matNoDataRow>
              <td class="mat-cell" [colSpan]="overDisplayed.length" style="text-align:center;padding:2rem;color:#6b7280;">No overloaded employees</td>
            </tr>
          </table>
          </div>
        </ng-template>
      </ng-template>
    </div>
  </mat-tab>
</mat-tab-group>

<!-- AI Query Box -->
<div class="ai-card">
  <div class="card-header">
    <h3>AI Assistant</h3>
  </div>
  <div class="card-body">
    <mat-form-field appearance="outline" class="ai-input">
      <mat-label>Ask about resources or risks</mat-label>
      <input matInput [(ngModel)]="aiQuery" placeholder="e.g. Find Java developer with 50% available" />
    </mat-form-field>
    <div class="ai-actions">
      <button mat-raised-button color="primary" [disabled]="!aiQuery || aiLoading" (click)="recommendResource()">
        Recommend Resource
      </button>
      <button mat-raised-button color="accent" [disabled]="!aiQuery || aiLoading" (click)="detectRisk()">
        Detect Risk
      </button>
      <mat-spinner *ngIf="aiLoading" diameter="22" class="ai-spinner" />
    </div>

    <div class="ai-error" *ngIf="aiError">{{ aiError }}</div>

    <div class="ai-results" *ngIf="aiRecs && aiRecs.length > 0">
      <h4>Recommended Resources</h4>
      <div class="result-list">
        <div class="result-item" *ngFor="let r of aiRecs">
          <strong>{{ r.employee }}</strong> — {{ r.available }}% available
        </div>
      </div>
    </div>
    <div class="state-message" *ngIf="aiRecs && aiRecs.length === 0">
      No matching resources found.
    </div>

    <div class="ai-results" *ngIf="aiRisks && aiRisks.length > 0">
      <h4>Risk Detection Results</h4>
      <div class="result-list">
        <div class="result-item risk" *ngFor="let risk of aiRisks">
          <span class="material-icons" style="font-size:1rem;color:#ef4444;vertical-align:middle;">warning</span>
          {{ risk }}
        </div>
      </div>
    </div>
    <div class="state-message" *ngIf="aiRisks && aiRisks.length === 0">
      No risks detected.
    </div>
  </div>
</div>
```

- [ ] **Replace `reports-dashboard.component.scss`**

```scss
.page-header {
  margin-bottom: 1.5rem;

  h2 {
    margin: 0;
    font-size: 1.375rem;
    font-weight: 600;
    color: #1a1a2e;
  }
}

.stats-row {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.summary-card {
  flex: 1;
  border: 1px solid #e5e7eb;
  border-radius: 8px;

  .card-body {
    padding: 1.25rem 1.5rem;
  }
}

.tab-content {
  padding-top: 1rem;
}

.filter-row {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-bottom: 1rem;

  mat-form-field { width: 180px; }
}

.table-container {
  overflow-x: auto;
}

table {
  width: 100%;
}

.overloaded {
  color: #ef4444;
  font-weight: 600;
}

.warning {
  color: #f59e0b;
  font-weight: 500;
}

.ai-card {
  margin-top: 2rem;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;

  .card-header {
    padding: 1.25rem 1.5rem 0;

    h3 {
      margin: 0;
      font-size: 1.125rem;
      font-weight: 600;
      color: #1a1a2e;
    }
  }

  .card-body {
    padding: 1rem 1.5rem 1.5rem;
  }
}

.ai-input {
  width: 100%;
}

.ai-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  margin-top: 0.75rem;
}

.ai-spinner {
  margin-left: 0.5rem;
}

.ai-error {
  color: #ef4444;
  font-size: 0.875rem;
  margin-top: 0.75rem;
}

.ai-results {
  margin-top: 1.25rem;

  h4 {
    margin: 0 0 0.5rem;
    font-size: 0.9375rem;
    font-weight: 600;
    color: #374151;
  }
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.result-item {
  padding: 0.625rem 0.75rem;
  background: #f9fafb;
  border-radius: 6px;
  font-size: 0.875rem;
  color: #374151;

  &.risk {
    display: flex;
    align-items: center;
    gap: 0.375rem;
  }
}
```

- [ ] **Commit**

```bash
git add pram-fe/src/app/features/reports/reports-dashboard.component.html pram-fe/src/app/features/reports/reports-dashboard.component.scss
git commit -m "style: restyle reports dashboard — border cards, stat layout, ai box"
```

---

### Task 12: Final Verification

**Files:**
- Check all modified files compile and look correct

- [ ] **Verify TypeScript compilation**

```bash
cd pram-fe && npx tsc --noEmit
```

Expected: No TypeScript errors.

- [ ] **Verify Angular build**

```bash
cd pram-fe && ng build
```

Expected: `✔` Build success, no errors.

- [ ] **Remove unused imports (if any)**
  - Check `project-list.component.ts` — remove `MatChipsModule` import if replaced with badges
  - Check all feature components no longer import unused Material modules

- [ ] **Commit any cleanup**

```bash
git add -A && git commit -m "chore: cleanup unused imports, verify build"
```
