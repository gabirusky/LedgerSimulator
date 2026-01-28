package com.fintech.ledger.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fintech.ledger.domain.entity.LedgerEntry;

/**
 * Repository interface for LedgerEntry entity operations.
 * <p>
 * Provides methods for retrieving ledger entries and calculating
 * account balances using the double-entry bookkeeping principle:
 * Balance = SUM(Credits) - SUM(Debits)
 */
@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    /**
     * Finds all ledger entries for an account, ordered by creation time (newest first).
     * <p>
     * Used for generating account statements and transaction history.
     *
     * @param accountId the account ID
     * @return list of ledger entries ordered by creation time descending
     */
    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    /**
     * Finds ledger entries for an account with pagination support.
     * <p>
     * Used for large account histories where loading all entries
     * would be impractical.
     *
     * @param accountId the account ID
     * @param pageable pagination parameters
     * @return paginated list of ledger entries
     */
    Page<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    /**
     * Finds all ledger entries associated with a specific transaction.
     * <p>
     * Each transaction should have exactly 2 entries: one DEBIT and one CREDIT.
     *
     * @param transactionId the transaction ID
     * @return list of ledger entries for the transaction
     */
    List<LedgerEntry> findByTransactionId(UUID transactionId);

    /**
     * Calculates the current balance for an account from ledger entries.
     * <p>
     * This is the authoritative source of truth for account balances.
     * Balance = SUM(Credits) - SUM(Debits)
     * <p>
     * Uses COALESCE to return 0 for accounts with no entries (new accounts).
     *
     * @param accountId the account ID
     * @return the calculated balance
     */
    @Query("SELECT COALESCE(" +
           "SUM(CASE WHEN e.entryType = com.fintech.ledger.domain.entity.EntryType.CREDIT THEN e.amount END) - " +
           "SUM(CASE WHEN e.entryType = com.fintech.ledger.domain.entity.EntryType.DEBIT THEN e.amount END), " +
           "0) FROM LedgerEntry e WHERE e.accountId = :accountId")
    BigDecimal calculateBalance(@Param("accountId") UUID accountId);

    /**
     * Finds the most recent ledger entry for an account.
     * <p>
     * Useful for getting the last known balance_after value
     * when creating new entries.
     *
     * @param accountId the account ID
     * @return Optional containing the most recent entry if any exist
     */
    @Query("SELECT e FROM LedgerEntry e WHERE e.accountId = :accountId " +
           "ORDER BY e.createdAt DESC LIMIT 1")
    Optional<LedgerEntry> findLatestByAccountId(@Param("accountId") UUID accountId);

    /**
     * Counts the number of ledger entries for an account.
     * <p>
     * Useful for checking if an account has any transaction history.
     *
     * @param accountId the account ID
     * @return the count of entries
     */
    long countByAccountId(UUID accountId);
}
