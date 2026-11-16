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
        @Schema(description = "분석 ID", example = "1")
        Long id,
        @Schema(description = "현재 분석 상태", example = "COMPLETED")
        AnalysisStatus status,
        @Schema(description = "반환된 확인 사항 수. 분석 완료 전이나 실패 시 0입니다.", example = "1")
        Long reviewCount,
        @Schema(description = "표시 순서로 정렬된 확인 사항. 완료 상태가 아니거나 확인 사항이 없으면 빈 배열입니다.")
        List<ItemResponse> items,
        @Schema(description = "실패 시 공개용 오류 코드. 그 외 상태에서는 null입니다.", example = "ANALYSIS_EXECUTION_FAILED", nullable = true)
        String errorCode,
        @Schema(description = "실패 시 공개용 오류 메시지. 그 외 상태에서는 null입니다.", example = "분석을 완료하지 못했습니다.", nullable = true)
        String message
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
            @Schema(description = "항목 ID", example = "1")
            Long id,
            @Schema(description = "표시 순서 (1부터 오름차순)", example = "1")
            Integer itemOrder,
            @Schema(description = "확인 사항 제목", example = "버튼 색상 확인")
            String title,
            @Schema(description = "확인 사항 유형", example = "MISSING_INFO")
            AnalysisClassification classification,
            @Schema(description = "관련 제작 기준. 해당 기준이 없으면 null입니다.", example = "기존 버튼 색상은 파란색입니다.", nullable = true)
            String criteriaEvidence,
            @Schema(description = "클라이언트 요청 원문 인용", example = "버튼 색상을 변경해 주세요.")
            String requestExcerpt,
            @Schema(description = "확인이 필요한 이유", example = "변경할 색상이 지정되지 않았습니다.")
            String reason,
            @Schema(description = "클라이언트에게 전달할 질문 초안", example = "버튼을 어떤 색상으로 변경할까요?")
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
