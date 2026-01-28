package com.fintech.ledger.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.fintech.ledger.domain.entity.Account;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

/**
 * Implementation of CustomAccountRepository for advanced account operations.
 * <p>
 * This class provides custom implementations that require direct EntityManager
 * access, particularly for sorted pessimistic locking to prevent deadlocks.
 */
@Repository
public class CustomAccountRepositoryImpl implements CustomAccountRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Acquires pessimistic write locks on multiple accounts in a consistent order.
     * <p>
     * Implementation details:
     * <ol>
     *   <li>Sort the account IDs to ensure consistent lock ordering</li>
     *   <li>Acquire locks one by one in sorted order</li>
     *   <li>Use PESSIMISTIC_WRITE to prevent concurrent modifications</li>
     *   <li>Throw EntityNotFoundException if any account is missing</li>
     * </ol>
     * <p>
     * The lock timeout is configured via JPA hints to prevent indefinite blocking.
     *
     * @param accountIds the list of account IDs to lock
     * @return list of locked Account entities in sorted UUID order
     * @throws EntityNotFoundException if any account is not found
     */
    @Override
    public List<Account> findAllByIdForUpdateSorted(List<UUID> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Create a sorted copy to ensure consistent lock ordering
        List<UUID> sortedIds = new ArrayList<>(accountIds);
        Collections.sort(sortedIds);

        List<Account> lockedAccounts = new ArrayList<>();

        for (UUID id : sortedIds) {
            Account account = entityManager.find(
                Account.class, 
                id, 
                LockModeType.PESSIMISTIC_WRITE
            );
            
            if (account == null) {
                throw new EntityNotFoundException("Account not found with ID: " + id);
            }
            
            lockedAccounts.add(account);
        }

        return lockedAccounts;
    }
}
