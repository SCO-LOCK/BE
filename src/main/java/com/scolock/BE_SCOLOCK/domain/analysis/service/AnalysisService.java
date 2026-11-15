package com.scolock.BE_SCOLOCK.domain.analysis.service;

import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisItem;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisItemRepository;
import com.scolock.BE_SCOLOCK.domain.analysis.repository.AnalysisRunRepository;
import com.scolock.BE_SCOLOCK.domain.project.entity.Project;
import com.scolock.BE_SCOLOCK.domain.project.service.ProjectService;
import com.scolock.BE_SCOLOCK.domain.request.repository.ClientRequestRepository;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

    private final ProjectService projectService;
    private final ClientRequestRepository clientRequestRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final AnalysisItemRepository analysisItemRepository;

    public AnalysisResponse getAnalysis(
            Long analysisId,
            Long userId
    ) {
        // 분석 존재 확인
        AnalysisRun run = analysisRunRepository.findById(analysisId)
                .orElseThrow(() -> new GeneralException(
                        ErrorStatus.ANALYSIS_NOT_FOUND
                ));

        // 분석에 연결된 프로젝트의 소유권 확인
        Project project = run.getClientRequest().getProject();
        if (!project.getOwner().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        // 해당 실행이 완료되었는지 확인
        if (run.getStatus() != AnalysisStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.ANALYSIS_RESULT_NOT_READY);
        }

        // 분석 결과 조회
        List<AnalysisItem> items = analysisItemRepository
                .findAllByAnalysisRun_IdOrderByItemOrderAsc(run.getId());

        return AnalysisResponse.from(run, items);
    }
}