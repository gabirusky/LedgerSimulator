package com.fintech.ledger.integration.repository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.ledger.domain.entity.Account;
import com.fintech.ledger.domain.entity.EntryType;
import com.fintech.ledger.domain.entity.LedgerEntry;
import com.fintech.ledger.domain.entity.Transaction;
import com.fintech.ledger.domain.entity.TransactionStatus;
import com.fintech.ledger.integration.AbstractIntegrationTest;
import com.fintech.ledger.repository.AccountRepository;
import com.fintech.ledger.repository.LedgerEntryRepository;
import com.fintech.ledger.repository.TransactionRepository;

/**
 * Integration tests for LedgerEntryRepository.
 * <p>
 * Tests balance calculations, entry retrieval, and pagination
 * using a real PostgreSQL database via Testcontainers.
 * <p>
 * Tasks: 279-284
 */
@Transactional
class LedgerEntryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Account sourceAccount;
    private Account targetAccount;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        // Create test accounts
        sourceAccount = accountRepository.save(new Account("11111111111", "Source Account"));
        targetAccount = accountRepository.save(new Account("22222222222", "Target Account"));

        // Create a test transaction
        transaction = new Transaction(
                "test-idempotency-key-" + UUID.randomUUID(),
                sourceAccount.getId(),
                targetAccount.getId(),
                new BigDecimal("100.00"),
                TransactionStatus.COMPLETED
        );
        transaction = transactionRepository.save(transaction);
    }

    @Nested
    @DisplayName("TASK-280: calculateBalance with credits only")
    class CalculateBalanceCreditsOnlyTests {

        @Test
        @DisplayName("should calculate balance with credits only")
        void shouldCalculateBalanceWithCreditsOnly() {
            // Given - Create credit entries
            LedgerEntry credit1 = new LedgerEntry(
                    transaction.getId(),
                    targetAccount.getId(),
                    EntryType.CREDIT,
                    new BigDecimal("100.00"),
                    new BigDecimal("100.00")
            );
            LedgerEntry credit2 = new LedgerEntry(
                    transaction.getId(),
                    targetAccount.getId(),
                    EntryType.CREDIT,
                    new BigDecimal("50.00"),
                    new BigDecimal("150.00")
            );
            ledgerEntryRepository.save(credit1);
            ledgerEntryRepository.save(credit2);

            // When
            BigDecimal balance = ledgerEntryRepository.calculateBalance(targetAccount.getId());

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("150.00"));
        }
    }

    @Nested
    @DisplayName("TASK-281: calculateBalance with debits only")
    class CalculateBalanceDebitsOnlyTests {

        @Test
        @DisplayName("should calculate negative balance with debits only")
        void shouldCalculateNegativeBalanceWithDebitsOnly() {
            // Given - Create debit entries
            LedgerEntry debit1 = new LedgerEntry(
                    transaction.getId(),
                    sourceAccount.getId(),
                    EntryType.DEBIT,
                    new BigDecimal("100.00"),
                    new BigDecimal("-100.00")
            );
            ledgerEntryRepository.save(debit1);

            // When
            BigDecimal balance = ledgerEntryRepository.calculateBalance(sourceAccount.getId());

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("-100.00"));
        }
    }

    @Nested
    @DisplayName("TASK-282: calculateBalance with mixed entries")
    class CalculateBalanceMixedTests {

        @Test
        @DisplayName("should calculate balance with credits and debits")
        void shouldCalculateBalanceWithMixedEntries() {
            // Given - Create mixed entries
            LedgerEntry credit = new LedgerEntry(
                    transaction.getId(),
                    targetAccount.getId(),
                    EntryType.CREDIT,
                    new BigDecimal("500.00"),
                    new BigDecimal("500.00")
            );
            LedgerEntry debit = new LedgerEntry(
                    transaction.getId(),
                    targetAccount.getId(),
                    EntryType.DEBIT,
                    new BigDecimal("200.00"),
                    new BigDecimal("300.00")
            );
            ledgerEntryRepository.save(credit);
            ledgerEntryRepository.save(debit);

            // When
            BigDecimal balance = ledgerEntryRepository.calculateBalance(targetAccount.getId());

            // Then
            assertThat(balance).isEqualByComparingTo(new BigDecimal("300.00"));
        }
    }

    @Nested
    @DisplayName("TASK-283: calculateBalance for new account")
    class CalculateBalanceNewAccountTests {

        @Test
        @DisplayName("should return zero for account with no entries")
        void shouldReturnZeroForNewAccount() {
            // Given - New account with no entries
            Account newAccount = accountRepository.save(new Account("33333333333", "New Account"));

            // When
            BigDecimal balance = ledgerEntryRepository.calculateBalance(newAccount.getId());

            // Then
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("getBalance should return zero for account with no entries")
        void getBalanceShouldReturnZeroForNewAccount() {
            // Given - New account with no entries
            Account newAccount = accountRepository.save(new Account("44444444444", "Empty Account"));

            // When
            BigDecimal balance = ledgerEntryRepository.getBalance(newAccount.getId());

            // Then
            assertThat(balance).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("TASK-284: findByAccountIdOrderByCreatedAtDesc")
    class FindByAccountIdWithPaginationTests {

        @Test
        @DisplayName("should return paginated entries ordered by creation date descending")
        void shouldReturnPaginatedEntriesOrderedByCreatedAtDesc() {
            // Given - Create multiple entries
            for (int i = 1; i <= 5; i++) {
                LedgerEntry entry = new LedgerEntry(
                        transaction.getId(),
                        targetAccount.getId(),
                        EntryType.CREDIT,
                        new BigDecimal(i * 10),
                        new BigDecimal(i * 10)
                );
                ledgerEntryRepository.save(entry);
            }

            // When - Get first page with 2 entries
            Page<LedgerEntry> page = ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(
                    targetAccount.getId(),
                    PageRequest.of(0, 2)
            );

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(5);
            assertThat(page.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("should return empty page for account with no entries")
        void shouldReturnEmptyPageForAccountWithNoEntries() {
            // Given
            Account emptyAccount = accountRepository.save(new Account("55555555555", "Empty Account"));

            // When
            Page<LedgerEntry> page = ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(
                    emptyAccount.getId(),
                    PageRequest.of(0, 10)
            );

            // Then
            assertThat(page.getContent()).isEmpty();
            assertThat(page.getTotalElements()).isZero();
        }
    }
}
