package com.scolock.BE_SCOLOCK.domain.analysis.service;

import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.dto.CreateAnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisItem;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisItemRepository;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisRunRepository;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisPersistenceService.AnalysisInput;
import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisPersistenceService analysisPersistenceService;
    private final AnalysisWorker analysisWorker;
    private final AnalysisRunRepository analysisRunRepository;
    private final AnalysisItemRepository analysisItemRepository;

    public CreateAnalysisResponse create(Long projectId, Long requestId, Long userId) {
        AnalysisInput analysisInput = analysisPersistenceService.start(projectId, requestId, userId);
        try {
            analysisWorker.analyze(analysisInput);
        } catch (TaskRejectedException e) {
            analysisPersistenceService.fail(analysisInput.analysisId(), "ANALYSIS_UNAVAILABLE", "분석 작업을 시작하지 못했습니다.");
            throw new GeneralException(ErrorStatus.SERVICE_UNAVAILABLE);
        }
        return new CreateAnalysisResponse(analysisInput.analysisId());
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysis(Long analysisId, Long userId) {
        // 분석 존재 확인(분석 실행 조회)
        AnalysisRun run = analysisRunRepository.findById(analysisId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.ANALYSIS_NOT_FOUND));

        // 분석에 연결된 프로젝트의 소유권 확인
        Project project = run.getClientRequest().getProject();
        if (!project.getOwner().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        // 분석이 완료된 경우에만 분석 결과를 조회
        List<AnalysisItem> items = run.getStatus() == AnalysisStatus.COMPLETED
                ? analysisItemRepository.findAllByAnalysisRunIdOrderByItemOrderAsc(run.getId())
                : List.of();

        // 현재 상태와 결과 반환
        return AnalysisResponse.from(run, items);
    }
}
