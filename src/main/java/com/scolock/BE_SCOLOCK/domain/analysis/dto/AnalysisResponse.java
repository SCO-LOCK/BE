package com.scolock.BE_SCOLOCK.domain.analysis.dto;

import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisItem;
import com.scolock.BE_SCOLOCK.domain.analysis.entity.AnalysisRun;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisClassification;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Comparator;
import java.util.List;

@Schema(description = "AI 분석 조회 응답")
public record AnalysisResponse(
        Long id,
        AnalysisStatus status,
        Long reviewCount,
        List<ItemResponse> items,
        String errorCode,// 분석 실패시 공개용 오류 코드
        String message // 분석 실패시 공개용 오류 메시지
) {

    public static AnalysisResponse from(
            AnalysisRun run,
            List<AnalysisItem> analysisItems
    ) {
        // 성공과 실패 상태를 나누었음.
        boolean completed = run.getStatus() == AnalysisStatus.COMPLETED;
        boolean failed = run.getStatus() == AnalysisStatus.FAILED;

        List<ItemResponse> items = completed
                ? analysisItems.stream()
                .sorted(Comparator.comparing(AnalysisItem::getItemOrder))
                .map(ItemResponse::from)
                .toList()
                : List.of();

        return new AnalysisResponse(
                run.getId(),
                run.getStatus(),
                (long) items.size(),
                items,
                failed ? "ANALYSIS_EXECUTION_FAILED" : null,
                failed ? "분석을 완료하지 못했습니다." : null
        );
    }

    @Schema(description = "AI 분석 세부 항목")
    public record ItemResponse(
            Long id,
            Integer itemOrder, //항목 표시 순서. 1부터 오름차순
            String title,
            AnalysisClassification classification,
            String criteriaEvidence,
            String requestExcerpt,
            String reason,
            String suggestedQuestion
    ) {

        public static ItemResponse from(AnalysisItem item) {
            return new ItemResponse(
                    item.getId(),
                    item.getItemOrder(),
                    item.getTitle(),
                    item.getClassification(),
                    item.getCriteriaEvidence(),
                    item.getRequestExcerpt(),
                    item.getReason(),
                    item.getSuggestedQuestion()
            );
        }
    }
}