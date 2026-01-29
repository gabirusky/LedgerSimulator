package com.fintech.ledger.service;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.fintech.ledger.domain.dto.response.AccountStatementResponse;

/**
 * Service interface for ledger/statement operations.
 * <p>
 * Provides methods for retrieving account transaction history
 * as statements containing ledger entries.
 */
public interface LedgerService {

    /**
     * Retrieves the full account statement with all ledger entries.
     * <p>
     * Note: For accounts with many transactions, prefer the paginated version.
     *
     * @param accountId the account UUID
     * @return the account statement with all entries
     * @throws com.fintech.ledger.exception.AccountNotFoundException if account not found
     */
    AccountStatementResponse getAccountStatement(UUID accountId);

    /**
     * Retrieves a paginated account statement.
     * <p>
     * Entries are ordered by creation time descending (newest first).
     *
     * @param accountId the account UUID
     * @param pageable pagination parameters
     * @return the account statement with paginated entries
     * @throws com.fintech.ledger.exception.AccountNotFoundException if account not found
     */
    AccountStatementResponse getAccountStatement(UUID accountId, Pageable pageable);
}
