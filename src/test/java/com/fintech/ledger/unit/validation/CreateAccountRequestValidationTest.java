package com.fintech.ledger.unit.validation;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * Validation tests for CreateAccountRequest DTO.
 */
@DisplayName("CreateAccountRequest Validation")
class CreateAccountRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("document field")
    class DocumentValidationTests {

        @Test
        @DisplayName("should fail validation when document is null")
        void should_FailValidation_When_DocumentIsNull() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest(null, "John Doe");

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("document"));
        }

        @Test
        @DisplayName("should fail validation when document is blank")
        void should_FailValidation_When_DocumentIsBlank() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest("   ", "John Doe");

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("document"));
        }

        @Test
        @DisplayName("should fail validation when document is empty")
        void should_FailValidation_When_DocumentIsEmpty() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest("", "John Doe");

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("document"));
        }

        @Test
        @DisplayName("should fail validation when document exceeds max length")
        void should_FailValidation_When_DocumentExceedsMaxLength() {
            // Given
            String longDocument = "a".repeat(51); // Max is 50
            CreateAccountRequest request = new CreateAccountRequest(longDocument, "John Doe");

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("document"));
        }
    }

    @Nested
    @DisplayName("name field")
    class NameValidationTests {

        @Test
        @DisplayName("should fail validation when name is null")
        void should_FailValidation_When_NameIsNull() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest("12345678901", null);

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("should fail validation when name is blank")
        void should_FailValidation_When_NameIsBlank() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest("12345678901", "   ");

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("should fail validation when name is empty")
        void should_FailValidation_When_NameIsEmpty() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest("12345678901", "");

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }

        @Test
        @DisplayName("should fail validation when name exceeds max length")
        void should_FailValidation_When_NameExceedsMaxLength() {
            // Given
            String longName = "a".repeat(256); // Max is 255
            CreateAccountRequest request = new CreateAccountRequest("12345678901", longName);

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isNotEmpty();
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        }
    }

    @Nested
    @DisplayName("valid request")
    class ValidRequestTests {

        @Test
        @DisplayName("should pass validation with valid document and name")
        void should_PassValidation_When_ValidRequest() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest("12345678901", "John Doe");

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("should pass validation with maximum length values")
        void should_PassValidation_When_MaxLengthValues() {
            // Given
            String maxDocument = "a".repeat(50);
            String maxName = "a".repeat(255);
            CreateAccountRequest request = new CreateAccountRequest(maxDocument, maxName);

            // When
            Set<ConstraintViolation<CreateAccountRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).isEmpty();
        }
    }
}
