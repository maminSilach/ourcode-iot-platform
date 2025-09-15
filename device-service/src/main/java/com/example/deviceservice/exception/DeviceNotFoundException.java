package com.example.deviceservice.exception;

public class DeviceNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Device with id = %s not found";

    public DeviceNotFoundException(String id) {
      super(ERROR_MESSAGE.formatted(id));
    }
}
