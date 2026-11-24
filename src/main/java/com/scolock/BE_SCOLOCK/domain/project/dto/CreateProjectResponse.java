package com.scolock.BE_SCOLOCK.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저장된 프로젝트")
public record CreateProjectResponse(
        @Schema(description = "저장된 프로젝트 ID", example = "1")
        Long projectId,
        @Schema(description = "텍스트로 작성한 제작 기준의 ID", example = "1")
        Long criteriaVersionID

) {
}
