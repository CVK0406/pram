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
import com.company.pram.mapper.AllocationMapper;
import com.company.pram.repository.AllocationRepository;
import com.company.pram.repository.EmployeeRepository;
import com.company.pram.repository.ProjectRepository;
import com.company.pram.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    private final AllocationRepository allocationRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public AllocationResponse createAllocation(AllocationRequest request) {
        // Lock the employee first to serialize transactions for this employee
        Employee employee = employeeRepository.findByIdForUpdate(request.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

        // Also lock existing allocations of this employee
        allocationRepository.findByEmployeeForUpdate(request.getEmployeeId());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));

        // Rule 3: Cannot allocate to a COMPLETED project
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new InvalidProjectStatusException("Cannot allocate to a COMPLETED project: " + project.getProjectCode());
        }

        // Rule 2: Total allocation must not exceed 100%
        LocalDate endDate = (request.getEndDate() != null) ? request.getEndDate() : LocalDate.of(9999, 12, 31);
        int overlappingTotal = allocationRepository.sumOverlappingAllocations(
                request.getEmployeeId(),
                request.getStartDate(),
                endDate,
                -1L
        );

        if (overlappingTotal + request.getAllocationPercent() > 100) {
            throw new AllocationExceededException("Employee allocation exceeds 100%");
        }

        Allocation allocation = AllocationMapper.toEntity(request, employee, project);
        Allocation savedAllocation = allocationRepository.save(allocation);
        return AllocationMapper.toResponse(savedAllocation);
    }

    @Override
    @Transactional
    public AllocationResponse updateAllocation(Long id, AllocationRequest request) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new AllocationNotFoundException(id));

        // Lock the employee first to serialize transactions for this employee
        Employee employee = employeeRepository.findByIdForUpdate(request.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

        // Also lock existing allocations of this employee
        allocationRepository.findByEmployeeForUpdate(request.getEmployeeId());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));

        // Rule 3: Cannot allocate to a COMPLETED project
        if (project.getStatus() == ProjectStatus.COMPLETED) {
            throw new InvalidProjectStatusException("Cannot allocate to a COMPLETED project: " + project.getProjectCode());
        }

        // Rule 2: Total allocation must not exceed 100% (exclude current allocation)
        LocalDate endDate = (request.getEndDate() != null) ? request.getEndDate() : LocalDate.of(9999, 12, 31);
        int overlappingTotal = allocationRepository.sumOverlappingAllocations(
                request.getEmployeeId(),
                request.getStartDate(),
                endDate,
                id
        );

        if (overlappingTotal + request.getAllocationPercent() > 100) {
            throw new AllocationExceededException("Employee allocation exceeds 100%");
        }

        allocation.setEmployee(employee);
        allocation.setProject(project);
        allocation.setAllocationPercent(request.getAllocationPercent());
        allocation.setRoleInProject(request.getRoleInProject());
        allocation.setStartDate(request.getStartDate());
        allocation.setEndDate(request.getEndDate());

        Allocation savedAllocation = allocationRepository.save(allocation);
        return AllocationMapper.toResponse(savedAllocation);
    }

    @Override
    @Transactional
    public void deleteAllocation(Long id) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new AllocationNotFoundException(id));
        
        allocation.setDeletedAt(LocalDateTime.now());
        allocationRepository.save(allocation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocationsByEmployee(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new EmployeeNotFoundException(employeeId);
        }
        return allocationRepository.findByEmployeeEmployeeIdAndDeletedAtIsNull(employeeId)
                .stream()
                .map(AllocationMapper::toResponse)
                .collect(Collectors.toList());
    }
}
