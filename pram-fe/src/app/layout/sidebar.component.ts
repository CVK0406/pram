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
      background: var(--sidebar-bg);
      display: flex;
      flex-direction: column;
      color: var(--text-secondary);
      border-right: 1px solid var(--border-color);
      user-select: none;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 1.5rem 1.25rem;
      border-bottom: 1px solid var(--border-color);
    }
    .brand-icon {
      font-size: 1.5rem;
      background: var(--accent-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      text-shadow: 0 0 10px rgba(99, 102, 241, 0.2);
    }
    .brand-text {
      font-size: 1.125rem;
      font-weight: 700;
      color: #fff;
      letter-spacing: 0.05em;
    }
    .nav {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
      padding: 1rem 0.75rem;
    }
    .nav-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.75rem 0.875rem;
      border-radius: 8px;
      color: var(--text-muted);
      text-decoration: none;
      font-size: 0.875rem;
      font-weight: 500;
      transition: all 0.2s ease;
    }
    .nav-item:hover {
      background: var(--row-hover-bg);
      color: var(--text-primary);
      transform: translateX(2px);
    }
    .nav-item.active {
      background: var(--accent-gradient);
      color: #ffffff;
      box-shadow: var(--glow-shadow);
      font-weight: 600;
    }
    .nav-icon {
      font-size: 1.25rem;
      width: 1.25rem;
      height: 1.25rem;
    }
    .sidebar-footer {
      padding: 1rem 1.25rem;
      border-top: 1px solid var(--border-color);
    }
    .version {
      font-size: 0.75rem;
      color: var(--text-muted);
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
