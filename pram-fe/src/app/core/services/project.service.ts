import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Project, ProjectRequest, ProjectStatusRequest, ProjectStatus } from '../models/project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiBaseUrl}/projects`;

  getAll(): Observable<Project[]> {
    return this.http.get<Project[]>(this.baseUrl);
  }

  getById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.baseUrl}/${id}`);
  }

  create(payload: ProjectRequest): Observable<Project> {
    return this.http.post<Project>(this.baseUrl, payload);
  }

  updateStatus(id: number, status: ProjectStatus): Observable<Project> {
    return this.http.put<Project>(`${this.baseUrl}/${id}/status`, { status } as ProjectStatusRequest);
  }
}
