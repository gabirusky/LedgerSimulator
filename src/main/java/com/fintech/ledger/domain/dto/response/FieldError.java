package com.fintech.ledger.domain.dto.response;

/**
 * Represents a single field validation error.
 *
 * @param field the field that failed validation
 * @param message the validation error message
 */
public record FieldError(
        String field,
        String message
) {
}
