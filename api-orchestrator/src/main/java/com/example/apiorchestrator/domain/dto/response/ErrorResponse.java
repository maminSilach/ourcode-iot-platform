package com.example.apiorchestrator.domain.dto.response;

public record ErrorResponse(
        String type,
        String title,
        int status
) {

  public static ErrorResponse toErrorResponse(String type, String title, int status) {
      return new ErrorResponse(type, title, status);
  }

}
