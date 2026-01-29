package com.fintech.ledger.domain.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new account.
 *
 * @param document the unique identifier document (e.g., CPF, CNPJ)
 * @param name the account holder's name
 */
@Schema(description = "Request body for creating a new account")
public record CreateAccountRequest(
        @Schema(description = "Unique identifier document (e.g., CPF, CNPJ)", example = "12345678901")
        @NotBlank(message = "Document is required")
        @Size(max = 50, message = "Document must not exceed 50 characters")
        String document,

        @Schema(description = "Account holder's name", example = "John Doe")
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name
) {
}
