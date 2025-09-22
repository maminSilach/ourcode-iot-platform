package com.example.failedeventsprocessor.exception;

public class AwsException extends RuntimeException {

    public AwsException(String message) {
        super(message);
    }

    public AwsException(Throwable cause) {
        super(cause);
    }
}
