package com.scolock.BE_SCOLOCK.domain.analysis.controller;

import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisService;
import com.scolock.BE_SCOLOCK.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analysis", description = "AI 분석 조회 API")
@RestController
@RequiredArgsConstructor
public class AnalysisController {

    // TODO: 로그인 구현 후 인증된 사용자의 ID로 교체
    private static final Long TEMPORARY_USER_ID = 1L;
    private final AnalysisService analysisService;

    @Operation(summary = "AI 분석 조회",
            description = "분석 실행의 상태를 조회합니다. "
                    + "COMPLETED 상태일 때만 분석 결과를 반환합니다. "
                    + "로그인 구현 전까지 사용자 ID는 1로 고정합니다."
    )
    @GetMapping(value = "/analyses/{analysisId}", produces = "application/json")
    public ApiResponse<AnalysisResponse> getAnalysis(
            @Parameter(description = "분석 실행 ID", example = "1")
            @PathVariable("analysisId")
            @Positive(message = "분석 ID는 양수여야 합니다.")
            Long analysisId
    ) {
        AnalysisResponse response = analysisService.getAnalysis(
                analysisId,
                TEMPORARY_USER_ID
        );

        return ApiResponse.onSuccess(response);
    }
}