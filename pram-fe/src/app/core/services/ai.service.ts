import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AiRequest, AiRecommendResponse, AiRiskResponse } from '../models/ai.model';

@Injectable({ providedIn: 'root' })
export class AiService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiBaseUrl}/ai`;

  recommendResource(query: string): Observable<AiRecommendResponse> {
    return this.http.post<AiRecommendResponse>(`${this.baseUrl}/recommend-resource`, { query } as AiRequest);
  }

  detectRisk(query: string): Observable<AiRiskResponse> {
    return this.http.post<AiRiskResponse>(`${this.baseUrl}/risk-detection`, { query } as AiRequest);
  }
}
