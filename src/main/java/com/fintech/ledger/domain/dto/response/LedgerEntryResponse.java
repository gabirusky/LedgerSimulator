package com.fintech.ledger.domain.dto.response;

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
public record LedgerEntryResponse(
        UUID id,
        UUID transactionId,
        String entryType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant createdAt
) {
}
