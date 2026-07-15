package com.company.pram.service.impl;

import com.company.pram.dto.response.AiRecommendResponse;
import com.company.pram.dto.response.AiRiskResponse;
import com.company.pram.dto.response.AvailableResourceResponse;
import com.company.pram.dto.response.EmployeeUtilizationResponse;
import com.company.pram.service.AiService;
import com.company.pram.service.ReportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AiServiceImpl implements AiService {

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AiServiceImpl(ReportService reportService, ObjectMapper objectMapper) {
        this.reportService = reportService;
        this.objectMapper = objectMapper;
    }

    @Value("${openrouter.api-key:}")
    private String apiKey;

    @Value("${openrouter.model:google/gemini-2.0-flash-exp:free}")
    private String model;

    @Value("${openrouter.url:https://openrouter.ai/api/v1/chat/completions}")
    private String apiUrl;

    // -----------------------------------------------------------------------
    // 8.1 AI Resource Recommendation — LLM-based
    // -----------------------------------------------------------------------

    @Override
    public AiRecommendResponse recommendResource(String query) {
        log.info("[AI RECOMMEND] query='{}'", query);

        // Fetch all available resources (with minAvailable = 0 to let the LLM filter
        // them)
        List<AvailableResourceResponse> available = reportService.getAvailableResources(0);
        String prompt = buildRecommendPrompt(query, available);

        try {
            String rawResponse = callOpenRouter(prompt);
            return parseRecommendations(rawResponse);
        } catch (Exception e) {
            log.error("[AI RECOMMEND] OpenRouter call failed: {}", e.getMessage());
            return AiRecommendResponse.builder()
                    .recommendedResources(new ArrayList<>())
                    .build();
        }
    }

    private String buildRecommendPrompt(String userQuery, List<AvailableResourceResponse> available) {
        StringBuilder sb = new StringBuilder();
        for (AvailableResourceResponse r : available) {
            sb.append("- ").append(r.getFullName())
                    .append(" (Role: ").append(r.getRole() != null ? r.getRole() : "Unknown").append("): ")
                    .append(r.getAvailable()).append("% available\n");
        }
        return """
                You are a resource recommendation assistant for a software outsourcing company.

                Available resource pool:
                %s

                User query: "%s"

                Based on the available resource pool and the user's query (matching required role/skills and minimum availability), recommend the matching resources.
                Format your output as a valid JSON object matching this schema:
                {
                  "recommendedResources": [
                    { "employee": "Name of Employee", "available": capacity_number }
                  ]
                }
                Return ONLY the JSON object, no explanations, no markdown, just valid JSON.
                """
                .formatted(sb.toString(), userQuery);
    }

    private AiRecommendResponse parseRecommendations(String content) {
        try {
            String trimmed = content.strip();
            if (trimmed.contains("```")) {
                Pattern fence = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");
                Matcher m = fence.matcher(trimmed);
                if (m.find())
                    trimmed = m.group(1).strip();
            }
            int start = trimmed.indexOf('{');
            int end = trimmed.lastIndexOf('}');
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
            return objectMapper.readValue(trimmed, AiRecommendResponse.class);
        } catch (Exception e) {
            log.error("[AI RECOMMEND] Parsing failed: {}", e.getMessage());
            return AiRecommendResponse.builder().recommendedResources(new ArrayList<>()).build();
        }
    }

    // -----------------------------------------------------------------------
    // 8.2 AI Risk Detection — calls OpenRouter LLM
    // -----------------------------------------------------------------------

    @Override
    public AiRiskResponse detectRisk(String query) {
        log.info("[AI RISK] query='{}'", query);

        // Fetch real utilization data
        List<EmployeeUtilizationResponse> utilization = reportService.getEmployeeUtilization();
        String utilizationSummary = buildUtilizationSummary(utilization);

        String prompt = buildRiskPrompt(query, utilizationSummary);

        try {
            String rawResponse = callOpenRouter(prompt);
            List<String> risks = parseRisks(rawResponse);
            return AiRiskResponse.builder().risks(risks).build();
        } catch (Exception e) {
            log.error("[AI RISK] OpenRouter call failed: {}", e.getMessage());
            return AiRiskResponse.builder()
                    .risks(List.of("AI service unavailable. Please check API key or try again later."))
                    .build();
        }
    }

    private String buildUtilizationSummary(List<EmployeeUtilizationResponse> utilization) {
        if (utilization.isEmpty())
            return "No employee data available.";
        StringBuilder sb = new StringBuilder();
        for (EmployeeUtilizationResponse u : utilization) {
            sb.append("- ").append(u.getFullName())
                    .append(" (").append(u.getEmployeeCode()).append("): ")
                    .append(u.getTotalAllocation()).append("% allocated\n");
        }
        return sb.toString();
    }

    private String buildRiskPrompt(String userQuery, String utilizationSummary) {
        return """
                You are a resource allocation risk analyst for a software outsourcing company.

                Current team utilization data:
                %s

                User query: "%s"

                Based on the utilization data above and the user's query, identify 2-4 specific risks.
                Return ONLY a valid JSON array of strings, no markdown, no explanation, just the JSON array.
                Example format: ["Risk 1 description", "Risk 2 description"]
                """.formatted(utilizationSummary, userQuery);
    }

    private String callOpenRouter(String prompt) throws Exception {
        String requestBody = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {
            {
                put("model", model);
                put("messages", List.of(new java.util.HashMap<String, String>() {
                    {
                        put("role", "user");
                        put("content", prompt);
                    }
                }));
            }
        });

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "http://localhost:8080")
                .header("X-Title", "PRAMS Risk Detection")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("[AI RISK] OpenRouter response status: {}", response.statusCode());

        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenRouter returned HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private List<String> parseRisks(String content) {
        try {
            // Try to parse the entire content as a JSON array
            String trimmed = content.strip();
            // Extract JSON array if wrapped in markdown code fences
            if (trimmed.contains("```")) {
                Pattern fence = Pattern.compile("```(?:json)?\\s*([\\s\\S]*?)\\s*```");
                Matcher m = fence.matcher(trimmed);
                if (m.find())
                    trimmed = m.group(1).strip();
            }
            // Find the first [ ... ] array
            int start = trimmed.indexOf('[');
            int end = trimmed.lastIndexOf(']');
            if (start >= 0 && end > start) {
                trimmed = trimmed.substring(start, end + 1);
            }
            JsonNode arr = objectMapper.readTree(trimmed);
            List<String> risks = new ArrayList<>();
            if (arr.isArray()) {
                for (JsonNode n : arr)
                    risks.add(n.asText());
            }
            return risks.isEmpty() ? List.of(content) : risks;
        } catch (Exception e) {
            log.warn("[AI RISK] Could not parse JSON from LLM response, returning raw: {}", content);
            return List.of(content);
        }
    }
}
