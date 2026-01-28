package com.fintech.ledger.domain.dto.response;

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
public record AccountResponse(
        UUID id,
        String document,
        String name,
        BigDecimal balance,
        Instant createdAt
) {
}
