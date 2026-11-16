package com.scolock.BE_SCOLOCK.domain.analysis.ai;

import lombok.Getter;

@Getter
public class AnalysisAiException extends RuntimeException {
    private final String code;

    public AnalysisAiException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
