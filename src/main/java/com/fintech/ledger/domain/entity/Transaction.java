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
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a financial transaction in the ledger system.
 * <p>
 * Each transaction is uniquely identified by an idempotency key to prevent
 * duplicate processing. Transactions create ledger entries for both the
 * source (DEBIT) and target (CREDIT) accounts.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Idempotency key is required")
    @Column(name = "idempotency_key", unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    @NotNull(message = "Source account ID is required")
    @Column(name = "source_account_id", nullable = false)
    private UUID sourceAccountId;

    @NotNull(message = "Target account ID is required")
    @Column(name = "target_account_id", nullable = false)
    private UUID targetAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Default no-args constructor required by JPA.
     */
    public Transaction() {
    }

    /**
     * Creates a new transaction with the specified details.
     * 
     * @param idempotencyKey unique key to prevent duplicate processing
     * @param sourceAccountId the account to debit
     * @param targetAccountId the account to credit
     * @param amount the amount to transfer
     * @param status the initial transaction status
     */
    public Transaction(String idempotencyKey, UUID sourceAccountId, UUID targetAccountId, 
                       BigDecimal amount, TransactionStatus status) {
        this.idempotencyKey = idempotencyKey;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.status = status;
    }

    /**
     * Full constructor for all fields.
     * 
     * @param id the unique identifier
     * @param idempotencyKey unique key to prevent duplicate processing
     * @param sourceAccountId the account to debit
     * @param targetAccountId the account to credit
     * @param amount the amount to transfer
     * @param status the transaction status
     * @param createdAt the creation timestamp
     */
    public Transaction(UUID id, String idempotencyKey, UUID sourceAccountId, UUID targetAccountId,
                       BigDecimal amount, TransactionStatus status, Instant createdAt) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getSourceAccountId() {
        return sourceAccountId;
    }

    public UUID getTargetAccountId() {
        return targetAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters

    public void setId(UUID id) {
        this.id = id;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public void setSourceAccountId(UUID sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public void setTargetAccountId(UUID targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
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
        Transaction that = (Transaction) o;
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
        return "Transaction{" +
                "id=" + id +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                ", sourceAccountId=" + sourceAccountId +
                ", targetAccountId=" + targetAccountId +
                ", amount=" + amount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
