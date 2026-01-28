package com.fintech.ledger.domain.entity;

/**
 * Represents the status of a financial transaction.
 * <p>
 * Transactions start as PENDING and transition to either COMPLETED or FAILED.
 * This is an immutable state machine - once COMPLETED or FAILED, status cannot change.
 */
public enum TransactionStatus {
    /**
     * Transaction has been created but not yet processed.
     */
    PENDING,
    
    /**
     * Transaction has been successfully processed.
     * All ledger entries have been created.
     */
    COMPLETED,
    
    /**
     * Transaction failed to process.
     * This could be due to insufficient funds or other business rule violations.
     */
    FAILED
}
