package com.scolock.BE_SCOLOCK.domain.analysis.dto;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;

public record AnalysisResponse(
        Long analysisId
) {
    public static AnalysisResponse from(AnalysisRun analysis) {
        return new AnalysisResponse(
                analysis.getId()
        );
    }
}
