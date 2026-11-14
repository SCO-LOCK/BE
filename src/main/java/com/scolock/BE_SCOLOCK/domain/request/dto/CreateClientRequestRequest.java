package com.scolock.BE_SCOLOCK.domain.request.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "클라이언트 요청 사항 등록 입력")
public record CreateClientRequestRequest(
        @Schema(description = "요청 사항 내용. 빈 문자열이나 공백만 입력할 수 없습니다.",
                example = "메인 화면의 버튼 색상을 변경해 주세요.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "요청 사항 내용을 입력해 주세요.")
        String requestText
) {
}
