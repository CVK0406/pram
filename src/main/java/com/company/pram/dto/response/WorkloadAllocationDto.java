package com.company.pram.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkloadAllocationDto {
    private String projectCode;
    private Integer allocationPercent;
    private String roleInProject;
}
