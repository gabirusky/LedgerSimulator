package com.fintech.ledger.exception;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.fintech.ledger.domain.dto.response.ErrorResponse;
import com.fintech.ledger.domain.dto.response.FieldError;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/**
 * Global exception handler that converts all exceptions to RFC 7807 Problem Details format.
 * <p>
 * This handler provides consistent error responses across the entire API by:
 * <ul>
 *   <li>Converting business exceptions to appropriate HTTP status codes</li>
 *   <li>Formatting all errors as RFC 7807 Problem Details</li>
 *   <li>Including request URI in the 'instance' field</li>
 *   <li>Adding timestamps to all error responses</li>
 *   <li>Logging exceptions at appropriate levels (WARN for 4xx, ERROR for 5xx)</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Error type URI prefixes
    private static final String ERROR_TYPE_PREFIX = "/errors/";

    // ==================== 404 Not Found Handlers ====================

    /**
     * Handles AccountNotFoundException.
     * Returns 404 Not Found when an account cannot be found by its ID.
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(
            AccountNotFoundException ex, WebRequest request) {
        log.warn("Account not found: accountId={}", ex.getAccountId());
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "account-not-found",
                "Account Not Found",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                getRequestUri(request),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles TransactionNotFoundException.
     * Returns 404 Not Found when a transaction cannot be found by its ID.
     */
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTransactionNotFoundException(
            TransactionNotFoundException ex, WebRequest request) {
        log.warn("Transaction not found: transactionId={}", ex.getTransactionId());
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "transaction-not-found",
                "Transaction Not Found",
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                getRequestUri(request),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ==================== 422 Unprocessable Entity Handlers ====================

    /**
     * Handles InsufficientFundsException.
     * Returns 422 Unprocessable Entity when a transfer cannot be completed due to insufficient funds.
     */
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(
            InsufficientFundsException ex, WebRequest request) {
        log.warn("Insufficient funds: accountId={}, available={}, requested={}",
                ex.getAccountId(), ex.getAvailable(), ex.getRequested());
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "insufficient-funds",
                "Insufficient Funds",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                getRequestUri(request),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // ==================== 409 Conflict Handlers ====================

    /**
     * Handles DuplicateDocumentException.
     * Returns 409 Conflict when attempting to create an account with an existing document.
     */
    @ExceptionHandler(DuplicateDocumentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateDocumentException(
            DuplicateDocumentException ex, WebRequest request) {
        log.warn("Duplicate document: document={}", maskDocument(ex.getDocument()));
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "duplicate-document",
                "Duplicate Document",
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                getRequestUri(request),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ==================== 400 Bad Request Handlers ====================

    /**
     * Handles TransferToSelfException.
     * Returns 400 Bad Request when attempting to transfer to the same account.
     */
    @ExceptionHandler(TransferToSelfException.class)
    public ResponseEntity<ErrorResponse> handleTransferToSelfException(
            TransferToSelfException ex, WebRequest request) {
        log.warn("Transfer to self attempted: accountId={}", ex.getAccountId());
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "transfer-to-self",
                "Invalid Transfer",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                getRequestUri(request),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles MissingIdempotencyKeyException.
     * Returns 400 Bad Request when the Idempotency-Key header is missing.
     */
    @ExceptionHandler(MissingIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleMissingIdempotencyKeyException(
            MissingIdempotencyKeyException ex, WebRequest request) {
        log.warn("Missing idempotency key for request: {}", getRequestUri(request));
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "missing-idempotency-key",
                "Missing Idempotency Key",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                getRequestUri(request),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles InvalidIdempotencyKeyException.
     * Returns 400 Bad Request when the Idempotency-Key has an invalid format.
     */
    @ExceptionHandler(InvalidIdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidIdempotencyKeyException(
            InvalidIdempotencyKeyException ex, WebRequest request) {
        log.warn("Invalid idempotency key: {}", ex.getIdempotencyKey());
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "invalid-idempotency-key",
                "Invalid Idempotency Key",
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                getRequestUri(request),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles MethodArgumentNotValidException (Bean Validation on @RequestBody).
     * Returns 400 Bad Request with detailed field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        log.warn("Validation failed: {} field errors", fieldErrors.size());
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "validation-failed",
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                "One or more fields have validation errors",
                getRequestUri(request),
                Instant.now(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles ConstraintViolationException (Bean Validation on path/query parameters).
     * Returns 400 Bad Request with detailed constraint violations.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        List<FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(this::toFieldError)
                .toList();

        log.warn("Constraint violation: {} violations", fieldErrors.size());
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "constraint-violation",
                "Constraint Violation",
                HttpStatus.BAD_REQUEST.value(),
                "One or more constraints were violated",
                getRequestUri(request),
                Instant.now(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ==================== 500 Internal Server Error Handler ====================

    /**
     * Handles all uncaught exceptions.
     * Returns 500 Internal Server Error with a generic message (to avoid leaking implementation details).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ErrorResponse response = new ErrorResponse(
                ERROR_TYPE_PREFIX + "internal-error",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred. Please try again later.",
                getRequestUri(request),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // ==================== Helper Methods ====================

    /**
     * Extracts the request URI from the WebRequest.
     */
    private String getRequestUri(WebRequest request) {
        String description = request.getDescription(false);
        // WebRequest.getDescription(false) returns "uri=/api/v1/..." format
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }

    /**
     * Converts a ConstraintViolation to a FieldError.
     */
    private FieldError toFieldError(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        // Extract just the field name from the full path (e.g., "executeTransfer.request.amount" -> "amount")
        int lastDot = propertyPath.lastIndexOf('.');
        String fieldName = lastDot >= 0 ? propertyPath.substring(lastDot + 1) : propertyPath;
        return new FieldError(fieldName, violation.getMessage());
    }

    /**
     * Masks sensitive document information for logging (privacy protection).
     */
    private String maskDocument(String document) {
        if (document == null || document.length() < 4) {
            return "***";
        }
        return document.substring(0, 3) + "***" + document.substring(document.length() - 2);
    }
}
