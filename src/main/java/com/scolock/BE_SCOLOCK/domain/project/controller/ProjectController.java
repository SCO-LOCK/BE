package com.scolock.BE_SCOLOCK.domain.project.controller;

import com.scolock.BE_SCOLOCK.domain.project.dto.CreateProjectRequest;
import com.scolock.BE_SCOLOCK.domain.project.dto.CreateProjectResponse;
import com.scolock.BE_SCOLOCK.domain.project.service.ProjectService;
import com.scolock.BE_SCOLOCK.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Project", description = "프로젝트 관련 API")
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    // TODO: 로그인 구현 후 인증된 사용자의 ID로 교체
    private static final Long TEMPORARY_USER_ID = 1L;

    private final ProjectService projectService;

    @Operation(summary = "프로젝트 생성",
            description = "프로젝트와 첫 번째 제작 기준 버전을 저장합니다. "
                    + "현재는 제작 기준에 대해 텍스트 입력만 지원합니다. "
                    + "로그인 구현 전까지 사용자 ID를 1로 고정합니다. ")

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateProjectResponse> create(
            @Valid @RequestBody CreateProjectRequest input) {
        return ApiResponse.onCreated(
                projectService.create(TEMPORARY_USER_ID, input));
    }
}
