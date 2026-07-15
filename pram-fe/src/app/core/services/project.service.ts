import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Project, ProjectRequest, ProjectStatus } from '../models/project.model';
import { Page } from '../models/page.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiBaseUrl}/projects`;

  getAll(status?: ProjectStatus | '', page = 0, size = 20): Observable<Page<Project>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) params = params.set('status', status);
    return this.http.get<Page<Project>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.baseUrl}/${id}`);
  }

  create(payload: ProjectRequest): Observable<Project> {
    return this.http.post<Project>(this.baseUrl, payload);
  }

  updateStatus(id: number, status: ProjectStatus): Observable<Project> {
    return this.http.put<Project>(`${this.baseUrl}/${id}/status`, { status });
  }
}
