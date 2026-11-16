package com.scolock.BE_SCOLOCK.domain.analysis.controller;

import com.scolock.BE_SCOLOCK.domain.analysis.dto.AnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.dto.CreateAnalysisResponse;
import com.scolock.BE_SCOLOCK.domain.analysis.service.AnalysisService;
import com.scolock.BE_SCOLOCK.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Tag(name = "Analyses", description = "프로젝트별 요청 사항 분석 API")
    @Operation(summary = "요청 사항 분석 생성",
            description = "로그인 구현 전까지 사용자 ID를 1로 고정합니다. "
                    + "ID 1 사용자가 소유한 프로젝트의 최신 제작 기준으로 요청 사항을 비동기 분석합니다. "
                    + "요청 본문은 필요하지 않으며, 분석 완료를 기다리지 않고 생성된 분석 ID를 반환합니다. "
                    + "동일한 요청 사항에 대기 중이거나 진행 중이거나 완료된 분석이 있으면 새 분석을 생성할 수 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202",
                    description = "분석 요청 접수 성공 (분석 완료를 의미하지 않음)", useReturnTypeSchema = true,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {"code":"COMMON202","message":"요청이 접수되었습니다.","data":{"analysisId":1}}
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "유효하지 않은 프로젝트/요청 사항 ID 또는 저장된 요청 내용이 비어 있음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "ID 양수 검증 실패", value = """
                                            {"code":"COMMON400","message":"잘못된 요청입니다.",
                                             "data":{"projectId":"프로젝트 ID는 양수여야 합니다."}}
                                            """),
                                    @ExampleObject(name = "잘못된 ID 형식 또는 빈 요청 내용", value = """
                                            {"code":"COMMON400","message":"잘못된 요청입니다.","data":null}
                                            """)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
                    description = "ID 1 사용자의 프로젝트가 아님",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON403","message":"접근 권한이 없습니다.","data":null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "프로젝트/요청 사항/제작 기준이 없거나 요청 사항이 해당 프로젝트에 속하지 않음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "프로젝트 없음", value = """
                                            {"code":"PROJECT404","message":"프로젝트를 찾을 수 없습니다.","data":null}
                                            """),
                                    @ExampleObject(name = "요청 사항 없음", value = """
                                            {"code":"REQUEST404","message":"요청 사항을 찾을 수 없습니다.","data":null}
                                            """),
                                    @ExampleObject(name = "제작 기준 없음", value = """
                                            {"code":"CRITERIA404","message":"제작 기준 버전을 찾을 수 없습니다.","data":null}
                                            """)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409",
                    description = "동일한 요청 사항에 대기 중이거나 진행 중이거나 완료된 분석이 있음",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "분석 진행 중", value = """
                                            {"code":"ANALYSIS409","message":"이미 분석 중인 요청입니다.","data":null}
                                            """),
                                    @ExampleObject(name = "분석 완료", value = """
                                            {"code":"ANALYSIS409_COMPLETED","message":"이미 분석이 완료된 요청입니다.","data":null}
                                            """)
                            })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON500","message":"서버 내부 오류가 발생했습니다.","data":null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503",
                    description = "분석 작업 실행 요청이 거부되어 작업을 시작하지 못함",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON503","message":"서비스를 일시적으로 이용할 수 없습니다.","data":null}
                                    """)))
    })
    @PostMapping("/projects/{projectId}/requests/{requestId}/analyses")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<CreateAnalysisResponse> create(
            @Parameter(description = "분석할 요청 사항이 속한 프로젝트 ID (양의 정수)", example = "1", required = true)
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @Parameter(description = "분석할 요청 사항 ID (양의 정수)", example = "1", required = true)
            @PathVariable @Positive(message = "요청 사항 ID는 양수여야 합니다.") Long requestId) {
        return ApiResponse.onAccepted(analysisService.create(projectId, requestId, TEMPORARY_USER_ID));
    }

    @Tag(name = "Analysis", description = "AI 분석 조회 API")
    @Operation(summary = "AI 분석 조회",
            description = "분석 실행의 상태를 조회합니다. "
                    + "COMPLETED 상태일 때만 분석 결과를 반환합니다. "
                    + "로그인 구현 전까지 사용자 ID는 1로 고정합니다."
    )
    @GetMapping(value = "/analyses/{analysisId}")
    public ApiResponse<AnalysisResponse> getAnalysis(
            @Parameter(description = "분석 실행 ID", example = "1")
            @PathVariable("analysisId")
            @Positive(message = "분석 ID는 양수여야 합니다.")
            Long analysisId
    ) {
        AnalysisResponse response = analysisService.getAnalysis(analysisId, TEMPORARY_USER_ID);
        return ApiResponse.onSuccess(response);
    }
}
