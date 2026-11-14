package com.scolock.BE_SCOLOCK.domain.request.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateClientRequestRequest(
        @NotBlank(message = "요청 사항 내용을 입력해 주세요.")
        String requestText
) {
}
