# Frontend UI Redesign — PRAMS

**Date:** 2026-07-15
**Project:** Project Resource Allocation Management System (PRAMS)
**Stack:** Angular 21 + Angular Material 21
**Approach:** Studio Minimal — dark sidebar, clean content, modern dev-tool aesthetic

## 1. Color Palette

| Token | Hex | Usage |
|---|---|---|
| `--surface` | `#fafafa` | Main content background |
| `--surface-sidebar` | `#1e1e2e` | Sidebar background |
| `--on-surface` | `#1a1a2e` | Primary text |
| `--on-surface-muted` | `#6b7280` | Secondary/muted text |
| `--primary` | `#4f46e5` | Accent (indigo-600) — buttons, links, active states |
| `--primary-muted` | `#eef2ff` | Subtle primary background (status pills, highlights) |
| `--border` | `#e5e7eb` | Card, table, input borders |
| `--error` | `#ef4444` | Error text, icons |
| `--success` | `#10b981` | Success indicators |
| `--warning` | `#f59e0b` | Warning indicators |

**Material theme override:** Custom palette using indigo as primary, neutral gray as surface. Tables get transparent background, no elevation. All components inherit M3 tokens via `mat.theme()`.

## 2. Layout — Sidebar Navigation

```
┌─────────────┬─────────────────────────────────┐
│ Sidebar     │ Content area                     │
│ 240px       │ (flex: 1, overflow-y: auto)      │
│             │                                  │
│ ┌─────────┐ │ ┌── Page header ─────────────────┐│
│ │ PRAMS   │ │ │ Title              [action btn]││
│ │ logo    │ │ └────────────────────────────────┘│
│ │         │ │ ┌── Route content ───────────────┐│
│ │👥 Empl. │ │ │                                ││
│ │📁 Proj. │ │ │    (router-outlet)             ││
│ │🔗 Alloc.│ │ │                                ││
│ │📊 Rpts  │ │ └────────────────────────────────┘│
│ │         │ │                                  │
│ └─────────┘ │                                  │
└─────────────┴─────────────────────────────────┘
```

- Sidebar: dark bg `#1e1e2e`, fixed height, app name at top
- Nav items: icon + label, active route highlighted with indigo left border
- Content: white bg, padded `1.5rem`
- Responsive: sidebar collapses on small screens (toggle via hamburger)

## 3. List Pages (Employee, Project, Allocation)

**Structure per page:**
- Page header row: title left, primary action button right
- Filter bar (where applicable): inline, compact
- Table: bordered, no elevation, transparent background
- Paginator: minimal, no background

**Table styling:**
- `border: 1px solid var(--border)`, `border-radius: 8px`
- Header row: `#f9fafb` background, uppercase `--on-surface-muted` text, `font-weight: 600`, `font-size: 0.75rem`
- Body rows: alternating white, hover `#f9fafb`
- Status chips: replaced with inline styled badge (colored dot + text)
- Empty state: centered muted text, icon optional
- Loading: centered spinner, muted text
- Error: centered error text + retry button (consistently styled)

**Project-specific:** Status transition button in actions column, styled as small outlined button.

## 4. Form Pages (Create/Edit)

**Structure:**
- Back navigation link at top (`← Back to ...`)
- Card wrapper: `border: 1px solid var(--border)`, `border-radius: 8px`, no shadow
- Card title inside
- Form fields: `appearance="outline"`, stacked, 1-column (max 560–600px)
- Field spacing: `1rem` gap
- Submission errors: inline below each field or general error block above actions
- Actions row: Cancel (stroked) left, Submit (raised primary) right
- Submit button shows spinner + disabled state while submitting

**Field overrides:**
- `--mdc-outlined-text-field-outline-width: 1px`
- Outline color `var(--border)` on idle, `var(--primary)` on focus

## 5. Reports Dashboard

**Structure preserved** (3 stat cards + 3 tabs + AI box), restyled:

- **Stat cards:** no shadow, border-style, large number (`2rem`, `font-weight: 700`), muted label
- **Tab group:** Material underline style, indigo active color
- **Tab content tables:** same table styling as list pages
- **Filter bar:** inline form field + button, compact
- **AI card:** border-style, compact padding, input full-width, two action buttons side by side

**Highlight classes:**
- Overloaded (`>90%`): `color: var(--error)`, `font-weight: 600`
- Warning (`70-90%`): `color: var(--warning)`, `font-weight: 500`

## 6. Workload Detail

- Back navigation link at top
- Summary card: border-style, employee name + subtitle
- Stat row: total allocation % and available % side by side, large numbers
- Material progress bar: recolor via theme (warn > 90%, accent > 70%, primary otherwise)
- Allocations table: same styling as list tables
- Empty state: centered muted text

## 7. Shared Components & States

All pages share these visual patterns:

| State | Style |
|---|---|
| **Loading** | Centered `mat-progress-spinner` (diameter 32) + "Loading..." muted text below |
| **Error** | Centered error-color text message + stroked "Retry" button |
| **Empty** | Centered muted text with optional icon |
| **Submitting** | Button shows inline spinner + disabled, all form fields disabled |

Error interceptor (existing) continues to show global snackbar for unexpected errors. No structural changes to the interceptor.

## 8. What's NOT changing

- No Angular version upgrade
- No NgRx or state management addition
- No new dependencies
- No HTML structure refactoring (only class/style changes + sidebar layout)
- No BE changes needed
- All existing routing stays, just sidebar replaces nav bar

## Implementation Scope

**Files to create:**
- `src/app/layout/sidebar.component.ts` — new sidebar component
- `src/app/layout/sidebar.component.html`
- `src/app/layout/sidebar.component.scss`

**Files to modify:**
- `src/styles.scss` — custom theme token overrides
- `src/app/app.html` — wrap with sidebar layout
- `src/app/app.scss` — remove old nav, add layout grid
- All 11 feature component SCSS files — apply new table/card/state styles
- All 11 feature component HTML files — minor class changes, back nav links

**Non-goals (future):**
- Dark mode toggle
- Advanced animations
- Drag-drop reordering
- Inline editing
