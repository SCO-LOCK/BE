package com.scolock.BE_SCOLOCK.domain.analysis.service;

import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisPersistenceService.AnalysisInput;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisPersistenceService analysisPersistenceService;
    private final AnalysisWorker analysisWorker;

    public AnalysisResponse create(Long projectId, Long requestId, Long userId) {
        AnalysisInput analysisInput = analysisPersistenceService.start(projectId, requestId, userId);
        try {
            analysisWorker.analyze(analysisInput);
        } catch (TaskRejectedException e) {
            analysisPersistenceService.fail(analysisInput.analysisId(), "ANALYSIS_UNAVAILABLE", "분석 작업을 시작하지 못했습니다.");
            throw new GeneralException(ErrorStatus.SERVICE_UNAVAILABLE);
        }
        return new AnalysisResponse(analysisInput.analysisId());
    }
}
