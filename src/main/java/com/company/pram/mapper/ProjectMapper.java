package com.company.pram.mapper;

import com.company.pram.dto.request.ProjectRequest;
import com.company.pram.dto.response.ProjectResponse;
import com.company.pram.entity.Project;
import com.company.pram.entity.ProjectStatus;

public class ProjectMapper {

    public static Project toEntity(ProjectRequest request) {
        if (request == null) {
            return null;
        }
        return Project.builder()
                .projectCode(request.getProjectCode())
                .projectName(request.getProjectName())
                .customer(request.getCustomer())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNING)
                .build();
    }

    public static ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }
        return ProjectResponse.builder()
                .projectId(project.getProjectId())
                .projectCode(project.getProjectCode())
                .projectName(project.getProjectName())
                .customer(project.getCustomer())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .build();
    }
}
