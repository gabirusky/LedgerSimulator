package com.fintech.ledger.integration.repository;

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
import com.fintech.ledger.integration.AbstractIntegrationTest;
import com.fintech.ledger.repository.AccountRepository;

/**
 * Integration tests for AccountRepository.
 * <p>
 * Tests CRUD operations, custom query methods, and database constraints
 * using a real PostgreSQL database via Testcontainers.
 * <p>
 * Tasks: 272-278
 */
@Transactional
class AccountRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account("12345678901", "Test User");
    }

    @Nested
    @DisplayName("TASK-274: save and findById")
    class SaveAndFindByIdTests {

        @Test
        @DisplayName("should save account and find by ID")
        void shouldSaveAndFindById() {
            // When
            Account saved = accountRepository.save(testAccount);

            // Then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getCreatedAt()).isNotNull();

            Optional<Account> found = accountRepository.findById(saved.getId());
            assertThat(found).isPresent();
            assertThat(found.get().getDocument()).isEqualTo("12345678901");
            assertThat(found.get().getName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("should return empty Optional for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            Optional<Account> found = accountRepository.findById(UUID.randomUUID());
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("TASK-275: findByDocument")
    class FindByDocumentTests {

        @Test
        @DisplayName("should find account by document")
        void shouldFindByDocument() {
            // Given
            accountRepository.save(testAccount);

            // When
            Optional<Account> found = accountRepository.findByDocument("12345678901");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Test User");
        }

        @Test
        @DisplayName("should return empty Optional for non-existent document")
        void shouldReturnEmptyForNonExistentDocument() {
            Optional<Account> found = accountRepository.findByDocument("nonexistent");
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("TASK-276: existsByDocument")
    class ExistsByDocumentTests {

        @Test
        @DisplayName("should return true when document exists")
        void shouldReturnTrueWhenExists() {
            accountRepository.save(testAccount);
            assertThat(accountRepository.existsByDocument("12345678901")).isTrue();
        }

        @Test
        @DisplayName("should return false when document does not exist")
        void shouldReturnFalseWhenNotExists() {
            assertThat(accountRepository.existsByDocument("nonexistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("TASK-277: findByIdForUpdate")
    class FindByIdForUpdateTests {

        @Test
        @DisplayName("should find account with pessimistic lock")
        void shouldFindWithPessimisticLock() {
            // Given
            Account saved = accountRepository.save(testAccount);

            // When - Acquire pessimistic lock
            Optional<Account> locked = accountRepository.findByIdForUpdate(saved.getId());

            // Then
            assertThat(locked).isPresent();
            assertThat(locked.get().getId()).isEqualTo(saved.getId());
        }

        @Test
        @DisplayName("should return empty for non-existent ID")
        void shouldReturnEmptyForNonExistentId() {
            Optional<Account> locked = accountRepository.findByIdForUpdate(UUID.randomUUID());
            assertThat(locked).isEmpty();
        }
    }

    @Nested
    @DisplayName("TASK-278: Unique constraint on document")
    class UniqueConstraintTests {

        @Test
        @DisplayName("should reject duplicate document")
        void shouldRejectDuplicateDocument() {
            // Given
            accountRepository.save(testAccount);
            accountRepository.flush();

            // When/Then
            Account duplicate = new Account("12345678901", "Another User");
            assertThatThrownBy(() -> {
                accountRepository.save(duplicate);
                accountRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
