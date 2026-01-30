package com.fintech.ledger.unit.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fintech.ledger.domain.dto.request.TransferRequest;
import com.fintech.ledger.domain.dto.response.TransferResponse;
import com.fintech.ledger.domain.entity.Account;
import com.fintech.ledger.domain.entity.EntryType;
import com.fintech.ledger.domain.entity.LedgerEntry;
import com.fintech.ledger.domain.entity.Transaction;
import com.fintech.ledger.domain.entity.TransactionStatus;
import com.fintech.ledger.exception.AccountNotFoundException;
import com.fintech.ledger.exception.InsufficientFundsException;
import com.fintech.ledger.exception.TransactionNotFoundException;
import com.fintech.ledger.mapper.TransactionMapper;
import com.fintech.ledger.repository.AccountRepository;
import com.fintech.ledger.repository.LedgerEntryRepository;
import com.fintech.ledger.repository.TransactionRepository;
import com.fintech.ledger.service.impl.TransferServiceImpl;

/**
 * Unit tests for TransferServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService")
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Captor
    private ArgumentCaptor<LedgerEntry> ledgerEntryCaptor;

    @Captor
    private ArgumentCaptor<Transaction> transactionCaptor;

    private UUID sourceId;
    private UUID targetId;
    private Account sourceAccount;
    private Account targetAccount;
    private TransferRequest transferRequest;
    private String idempotencyKey;
    private TransferResponse transferResponse;

    @BeforeEach
    void setUp() {
        sourceId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        targetId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        sourceAccount = new Account(sourceId, "12345678901", "Source User", Instant.now(), null);
        targetAccount = new Account(targetId, "98765432109", "Target User", Instant.now(), null);
        transferRequest = new TransferRequest(sourceId, targetId, BigDecimal.valueOf(100));
        idempotencyKey = "unique-key-123";
        transferResponse = new TransferResponse(
                UUID.randomUUID(), sourceId, targetId, BigDecimal.valueOf(100),
                "COMPLETED", Instant.now()
        );
    }

    @Nested
    @DisplayName("executeTransfer()")
    class ExecuteTransferTests {

        @Test
        @DisplayName("should execute transfer successfully when sufficient funds")
        void should_ExecuteTransfer_When_SufficientFunds() {
            // Given
            BigDecimal sourceBalance = BigDecimal.valueOf(500);
            BigDecimal targetBalance = BigDecimal.valueOf(200);
            Transaction savedTransaction = new Transaction(
                    UUID.randomUUID(), idempotencyKey, sourceId, targetId,
                    BigDecimal.valueOf(100), TransactionStatus.COMPLETED, Instant.now()
            );

            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(accountRepository.findAllByIdForUpdateSorted(anyList())).thenReturn(List.of(sourceAccount, targetAccount));
            when(ledgerEntryRepository.getBalance(sourceId)).thenReturn(sourceBalance);
            when(ledgerEntryRepository.getBalance(targetId)).thenReturn(targetBalance);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transferResponse);

            // When
            TransferResponse response = transferService.executeTransfer(transferRequest, idempotencyKey);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("COMPLETED");
            verify(transactionRepository).findByIdempotencyKey(idempotencyKey);
            verify(ledgerEntryRepository, times(2)).save(any(LedgerEntry.class));
        }

        @Test
        @DisplayName("should return cached response when idempotency key exists")
        void should_ReturnCachedResponse_When_IdempotencyKeyExists() {
            // Given
            Transaction existingTransaction = new Transaction(
                    UUID.randomUUID(), idempotencyKey, sourceId, targetId,
                    BigDecimal.valueOf(100), TransactionStatus.COMPLETED, Instant.now()
            );
            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingTransaction));
            when(transactionMapper.toResponse(existingTransaction)).thenReturn(transferResponse);

            // When
            TransferResponse response = transferService.executeTransfer(transferRequest, idempotencyKey);

            // Then
            assertThat(response).isNotNull();
            verify(transactionRepository).findByIdempotencyKey(idempotencyKey);
            verify(accountRepository, never()).findAllByIdForUpdateSorted(anyList());
            verify(ledgerEntryRepository, never()).save(any(LedgerEntry.class));
        }

        @Test
        @DisplayName("should throw InsufficientFundsException when balance too low")
        void should_ThrowInsufficientFundsException_When_BalanceTooLow() {
            // Given
            BigDecimal sourceBalance = BigDecimal.valueOf(50); // Less than requested 100
            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(accountRepository.findAllByIdForUpdateSorted(anyList())).thenReturn(List.of(sourceAccount, targetAccount));
            when(ledgerEntryRepository.getBalance(sourceId)).thenReturn(sourceBalance);

            // When & Then
            assertThatThrownBy(() -> transferService.executeTransfer(transferRequest, idempotencyKey))
                    .isInstanceOf(InsufficientFundsException.class);

            verify(ledgerEntryRepository, never()).save(any(LedgerEntry.class));
        }

        @Test
        @DisplayName("should throw AccountNotFoundException when source account not found")
        void should_ThrowAccountNotFoundException_When_SourceNotFound() {
            // Given - only target account returned (source missing)
            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(accountRepository.findAllByIdForUpdateSorted(anyList())).thenReturn(List.of(targetAccount));

            // When & Then
            assertThatThrownBy(() -> transferService.executeTransfer(transferRequest, idempotencyKey))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(ledgerEntryRepository, never()).save(any(LedgerEntry.class));
        }

        @Test
        @DisplayName("should throw AccountNotFoundException when target account not found")
        void should_ThrowAccountNotFoundException_When_TargetNotFound() {
            // Given - only source account returned (target missing)
            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(accountRepository.findAllByIdForUpdateSorted(anyList())).thenReturn(List.of(sourceAccount));

            // When & Then
            assertThatThrownBy(() -> transferService.executeTransfer(transferRequest, idempotencyKey))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(ledgerEntryRepository, never()).save(any(LedgerEntry.class));
        }

        @Test
        @DisplayName("should create DEBIT entry for source account")
        void should_CreateDebitEntryForSource() {
            // Given
            BigDecimal sourceBalance = BigDecimal.valueOf(500);
            BigDecimal targetBalance = BigDecimal.valueOf(200);
            Transaction savedTransaction = new Transaction(
                    UUID.randomUUID(), idempotencyKey, sourceId, targetId,
                    BigDecimal.valueOf(100), TransactionStatus.COMPLETED, Instant.now()
            );

            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(accountRepository.findAllByIdForUpdateSorted(anyList())).thenReturn(List.of(sourceAccount, targetAccount));
            when(ledgerEntryRepository.getBalance(sourceId)).thenReturn(sourceBalance);
            when(ledgerEntryRepository.getBalance(targetId)).thenReturn(targetBalance);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transferResponse);

            // When
            transferService.executeTransfer(transferRequest, idempotencyKey);

            // Then
            verify(ledgerEntryRepository, times(2)).save(ledgerEntryCaptor.capture());
            List<LedgerEntry> entries = ledgerEntryCaptor.getAllValues();
            
            LedgerEntry debitEntry = entries.stream()
                    .filter(e -> e.getEntryType() == EntryType.DEBIT)
                    .findFirst()
                    .orElseThrow();
            
            assertThat(debitEntry.getAccountId()).isEqualTo(sourceId);
            assertThat(debitEntry.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(debitEntry.getBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(400)); // 500 - 100
        }

        @Test
        @DisplayName("should create CREDIT entry for target account")
        void should_CreateCreditEntryForTarget() {
            // Given
            BigDecimal sourceBalance = BigDecimal.valueOf(500);
            BigDecimal targetBalance = BigDecimal.valueOf(200);
            Transaction savedTransaction = new Transaction(
                    UUID.randomUUID(), idempotencyKey, sourceId, targetId,
                    BigDecimal.valueOf(100), TransactionStatus.COMPLETED, Instant.now()
            );

            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(accountRepository.findAllByIdForUpdateSorted(anyList())).thenReturn(List.of(sourceAccount, targetAccount));
            when(ledgerEntryRepository.getBalance(sourceId)).thenReturn(sourceBalance);
            when(ledgerEntryRepository.getBalance(targetId)).thenReturn(targetBalance);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transferResponse);

            // When
            transferService.executeTransfer(transferRequest, idempotencyKey);

            // Then
            verify(ledgerEntryRepository, times(2)).save(ledgerEntryCaptor.capture());
            List<LedgerEntry> entries = ledgerEntryCaptor.getAllValues();
            
            LedgerEntry creditEntry = entries.stream()
                    .filter(e -> e.getEntryType() == EntryType.CREDIT)
                    .findFirst()
                    .orElseThrow();
            
            assertThat(creditEntry.getAccountId()).isEqualTo(targetId);
            assertThat(creditEntry.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(creditEntry.getBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(300)); // 200 + 100
        }

        @Test
        @DisplayName("should set transaction status to COMPLETED")
        void should_SetTransactionStatusCompleted() {
            // Given
            BigDecimal sourceBalance = BigDecimal.valueOf(500);
            BigDecimal targetBalance = BigDecimal.valueOf(200);
            Transaction savedTransaction = new Transaction(
                    UUID.randomUUID(), idempotencyKey, sourceId, targetId,
                    BigDecimal.valueOf(100), TransactionStatus.COMPLETED, Instant.now()
            );

            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(accountRepository.findAllByIdForUpdateSorted(anyList())).thenReturn(List.of(sourceAccount, targetAccount));
            when(ledgerEntryRepository.getBalance(sourceId)).thenReturn(sourceBalance);
            when(ledgerEntryRepository.getBalance(targetId)).thenReturn(targetBalance);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(transferResponse);

            // When
            transferService.executeTransfer(transferRequest, idempotencyKey);

            // Then
            verify(transactionRepository, times(2)).save(transactionCaptor.capture());
            List<Transaction> savedTransactions = transactionCaptor.getAllValues();
            
            // Second save should be with COMPLETED status
            Transaction finalTransaction = savedTransactions.get(1);
            assertThat(finalTransaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        }

        @Test
        @DisplayName("should sort account IDs for locking when source > target")
        void should_SortAccountIdsForLocking_When_SourceGreaterThanTarget() {
            // Given - using simple sequential UUIDs where order is predictable
            // UUID.compareTo compares lexicographically, so we use UUIDs with clear ordering
            UUID firstId = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID secondId = UUID.fromString("00000000-0000-0000-0000-000000000002");
            
            // Request with larger ID as source (should get sorted to second position)
            TransferRequest request = new TransferRequest(secondId, firstId, BigDecimal.valueOf(100));
            
            Account firstAccount = new Account(firstId, "111", "First", Instant.now(), null);
            Account secondAccount = new Account(secondId, "222", "Second", Instant.now(), null);

            when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
            when(accountRepository.findAllByIdForUpdateSorted(anyList())).thenReturn(List.of(firstAccount, secondAccount));
            when(ledgerEntryRepository.getBalance(secondId)).thenReturn(BigDecimal.valueOf(500));
            when(ledgerEntryRepository.getBalance(firstId)).thenReturn(BigDecimal.valueOf(200));
            when(transactionRepository.save(any(Transaction.class))).thenReturn(
                    new Transaction(UUID.randomUUID(), idempotencyKey, secondId, firstId, 
                            BigDecimal.valueOf(100), TransactionStatus.COMPLETED, Instant.now())
            );
            when(transactionMapper.toResponse(any(Transaction.class))).thenReturn(
                    new TransferResponse(UUID.randomUUID(), secondId, firstId, 
                            BigDecimal.valueOf(100), "COMPLETED", Instant.now())
            );

            // When
            transferService.executeTransfer(request, idempotencyKey);

            // Then - verify that accounts were requested in sorted order
            ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(accountRepository).findAllByIdForUpdateSorted(idsCaptor.capture());
            List<UUID> requestedIds = idsCaptor.getValue();
            
            // IDs should be in sorted order (smaller first)
            assertThat(requestedIds).containsExactly(firstId, secondId);
        }
    }

    @Nested
    @DisplayName("getTransfer()")
    class GetTransferTests {

        @Test
        @DisplayName("should return transfer when transaction exists")
        void should_ReturnTransfer_When_TransactionExists() {
            // Given
            UUID transactionId = UUID.randomUUID();
            Transaction transaction = new Transaction(
                    transactionId, idempotencyKey, sourceId, targetId,
                    BigDecimal.valueOf(100), TransactionStatus.COMPLETED, Instant.now()
            );
            when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
            when(transactionMapper.toResponse(transaction)).thenReturn(transferResponse);

            // When
            TransferResponse response = transferService.getTransfer(transactionId);

            // Then
            assertThat(response).isNotNull();
            verify(transactionRepository).findById(transactionId);
        }

        @Test
        @DisplayName("should throw TransactionNotFoundException when transaction does not exist")
        void should_ThrowTransactionNotFoundException_When_TransactionNotExists() {
            // Given
            UUID transactionId = UUID.randomUUID();
            when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> transferService.getTransfer(transactionId))
                    .isInstanceOf(TransactionNotFoundException.class);

            verify(transactionRepository).findById(transactionId);
        }
    }
}
