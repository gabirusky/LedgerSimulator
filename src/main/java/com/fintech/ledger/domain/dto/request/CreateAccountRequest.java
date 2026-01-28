package com.fintech.ledger.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new account.
 *
 * @param document the unique identifier document (e.g., CPF, CNPJ)
 * @param name the account holder's name
 */
public record CreateAccountRequest(
        @NotBlank(message = "Document is required")
        @Size(max = 50, message = "Document must not exceed 50 characters")
        String document,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name
) {
}
