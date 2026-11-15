package com.scolock.BE_SCOLOCK.domain.analysis.service;

import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisRunRepository;
import com.scolock.BE_SCOLOCK.domain.criteria.entity.CriteriaVersion;
import com.scolock.BE_SCOLOCK.domain.criteria.repository.CriteriaVersionRepository;
import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.project.service.ProjectService;
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
public class AnalysisService {

    private final ProjectService projectService;
    private final ClientRequestRepository clientRequestRepository;
    private final CriteriaVersionRepository criteriaVersionRepository;
    private final AnalysisRunRepository analysisRunRepository;

    @Transactional
    public AnalysisResponse create(Long projectId, Long requestId, Long userId) {
        Project project = projectService.getProjectById(projectId);
        if (!project.getOwner().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        ClientRequest request = clientRequestRepository.findByIdAndProjectId(requestId, projectId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLIENT_REQUEST_NOT_FOUND));
        CriteriaVersion criteriaVersion = criteriaVersionRepository.findFirstByProjectIdOrderByVersionNoDesc(projectId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CRITERIA_VERSION_NOT_FOUND));

        AnalysisRun analysis = AnalysisRun.builder()
                .clientRequest(request)
                .criteriaVersion(criteriaVersion)
                .status(AnalysisStatus.PENDING)
                .build();
        return AnalysisResponse.from(analysisRunRepository.save(analysis));
    }
}
