package com.company.pram.service;

import com.company.pram.dto.response.AvailableResourceResponse;
import com.company.pram.dto.response.EmployeeUtilizationResponse;
import com.company.pram.dto.response.EmployeeWorkloadResponse;
import com.company.pram.dto.response.OverloadedEmployeeResponse;

import java.util.List;

public interface ReportService {
    List<EmployeeUtilizationResponse> getEmployeeUtilization();
    List<AvailableResourceResponse> getAvailableResources(Integer minAvailable);
    List<OverloadedEmployeeResponse> getOverloadedEmployees();
    EmployeeWorkloadResponse getEmployeeWorkload(Long employeeId);
}
