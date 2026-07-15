package com.company.pram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUtilizationResponse {
    private Long employeeId;
    private String employeeCode;
    private String fullName;
    private Integer totalAllocation;
}
