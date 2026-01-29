package com.fintech.ledger.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a single field validation error.
 *
 * @param field the field that failed validation
 * @param message the validation error message
 */
@Schema(description = "Field validation error details")
public record FieldError(
        @Schema(description = "Field that failed validation", example = "amount")
        String field,

        @Schema(description = "Validation error message", example = "Amount must be positive")
        String message
) {
}
