export interface AiRequest {
  query: string;
}

export interface RecommendedResource {
  employee: string;
  available: number;
}

export interface AiRecommendResponse {
  recommendedResources: RecommendedResource[];
}

export interface AiRiskResponse {
  risks: string[];
}
