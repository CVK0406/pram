package com.company.pram.service;

import com.company.pram.dto.response.AiRecommendResponse;
import com.company.pram.dto.response.AiRiskResponse;

public interface AiService {
    AiRecommendResponse recommendResource(String query);
    AiRiskResponse detectRisk(String query);
}
