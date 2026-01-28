package com.fintech.ledger.domain.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for transfer/transaction information.
 *
 * @param transactionId the unique transaction identifier
 * @param sourceAccountId the account that was debited
 * @param targetAccountId the account that was credited
 * @param amount the amount transferred
 * @param status the transaction status (PENDING, COMPLETED, FAILED)
 * @param createdAt the transaction creation timestamp
 */
public record TransferResponse(
        UUID transactionId,
        UUID sourceAccountId,
        UUID targetAccountId,
        BigDecimal amount,
        String status,
        Instant createdAt
) {
}
