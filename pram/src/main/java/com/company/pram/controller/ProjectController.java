package com.company.pram.controller;

import com.company.pram.dto.request.ProjectRequest;
import com.company.pram.dto.request.ProjectStatusRequest;
import com.company.pram.dto.response.ProjectResponse;
import com.company.pram.entity.ProjectStatus;
import com.company.pram.service.ProjectService;
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
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project management — CRUD and status transitions (PLANNING → ACTIVE → COMPLETED).")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Create a new project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Duplicate projectCode")
    })
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get project by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project found"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(
            @Parameter(description = "Project ID") @PathVariable Long id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all projects (paginated, filterable by status)",
            description = "Returns paginated projects. Optionally filter by status: PLANNING, ACTIVE, or COMPLETED.")
    @ApiResponse(responseCode = "200", description = "Paginated project list")
    @GetMapping
    public ResponseEntity<Page<ProjectResponse>> getProjects(
            @Parameter(description = "Filter by project status (optional)") @RequestParam(required = false) ProjectStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectResponse> response = projectService.getProjects(status, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update project status",
            description = "Transitions project status following the allowed sequence: PLANNING → ACTIVE → COMPLETED. " +
                    "Skipping steps or reversing is not allowed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @Parameter(description = "Project ID") @PathVariable Long id,
            @Valid @RequestBody ProjectStatusRequest statusRequest) {
        ProjectResponse response = projectService.updateProjectStatus(id, statusRequest.getStatus());
        return ResponseEntity.ok(response);
    }
}
