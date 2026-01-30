package com.fintech.ledger.unit.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fintech.ledger.domain.dto.response.AccountStatementResponse;
import com.fintech.ledger.domain.dto.response.LedgerEntryResponse;
import com.fintech.ledger.domain.entity.Account;
import com.fintech.ledger.domain.entity.EntryType;
import com.fintech.ledger.domain.entity.LedgerEntry;
import com.fintech.ledger.exception.AccountNotFoundException;
import com.fintech.ledger.mapper.LedgerEntryMapper;
import com.fintech.ledger.repository.AccountRepository;
import com.fintech.ledger.repository.LedgerEntryRepository;
import com.fintech.ledger.service.impl.LedgerServiceImpl;

/**
 * Unit tests for LedgerServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LedgerService")
class LedgerServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private LedgerEntryMapper ledgerEntryMapper;

    @InjectMocks
    private LedgerServiceImpl ledgerService;

    private UUID accountId;
    private Account account;
    private LedgerEntry ledgerEntry;
    private LedgerEntryResponse entryResponse;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account(accountId, "12345678901", "John Doe", Instant.now(), null);
        
        UUID transactionId = UUID.randomUUID();
        // Use the 5-arg constructor (transactionId, accountId, entryType, amount, balanceAfter)
        ledgerEntry = new LedgerEntry(
                transactionId, accountId,
                EntryType.CREDIT, BigDecimal.valueOf(100), BigDecimal.valueOf(1000)
        );
        ledgerEntry.setCreatedAt(Instant.now());
        ledgerEntry.setId(UUID.randomUUID());
        
        entryResponse = new LedgerEntryResponse(
                ledgerEntry.getId(), transactionId, "CREDIT",
                BigDecimal.valueOf(100), BigDecimal.valueOf(1000), Instant.now()
        );
    }

    @Nested
    @DisplayName("getAccountStatement() - unpaginated")
    class GetAccountStatementUnpaginatedTests {

        @Test
        @DisplayName("should return statement when account exists")
        void should_ReturnStatement_When_AccountExists() {
            // Given
            BigDecimal balance = BigDecimal.valueOf(1000);
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(ledgerEntryRepository.getBalance(accountId)).thenReturn(balance);
            when(ledgerEntryRepository.findRecentByAccountId(accountId, 100)).thenReturn(List.of(ledgerEntry));
            when(ledgerEntryMapper.toResponseList(List.of(ledgerEntry))).thenReturn(List.of(entryResponse));

            // When
            AccountStatementResponse response = ledgerService.getAccountStatement(accountId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accountId()).isEqualTo(accountId);
            assertThat(response.accountName()).isEqualTo("John Doe");
            assertThat(response.currentBalance()).isEqualTo(balance);
            assertThat(response.entries()).hasSize(1);
            verify(accountRepository).findById(accountId);
            verify(ledgerEntryRepository).getBalance(accountId);
        }

        @Test
        @DisplayName("should throw AccountNotFoundException when account does not exist")
        void should_ThrowAccountNotFoundException_When_AccountNotExists() {
            // Given
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> ledgerService.getAccountStatement(accountId))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(accountRepository).findById(accountId);
        }

        @Test
        @DisplayName("should return empty entries for new account")
        void should_ReturnEmptyEntries_When_NewAccount() {
            // Given
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(ledgerEntryRepository.getBalance(accountId)).thenReturn(BigDecimal.ZERO);
            when(ledgerEntryRepository.findRecentByAccountId(accountId, 100)).thenReturn(Collections.emptyList());
            when(ledgerEntryMapper.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            // When
            AccountStatementResponse response = ledgerService.getAccountStatement(accountId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.currentBalance()).isEqualTo(BigDecimal.ZERO);
            assertThat(response.entries()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getAccountStatement() - paginated")
    class GetAccountStatementPaginatedTests {

        @Test
        @DisplayName("should return paginated statement when account exists")
        void should_ReturnPaginatedStatement_When_AccountExists() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            BigDecimal balance = BigDecimal.valueOf(1000);
            Page<LedgerEntry> entryPage = new PageImpl<>(List.of(ledgerEntry));
            
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(ledgerEntryRepository.getBalance(accountId)).thenReturn(balance);
            when(ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)).thenReturn(entryPage);
            when(ledgerEntryMapper.toResponseList(List.of(ledgerEntry))).thenReturn(List.of(entryResponse));

            // When
            AccountStatementResponse response = ledgerService.getAccountStatement(accountId, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.accountId()).isEqualTo(accountId);
            assertThat(response.entries()).hasSize(1);
            verify(ledgerEntryRepository).findByAccountIdOrderByCreatedAtDesc(accountId, pageable);
        }

        @Test
        @DisplayName("should throw AccountNotFoundException for paginated request when account does not exist")
        void should_ThrowAccountNotFoundException_When_AccountNotExistsForPaginated() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> ledgerService.getAccountStatement(accountId, pageable))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(accountRepository).findById(accountId);
        }
    }
}
