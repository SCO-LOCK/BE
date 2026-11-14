package com.scolock.BE_SCOLOCK.global.apiPayload;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonPropertyOrder({"code", "message", "data"})
@AllArgsConstructor
@Getter
public class ApiResponse<T> {

    private String code;
    private String message;
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
