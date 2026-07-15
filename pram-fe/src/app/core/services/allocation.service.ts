import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Allocation, AllocationRequest } from '../models/allocation.model';

@Injectable({ providedIn: 'root' })
export class AllocationService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiBaseUrl}/allocations`;

  getAll(employeeId?: number): Observable<Allocation[]> {
    let params = new HttpParams();
    if (employeeId !== undefined) {
      params = params.set('employeeId', employeeId);
    }
    return this.http.get<Allocation[]>(this.baseUrl, { params });
  }

  create(payload: AllocationRequest): Observable<Allocation> {
    return this.http.post<Allocation>(this.baseUrl, payload);
  }

  update(id: number, payload: AllocationRequest): Observable<Allocation> {
    return this.http.put<Allocation>(`${this.baseUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
