package com.company.pram.controller;

import com.company.pram.dto.response.AvailableResourceResponse;
import com.company.pram.dto.response.EmployeeUtilizationResponse;
import com.company.pram.dto.response.OverloadedEmployeeResponse;
import com.company.pram.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Business reporting endpoints — utilization, availability, and overload analysis.")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Employee utilization report",
            description = "Returns all employees with their total active allocation percentage. " +
                    "Includes employees with 0% allocation (fully available).")
    @ApiResponse(responseCode = "200", description = "Utilization report returned")
    @GetMapping("/utilization")
    public ResponseEntity<List<EmployeeUtilizationResponse>> getEmployeeUtilization() {
        List<EmployeeUtilizationResponse> report = reportService.getEmployeeUtilization();
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Available resource report",
            description = "Returns employees with remaining capacity (100% - activeAllocation). " +
                    "Filter by minimum available percentage using `minAvailable` (default: any availability > 0).")
    @ApiResponse(responseCode = "200", description = "Available resource report returned")
    @GetMapping("/available")
    public ResponseEntity<List<AvailableResourceResponse>> getAvailableResources(
            @Parameter(description = "Minimum available percentage to include (e.g., 50 = at least 50% free)")
            @RequestParam(required = false) Integer minAvailable) {
        List<AvailableResourceResponse> report = reportService.getAvailableResources(minAvailable);
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "Overloaded employee report",
            description = "Returns employees whose total active allocation exceeds 90%.")
    @ApiResponse(responseCode = "200", description = "Overloaded employee report returned")
    @GetMapping("/overloaded")
    public ResponseEntity<List<OverloadedEmployeeResponse>> getOverloadedEmployees() {
        List<OverloadedEmployeeResponse> report = reportService.getOverloadedEmployees();
        return ResponseEntity.ok(report);
    }
}
