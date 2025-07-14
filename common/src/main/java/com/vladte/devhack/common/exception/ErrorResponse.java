package com.vladte.devhack.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response for REST API.
 * Provides a consistent format for all error responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}