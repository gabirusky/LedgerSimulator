package com.fintech.ledger.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fintech.ledger.domain.entity.Transaction;

/**
 * Repository interface for Transaction entity operations.
 * <p>
 * Provides standard CRUD operations and idempotency key lookups
 * to prevent duplicate transaction processing.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Finds a transaction by its idempotency key.
     * <p>
     * Used to check for duplicate requests and return cached responses
     * for idempotent retries. The idempotency_key column is indexed
     * for efficient lookups.
     *
     * @param idempotencyKey the unique idempotency key from the client
     * @return Optional containing the transaction if found
     */
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Checks if a transaction with the given idempotency key exists.
     * <p>
     * More efficient than findByIdempotencyKey when you only need
     * to check existence without loading the full entity.
     *
     * @param idempotencyKey the unique idempotency key to check
     * @return true if a transaction with this key exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);
}
