package com.fintech.ledger.exception;

import java.util.UUID;

/**
 * Exception thrown when an account cannot be found by its ID.
 */
public class AccountNotFoundException extends RuntimeException {

    private final UUID accountId;

    public AccountNotFoundException(UUID accountId) {
        super("Account not found with ID: " + accountId);
        this.accountId = accountId;
    }

    public AccountNotFoundException(UUID accountId, String message) {
        super(message);
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
