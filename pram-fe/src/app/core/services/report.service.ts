import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { EmployeeUtilization, AvailableResource, OverloadedEmployee } from '../models/report.model';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiBaseUrl}/reports`;

  getUtilization(): Observable<EmployeeUtilization[]> {
    return this.http.get<EmployeeUtilization[]>(`${this.baseUrl}/utilization`);
  }

  getAvailable(minAvailable?: number): Observable<AvailableResource[]> {
    let params = new HttpParams();
    if (minAvailable !== undefined) {
      params = params.set('minAvailable', minAvailable);
    }
    return this.http.get<AvailableResource[]>(`${this.baseUrl}/available`, { params });
  }

  getOverloaded(): Observable<OverloadedEmployee[]> {
    return this.http.get<OverloadedEmployee[]>(`${this.baseUrl}/overloaded`);
  }
}
