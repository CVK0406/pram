package com.company.pram.service.impl;

import com.company.pram.dto.response.AiRecommendResponse;
import com.company.pram.dto.response.AiRiskResponse;
import com.company.pram.dto.response.AvailableResourceResponse;
import com.company.pram.dto.response.EmployeeUtilizationResponse;
import com.company.pram.service.AiService;
import com.company.pram.service.ReportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

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
    // 8.1 AI Resource Recommendation — rule-based, no LLM needed
    // -----------------------------------------------------------------------

    @Override
    public AiRecommendResponse recommendResource(String query) {
        log.info("[AI RECOMMEND] query='{}'", query);

        // Extract minAvailable (e.g. "50%", "50 percent", "tối thiểu 50")
        int minAvailable = extractMinAvailable(query);

        // Extract role keyword (anything that isn't a number/percent/stopword)
        String role = extractRole(query);

        log.info("[AI RECOMMEND] parsed role='{}', minAvailable={}", role, minAvailable);

        List<AvailableResourceResponse> available = reportService.getAvailableResources(minAvailable);

        // Filter by role (case-insensitive substring match against employee's role field)
        List<AiRecommendResponse.RecommendedResource> result = available.stream()
                .filter(r -> role.isBlank() ||
                        (r.getRole() != null && r.getRole().toLowerCase().contains(role.toLowerCase())))
                .map(r -> AiRecommendResponse.RecommendedResource.builder()
                        .employee(r.getFullName())
                        .available(r.getAvailable())
                        .build())
                .collect(Collectors.toList());

        return AiRecommendResponse.builder().recommendedResources(result).build();
    }

    private int extractMinAvailable(String query) {
        // Match patterns like "50%", "50 %", "tối thiểu 50", "minimum 50", "ít nhất 50"
        Pattern p = Pattern.compile("(\\d{1,3})\\s*%?");
        Matcher m = p.matcher(query);
        if (m.find()) {
            int val = Integer.parseInt(m.group(1));
            if (val >= 1 && val <= 100) return val;
        }
        return 0;
    }

    private String extractRole(String query) {
        // Remove numbers, percent signs, and common filler words
        String cleaned = query
                .replaceAll("\\d+\\s*%?", "")
                .replaceAll("(?i)(tìm|find|tối thiểu|minimum|ít nhất|at least|còn|available|developer|percent|còn|của|tìm kiếm)", " $1 ")
                // keep meaningful words (letters only, 3+ chars)
                .trim();

        // Try to find a meaningful role phrase: e.g. "Java Developer", "Backend", "Frontend"
        Pattern rolePattern = Pattern.compile("(?i)(java|python|backend|frontend|fullstack|full.stack|qa|tester|devops|senior|junior|developer|engineer|designer|analyst)");
        Matcher m = rolePattern.matcher(query);
        StringBuilder roleBuilder = new StringBuilder();
        while (m.find()) {
            if (!roleBuilder.isEmpty()) roleBuilder.append(" ");
            roleBuilder.append(m.group());
        }
        return roleBuilder.toString().trim();
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
        if (utilization.isEmpty()) return "No employee data available.";
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
        String requestBody = objectMapper.writeValueAsString(new java.util.HashMap<String, Object>() {{
            put("model", model);
            put("messages", List.of(new java.util.HashMap<String, String>() {{
                put("role", "user");
                put("content", prompt);
            }}));
        }});

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
                if (m.find()) trimmed = m.group(1).strip();
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
                for (JsonNode n : arr) risks.add(n.asText());
            }
            return risks.isEmpty() ? List.of(content) : risks;
        } catch (Exception e) {
            log.warn("[AI RISK] Could not parse JSON from LLM response, returning raw: {}", content);
            return List.of(content);
        }
    }
}
