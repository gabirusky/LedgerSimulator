package com.fintech.ledger.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for ledger entry information.
 *
 * @param id the unique entry identifier
 * @param transactionId the associated transaction identifier
 * @param entryType the type of entry (DEBIT or CREDIT)
 * @param amount the entry amount
 * @param balanceAfter the account balance after this entry
 * @param createdAt the entry creation timestamp
 */
@Schema(description = "Individual ledger entry for double-entry bookkeeping")
public record LedgerEntryResponse(
        @Schema(description = "Entry UUID", example = "550e8400-e29b-41d4-a716-446655440003")
        UUID id,

        @Schema(description = "Associated transaction UUID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID transactionId,

        @Schema(description = "Type of entry", example = "CREDIT", allowableValues = {"DEBIT", "CREDIT"})
        String entryType,

        @Schema(description = "Entry amount", example = "100.00")
        BigDecimal amount,

        @Schema(description = "Account balance after this entry", example = "1100.00")
        BigDecimal balanceAfter,

        @Schema(description = "Entry creation timestamp")
        Instant createdAt
) {
}
