package com.example.eventservice.handler;

import com.example.eventservice.dto.response.ErrorResponse;
import com.example.eventservice.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ErrorResponse> notFoundException(ServletRequestBindingException e, HttpServletRequest req) {
        ErrorResponse errorResponse = logAndInitializeErrorResponse(e, req, BAD_REQUEST);
        return ResponseEntity.status(NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundException(NotFoundException e, HttpServletRequest req) {
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

        String uri = req.getRequestURI();
        return ErrorResponse.toErrorResponse(uri, message, status.value());
    }
}
