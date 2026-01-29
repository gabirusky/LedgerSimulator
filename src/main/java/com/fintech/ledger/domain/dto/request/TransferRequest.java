package com.fintech.ledger.domain.dto.request;

import com.fintech.ledger.validation.DifferentAccounts;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body for executing a money transfer")
@DifferentAccounts
public record TransferRequest(
        @Schema(description = "UUID of the account to debit (sender)", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotNull(message = "Source account ID is required")
        UUID sourceAccountId,

        @Schema(description = "UUID of the account to credit (receiver)", example = "550e8400-e29b-41d4-a716-446655440001")
        @NotNull(message = "Target account ID is required")
        UUID targetAccountId,

        @Schema(description = "Amount to transfer (minimum 0.01)", example = "100.00")
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        BigDecimal amount
) {
}
