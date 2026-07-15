package com.company.pram.service.impl;

import com.company.pram.dto.request.ProjectRequest;
import com.company.pram.dto.response.ProjectResponse;
import com.company.pram.entity.Project;
import com.company.pram.entity.ProjectStatus;
import com.company.pram.exception.DuplicateResourceException;
import com.company.pram.exception.InvalidProjectStatusException;
import com.company.pram.exception.ProjectNotFoundException;
import com.company.pram.mapper.ProjectMapper;
import com.company.pram.repository.ProjectRepository;
import com.company.pram.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        if (projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new DuplicateResourceException("Project code already exists: " + request.getProjectCode());
        }

        Project project = ProjectMapper.toEntity(request);
        Project savedProject = projectRepository.save(project);
        return ProjectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        return ProjectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(ProjectStatus status, Pageable pageable) {
        if (status == null) {
            return projectRepository.findAll(pageable).map(ProjectMapper::toResponse);
        }
        return projectRepository.findByStatus(status, pageable).map(ProjectMapper::toResponse);
    }

    @Override
    @Transactional
    public ProjectResponse updateProjectStatus(Long id, ProjectStatus newStatus) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));

        ProjectStatus currentStatus = project.getStatus();

        if (currentStatus == newStatus) {
            return ProjectMapper.toResponse(project);
        }

        // Validate sequence PLANNING -> ACTIVE -> COMPLETED
        if (currentStatus == ProjectStatus.PLANNING && newStatus != ProjectStatus.ACTIVE) {
            throw new InvalidProjectStatusException("Invalid status transition from PLANNING to " + newStatus);
        } else if (currentStatus == ProjectStatus.ACTIVE && newStatus != ProjectStatus.COMPLETED) {
            throw new InvalidProjectStatusException("Invalid status transition from ACTIVE to " + newStatus);
        } else if (currentStatus == ProjectStatus.COMPLETED) {
            throw new InvalidProjectStatusException("Cannot transition out of COMPLETED status");
        }

        project.setStatus(newStatus);
        Project updatedProject = projectRepository.save(project);
        return ProjectMapper.toResponse(updatedProject);
    }
}
