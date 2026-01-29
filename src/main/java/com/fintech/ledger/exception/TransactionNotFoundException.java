package com.fintech.ledger.exception;

import java.util.UUID;

/**
 * Exception thrown when a transaction cannot be found by its ID.
 */
public class TransactionNotFoundException extends RuntimeException {

    private final UUID transactionId;

    public TransactionNotFoundException(UUID transactionId) {
        super("Transaction not found with ID: " + transactionId);
        this.transactionId = transactionId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }
}
