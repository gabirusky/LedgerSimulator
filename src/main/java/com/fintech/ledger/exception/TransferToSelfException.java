package com.fintech.ledger.exception;

import java.util.UUID;

/**
 * Exception thrown when attempting to transfer funds from an account to itself.
 */
public class TransferToSelfException extends RuntimeException {

    private final UUID accountId;

    public TransferToSelfException(UUID accountId) {
        super("Cannot transfer funds to the same account: " + accountId);
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
