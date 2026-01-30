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
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;
import com.fintech.ledger.domain.entity.Account;
import com.fintech.ledger.exception.AccountNotFoundException;
import com.fintech.ledger.exception.DuplicateDocumentException;
import com.fintech.ledger.mapper.AccountMapper;
import com.fintech.ledger.repository.AccountRepository;
import com.fintech.ledger.repository.LedgerEntryRepository;
import com.fintech.ledger.service.impl.AccountServiceImpl;

/**
 * Unit tests for AccountServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService")
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerEntryRepository ledgerEntryRepository;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    private UUID accountId;
    private Account account;
    private AccountResponse accountResponse;
    private CreateAccountRequest createRequest;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account(accountId, "12345678901", "John Doe", Instant.now(), null);
        accountResponse = new AccountResponse(accountId, "12345678901", "John Doe", BigDecimal.valueOf(1000), Instant.now());
        createRequest = new CreateAccountRequest("12345678901", "John Doe");
    }

    @Nested
    @DisplayName("createAccount()")
    class CreateAccountTests {

        @Test
        @DisplayName("should create account successfully when document does not exist")
        void should_CreateAccount_When_DocumentNotExists() {
            // Given
            when(accountRepository.existsByDocument("12345678901")).thenReturn(false);
            when(accountMapper.toEntity(createRequest)).thenReturn(account);
            when(accountRepository.save(account)).thenReturn(account);
            when(accountMapper.toResponse(account, BigDecimal.ZERO)).thenReturn(
                    new AccountResponse(accountId, "12345678901", "John Doe", BigDecimal.ZERO, Instant.now())
            );

            // When
            AccountResponse response = accountService.createAccount(createRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(accountId);
            assertThat(response.balance()).isEqualTo(BigDecimal.ZERO);
            verify(accountRepository).existsByDocument("12345678901");
            verify(accountRepository).save(account);
        }

        @Test
        @DisplayName("should throw DuplicateDocumentException when document already exists")
        void should_ThrowDuplicateDocumentException_When_DocumentExists() {
            // Given
            when(accountRepository.existsByDocument("12345678901")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> accountService.createAccount(createRequest))
                    .isInstanceOf(DuplicateDocumentException.class);

            verify(accountRepository).existsByDocument("12345678901");
            verify(accountRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getAccount()")
    class GetAccountTests {

        @Test
        @DisplayName("should return account with balance when account exists")
        void should_ReturnAccountWithBalance_When_AccountExists() {
            // Given
            BigDecimal balance = BigDecimal.valueOf(1000);
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(ledgerEntryRepository.getBalance(accountId)).thenReturn(balance);
            when(accountMapper.toResponse(account, balance)).thenReturn(accountResponse);

            // When
            AccountResponse response = accountService.getAccount(accountId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(accountId);
            assertThat(response.balance()).isEqualTo(balance);
            verify(accountRepository).findById(accountId);
            verify(ledgerEntryRepository).getBalance(accountId);
        }

        @Test
        @DisplayName("should throw AccountNotFoundException when account does not exist")
        void should_ThrowAccountNotFoundException_When_AccountNotExists() {
            // Given
            when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> accountService.getAccount(accountId))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(accountRepository).findById(accountId);
            verify(ledgerEntryRepository, never()).getBalance(any());
        }
    }

    @Nested
    @DisplayName("getAccountBalance()")
    class GetAccountBalanceTests {

        @Test
        @DisplayName("should return balance when account exists")
        void should_ReturnBalance_When_AccountExists() {
            // Given
            BigDecimal expectedBalance = BigDecimal.valueOf(500);
            when(accountRepository.existsById(accountId)).thenReturn(true);
            when(ledgerEntryRepository.getBalance(accountId)).thenReturn(expectedBalance);

            // When
            BigDecimal balance = accountService.getAccountBalance(accountId);

            // Then
            assertThat(balance).isEqualTo(expectedBalance);
            verify(accountRepository).existsById(accountId);
            verify(ledgerEntryRepository).getBalance(accountId);
        }

        @Test
        @DisplayName("should throw AccountNotFoundException when account does not exist")
        void should_ThrowAccountNotFoundException_When_AccountNotExistsForBalance() {
            // Given
            when(accountRepository.existsById(accountId)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> accountService.getAccountBalance(accountId))
                    .isInstanceOf(AccountNotFoundException.class);

            verify(accountRepository).existsById(accountId);
            verify(ledgerEntryRepository, never()).getBalance(any());
        }
    }

    @Nested
    @DisplayName("getAllAccounts()")
    class GetAllAccountsTests {

        @Test
        @DisplayName("should return paginated accounts with balances")
        void should_ReturnPaginatedAccounts_WithBalances() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Account> accountPage = new PageImpl<>(List.of(account));
            BigDecimal balance = BigDecimal.valueOf(1000);
            
            when(accountRepository.findAll(pageable)).thenReturn(accountPage);
            when(ledgerEntryRepository.getBalance(accountId)).thenReturn(balance);
            when(accountMapper.toResponse(account, balance)).thenReturn(accountResponse);

            // When
            Page<AccountResponse> response = accountService.getAllAccounts(pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).id()).isEqualTo(accountId);
            verify(accountRepository).findAll(pageable);
        }

        @Test
        @DisplayName("should return empty page when no accounts exist")
        void should_ReturnEmptyPage_When_NoAccountsExist() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Account> emptyPage = Page.empty(pageable);
            when(accountRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            Page<AccountResponse> response = accountService.getAllAccounts(pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
            verify(accountRepository).findAll(pageable);
        }
    }
}
