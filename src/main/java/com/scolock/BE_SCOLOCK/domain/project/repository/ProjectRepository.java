package com.scolock.BE_SCOLOCK.domain.project.repository;

import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
