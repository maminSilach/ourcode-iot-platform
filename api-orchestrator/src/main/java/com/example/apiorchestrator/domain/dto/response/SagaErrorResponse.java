package com.example.apiorchestrator.domain.dto.response;

import com.example.apiorchestrator.enums.FacadeService;

public record SagaErrorResponse(
        FacadeService service,
        boolean compensated,
        ErrorResponse errorResponse
) {

    public static SagaErrorResponse of(FacadeService service, ErrorResponse errorResponse) {
        return new SagaErrorResponse(service, true, errorResponse);
    }
}
