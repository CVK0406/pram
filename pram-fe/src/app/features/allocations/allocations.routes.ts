import { Route } from '@angular/router';
import { AllocationListComponent } from './allocation-list.component';
import { AllocationFormComponent } from './allocation-form.component';

export default [
  { path: '', component: AllocationListComponent },
  { path: 'create', component: AllocationFormComponent },
] as Route[];
