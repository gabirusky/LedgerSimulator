package com.fintech.ledger.validation;

import com.fintech.ledger.domain.dto.request.TransferRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for {@link DifferentAccounts} annotation.
 * <p>
 * Validates that the source and target account IDs are not the same
 * in a transfer request.
 */
public class DifferentAccountsValidator implements ConstraintValidator<DifferentAccounts, TransferRequest> {

    @Override
    public void initialize(DifferentAccounts constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(TransferRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let @NotNull handle null validation
        }

        if (request.sourceAccountId() == null || request.targetAccountId() == null) {
            return true; // Let @NotNull handle null validation
        }

        return !request.sourceAccountId().equals(request.targetAccountId());
    }
}
