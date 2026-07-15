package com.company.pram.repository;

import com.company.pram.entity.Project;
import com.company.pram.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    boolean existsByProjectCode(String projectCode);
    Page<Project> findByStatus(ProjectStatus status, Pageable pageable);
}
