package com.scolock.BE_SCOLOCK.domain.request.service;

import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.project.service.ProjectService;
import com.scolock.BE_SCOLOCK.domain.request.dto.ClientRequestResponse;
import com.scolock.BE_SCOLOCK.domain.request.dto.CreateClientRequestRequest;
import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import com.scolock.BE_SCOLOCK.domain.request.repository.ClientRequestRepository;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientRequestService {

    private final ProjectService projectService;
    private final ClientRequestRepository clientRequestRepository;

    @Transactional
    public ClientRequestResponse create(Long projectId, Long userId, CreateClientRequestRequest input) {
        Project project = projectService.getProjectById(projectId);
        if (!project.getOwner().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        ClientRequest request = ClientRequest.builder()
                .project(project)
                .requestText(input.requestText())
                .build();
        return ClientRequestResponse.from(clientRequestRepository.save(request));
    }
}
