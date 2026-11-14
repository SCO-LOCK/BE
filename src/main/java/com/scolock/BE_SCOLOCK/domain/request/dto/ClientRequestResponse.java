package com.scolock.BE_SCOLOCK.domain.request.dto;

import com.scolock.BE_SCOLOCK.domain.request.entity.ClientRequest;
import java.time.LocalDateTime;

public record ClientRequestResponse(
        LocalDateTime createdAt,
        String requestText,
        Long projectId,
        Long id
) {
    public static ClientRequestResponse from(ClientRequest request) {
        return new ClientRequestResponse(
                request.getCreatedAt(),
                request.getRequestText(),
                request.getProject().getId(),
                request.getId()
        );
    }
}
