package com.fintech.ledger.repository;

import com.fintech.ledger.domain.entity.Account;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Account entity operations.
 * <p>
 * Provides standard CRUD operations through JpaRepository,
 * custom finder methods, and pessimistic locking for concurrent transfers.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>, CustomAccountRepository {

    /**
     * Finds an account by its unique document number.
     *
     * @param document the document number (e.g., CPF, CNPJ)
     * @return Optional containing the account if found
     */
    Optional<Account> findByDocument(String document);

    /**
     * Checks if an account with the given document number exists.
     *
     * @param document the document number to check
     * @return true if an account with this document exists
     */
    boolean existsByDocument(String document);

    /**
     * Finds an account by ID with a pessimistic write lock.
     * <p>
     * This is essential for preventing race conditions during transfers.
     * The lock is held until the transaction commits or rolls back.
     * Uses a 5-second lock timeout to prevent indefinite blocking.
     *
     * @param id the account ID to lock
     * @return Optional containing the locked account if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
        @QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")
    })
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") UUID id);
}
