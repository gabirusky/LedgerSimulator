package com.fintech.ledger.domain.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for an account statement containing all ledger entries.
 *
 * @param accountId the account identifier
 * @param accountName the account holder's name
 * @param currentBalance the current account balance
 * @param entries the list of ledger entries for this account
 */
@Schema(description = "Account statement with ledger entries")
public record AccountStatementResponse(
        @Schema(description = "Account UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID accountId,

        @Schema(description = "Account holder's name", example = "John Doe")
        String accountName,

        @Schema(description = "Current account balance", example = "1000.00")
        BigDecimal currentBalance,

        @Schema(description = "List of ledger entries (newest first)")
        List<LedgerEntryResponse> entries
) {
}
