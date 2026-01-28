package com.fintech.ledger.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an immutable entry in the double-entry bookkeeping ledger.
 * <p>
 * Ledger entries are APPEND-ONLY and should NEVER be modified or deleted.
 * Every financial transaction creates exactly two entries:
 * <ul>
 *   <li>DEBIT entry for the source account (money out)</li>
 *   <li>CREDIT entry for the target account (money in)</li>
 * </ul>
 * The balance for any account is calculated as: SUM(Credits) - SUM(Debits)
 */
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Transaction ID is required")
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @NotNull(message = "Account ID is required")
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @NotNull(message = "Entry type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @NotNull(message = "Amount is required")
    @PositiveOrZero(message = "Amount must not be negative")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Balance after is required")
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Default no-args constructor required by JPA.
     */
    public LedgerEntry() {
    }

    /**
     * Creates a new ledger entry with the specified details.
     * 
     * @param transactionId the associated transaction
     * @param accountId the account this entry belongs to
     * @param entryType DEBIT or CREDIT
     * @param amount the amount of this entry
     * @param balanceAfter the account balance after this entry
     */
    public LedgerEntry(UUID transactionId, UUID accountId, EntryType entryType, 
                       BigDecimal amount, BigDecimal balanceAfter) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    /**
     * Full constructor for all fields.
     * 
     * @param id the unique identifier
     * @param transactionId the associated transaction
     * @param accountId the account this entry belongs to
     * @param entryType DEBIT or CREDIT
     * @param amount the amount of this entry
     * @param balanceAfter the account balance after this entry
     * @param createdAt the creation timestamp
     */
    public LedgerEntry(UUID id, UUID transactionId, UUID accountId, EntryType entryType,
                       BigDecimal amount, BigDecimal balanceAfter, Instant createdAt) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Equality is based solely on the entity ID.
     * This follows JPA best practices to avoid issues with Hibernate proxies.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LedgerEntry that = (LedgerEntry) o;
        return id != null && Objects.equals(id, that.id);
    }

    /**
     * Hash code is based solely on the entity ID.
     * Returns a constant for new entities (id == null) to maintain consistency.
     */
    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : getClass().hashCode();
    }

    @Override
    public String toString() {
        return "LedgerEntry{" +
                "id=" + id +
                ", transactionId=" + transactionId +
                ", accountId=" + accountId +
                ", entryType=" + entryType +
                ", amount=" + amount +
                ", balanceAfter=" + balanceAfter +
                ", createdAt=" + createdAt +
                '}';
    }
}
