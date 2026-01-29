package com.fintech.ledger.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;

/**
 * Service interface for account management operations.
 * <p>
 * Provides methods for creating accounts, retrieving account details,
 * and calculating account balances from ledger entries.
 */
public interface AccountService {

    /**
     * Creates a new account with the given details.
     * <p>
     * The account is created with an initial balance of zero.
     * The document number must be unique across all accounts.
     *
     * @param request the account creation request containing document and name
     * @return the created account response with balance
     * @throws com.fintech.ledger.exception.DuplicateDocumentException if document already exists
     */
    AccountResponse createAccount(CreateAccountRequest request);

    /**
     * Retrieves an account by its unique identifier.
     * <p>
     * The returned response includes the current balance calculated
     * from ledger entries.
     *
     * @param id the account UUID
     * @return the account response with current balance
     * @throws com.fintech.ledger.exception.AccountNotFoundException if account not found
     */
    AccountResponse getAccount(UUID id);

    /**
     * Gets the current balance for an account.
     * <p>
     * Uses the fast-path balance lookup from the most recent ledger entry.
     *
     * @param id the account UUID
     * @return the current balance
     * @throws com.fintech.ledger.exception.AccountNotFoundException if account not found
     */
    BigDecimal getAccountBalance(UUID id);

    /**
     * Retrieves all accounts with pagination support.
     * <p>
     * Each account in the result includes its current balance.
     *
     * @param pageable pagination parameters
     * @return a page of account responses
     */
    Page<AccountResponse> getAllAccounts(Pageable pageable);
}
