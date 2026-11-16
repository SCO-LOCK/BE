package com.scolock.BE_SCOLOCK.domain.analysis.dto;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "접수된 요청 사항 분석 정보")
public record AnalysisResponse(
        @Schema(description = "생성된 분석 ID", example = "1")
        Long analysisId
) {
    public static AnalysisResponse from(AnalysisRun analysis) {
        return new AnalysisResponse(
                analysis.getId()
        );
    }
}
