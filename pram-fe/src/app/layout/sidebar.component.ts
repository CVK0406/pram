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
