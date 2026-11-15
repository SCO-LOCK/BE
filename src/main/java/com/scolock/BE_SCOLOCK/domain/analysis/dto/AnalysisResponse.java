package com.scolock.BE_SCOLOCK.domain.analysis.dto;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;

public record AnalysisResponse(
        Long analysisId,
        AnalysisStatus status
) {
    public static AnalysisResponse from(AnalysisRun analysis) {
        return new AnalysisResponse(
                analysis.getId(),
                analysis.getStatus()
        );
    }
}
