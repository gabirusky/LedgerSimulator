package com.fintech.ledger.integration.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.ledger.domain.entity.Account;
import com.fintech.ledger.domain.entity.Transaction;
import com.fintech.ledger.domain.entity.TransactionStatus;
import com.fintech.ledger.integration.AbstractIntegrationTest;
import com.fintech.ledger.repository.AccountRepository;
import com.fintech.ledger.repository.TransactionRepository;

/**
 * Integration tests for TransactionRepository.
 * <p>
 * Tests idempotency key lookups and unique constraints
 * using a real PostgreSQL database via Testcontainers.
 * <p>
 * Tasks: 285-288
 */
@Transactional
class TransactionRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Account sourceAccount;
    private Account targetAccount;
    private Transaction testTransaction;
    private String idempotencyKey;

    @BeforeEach
    void setUp() {
        // Create test accounts
        sourceAccount = accountRepository.save(new Account("11111111111", "Source Account"));
        targetAccount = accountRepository.save(new Account("22222222222", "Target Account"));

        // Create test idempotency key
        idempotencyKey = "test-key-" + UUID.randomUUID();

        // Create test transaction
        testTransaction = new Transaction(
                idempotencyKey,
                sourceAccount.getId(),
                targetAccount.getId(),
                new BigDecimal("100.00"),
                TransactionStatus.COMPLETED
        );
    }

    @Nested
    @DisplayName("TASK-286: findByIdempotencyKey")
    class FindByIdempotencyKeyTests {

        @Test
        @DisplayName("should find transaction by idempotency key")
        void shouldFindByIdempotencyKey() {
            // Given
            transactionRepository.save(testTransaction);

            // When
            Optional<Transaction> found = transactionRepository.findByIdempotencyKey(idempotencyKey);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getIdempotencyKey()).isEqualTo(idempotencyKey);
            assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(found.get().getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        }

        @Test
        @DisplayName("should return empty for non-existent idempotency key")
        void shouldReturnEmptyForNonExistentKey() {
            Optional<Transaction> found = transactionRepository.findByIdempotencyKey("nonexistent-key");
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("TASK-287: existsByIdempotencyKey")
    class ExistsByIdempotencyKeyTests {

        @Test
        @DisplayName("should return true when idempotency key exists")
        void shouldReturnTrueWhenKeyExists() {
            // Given
            transactionRepository.save(testTransaction);

            // When/Then
            assertThat(transactionRepository.existsByIdempotencyKey(idempotencyKey)).isTrue();
        }

        @Test
        @DisplayName("should return false when idempotency key does not exist")
        void shouldReturnFalseWhenKeyNotExists() {
            assertThat(transactionRepository.existsByIdempotencyKey("nonexistent-key")).isFalse();
        }
    }

    @Nested
    @DisplayName("TASK-288: Unique constraint on idempotency_key")
    class UniqueConstraintTests {

        @Test
        @DisplayName("should reject duplicate idempotency key")
        void shouldRejectDuplicateIdempotencyKey() {
            // Given - Save first transaction
            transactionRepository.save(testTransaction);
            transactionRepository.flush();

            // When/Then - Try to save another with same key
            Transaction duplicate = new Transaction(
                    idempotencyKey, // Same key!
                    targetAccount.getId(),
                    sourceAccount.getId(),
                    new BigDecimal("50.00"),
                    TransactionStatus.PENDING
            );

            assertThatThrownBy(() -> {
                transactionRepository.save(duplicate);
                transactionRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
