package com.company.pram.service.impl;

import com.company.pram.dto.request.AllocationRequest;
import com.company.pram.dto.response.AllocationResponse;
import com.company.pram.entity.Allocation;
import com.company.pram.entity.Employee;
import com.company.pram.entity.Project;
import com.company.pram.exception.AllocationNotFoundException;
import com.company.pram.exception.EmployeeNotFoundException;
import com.company.pram.exception.ProjectNotFoundException;
import com.company.pram.mapper.AllocationMapper;
import com.company.pram.repository.AllocationRepository;
import com.company.pram.repository.EmployeeRepository;
import com.company.pram.repository.ProjectRepository;
import com.company.pram.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));

        Allocation allocation = AllocationMapper.toEntity(request, employee, project);
        Allocation savedAllocation = allocationRepository.save(allocation);
        return AllocationMapper.toResponse(savedAllocation);
    }

    @Override
    @Transactional
    public AllocationResponse updateAllocation(Long id, AllocationRequest request) {
        Allocation allocation = allocationRepository.findById(id)
                .orElseThrow(() -> new AllocationNotFoundException(id));

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(request.getEmployeeId()));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ProjectNotFoundException(request.getProjectId()));

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
