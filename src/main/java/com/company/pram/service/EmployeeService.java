package com.company.pram.service;

import com.company.pram.dto.request.EmployeeRequest;
import com.company.pram.dto.response.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeResponse createEmployee(EmployeeRequest request);
    EmployeeResponse getEmployeeById(Long id);
    Page<EmployeeResponse> getAllEmployees(Pageable pageable);
}
