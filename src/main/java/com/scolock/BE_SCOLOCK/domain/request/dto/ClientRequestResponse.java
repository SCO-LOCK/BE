package com.scolock.BE_SCOLOCK.domain.request.dto;

import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "등록된 클라이언트 요청 사항")
public record ClientRequestResponse(
        @Schema(description = "요청 사항 등록 일시", example = "2026-11-14T15:00:00")
        LocalDateTime createdAt,
        @Schema(description = "등록된 요청 사항 내용", example = "메인 화면의 버튼 색상을 변경해 주세요.")
        String requestText,
        @Schema(description = "요청 사항이 속한 프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "생성된 클라이언트 요청 사항 ID", example = "1")
        Long id
) {
    public static ClientRequestResponse from(ClientRequest request) {
        return new ClientRequestResponse(
                request.getCreatedAt(),
                request.getRequestText(),
                request.getProject().getId(),
                request.getId()
        );
    }
}
