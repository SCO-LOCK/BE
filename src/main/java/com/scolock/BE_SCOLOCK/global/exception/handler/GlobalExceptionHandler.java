package com.scolock.BE_SCOLOCK.global.exception.handler;

import com.scolock.BE_SCOLOCK.global.apiPayload.ApiResponse;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.ErrorReasonDTO;
import com.scolock.BE_SCOLOCK.global.apiPayload.code.status.ErrorStatus;
import com.scolock.BE_SCOLOCK.global.exception.GeneralException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // HTTP 공통 오류만 명시적으로 매핑하여 도메인별 오류 코드와 구분한다.
    private static final Map<Integer, ErrorStatus> COMMON_HTTP_ERRORS = Map.ofEntries(
            Map.entry(400, ErrorStatus.BAD_REQUEST),
            Map.entry(401, ErrorStatus.UNAUTHORIZED),
            Map.entry(403, ErrorStatus.FORBIDDEN),
            Map.entry(404, ErrorStatus.NOT_FOUND),
            Map.entry(405, ErrorStatus.METHOD_NOT_ALLOWED),
            Map.entry(406, ErrorStatus.NOT_ACCEPTABLE),
            Map.entry(413, ErrorStatus.CONTENT_TOO_LARGE),
            Map.entry(415, ErrorStatus.UNSUPPORTED_MEDIA_TYPE),
            Map.entry(500, ErrorStatus.INTERNAL_SERVER_ERROR),
            Map.entry(503, ErrorStatus.SERVICE_UNAVAILABLE));

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException exception) {
        ErrorReasonDTO reason = exception.getErrorReasonHttpStatus();
        if (reason.getHttpStatus().is5xxServerError()) {
            log.error("Application request processing error", exception);
        }
        return ResponseEntity.status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> addError(
                errors, violation.getPropertyPath().toString(), violation.getMessage()));
        ErrorReasonDTO reason = ErrorStatus.BAD_REQUEST.getReasonHttpStatus();
        return ResponseEntity.status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(reason.getCode(), reason.getMessage(), errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        log.error("Unexpected request processing error", exception);
        ErrorReasonDTO reason = ErrorStatus.INTERNAL_SERVER_ERROR.getReasonHttpStatus();
        return ResponseEntity.status(reason.getHttpStatus())
                .body(ApiResponse.onFailure(reason.getCode(), reason.getMessage(), null));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ObjectError error : exception.getBindingResult().getAllErrors()) {
            String field = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            addError(errors, field, error.getDefaultMessage());
        }
        return handleExceptionInternal(exception, failure(status, errors), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // 반환값 검증 실패는 서버 오류이므로 내부 검증 내용을 응답에 노출하지 않는다.
        if (exception.isForReturnValue()) {
            return handleExceptionInternal(exception, null, headers, status, request);
        }

        Map<String, String> errors = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> {
            String parameterName = result.getMethodParameter().getParameterName();
            String parameter = parameterName != null ? parameterName
                    : "arg" + result.getMethodParameter().getParameterIndex();
            result.getResolvableErrors().forEach(error -> {
                String field = error instanceof FieldError fieldError ? fieldError.getField() : parameter;
                addError(errors, field, error.getDefaultMessage());
            });
        });
        exception.getCrossParameterValidationResults().forEach(error ->
                addError(errors, "parameters", error.getDefaultMessage()));
        return handleExceptionInternal(exception, failure(status, errors), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Spring MVC request processing error", exception);
        }
        Object response = body instanceof ApiResponse<?> ? body : failure(status, null);
        return super.handleExceptionInternal(exception, response, headers, status, request);
    }

    private <T> ApiResponse<T> failure(HttpStatusCode status, T data) {
        ErrorStatus commonError = COMMON_HTTP_ERRORS.get(status.value());
        if (commonError != null) {
            return ApiResponse.onFailure(commonError.getCode(), commonError.getMessage(), data);
        }
        String message = status.is5xxServerError()
                ? ErrorStatus.INTERNAL_SERVER_ERROR.getMessage() : "요청을 처리할 수 없습니다.";
        return ApiResponse.onFailure("COMMON" + status.value(), message, data);
    }

    private void addError(Map<String, String> errors, String field, String message) {
        String errorMessage = message != null ? message : "유효하지 않은 값입니다.";
        errors.merge(field, errorMessage, (existing, additional) -> existing + ", " + additional);
    }
}
