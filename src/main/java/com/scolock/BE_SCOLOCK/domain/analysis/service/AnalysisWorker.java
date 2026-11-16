package com.scolock.BE_SCOLOCK.domain.analysis.service;

import com.scolock.BE_SCOLOCK.domain.analysis.ai.AnalysisAiException;
import com.scolock.BE_SCOLOCK.domain.analysis.ai.RequestAnalysisClient;
import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResult;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisPersistenceService.AnalysisInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisWorker {

    private final AnalysisPersistenceService analysisPersistenceService;
    private final RequestAnalysisClient requestAnalysisClient;

    @Async("analysisExecutor")
    public void analyze(AnalysisInput input) {
        try {
            analysisPersistenceService.markProcessing(input.analysisId());
            AnalysisResult result = requestAnalysisClient.analyze(input.baseline(), input.requestText());
            analysisPersistenceService.complete(input.analysisId(), result, requestAnalysisClient.modelName());
        } catch (AnalysisAiException e) {
            analysisPersistenceService.fail(input.analysisId(), e.getCode(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("분석 처리 실패: analysisId={}", input.analysisId(), e);
            analysisPersistenceService.fail(input.analysisId(), "ANALYSIS_ERROR", "분석 처리 중 오류가 발생했습니다.");
        }
    }
}
