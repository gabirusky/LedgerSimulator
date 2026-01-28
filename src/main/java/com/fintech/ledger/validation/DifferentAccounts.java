package com.fintech.ledger.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation to ensure source and target accounts are different.
 * <p>
 * This annotation should be applied to {@code TransferRequest} records or classes
 * that have {@code sourceAccountId} and {@code targetAccountId} fields.
 */
@Documented
@Constraint(validatedBy = DifferentAccountsValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DifferentAccounts {
    
    String message() default "Source and target accounts must be different";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
