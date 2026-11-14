package com.scolock.BE_SCOLOCK.domain.request.controller;

import com.scolock.BE_SCOLOCK.domain.request.dto.ClientRequestResponse;
import com.scolock.BE_SCOLOCK.domain.request.dto.CreateClientRequestRequest;
import com.scolock.BE_SCOLOCK.domain.request.service.ClientRequestService;
import com.scolock.BE_SCOLOCK.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Client Requests", description = "프로젝트별 클라이언트 요청 사항 API")
@RestController
@RequestMapping("/projects/{projectId}/requests")
@RequiredArgsConstructor
public class ClientRequestController {

    // TODO: 로그인 구현 후 인증된 사용자의 ID로 교체
    private static final Long TEMPORARY_USER_ID = 1L;

    private final ClientRequestService clientRequestService;

    @Operation(summary = "클라이언트 요청 사항 등록",
            description = "로그인 구현 전까지 사용자 ID를 1로 고정합니다. "
                    + "ID 1 사용자가 소유한 기존 프로젝트에 요청 사항을 등록합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201",
                    description = "요청 사항 등록 성공", useReturnTypeSchema = true),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
                    description = "요청 내용 누락/빈 문자열/공백, 잘못된 JSON 또는 유효하지 않은 프로젝트 ID",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(name = "필드 검증 실패", value = """
                                            {"code":"COMMON400","message":"잘못된 요청입니다.",
                                             "data":{"requestText":"요청 사항 내용을 입력해 주세요."}}
                                            """),
                                    @ExampleObject(name = "잘못된 JSON 또는 ID 형식", value = """
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
                    description = "존재하지 않는 프로젝트",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"PROJECT404","message":"프로젝트를 찾을 수 없습니다.","data":null}
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON500","message":"서버 내부 오류가 발생했습니다.","data":null}
                                    """)))
    })
    @PostMapping(produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ClientRequestResponse> create(
            @Parameter(description = "요청 사항을 등록할 프로젝트 ID (양의 정수)", example = "1", required = true)
            @PathVariable @Positive(message = "프로젝트 ID는 양수여야 합니다.") Long projectId,
            @Valid @RequestBody CreateClientRequestRequest input) {
        return ApiResponse.onCreated(clientRequestService.create(projectId, TEMPORARY_USER_ID, input));
    }
}
