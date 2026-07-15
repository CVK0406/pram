package com.company.pram.controller;

import com.company.pram.dto.response.AvailableResourceResponse;
import com.company.pram.dto.response.EmployeeUtilizationResponse;
import com.company.pram.dto.response.OverloadedEmployeeResponse;
import com.company.pram.service.ReportService;
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
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/utilization")
    public ResponseEntity<List<EmployeeUtilizationResponse>> getEmployeeUtilization() {
        List<EmployeeUtilizationResponse> report = reportService.getEmployeeUtilization();
        return ResponseEntity.ok(report);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableResourceResponse>> getAvailableResources(
            @RequestParam(required = false) Integer minAvailable) {
        List<AvailableResourceResponse> report = reportService.getAvailableResources(minAvailable);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/overloaded")
    public ResponseEntity<List<OverloadedEmployeeResponse>> getOverloadedEmployees() {
        List<OverloadedEmployeeResponse> report = reportService.getOverloadedEmployees();
        return ResponseEntity.ok(report);
    }
}
