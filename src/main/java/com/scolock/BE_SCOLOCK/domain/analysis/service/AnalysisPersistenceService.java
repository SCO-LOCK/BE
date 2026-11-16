package com.scolock.BE_SCOLOCK.domain.analysis.service;

import com.scolock.BE_SCOLOCK.domain.analysis.ai.RequestAnalysisClient;
import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResult;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisItem;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisRunRepository;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisItemRepository;
import com.scolock.BE_SCOLOCK.domain.criteria.entity.CriteriaVersion;
import com.scolock.BE_SCOLOCK.domain.criteria.service.CriteriaVersionService;
import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.project.service.ProjectService;
import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import com.scolock.BE_SCOLOCK.domain.request.service.ClientRequestService;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import java.util.List;
import java.util.stream.IntStream;
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
    private final ClientRequestService clientRequestService;
    private final CriteriaVersionService criteriaVersionService;
    private final AnalysisRunRepository analysisRunRepository;
    private final AnalysisItemRepository analysisItemRepository;

    public AnalysisRun getAnalysisRunById(Long id) {
        return analysisRunRepository.findById(id)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ANALYSIS_NOT_FOUND));
    }

    @Transactional
    public AnalysisInput start(Long projectId, Long requestId, Long userId) {
        Project project = projectService.getProjectById(projectId);
        if (!project.getOwner().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        ClientRequest request = clientRequestService.getClientRequestByIdAndProjectId(requestId, projectId);
        if (request.getRequestText() == null || request.getRequestText().isBlank()) {
            throw new GeneralException(ErrorStatus.BAD_REQUEST);
        }
        if (analysisRunRepository.existsByClientRequestIdAndStatusIn(requestId,
                List.of(AnalysisStatus.PENDING, AnalysisStatus.PROCESSING))) {
            throw new GeneralException(ErrorStatus.ANALYSIS_IN_PROGRESS);
        }
        CriteriaVersion criteriaVersion = criteriaVersionService.getLatestCriteriaVersionByProjectId(projectId);

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
        AnalysisRun run = getAnalysisRunById(id);
        run.startProcessing();
    }

    @Transactional
    public void complete(Long id, AnalysisResult result, String model) {
        AnalysisRun run = getAnalysisRunById(id);
        List<AnalysisItem> items = IntStream.range(0, result.findings().size())
                .mapToObj(index -> {
                    AnalysisResult.Finding finding = result.findings().get(index);
                    return AnalysisItem.builder()
                            .analysisRun(run)
                            .itemOrder(index + 1)
                            .title(finding.title())
                            .requestExcerpt(finding.requestExcerpt())
                            .classification(finding.type())
                            .reason(finding.reason())
                            .criteriaEvidence(finding.existingBaseline())
                            .suggestedQuestion(finding.suggestedQuestion())
                            .build();
                })
                .toList();
        analysisItemRepository.saveAll(items);
        run.complete(mapper.writeValueAsString(result), model);
    }

    @Transactional
    public void fail(Long id, String code, String message) {
        AnalysisRun run = getAnalysisRunById(id);
        run.fail(code, message);
    }

    public record AnalysisInput(
            Long analysisId,
            String baseline,
            String requestText
    ) {
    }
}
