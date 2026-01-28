package com.fintech.ledger.domain.dto.request;

import com.fintech.ledger.validation.DifferentAccounts;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for executing a transfer between accounts.
 * <p>
 * The source and target accounts must be different (validated via {@link DifferentAccounts}).
 *
 * @param sourceAccountId the account to debit
 * @param targetAccountId the account to credit
 * @param amount the amount to transfer (must be positive and at least 0.01)
 */
@DifferentAccounts
public record TransferRequest(
        @NotNull(message = "Source account ID is required")
        UUID sourceAccountId,

        @NotNull(message = "Target account ID is required")
        UUID targetAccountId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount
) {
}
