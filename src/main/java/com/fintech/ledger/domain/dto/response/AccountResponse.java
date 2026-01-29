package com.fintech.ledger.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for account information.
 *
 * @param id the unique identifier
 * @param document the unique identifier document (e.g., CPF, CNPJ)
 * @param name the account holder's name
 * @param balance the current account balance (calculated from ledger entries)
 * @param createdAt the account creation timestamp
 */
@Schema(description = "Account information with current balance")
public record AccountResponse(
        @Schema(description = "Account UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "Unique identifier document (e.g., CPF, CNPJ)", example = "12345678901")
        String document,

        @Schema(description = "Account holder's name", example = "John Doe")
        String name,

        @Schema(description = "Current balance calculated from ledger entries", example = "1000.00")
        BigDecimal balance,

        @Schema(description = "Account creation timestamp")
        Instant createdAt
) {
}
