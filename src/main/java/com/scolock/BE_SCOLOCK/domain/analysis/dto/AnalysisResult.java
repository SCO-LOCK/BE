package com.scolock.BE_SCOLOCK.domain.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.scolock.BE_SCOLOCK.domain.analysis.enums.AnalysisClassification;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AnalysisResult(
        @NotNull @Size(max = AnalysisResult.MAX_FINDINGS) List<@NotNull @Valid Finding> findings
) {
    public static final int MAX_FINDINGS = 3;

    public record Finding(
            @NotNull AnalysisClassification type,
            @NotBlank String title,
            @JsonProperty(value = "existing_baseline", required = true) @NotNull String existingBaseline,
            @JsonProperty(value = "request_excerpt", required = true) @NotBlank String requestExcerpt,
            @NotBlank String reason,
            @JsonProperty(value = "suggested_question", required = true) @NotBlank String suggestedQuestion
    ) {
    }
}
