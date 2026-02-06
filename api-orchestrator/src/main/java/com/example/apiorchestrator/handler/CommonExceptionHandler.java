package com.example.apiorchestrator.handler;

import com.example.apiorchestrator.domain.dto.response.ErrorResponse;
import com.example.apiorchestrator.domain.dto.response.SagaErrorResponse;
import com.example.apiorchestrator.exception.SagaException;
import feign.FeignException;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FeignException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleApiError(FeignException ex, ServerWebExchange exchange) {
        var rootHttpStatus = HttpStatus.valueOf(ex.status());
        var errorResponse = logAndinitializeErrorResponse(ex, exchange, rootHttpStatus);

        return Mono.just(
                ResponseEntity.status(rootHttpStatus).body(errorResponse)
        );
    }

    @ExceptionHandler(SagaException.class)
    public Mono<ResponseEntity<SagaErrorResponse>> handleSagaException(SagaException ex, ServerWebExchange exchange) {
        var rootHttpStatus = HttpStatus.BAD_GATEWAY;
        var errorResponse = logAndinitializeErrorResponse(ex, exchange, rootHttpStatus);

        return Mono.just(
                ResponseEntity.status(rootHttpStatus).body(
                        SagaErrorResponse.of(ex.getService(), errorResponse)
                )
        );
    }

    @ExceptionHandler(StatusRuntimeException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGrpcException(StatusRuntimeException ex, ServerWebExchange exchange) {
        var rootHttpStatus = HttpStatus.valueOf(ex.getStatus().getCode().value());
        var errorResponse = logAndinitializeErrorResponse(ex, exchange, rootHttpStatus);

        return Mono.just(
                ResponseEntity.status(rootHttpStatus).body(errorResponse)
        );
    }

    private <T extends Exception> ErrorResponse logAndinitializeErrorResponse(T e, ServerWebExchange exchange, HttpStatus status) {
        String message = e.getMessage();
        String uri = exchange.getRequest().getURI().toString();
        log.error(message);

        return ErrorResponse.toErrorResponse(uri, message, status.value());
    }
}
