package org.example.usermanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class ApiErrorResponse {
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;
}