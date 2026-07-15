package com.company.pram.service.impl;

import com.company.pram.dto.request.ProjectRequest;
import com.company.pram.dto.response.ProjectResponse;
import com.company.pram.entity.Project;
import com.company.pram.entity.ProjectStatus;
import com.company.pram.exception.DuplicateResourceException;
import com.company.pram.exception.InvalidProjectStatusException;
import com.company.pram.exception.ProjectNotFoundException;
import com.company.pram.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project planningProject;
    private Project completedProject;
    private ProjectRequest createRequest;

    @BeforeEach
    void setUp() {
        planningProject = Project.builder()
                .projectId(1L)
                .projectCode("NCG")
                .projectName("NCG Platform")
                .customer("NCG Corp")
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.PLANNING)
                .build();


        completedProject = Project.builder()
                .projectId(3L)
                .projectCode("OLD")
                .projectName("Old Project")
                .customer("Old Corp")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .status(ProjectStatus.COMPLETED)
                .build();

        createRequest = ProjectRequest.builder()
                .projectCode("NEW01")
                .projectName("New Project")
                .customer("New Corp")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.PLANNING)
                .build();
    }

    @Test
    @DisplayName("TC-01: createProject - success returns ProjectResponse")
    void createProject_success() {
        Project savedProject = Project.builder()
                .projectId(99L)
                .projectCode("NEW01")
                .projectName("New Project")
                .customer("New Corp")
                .startDate(LocalDate.of(2026, 9, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.PLANNING)
                .build();

        when(projectRepository.existsByProjectCode("NEW01")).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectResponse response = projectService.createProject(createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getProjectCode()).isEqualTo("NEW01");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.PLANNING);
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("TC-02: createProject - duplicate projectCode throws DuplicateResourceException (409)")
    void createProject_duplicateCode_throwsException() {
        when(projectRepository.existsByProjectCode("NEW01")).thenReturn(true);

        assertThatThrownBy(() -> projectService.createProject(createRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("NEW01");

        verify(projectRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-03: updateProjectStatus - PLANNING -> ACTIVE succeeds")
    void updateStatus_planningToActive_succeeds() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(planningProject));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        ProjectResponse response = projectService.updateProjectStatus(1L, ProjectStatus.ACTIVE);

        assertThat(response.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    }

    @Test
    @DisplayName("TC-04: updateProjectStatus - PLANNING -> COMPLETED throws InvalidProjectStatusException")
    void updateStatus_planningToCompleted_throwsException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(planningProject));

        assertThatThrownBy(() -> projectService.updateProjectStatus(1L, ProjectStatus.COMPLETED))
                .isInstanceOf(InvalidProjectStatusException.class)
                .hasMessageContaining("PLANNING");
    }

    @Test
    @DisplayName("TC-05: updateProjectStatus - COMPLETED -> any throws InvalidProjectStatusException")
    void updateStatus_fromCompleted_throwsException() {
        when(projectRepository.findById(3L)).thenReturn(Optional.of(completedProject));

        assertThatThrownBy(() -> projectService.updateProjectStatus(3L, ProjectStatus.ACTIVE))
                .isInstanceOf(InvalidProjectStatusException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("TC-06: getProjectById - not found throws ProjectNotFoundException (404)")
    void getProjectById_notFound_throwsException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectById(999L))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessageContaining("999");
    }
}
