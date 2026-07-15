package com.company.pram.service.impl;

import com.company.pram.dto.response.*;
import com.company.pram.entity.Allocation;
import com.company.pram.entity.Employee;
import com.company.pram.exception.EmployeeNotFoundException;
import com.company.pram.repository.AllocationRepository;
import com.company.pram.repository.EmployeeRepository;
import com.company.pram.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final EmployeeRepository employeeRepository;
    private final AllocationRepository allocationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeUtilizationResponse> getEmployeeUtilization() {
        List<Employee> employees = employeeRepository.findAll();
        List<Allocation> activeAllocations = allocationRepository.findActiveAllocations(LocalDate.now());

        Map<Long, Integer> utilizationMap = activeAllocations.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getEmployee().getEmployeeId(),
                        Collectors.summingInt(Allocation::getAllocationPercent)
                ));

        return employees.stream()
                .map(e -> EmployeeUtilizationResponse.builder()
                        .employeeId(e.getEmployeeId())
                        .employeeCode(e.getEmployeeCode())
                        .fullName(e.getFullName())
                        .totalAllocation(utilizationMap.getOrDefault(e.getEmployeeId(), 0))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailableResourceResponse> getAvailableResources(Integer minAvailable) {
        List<Employee> employees = employeeRepository.findAll();
        List<Allocation> activeAllocations = allocationRepository.findActiveAllocations(LocalDate.now());

        Map<Long, Integer> utilizationMap = activeAllocations.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getEmployee().getEmployeeId(),
                        Collectors.summingInt(Allocation::getAllocationPercent)
                ));

        int minAvail = minAvailable != null ? minAvailable : 1;

        return employees.stream()
                .map(e -> {
                    int used = utilizationMap.getOrDefault(e.getEmployeeId(), 0);
                    return AvailableResourceResponse.builder()
                            .employeeId(e.getEmployeeId())
                            .fullName(e.getFullName())
                            .role(e.getRole())
                            .available(100 - used)
                            .build();
                })
                .filter(r -> r.getAvailable() >= minAvail)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OverloadedEmployeeResponse> getOverloadedEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        List<Allocation> activeAllocations = allocationRepository.findActiveAllocations(LocalDate.now());

        Map<Long, Integer> utilizationMap = activeAllocations.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getEmployee().getEmployeeId(),
                        Collectors.summingInt(Allocation::getAllocationPercent)
                ));

        return employees.stream()
                .map(e -> {
                    int total = utilizationMap.getOrDefault(e.getEmployeeId(), 0);
                    return OverloadedEmployeeResponse.builder()
                            .employeeId(e.getEmployeeId())
                            .fullName(e.getFullName())
                            .totalAllocation(total)
                            .build();
                })
                .filter(r -> r.getTotalAllocation() > 90)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeWorkloadResponse getEmployeeWorkload(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        LocalDate now = LocalDate.now();
        List<Allocation> activeAllocations = allocationRepository.findByEmployeeEmployeeIdAndDeletedAtIsNull(employeeId)
                .stream()
                .filter(a -> a.getStartDate().compareTo(now) <= 0 && (a.getEndDate() == null || a.getEndDate().compareTo(now) >= 0))
                .collect(Collectors.toList());

        int total = activeAllocations.stream()
                .mapToInt(Allocation::getAllocationPercent)
                .sum();

        List<WorkloadAllocationDto> workloadAllocations = activeAllocations.stream()
                .map(a -> WorkloadAllocationDto.builder()
                        .projectCode(a.getProject().getProjectCode())
                        .allocationPercent(a.getAllocationPercent())
                        .roleInProject(a.getRoleInProject())
                        .build())
                .collect(Collectors.toList());

        return EmployeeWorkloadResponse.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getFullName())
                .totalAllocation(total)
                .available(100 - total)
                .allocations(workloadAllocations)
                .build();
    }
}
