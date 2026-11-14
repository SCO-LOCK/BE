package com.scolock.BE_SCOLOCK.global.apiPayload;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonPropertyOrder({"code", "message", "data"})
@AllArgsConstructor
@Getter
@Schema(description = "공통 API 응답")
public class ApiResponse<T> {

    @Schema(description = "응답 코드", example = "COMMON201")
    private String code;
    @Schema(description = "응답 메시지", example = "생성이 완료되었습니다.")
    private String message;
    @Schema(description = "성공 시 결과 데이터, 검증 실패 시 필드별 오류 메시지. 데이터가 없으면 null입니다.")
    private T data;

    public static <T> ApiResponse<T> onSuccess(T data) {
        return new ApiResponse<>(SuccessStatus.OK.getCode(), SuccessStatus.OK.getMessage(), data);
    }

    public static <T> ApiResponse<T> onCreated(T data) {
        return new ApiResponse<>(SuccessStatus.CREATED.getCode(), SuccessStatus.CREATED.getMessage(), data);
    }

    public static <T> ApiResponse<T> onFailure(String code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
}
