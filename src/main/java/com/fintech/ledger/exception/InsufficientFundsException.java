package com.fintech.ledger.exception;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Exception thrown when a transfer cannot be completed due to insufficient funds.
 */
public class InsufficientFundsException extends RuntimeException {

    private final UUID accountId;
    private final BigDecimal available;
    private final BigDecimal requested;

    public InsufficientFundsException(UUID accountId, BigDecimal available, BigDecimal requested) {
        super(String.format(
            "Insufficient funds in account %s: available=%s, requested=%s",
            accountId, available, requested
        ));
        this.accountId = accountId;
        this.available = available;
        this.requested = requested;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public BigDecimal getAvailable() {
        return available;
    }

    public BigDecimal getRequested() {
        return requested;
    }
}
