import { Route } from '@angular/router';
import { ProjectListComponent } from './project-list.component';
import { ProjectFormComponent } from './project-form.component';

export default [
  { path: '', component: ProjectListComponent },
  { path: 'create', component: ProjectFormComponent },
] as Route[];
