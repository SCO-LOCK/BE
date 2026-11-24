package com.scolock.BE_SCOLOCK.domain.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "프로젝트 생성")
public record CreateProjectRequest(
        @Schema(description = "프로젝트 명", example = "B 브랜드 숏폼 광고")
        @NotBlank(message = "프로젝트 명을 입력해주세요.") @Size(max = 150) String name,
        @Schema(example = "B 브랜든") @Size(max = 150) String clientName,
        @Schema(description = "영상 형식, 스타일, 인물, 제품, 원고, 레퍼런스 등 현재까지 정해진 내용이 텍스트로 작성됨")
        String criteriaText,
        @Schema(description = "첨부된 제작 기준 파일")
        MultipartFile file
) {
}