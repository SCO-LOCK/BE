package com.scolock.BE_SCOLOCK.domain.request.controller;

import com.scolock.BE_SCOLOCK.domain.request.dto.ClientRequestResponse;
import com.scolock.BE_SCOLOCK.domain.request.dto.CreateClientRequestRequest;
import com.scolock.BE_SCOLOCK.domain.request.service.ClientRequestService;
import com.scolock.BE_SCOLOCK.global.apiPayload.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/requests")
@RequiredArgsConstructor
public class ClientRequestController {

    // TODO: 로그인 구현 후 인증된 사용자의 ID로 교체
    private static final Long TEMPORARY_USER_ID = 1L;

    private final ClientRequestService clientRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClientRequestResponse> create(
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @Valid @RequestBody CreateClientRequestRequest input) {
        return ApiResponse.onCreated(clientRequestService.create(projectId, TEMPORARY_USER_ID, input));
    }
}
