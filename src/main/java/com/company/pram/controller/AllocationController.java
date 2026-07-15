package com.company.pram.controller;

import com.company.pram.dto.request.AllocationRequest;
import com.company.pram.dto.response.AllocationResponse;
import com.company.pram.service.AllocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/allocations")
@RequiredArgsConstructor
@Tag(name = "Allocation", description = "Resource allocation management — core business module. " +
        "Enforces: total allocation ≤ 100%, no allocation to COMPLETED projects, date boundary checks.")
public class AllocationController {

    private final AllocationService allocationService;

    @Operation(summary = "Create a new allocation",
            description = "Assigns an employee to a project with a given allocation percentage. " +
                    "Validates: Rule 1 (1-100%), Rule 2 (total ≤ 100%), Rule 3 (project not COMPLETED).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Allocation created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business rule violation (>100%, COMPLETED project)"),
            @ApiResponse(responseCode = "404", description = "Employee or Project not found")
    })
    @PostMapping
    public ResponseEntity<AllocationResponse> createAllocation(@Valid @RequestBody AllocationRequest request) {
        AllocationResponse response = allocationService.createAllocation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an allocation",
            description = "Updates an existing allocation. Applies same business rules as create, " +
                    "but excludes the current allocation from the 100% sum check.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Allocation updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or business rule violation"),
            @ApiResponse(responseCode = "404", description = "Allocation, Employee, or Project not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AllocationResponse> updateAllocation(
            @Parameter(description = "Allocation ID to update") @PathVariable Long id,
            @Valid @RequestBody AllocationRequest request) {
        AllocationResponse response = allocationService.updateAllocation(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Soft-delete an allocation",
            description = "Marks an allocation as deleted (sets deletedAt timestamp). Does not physically remove the record.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Allocation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Allocation not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAllocation(
            @Parameter(description = "Allocation ID to delete") @PathVariable Long id) {
        allocationService.deleteAllocation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get allocations by employee",
            description = "Returns all active (non-deleted) allocations for a given employee.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of allocations returned"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping
    public ResponseEntity<List<AllocationResponse>> getAllocationsByEmployee(
            @Parameter(description = "Employee ID to filter by", required = true) @RequestParam Long employeeId) {
        List<AllocationResponse> response = allocationService.getAllocationsByEmployee(employeeId);
        return ResponseEntity.ok(response);
    }
}
