package com.fintech.ledger.service;

import java.util.UUID;

import com.fintech.ledger.domain.dto.request.TransferRequest;
import com.fintech.ledger.domain.dto.response.TransferResponse;

/**
 * Service interface for transfer operations.
 * <p>
 * Handles atomic money transfers between accounts using double-entry
 * bookkeeping principles. All transfers are idempotent based on
 * the provided idempotency key.
 */
public interface TransferService {

    /**
     * Executes an atomic transfer between two accounts.
     * <p>
     * This operation:
     * <ul>
     *   <li>Checks idempotency key for duplicate prevention</li>
     *   <li>Acquires locks in sorted order to prevent deadlocks</li>
     *   <li>Validates sufficient funds in source account</li>
     *   <li>Creates DEBIT entry for source account</li>
     *   <li>Creates CREDIT entry for target account</li>
     *   <li>Records the transaction with COMPLETED status</li>
     * </ul>
     *
     * @param request the transfer request with source, target, and amount
     * @param idempotencyKey unique key for duplicate prevention
     * @return the transfer response with transaction details
     * @throws com.fintech.ledger.exception.AccountNotFoundException if source or target not found
     * @throws com.fintech.ledger.exception.InsufficientFundsException if source has insufficient funds
     */
    TransferResponse executeTransfer(TransferRequest request, String idempotencyKey);

    /**
     * Retrieves a transfer/transaction by its unique identifier.
     *
     * @param transactionId the transaction UUID
     * @return the transfer response with transaction details
     * @throws com.fintech.ledger.exception.TransactionNotFoundException if transaction not found
     */
    TransferResponse getTransfer(UUID transactionId);
}
