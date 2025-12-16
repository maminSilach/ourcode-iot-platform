package com.example.apiorchestrator.exception;


import com.example.apiorchestrator.enums.FacadeService;
import lombok.Getter;

@Getter
public class SagaException extends RuntimeException {

    private final FacadeService service;

    public SagaException(Throwable cause, FacadeService service) {
        super(cause);

        this.service = service;
    }
}
