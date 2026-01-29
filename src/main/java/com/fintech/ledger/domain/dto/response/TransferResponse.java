package com.fintech.ledger.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
@Schema(description = "Transfer/transaction information")
public record TransferResponse(
        @Schema(description = "Transaction UUID", example = "550e8400-e29b-41d4-a716-446655440002")
        UUID transactionId,

        @Schema(description = "UUID of the account that was debited", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID sourceAccountId,

        @Schema(description = "UUID of the account that was credited", example = "550e8400-e29b-41d4-a716-446655440001")
        UUID targetAccountId,

        @Schema(description = "Amount transferred", example = "100.00")
        BigDecimal amount,

        @Schema(description = "Transaction status", example = "COMPLETED", allowableValues = {"PENDING", "COMPLETED", "FAILED"})
        String status,

        @Schema(description = "Transaction creation timestamp")
        Instant createdAt
) {
}
