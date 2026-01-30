package com.fintech.ledger.unit.validation;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fintech.ledger.domain.dto.request.TransferRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Validation tests for TransferRequest DTO.
 */
@DisplayName("TransferRequest Validation")
class TransferRequestValidationTest {

    private Validator validator;
    
    private UUID sourceId;
    private UUID targetId;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        sourceId = UUID.randomUUID();
        targetId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("amount field")
    class AmountValidationTests {

        @Test
        @DisplayName("should fail validation when amount is null")
        void should_FailValidation_When_AmountIsNull() {
            // Given
            TransferRequest request = new TransferRequest(sourceId, targetId, null);

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("should fail validation when amount is negative")
        void should_FailValidation_When_AmountIsNegative() {
            // Given
            TransferRequest request = new TransferRequest(sourceId, targetId, BigDecimal.valueOf(-100));

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("should fail validation when amount is zero")
        void should_FailValidation_When_AmountIsZero() {
            // Given
            TransferRequest request = new TransferRequest(sourceId, targetId, BigDecimal.ZERO);

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("should fail validation when amount is less than 0.01")
        void should_FailValidation_When_AmountLessThanMinimum() {
            // Given
            TransferRequest request = new TransferRequest(sourceId, targetId, new BigDecimal("0.001"));

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("amount"));
        }

        @Test
        @DisplayName("should pass validation when amount is exactly 0.01")
        void should_PassValidation_When_AmountIsMinimum() {
            // Given
            TransferRequest request = new TransferRequest(sourceId, targetId, new BigDecimal("0.01"));

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("account IDs")
    class AccountIdValidationTests {

        @Test
        @DisplayName("should fail validation when source account ID is null")
        void should_FailValidation_When_SourceAccountIdIsNull() {
            // Given
            TransferRequest request = new TransferRequest(null, targetId, BigDecimal.valueOf(100));

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("sourceAccountId"));
        }

        @Test
        @DisplayName("should fail validation when target account ID is null")
        void should_FailValidation_When_TargetAccountIdIsNull() {
            // Given
            TransferRequest request = new TransferRequest(sourceId, null, BigDecimal.valueOf(100));

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("targetAccountId"));
        }
    }

    @Nested
    @DisplayName("@DifferentAccounts constraint")
    class DifferentAccountsValidationTests {

        @Test
        @DisplayName("should fail validation when source and target are the same")
        void should_FailValidation_When_SourceEqualsTarget() {
            // Given
            UUID sameId = UUID.randomUUID();
            TransferRequest request = new TransferRequest(sameId, sameId, BigDecimal.valueOf(100));

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            // The @DifferentAccounts annotation applies to the class level
            assertThat(violations).anyMatch(v -> 
                    v.getMessage().contains("Source and target accounts must be different") ||
                    v.getRootBeanClass().equals(TransferRequest.class));
        }
    }

    @Nested
    @DisplayName("valid request")
    class ValidRequestTests {

        @Test
        @DisplayName("should pass validation with valid request")
        void should_PassValidation_When_ValidRequest() {
            // Given
            TransferRequest request = new TransferRequest(sourceId, targetId, BigDecimal.valueOf(100));

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should pass validation with large amount")
        void should_PassValidation_When_LargeAmount() {
            // Given
            TransferRequest request = new TransferRequest(
                    sourceId, targetId, new BigDecimal("9999999999999999999.99")
            );

            // When
            Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }
}
