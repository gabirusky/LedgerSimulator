package com.fintech.ledger.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a financial account in the ledger system.
 * <p>
 * Accounts are identified by a unique document number (e.g., CPF, CNPJ).
 * The balance is NOT stored as a column but calculated from ledger entries
 * to ensure data integrity and prevent race conditions.
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotBlank(message = "Document is required")
    @Column(name = "document", unique = true, nullable = false, length = 50)
    private String document;

    @NotBlank(message = "Name is required")
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Default no-args constructor required by JPA.
     */
    public Account() {
    }

    /**
     * Creates a new account with the specified document and name.
     * 
     * @param document the unique identifier document (e.g., CPF, CNPJ)
     * @param name the account holder's name
     */
    public Account(String document, String name) {
        this.document = document;
        this.name = name;
    }

    /**
     * Full constructor for all fields.
     * 
     * @param id the unique identifier
     * @param document the unique identifier document
     * @param name the account holder's name
     * @param createdAt the creation timestamp
     * @param updatedAt the last update timestamp
     */
    public Account(UUID id, String document, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.document = document;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getDocument() {
        return document;
    }

    public String getName() {
        return name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters

    public void setId(UUID id) {
        this.id = id;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Equality is based solely on the entity ID.
     * This follows JPA best practices to avoid issues with Hibernate proxies.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return id != null && Objects.equals(id, account.id);
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
        return "Account{" +
                "id=" + id +
                ", document='" + document + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
