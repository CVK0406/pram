package com.company.pram.mapper;

import com.company.pram.dto.request.EmployeeRequest;
import com.company.pram.dto.response.EmployeeResponse;
import com.company.pram.entity.Employee;

public class EmployeeMapper {

    public static Employee toEntity(EmployeeRequest request) {
        if (request == null) {
            return null;
        }
        return Employee.builder()
                .employeeCode(request.getEmployeeCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(request.getRole())
                .department(request.getDepartment())
                .build();
    }

    public static EmployeeResponse toResponse(Employee employee) {
        if (employee == null) {
            return null;
        }
        return EmployeeResponse.builder()
                .employeeId(employee.getEmployeeId())
                .employeeCode(employee.getEmployeeCode())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .role(employee.getRole())
                .department(employee.getDepartment())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }
}
