import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Employee, EmployeeRequest } from '../models/employee.model';
import { EmployeeWorkload } from '../models/workload.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiBaseUrl}/employees`;

  getAll(): Observable<Employee[]> {
    return this.http.get<Employee[]>(this.baseUrl);
  }

  getById(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.baseUrl}/${id}`);
  }

  create(payload: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, payload);
  }

  getWorkload(id: number): Observable<EmployeeWorkload> {
    return this.http.get<EmployeeWorkload>(`${this.baseUrl}/${id}/workload`);
  }
}
