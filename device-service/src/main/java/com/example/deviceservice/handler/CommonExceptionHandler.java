package com.example.deviceservice.handler;

import com.example.deviceservice.dto.response.ErrorResponse;
import com.example.deviceservice.enums.DomainType;
import com.example.deviceservice.exception.DeviceNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static com.example.deviceservice.enums.DomainType.COMMON;
import static com.example.deviceservice.enums.DomainType.DEVICE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    private final Map<Class<?>, Counter> counters;

    public CommonExceptionHandler(MeterRegistry meterRegistry) {
        counters = Map.of(
                DeviceNotFoundException.class, getCounterByDomainType(DEVICE, meterRegistry),
                Exception.class, getCounterByDomainType(COMMON, meterRegistry)
        );
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundException(DeviceNotFoundException e, HttpServletRequest req) {
        ErrorResponse errorResponse = logAndInitializeErrorResponse(e, req, NOT_FOUND);
        return ResponseEntity.status(NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(Exception e, HttpServletRequest req) {
        ErrorResponse errorResponse = logAndInitializeErrorResponse(e, req, INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private <T extends Exception> ErrorResponse logAndInitializeErrorResponse(T e, HttpServletRequest req, HttpStatus status) {
        String message = e.getMessage();
        log.error(message);
        Counter counter = counters.get(e.getClass());
        counter.increment();

        String uri = req.getRequestURI();
        return ErrorResponse.toErrorResponse(uri, message, status.value());
    }

    private Counter getCounterByDomainType(DomainType domainType, MeterRegistry meterRegistry) {
        return Counter.builder("exceptions.total")
                .description("Total number of exceptions by type")
                .tag("domain", domainType.name())
                .register(meterRegistry);
    }
}
