package com.fintech.ledger.exception;

/**
 * Exception thrown when the Idempotency-Key header is missing from a request.
 */
public class MissingIdempotencyKeyException extends RuntimeException {

    public MissingIdempotencyKeyException() {
        super("Idempotency-Key header is required for this operation");
    }

    public MissingIdempotencyKeyException(String message) {
        super(message);
    }
}
