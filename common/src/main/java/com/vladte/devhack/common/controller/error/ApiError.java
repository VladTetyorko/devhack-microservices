package com.vladte.devhack.common.controller.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response for REST API.
 * This class provides a consistent structure for error responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ApiError {

    @Schema(description = "HTTP status code", example = "400")
    private HttpStatus status;

    @Schema(description = "Error type", example = "Validation error")
    private String error;

    @Schema(description = "Detailed error message", example = "Invalid input data")
    private String message;

    @Schema(description = "Field-specific error details")
    private Map<String, String> errors;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Timestamp when the error occurred", example = "2023-07-12 14:30:45")
    private LocalDateTime timestamp;

    /**
     * Constructor for simple errors without field-specific details.
     *
     * @param status    the HTTP status
     * @param error     the error type
     * @param message   the error message
     * @param timestamp the timestamp
     */
    public ApiError(HttpStatus status, String error, String message, LocalDateTime timestamp) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }
}