package com.fintech.ledger.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.ledger.domain.dto.response.AccountStatementResponse;
import com.fintech.ledger.domain.dto.response.LedgerEntryResponse;
import com.fintech.ledger.domain.entity.Account;
import com.fintech.ledger.domain.entity.LedgerEntry;
import com.fintech.ledger.exception.AccountNotFoundException;
import com.fintech.ledger.mapper.LedgerEntryMapper;
import com.fintech.ledger.repository.AccountRepository;
import com.fintech.ledger.repository.LedgerEntryRepository;
import com.fintech.ledger.service.LedgerService;

/**
 * Implementation of LedgerService for account statement operations.
 * <p>
 * Provides methods for retrieving account transaction history
 * with support for both paginated and full statement retrieval.
 */
@Service
@Transactional(readOnly = true)
public class LedgerServiceImpl implements LedgerService {

    private static final Logger log = LoggerFactory.getLogger(LedgerServiceImpl.class);

    private static final int DEFAULT_STATEMENT_LIMIT = 100;

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerEntryMapper ledgerEntryMapper;

    public LedgerServiceImpl(
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            LedgerEntryMapper ledgerEntryMapper) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.ledgerEntryMapper = ledgerEntryMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountStatementResponse getAccountStatement(UUID accountId) {
        log.debug("Fetching full statement for account ID: {}", accountId);

        // Validate account exists and get details
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found with ID: {}", accountId);
                    return new AccountNotFoundException(accountId);
                });

        // Get current balance using fast-path
        BigDecimal currentBalance = ledgerEntryRepository.getBalance(accountId);

        // Get recent entries using cursor-based method (limited for safety)
        List<LedgerEntry> entries = ledgerEntryRepository.findRecentByAccountId(
                accountId, DEFAULT_STATEMENT_LIMIT);

        List<LedgerEntryResponse> entryResponses = ledgerEntryMapper.toResponseList(entries);

        log.debug("Retrieved {} entries for account {}", entries.size(), accountId);

        return new AccountStatementResponse(
                account.getId(),
                account.getName(),
                currentBalance,
                entryResponses
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountStatementResponse getAccountStatement(UUID accountId, Pageable pageable) {
        log.debug("Fetching paginated statement for account ID: {}, page: {}",
                accountId, pageable);

        // Validate account exists and get details
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found with ID: {}", accountId);
                    return new AccountNotFoundException(accountId);
                });

        // Get current balance using fast-path
        BigDecimal currentBalance = ledgerEntryRepository.getBalance(accountId);

        // Get paginated entries
        Page<LedgerEntry> entriesPage = ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(
                accountId, pageable);

        List<LedgerEntryResponse> entryResponses = ledgerEntryMapper.toResponseList(
                entriesPage.getContent());

        log.debug("Retrieved {} entries (page {} of {}) for account {}",
                entriesPage.getNumberOfElements(),
                entriesPage.getNumber(),
                entriesPage.getTotalPages(),
                accountId);

        return new AccountStatementResponse(
                account.getId(),
                account.getName(),
                currentBalance,
                entryResponses
        );
    }
}
