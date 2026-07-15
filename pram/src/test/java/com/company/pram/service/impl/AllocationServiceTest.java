package com.company.pram.service.impl;

import com.company.pram.dto.request.AllocationRequest;
import com.company.pram.dto.response.AllocationResponse;
import com.company.pram.entity.Allocation;
import com.company.pram.entity.Employee;
import com.company.pram.entity.Project;
import com.company.pram.entity.ProjectStatus;
import com.company.pram.exception.AllocationExceededException;
import com.company.pram.exception.AllocationNotFoundException;
import com.company.pram.exception.EmployeeNotFoundException;
import com.company.pram.exception.InvalidProjectStatusException;
import com.company.pram.exception.ProjectNotFoundException;
import com.company.pram.repository.AllocationRepository;
import com.company.pram.repository.EmployeeRepository;
import com.company.pram.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {

    @Mock
    private AllocationRepository allocationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private AllocationServiceImpl allocationService;

    private Employee employee;
    private Project activeProject;
    private Project completedProject;
    private AllocationRequest request;
    private Allocation savedAllocation;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .employeeId(1L)
                .employeeCode("EMP001")
                .fullName("Tuan Ho Anh")
                .email("tuan@company.com")
                .role("Developer")
                .department("FSOFT")
                .build();

        activeProject = Project.builder()
                .projectId(10L)
                .projectCode("NCG")
                .projectName("NCG Platform")
                .customer("NCG Corp")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.ACTIVE)
                .build();

        completedProject = Project.builder()
                .projectId(20L)
                .projectCode("OLD")
                .projectName("Old Project")
                .customer("Old Corp")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(ProjectStatus.COMPLETED)
                .build();

        request = AllocationRequest.builder()
                .employeeId(1L)
                .projectId(10L)
                .allocationPercent(50)
                .roleInProject("Backend Developer")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        savedAllocation = Allocation.builder()
                .allocationId(100L)
                .employee(employee)
                .project(activeProject)
                .allocationPercent(50)
                .roleInProject("Backend Developer")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();
    }

    // -----------------------------------------------------------------------
    // createAllocation tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-01: createAllocation - success returns AllocationResponse")
    void createAllocation_success() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeForUpdate(1L)).thenReturn(Collections.emptyList());
        when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
        when(allocationRepository.sumOverlappingAllocations(eq(1L), any(), any(), eq(-1L))).thenReturn(0);
        when(allocationRepository.save(any(Allocation.class))).thenReturn(savedAllocation);

        AllocationResponse response = allocationService.createAllocation(request);

        assertThat(response).isNotNull();
        assertThat(response.getAllocationId()).isEqualTo(100L);
        assertThat(response.getAllocationPercent()).isEqualTo(50);
        assertThat(response.getEmployeeName()).isEqualTo("Tuan Ho Anh");
        assertThat(response.getProjectCode()).isEqualTo("NCG");
        verify(allocationRepository, times(1)).save(any(Allocation.class));
    }

    @Test
    @DisplayName("TC-02: createAllocation - employee not found throws EmployeeNotFoundException")
    void createAllocation_employeeNotFound_throwsException() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.createAllocation(request))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessageContaining("1");
    }

    @Test
    @DisplayName("TC-03: createAllocation - project not found throws ProjectNotFoundException")
    void createAllocation_projectNotFound_throwsException() {
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeForUpdate(1L)).thenReturn(Collections.emptyList());
        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.createAllocation(request))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("10");
    }

    @Test
    @DisplayName("TC-04: createAllocation - Rule 3: project COMPLETED throws InvalidProjectStatusException")
    void createAllocation_projectCompleted_throwsException() {
        request = AllocationRequest.builder()
                .employeeId(1L)
                .projectId(20L)
                .allocationPercent(50)
                .roleInProject("Dev")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .build();

        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeForUpdate(1L)).thenReturn(Collections.emptyList());
        when(projectRepository.findById(20L)).thenReturn(Optional.of(completedProject));

        assertThatThrownBy(() -> allocationService.createAllocation(request))
                .isInstanceOf(InvalidProjectStatusException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("TC-05: createAllocation - Rule 2: total allocation > 100% throws AllocationExceededException")
    void createAllocation_exceedsCapacity_throwsException() {
        // Existing 60% + new 50% = 110% -> should fail
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeForUpdate(1L)).thenReturn(Collections.emptyList());
        when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
        when(allocationRepository.sumOverlappingAllocations(eq(1L), any(), any(), eq(-1L))).thenReturn(60);

        assertThatThrownBy(() -> allocationService.createAllocation(request))
                .isInstanceOf(AllocationExceededException.class)
                .hasMessageContaining("100%");
    }

    @Test
    @DisplayName("TC-06: createAllocation - Rule 2 boundary: exactly 100% succeeds")
    void createAllocation_exactlyAt100_succeeds() {
        // Existing 50% + new 50% = 100% -> should succeed
        AllocationRequest req50 = AllocationRequest.builder()
                .employeeId(1L)
                .projectId(10L)
                .allocationPercent(50)
                .roleInProject("QA")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeForUpdate(1L)).thenReturn(Collections.emptyList());
        when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
        when(allocationRepository.sumOverlappingAllocations(eq(1L), any(), any(), eq(-1L))).thenReturn(50);
        when(allocationRepository.save(any(Allocation.class))).thenReturn(savedAllocation);

        AllocationResponse response = allocationService.createAllocation(req50);

        assertThat(response).isNotNull();
        verify(allocationRepository, times(1)).save(any(Allocation.class));
    }

    // -----------------------------------------------------------------------
    // updateAllocation tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-07: updateAllocation - allocation not found throws AllocationNotFoundException")
    void updateAllocation_notFound_throwsException() {
        when(allocationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.updateAllocation(999L, request))
                .isInstanceOf(AllocationNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("TC-08: updateAllocation - Rule 2: update excludes the current allocation from sum")
    void updateAllocation_excludesCurrentAllocation_succeeds() {
        Long allocationId = 100L;
        Allocation existingAllocation = Allocation.builder()
                .allocationId(allocationId)
                .employee(employee)
                .project(activeProject)
                .allocationPercent(60)
                .roleInProject("Old Role")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        // Employee already has 60% (from other allocations) but this 60% is from the current one being updated.
        // After exclusion, overlap sum is 0, so new 80% should succeed.
        AllocationRequest updateReq = AllocationRequest.builder()
                .employeeId(1L)
                .projectId(10L)
                .allocationPercent(80)
                .roleInProject("Updated Role")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        Allocation updatedAllocation = Allocation.builder()
                .allocationId(allocationId)
                .employee(employee)
                .project(activeProject)
                .allocationPercent(80)
                .roleInProject("Updated Role")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        when(allocationRepository.findById(allocationId)).thenReturn(Optional.of(existingAllocation));
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeForUpdate(1L)).thenReturn(Collections.emptyList());
        when(projectRepository.findById(10L)).thenReturn(Optional.of(activeProject));
        // sumOverlappingAllocations excludes the current allocationId = 100L, returns 0
        when(allocationRepository.sumOverlappingAllocations(eq(1L), any(), any(), eq(allocationId))).thenReturn(0);
        when(allocationRepository.save(any(Allocation.class))).thenReturn(updatedAllocation);

        AllocationResponse response = allocationService.updateAllocation(allocationId, updateReq);

        assertThat(response.getAllocationPercent()).isEqualTo(80);
        assertThat(response.getRoleInProject()).isEqualTo("Updated Role");
    }

    @Test
    @DisplayName("TC-09: updateAllocation - Rule 3: project COMPLETED throws InvalidProjectStatusException")
    void updateAllocation_projectCompleted_throwsException() {
        Allocation existingAllocation = Allocation.builder()
                .allocationId(100L)
                .employee(employee)
                .project(activeProject)
                .allocationPercent(50)
                .roleInProject("Dev")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build();

        AllocationRequest updateToCompleted = AllocationRequest.builder()
                .employeeId(1L)
                .projectId(20L)  // completed project
                .allocationPercent(50)
                .roleInProject("Dev")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 6, 30))
                .build();

        when(allocationRepository.findById(100L)).thenReturn(Optional.of(existingAllocation));
        when(employeeRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(employee));
        when(allocationRepository.findByEmployeeForUpdate(1L)).thenReturn(Collections.emptyList());
        when(projectRepository.findById(20L)).thenReturn(Optional.of(completedProject));

        assertThatThrownBy(() -> allocationService.updateAllocation(100L, updateToCompleted))
                .isInstanceOf(InvalidProjectStatusException.class)
                .hasMessageContaining("COMPLETED");
    }

    // -----------------------------------------------------------------------
    // deleteAllocation tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-10: deleteAllocation - allocation not found throws AllocationNotFoundException")
    void deleteAllocation_notFound_throwsException() {
        when(allocationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> allocationService.deleteAllocation(999L))
                .isInstanceOf(AllocationNotFoundException.class)
                .hasMessageContaining("999");
    }
}
