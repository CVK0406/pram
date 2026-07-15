package com.company.pram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeWorkloadResponse {
    private Long employeeId;
    private String employeeName;
    private Integer totalAllocation;
    private Integer available;
    private List<WorkloadAllocationDto> allocations;
}
