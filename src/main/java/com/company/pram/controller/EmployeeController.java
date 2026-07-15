package com.company.pram.controller;

import com.company.pram.dto.request.EmployeeRequest;
import com.company.pram.dto.response.EmployeeResponse;
import com.company.pram.dto.response.EmployeeWorkloadResponse;
import com.company.pram.service.EmployeeService;
import com.company.pram.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Tag(name = "Employee", description = "Employee management — CRUD operations and workload details.")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ReportService reportService;

    @Operation(summary = "Create a new employee")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Duplicate employeeCode or email")
    })
    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get employee by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all employees (paginated)")
    @ApiResponse(responseCode = "200", description = "Paginated employee list")
    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmployeeResponse> response = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get employee workload detail",
            description = "Returns the employee's active allocations, total allocation percentage, and remaining availability.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Workload detail returned"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{id}/workload")
    public ResponseEntity<EmployeeWorkloadResponse> getEmployeeWorkload(
            @Parameter(description = "Employee ID") @PathVariable Long id) {
        EmployeeWorkloadResponse response = reportService.getEmployeeWorkload(id);
        return ResponseEntity.ok(response);
    }
}
