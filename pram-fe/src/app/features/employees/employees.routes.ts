import { Route } from '@angular/router';
import { EmployeeListComponent } from './employee-list.component';
import { EmployeeFormComponent } from './employee-form.component';
import { WorkloadDetailComponent } from './workload-detail.component';

export default [
  { path: '', component: EmployeeListComponent },
  { path: 'create', component: EmployeeFormComponent },
  { path: ':id/workload', component: WorkloadDetailComponent },
] as Route[];
