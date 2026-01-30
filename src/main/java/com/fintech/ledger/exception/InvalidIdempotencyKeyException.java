package com.fintech.ledger.exception;

/**
 * Exception thrown when an idempotency key has an invalid format.
 */
public class InvalidIdempotencyKeyException extends RuntimeException {

    private final String idempotencyKey;

    public InvalidIdempotencyKeyException(String idempotencyKey) {
        super("Invalid idempotency key format: " + idempotencyKey);
        this.idempotencyKey = idempotencyKey;
    }

    public InvalidIdempotencyKeyException(String idempotencyKey, String message) {
        super(message);
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
