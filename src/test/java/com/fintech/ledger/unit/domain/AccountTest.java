package com.fintech.ledger.unit.domain;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fintech.ledger.domain.entity.Account;

/**
 * Unit tests for Account entity.
 * Tests equals, hashCode, and entity creation.
 */
@DisplayName("Account Entity")
class AccountTest {

    @Nested
    @DisplayName("equals()")
    class EqualsTests {

        @Test
        @DisplayName("should return true when comparing accounts with same ID")
        void should_ReturnTrue_When_SameId() {
            // Given
            UUID id = UUID.randomUUID();
            Account account1 = new Account(id, "12345678901", "John Doe", Instant.now(), null);
            Account account2 = new Account(id, "98765432109", "Jane Doe", Instant.now(), null);

            // When & Then
            assertThat(account1).isEqualTo(account2);
        }

        @Test
        @DisplayName("should return false when comparing accounts with different IDs")
        void should_ReturnFalse_When_DifferentId() {
            // Given
            Account account1 = new Account(UUID.randomUUID(), "12345678901", "John Doe", Instant.now(), null);
            Account account2 = new Account(UUID.randomUUID(), "12345678901", "John Doe", Instant.now(), null);

            // When & Then
            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("should return false when comparing with null")
        void should_ReturnFalse_When_ComparedWithNull() {
            // Given
            Account account = new Account(UUID.randomUUID(), "12345678901", "John Doe", Instant.now(), null);

            // When & Then
            assertThat(account).isNotEqualTo(null);
        }

        @Test
        @DisplayName("should return false when ID is null")
        void should_ReturnFalse_When_IdIsNull() {
            // Given
            Account account1 = new Account("12345678901", "John Doe");
            Account account2 = new Account("12345678901", "John Doe");

            // When & Then
            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("should return true when comparing same instance")
        void should_ReturnTrue_When_SameInstance() {
            // Given
            Account account = new Account(UUID.randomUUID(), "12345678901", "John Doe", Instant.now(), null);

            // When & Then
            assertThat(account).isEqualTo(account);
        }
    }

    @Nested
    @DisplayName("hashCode()")
    class HashCodeTests {

        @Test
        @DisplayName("should return same hash code for accounts with same ID")
        void should_ReturnSameHashCode_When_SameId() {
            // Given
            UUID id = UUID.randomUUID();
            Account account1 = new Account(id, "12345678901", "John Doe", Instant.now(), null);
            Account account2 = new Account(id, "98765432109", "Jane Doe", Instant.now(), null);

            // When & Then
            assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
        }

        @Test
        @DisplayName("should return class hash code when ID is null")
        void should_ReturnClassHashCode_When_IdIsNull() {
            // Given
            Account account = new Account("12345678901", "John Doe");

            // When & Then
            assertThat(account.hashCode()).isEqualTo(Account.class.hashCode());
        }
    }

    @Nested
    @DisplayName("Entity Creation")
    class EntityCreationTests {

        @Test
        @DisplayName("should create account with document and name")
        void should_CreateAccount_WithDocumentAndName() {
            // Given
            String document = "12345678901";
            String name = "John Doe";

            // When
            Account account = new Account(document, name);

            // Then
            assertThat(account.getDocument()).isEqualTo(document);
            assertThat(account.getName()).isEqualTo(name);
            assertThat(account.getId()).isNull();
            assertThat(account.getCreatedAt()).isNull();
            assertThat(account.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("should create account with full constructor")
        void should_CreateAccount_WithAllFields() {
            // Given
            UUID id = UUID.randomUUID();
            String document = "12345678901";
            String name = "John Doe";
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();

            // When
            Account account = new Account(id, document, name, createdAt, updatedAt);

            // Then
            assertThat(account.getId()).isEqualTo(id);
            assertThat(account.getDocument()).isEqualTo(document);
            assertThat(account.getName()).isEqualTo(name);
            assertThat(account.getCreatedAt()).isEqualTo(createdAt);
            assertThat(account.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("should create account with no-args constructor and setters")
        void should_CreateAccount_WithNoArgsConstructorAndSetters() {
            // Given
            UUID id = UUID.randomUUID();
            String document = "12345678901";
            String name = "John Doe";
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();

            // When
            Account account = new Account();
            account.setId(id);
            account.setDocument(document);
            account.setName(name);
            account.setCreatedAt(createdAt);
            account.setUpdatedAt(updatedAt);

            // Then
            assertThat(account.getId()).isEqualTo(id);
            assertThat(account.getDocument()).isEqualTo(document);
            assertThat(account.getName()).isEqualTo(name);
            assertThat(account.getCreatedAt()).isEqualTo(createdAt);
            assertThat(account.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("should produce valid toString representation")
        void should_ProduceValidToString() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "12345678901", "John Doe", Instant.now(), null);

            // When
            String stringRepresentation = account.toString();

            // Then
            assertThat(stringRepresentation).contains("Account");
            assertThat(stringRepresentation).contains(id.toString());
            assertThat(stringRepresentation).contains("12345678901");
            assertThat(stringRepresentation).contains("John Doe");
        }
    }
}
