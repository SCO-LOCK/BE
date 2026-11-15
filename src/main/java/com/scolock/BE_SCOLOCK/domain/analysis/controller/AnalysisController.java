package com.scolock.BE_SCOLOCK.domain.analysis.controller;

import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisService;
import com.scolock.BE_SCOLOCK.global.apiPayload.ApiResponse;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnalysisController {

    // TODO: 로그인 구현 후 인증된 사용자의 ID로 교체
    private static final Long TEMPORARY_USER_ID = 1L;

    private final AnalysisService analysisService;

    @PostMapping("/projects/{projectId}/requests/{requestId}/analyses")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<AnalysisResponse> create(
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @PathVariable @Positive(message = "요청 사항 ID는 양수여야 합니다.") Long requestId) {
        return ApiResponse.onAccepted(analysisService.create(projectId, requestId, TEMPORARY_USER_ID));
    }
}
