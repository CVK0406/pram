package com.company.pram.dto.response;

import com.company.pram.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long projectId;
    private String projectCode;
    private String projectName;
    private String customer;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
}
