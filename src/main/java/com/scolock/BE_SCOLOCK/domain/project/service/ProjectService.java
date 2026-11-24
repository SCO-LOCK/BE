package com.scolock.BE_SCOLOCK.domain.project.service;

import com.scolock.BE_SCOLOCK.domain.criteria.entity.CriteriaVersion;
import com.scolock.BE_SCOLOCK.domain.criteria.repository.CriteriaVersionRepository;
import com.scolock.BE_SCOLOCK.domain.project.dto.CreateProjectRequest;
import com.scolock.BE_SCOLOCK.domain.project.dto.CreateProjectResponse;
import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.project.repository.ProjectRepository;
import com.scolock.BE_SCOLOCK.domain.user.entity.User;
import com.scolock.BE_SCOLOCK.domain.user.repository.UserRepository;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CriteriaVersionRepository criteriaVersionRepository;

    public Project getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PROJECT_NOT_FOUND));
    }

    @Transactional
    public CreateProjectResponse create(Long userId, CreateProjectRequest input) {
        boolean hasText = StringUtils.hasText(input.criteriaText());
        boolean hasFile = input.file() != null && !input.file().isEmpty();

        // 기존 제작 기준인 텍스트나 파일 중 하나라도 있어야함.
        if (!hasText || !hasFile) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST);
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOT_FOUND));

        Project project = projectRepository.save(Project.builder()
                .owner(owner)
                .name(input.name().strip())
                .clientName(input.clientName() == null ? null : input.clientName().strip())
                .build());

        CriteriaVersion version = criteriaVersionRepository.save(
                CriteriaVersion.builder()
                        .project(project)
                        .versionNo(1) // 기본적으로 1
                        .criteriaText(input.criteriaText()) // 텍스트로 작성된 제작 기준을 사용.
                        .build()
        );

        return new CreateProjectResponse(project.getId(), version.getId());
    }
}
