package com.fintech.ledger.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.fintech.ledger.service.TransferService;

/**
 * Implementation of TransferService for atomic money transfers.
 * <p>
 * Handles the complete transfer flow with:
 * <ul>
 *   <li>Idempotency key checking for duplicate prevention</li>
 *   <li>Sorted lock acquisition for deadlock prevention</li>
 *   <li>Balance validation before transfer</li>
 *   <li>Double-entry bookkeeping with DEBIT and CREDIT entries</li>
 * </ul>
 */
@Service
@Transactional
public class TransferServiceImpl implements TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferServiceImpl.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final TransactionMapper transactionMapper;

    public TransferServiceImpl(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            LedgerEntryRepository ledgerEntryRepository,
            TransactionMapper transactionMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.transactionMapper = transactionMapper;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransferResponse executeTransfer(TransferRequest request, String idempotencyKey) {
        log.info("Starting transfer: source={}, target={}, amount={}, idempotencyKey={}",
                request.sourceAccountId(), request.targetAccountId(), request.amount(), idempotencyKey);

        // Step 1: Check idempotency - return cached response if exists
        Optional<Transaction> existingTransaction = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransaction.isPresent()) {
            log.info("Returning cached response for idempotency key: {}", idempotencyKey);
            return transactionMapper.toResponse(existingTransaction.get());
        }

        // Step 2: Sort account IDs for deadlock prevention
        UUID sourceId = request.sourceAccountId();
        UUID targetId = request.targetAccountId();
        UUID firstId = sourceId.compareTo(targetId) < 0 ? sourceId : targetId;
        UUID secondId = sourceId.compareTo(targetId) < 0 ? targetId : sourceId;

        // Step 3: Acquire pessimistic locks in sorted order
        log.debug("Acquiring locks in order: first={}, second={}", firstId, secondId);
        List<Account> lockedAccounts = accountRepository.findAllByIdForUpdateSorted(List.of(firstId, secondId));

        // Verify both accounts exist (locks are held for the transaction duration)
        if (lockedAccounts.stream().noneMatch(a -> a.getId().equals(firstId))) {
            throw new AccountNotFoundException(firstId);
        }
        if (lockedAccounts.stream().noneMatch(a -> a.getId().equals(secondId))) {
            throw new AccountNotFoundException(secondId);
        }

        // Step 4: Validate source account has sufficient funds
        BigDecimal sourceBalance = ledgerEntryRepository.getBalance(sourceId);
        BigDecimal transferAmount = request.amount();

        if (sourceBalance.compareTo(transferAmount) < 0) {
            log.warn("Insufficient funds: account={}, available={}, requested={}",
                    sourceId, sourceBalance, transferAmount);
            throw new InsufficientFundsException(sourceId, sourceBalance, transferAmount);
        }

        // Step 5: Create Transaction record with PENDING status
        Transaction transaction = new Transaction(
                idempotencyKey,
                sourceId,
                targetId,
                transferAmount,
                TransactionStatus.PENDING
        );
        Transaction savedTransaction = transactionRepository.save(transaction);
        log.debug("Created transaction with ID: {}", savedTransaction.getId());

        // Step 6: Create DEBIT entry for source account
        BigDecimal newSourceBalance = sourceBalance.subtract(transferAmount);
        LedgerEntry debitEntry = new LedgerEntry(
                savedTransaction.getId(),
                sourceId,
                EntryType.DEBIT,
                transferAmount,
                newSourceBalance
        );
        ledgerEntryRepository.save(debitEntry);
        log.debug("Created DEBIT entry for source account: {}", sourceId);

        // Step 7: Create CREDIT entry for target account
        BigDecimal targetBalance = ledgerEntryRepository.getBalance(targetId);
        BigDecimal newTargetBalance = targetBalance.add(transferAmount);
        LedgerEntry creditEntry = new LedgerEntry(
                savedTransaction.getId(),
                targetId,
                EntryType.CREDIT,
                transferAmount,
                newTargetBalance
        );
        ledgerEntryRepository.save(creditEntry);
        log.debug("Created CREDIT entry for target account: {}", targetId);

        // Step 8: Update Transaction status to COMPLETED
        savedTransaction.setStatus(TransactionStatus.COMPLETED);
        Transaction completedTransaction = transactionRepository.save(savedTransaction);

        log.info("Transfer completed successfully: transactionId={}, source={}, target={}, amount={}",
                completedTransaction.getId(), sourceId, targetId, transferAmount);

        return transactionMapper.toResponse(completedTransaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TransferResponse getTransfer(UUID transactionId) {
        log.debug("Fetching transaction with ID: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found with ID: {}", transactionId);
                    return new TransactionNotFoundException(transactionId);
                });

        return transactionMapper.toResponse(transaction);
    }
}
