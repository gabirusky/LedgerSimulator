package com.fintech.ledger.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * RFC 7807 Problem Details response format for error responses.
 * <p>
 * This provides a standard, machine-readable format for API errors.
 *
 * @param type a URI reference that identifies the problem type
 * @param title a short, human-readable summary of the problem type
 * @param status the HTTP status code
 * @param detail a human-readable explanation specific to this occurrence
 * @param instance a URI reference that identifies the specific occurrence
 * @param timestamp when the error occurred
 * @param errors list of field validation errors (null if not a validation error)
 */
@Schema(description = "RFC 7807 Problem Details error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @Schema(description = "URI reference identifying the problem type", example = "about:blank")
        String type,

        @Schema(description = "Short, human-readable summary of the problem", example = "Not Found")
        String title,

        @Schema(description = "HTTP status code", example = "404")
        int status,

        @Schema(description = "Human-readable explanation specific to this occurrence", example = "Account not found with id: 550e8400-e29b-41d4-a716-446655440000")
        String detail,

        @Schema(description = "URI reference identifying this specific occurrence", example = "/api/v1/accounts/550e8400-e29b-41d4-a716-446655440000")
        String instance,

        @Schema(description = "When the error occurred")
        Instant timestamp,

        @Schema(description = "List of field validation errors (only for validation failures)")
        List<FieldError> errors
) {
    /**
     * Creates an error response without field errors.
     */
    public ErrorResponse(String type, String title, int status, String detail, String instance, Instant timestamp) {
        this(type, title, status, detail, instance, timestamp, null);
    }

    /**
     * Creates an error response with field errors for validation failures.
     */
    public static ErrorResponse validationError(String instance, List<FieldError> errors) {
        return new ErrorResponse(
                "about:blank",
                "Validation Failed",
                400,
                "One or more fields have validation errors",
                instance,
                Instant.now(),
                errors
        );
    }

    /**
     * Creates a not found error response.
     */
    public static ErrorResponse notFound(String detail, String instance) {
        return new ErrorResponse(
                "about:blank",
                "Not Found",
                404,
                detail,
                instance,
                Instant.now()
        );
    }

    /**
     * Creates a conflict error response (e.g., duplicate resource).
     */
    public static ErrorResponse conflict(String detail, String instance) {
        return new ErrorResponse(
                "about:blank",
                "Conflict",
                409,
                detail,
                instance,
                Instant.now()
        );
    }

    /**
     * Creates an unprocessable entity error response (e.g., business rule violation).
     */
    public static ErrorResponse unprocessableEntity(String detail, String instance) {
        return new ErrorResponse(
                "about:blank",
                "Unprocessable Entity",
                422,
                detail,
                instance,
                Instant.now()
        );
    }

    /**
     * Creates a bad request error response.
     */
    public static ErrorResponse badRequest(String detail, String instance) {
        return new ErrorResponse(
                "about:blank",
                "Bad Request",
                400,
                detail,
                instance,
                Instant.now()
        );
    }

    /**
     * Creates an internal server error response.
     */
    public static ErrorResponse internalServerError(String detail, String instance) {
        return new ErrorResponse(
                "about:blank",
                "Internal Server Error",
                500,
                detail,
                instance,
                Instant.now()
        );
    }
}
