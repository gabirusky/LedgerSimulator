package com.fintech.ledger.domain.dto.response;

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
public record AccountStatementResponse(
        UUID accountId,
        String accountName,
        BigDecimal currentBalance,
        List<LedgerEntryResponse> entries
) {
}
