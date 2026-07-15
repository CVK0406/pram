package com.company.pram.mapper;

import com.company.pram.dto.request.AllocationRequest;
import com.company.pram.dto.response.AllocationResponse;
import com.company.pram.entity.Allocation;
import com.company.pram.entity.Employee;
import com.company.pram.entity.Project;

public class AllocationMapper {

    public static Allocation toEntity(AllocationRequest request, Employee employee, Project project) {
        if (request == null) {
            return null;
        }
        return Allocation.builder()
                .employee(employee)
                .project(project)
                .allocationPercent(request.getAllocationPercent())
                .roleInProject(request.getRoleInProject())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();
    }

    public static AllocationResponse toResponse(Allocation allocation) {
        if (allocation == null) {
            return null;
        }
        return AllocationResponse.builder()
                .allocationId(allocation.getAllocationId())
                .employeeId(allocation.getEmployee() != null ? allocation.getEmployee().getEmployeeId() : null)
                .employeeName(allocation.getEmployee() != null ? allocation.getEmployee().getFullName() : null)
                .projectId(allocation.getProject() != null ? allocation.getProject().getProjectId() : null)
                .projectCode(allocation.getProject() != null ? allocation.getProject().getProjectCode() : null)
                .allocationPercent(allocation.getAllocationPercent())
                .roleInProject(allocation.getRoleInProject())
                .startDate(allocation.getStartDate())
                .endDate(allocation.getEndDate())
                .build();
    }
}
