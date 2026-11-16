package com.scolock.BE_SCOLOCK.domain.analysis.service;

import com.scolock.BE_SCOLOCK.domain.analysis.ai.RequestAnalysisClient;
import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResult;
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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisPersistenceService {

    private final JsonMapper mapper;
    private final ProjectService projectService;
    private final ClientRequestRepository clientRequestRepository;
    private final CriteriaVersionRepository criteriaVersionRepository;
    private final AnalysisRunRepository analysisRunRepository;

    @Transactional
    public AnalysisInput start(Long projectId, Long requestId, Long userId) {
        Project project = projectService.getProjectById(projectId);
        if (!project.getOwner().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        ClientRequest request = clientRequestRepository.findByIdAndProjectId(requestId, projectId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CLIENT_REQUEST_NOT_FOUND));
        if (request.getRequestText() == null || request.getRequestText().isBlank()) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST);
        }
        if (analysisRunRepository.existsByClientRequestIdAndStatusIn(requestId,
                List.of(AnalysisStatus.PENDING, AnalysisStatus.PROCESSING))) {
            throw new GeneralException(ErrorStatus.ANALYSIS_IN_PROGRESS);
        }
        CriteriaVersion criteriaVersion = criteriaVersionRepository.findFirstByProjectIdOrderByVersionNoDesc(projectId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CRITERIA_VERSION_NOT_FOUND));

        AnalysisRun analysis = AnalysisRun.builder()
                .clientRequest(request)
                .criteriaVersion(criteriaVersion)
                .status(AnalysisStatus.PENDING)
                .promptVersion(RequestAnalysisClient.PROMPT_VERSION)
                .build();
        analysisRunRepository.save(analysis);
        return new AnalysisInput(analysis.getId(), criteriaVersion.getCriteriaText(), request.getRequestText());
    }

    @Transactional
    public void markProcessing(Long id) {
        AnalysisRun run = analysisRunRepository.findById(id).orElseThrow();
        run.startProcessing();
    }

    @Transactional
    public void complete(Long id, AnalysisResult result, String model) {
        AnalysisRun run = analysisRunRepository.findById(id).orElseThrow();
        run.complete(mapper.writeValueAsString(result), model);
    }

    @Transactional
    public void fail(Long id, String code, String message) {
        AnalysisRun run = analysisRunRepository.findById(id).orElseThrow();
        run.fail(code, message);
    }

    public record AnalysisInput(
            Long analysisId,
            String baseline,
            String requestText
    ) {
    }
}
