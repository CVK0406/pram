package com.company.pram.service;

import com.company.pram.dto.request.ProjectRequest;
import com.company.pram.dto.response.ProjectResponse;
import com.company.pram.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse getProjectById(Long id);
    Page<ProjectResponse> getProjects(ProjectStatus status, Pageable pageable);
    ProjectResponse updateProjectStatus(Long id, ProjectStatus newStatus);
}
