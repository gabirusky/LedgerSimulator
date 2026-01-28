package com.fintech.ledger.repository;

import java.util.List;
import java.util.UUID;

import com.fintech.ledger.domain.entity.Account;

/**
 * Custom repository interface for advanced Account operations.
 * <p>
 * Defines methods that require custom implementation beyond what
 * Spring Data JPA can generate automatically, particularly for
 * batch operations and deadlock-safe locking.
 */
public interface CustomAccountRepository {

    /**
     * Acquires pessimistic write locks on multiple accounts in a consistent order.
     * <p>
     * This method is crucial for preventing deadlocks in concurrent transfers.
     * The accounts are locked in sorted UUID order, ensuring that any two
     * transactions locking the same set of accounts will acquire locks
     * in the same sequence.
     * <p>
     * Example: If Transfer A→B and Transfer B→A run concurrently,
     * both will lock accounts in the same order (e.g., A then B if A < B),
     * preventing circular wait conditions.
     *
     * @param accountIds the list of account IDs to lock
     * @return list of locked Account entities in sorted UUID order
     * @throws jakarta.persistence.EntityNotFoundException if any account is not found
     */
    List<Account> findAllByIdForUpdateSorted(List<UUID> accountIds);
}
