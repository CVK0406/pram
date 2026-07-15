package com.company.pram.service.impl;

import com.company.pram.dto.response.DashboardResponse;
import com.company.pram.repository.AllocationRepository;
import com.company.pram.repository.EmployeeRepository;
import com.company.pram.repository.ProjectRepository;
import com.company.pram.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final AllocationRepository allocationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        long totalEmployees = employeeRepository.count();
        long totalProjects = projectRepository.count();
        long activeAllocations = allocationRepository.countActiveAllocations(LocalDate.now());

        return DashboardResponse.builder()
                .totalEmployees(totalEmployees)
                .totalProjects(totalProjects)
                .activeAllocations(activeAllocations)
                .build();
    }
}
