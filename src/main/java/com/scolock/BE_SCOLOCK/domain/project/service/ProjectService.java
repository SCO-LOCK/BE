package com.scolock.BE_SCOLOCK.domain.project.service;

import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.project.repository.ProjectRepository;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROJECT_NOT_FOUND));
    }
}
