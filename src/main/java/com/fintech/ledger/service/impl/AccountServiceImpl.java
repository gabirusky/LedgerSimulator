package com.fintech.ledger.service.impl;

import java.math.BigDecimal;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;
import com.fintech.ledger.domain.entity.Account;
import com.fintech.ledger.exception.AccountNotFoundException;
import com.fintech.ledger.exception.DuplicateDocumentException;
import com.fintech.ledger.mapper.AccountMapper;
import com.fintech.ledger.repository.AccountRepository;
import com.fintech.ledger.repository.LedgerEntryRepository;
import com.fintech.ledger.service.AccountService;

/**
 * Implementation of AccountService for account management operations.
 * <p>
 * Handles account creation with duplicate detection, account retrieval
 * with balance calculation from ledger entries, and paginated listing.
 */
@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountMapper accountMapper;

    public AccountServiceImpl(
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountMapper = accountMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account with document: {}", maskDocument(request.document()));

        // Check for duplicate document
        if (accountRepository.existsByDocument(request.document())) {
            log.warn("Duplicate account creation attempt for document: {}", maskDocument(request.document()));
            throw new DuplicateDocumentException(request.document());
        }

        // Create and save account
        Account account = accountMapper.toEntity(request);
        Account savedAccount = accountRepository.save(account);

        log.info("Account created successfully with ID: {}", savedAccount.getId());

        // New accounts have zero balance
        return accountMapper.toResponse(savedAccount, BigDecimal.ZERO);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResponse getAccount(UUID id) {
        log.debug("Fetching account with ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Account not found with ID: {}", id);
                    return new AccountNotFoundException(id);
                });

        // Use fast-path balance lookup
        BigDecimal balance = ledgerEntryRepository.getBalance(id);

        return accountMapper.toResponse(account, balance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getAccountBalance(UUID id) {
        log.debug("Fetching balance for account ID: {}", id);

        // Verify account exists
        if (!accountRepository.existsById(id)) {
            log.warn("Account not found with ID: {}", id);
            throw new AccountNotFoundException(id);
        }

        // Use fast-path balance lookup
        return ledgerEntryRepository.getBalance(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        log.debug("Fetching all accounts with pagination: {}", pageable);

        return accountRepository.findAll(pageable)
                .map(account -> {
                    BigDecimal balance = ledgerEntryRepository.getBalance(account.getId());
                    return accountMapper.toResponse(account, balance);
                });
    }

    /**
     * Masks a document number for logging purposes (privacy).
     * Shows only last 4 characters, e.g., "123.456.789-00" -> "***-00"
     */
    private String maskDocument(String document) {
        if (document == null || document.length() <= 4) {
            return "****";
        }
        return "***" + document.substring(document.length() - 4);
    }
}
