package com.company.pram.controller;

import com.company.pram.dto.request.AiRequest;
import com.company.pram.dto.response.AiRecommendResponse;
import com.company.pram.dto.response.AiRiskResponse;
import com.company.pram.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Bonus", description = "AI-powered resource recommendation and risk detection (Bonus features — SRS §8).")
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI Resource Recommendation",
            description = "Parses a free-text query (e.g. 'Find Java Developer with at least 50% available') " +
                    "and returns matching employees using rule-based extraction + available resource report.")
    @ApiResponse(responseCode = "200", description = "Recommended resources returned")
    @PostMapping("/recommend-resource")
    public ResponseEntity<AiRecommendResponse> recommendResource(@RequestBody AiRequest request) {
        AiRecommendResponse response = aiService.recommendResource(request.getQuery());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "AI Risk Detection",
            description = "Fetches real-time team utilization data, builds a prompt, and calls an LLM (via OpenRouter) " +
                    "to identify resource allocation risks based on the user's query.")
    @ApiResponse(responseCode = "200", description = "Risk analysis returned")
    @PostMapping("/risk-detection")
    public ResponseEntity<AiRiskResponse> detectRisk(@RequestBody AiRequest request) {
        AiRiskResponse response = aiService.detectRisk(request.getQuery());
        return ResponseEntity.ok(response);
    }
}
