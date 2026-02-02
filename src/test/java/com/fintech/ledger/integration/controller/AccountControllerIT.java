package com.fintech.ledger.integration.controller;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;
import com.fintech.ledger.domain.dto.response.ErrorResponse;
import com.fintech.ledger.integration.AbstractIntegrationTest;

/**
 * Integration tests for AccountController.
 * <p>
 * Tests HTTP endpoints for account management using full Spring context
 * and real database via Testcontainers.
 * <p>
 * Tasks: 289-296
 */
class AccountControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BASE_URL = "/api/v1/accounts";

    @Nested
    @DisplayName("TASK-292: POST /api/v1/accounts - 201 Created")
    class CreateAccountSuccessTests {

        @Test
        @DisplayName("should create account and return 201 Created")
        void shouldCreateAccountSuccessfully() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest(
                    "12345678901",
                    "John Doe"
            );

            // When
            ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                    BASE_URL,
                    request,
                    AccountResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isNotNull();
            assertThat(response.getBody().document()).isEqualTo("12345678901");
            assertThat(response.getBody().name()).isEqualTo("John Doe");
            assertThat(response.getBody().balance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getBody().createdAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("TASK-293: POST /api/v1/accounts - 409 Duplicate")
    class CreateAccountDuplicateTests {

        @Test
        @DisplayName("should return 409 Conflict for duplicate document")
        void shouldReturn409ForDuplicateDocument() {
            // Given - Create first account
            CreateAccountRequest request = new CreateAccountRequest(
                    "99999999999",
                    "First User"
            );
            restTemplate.postForEntity(BASE_URL, request, AccountResponse.class);

            // When - Try to create duplicate
            CreateAccountRequest duplicate = new CreateAccountRequest(
                    "99999999999",
                    "Second User"
            );
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    BASE_URL,
                    duplicate,
                    ErrorResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
        }
    }

    @Nested
    @DisplayName("TASK-294: POST /api/v1/accounts - 400 Validation Error")
    class CreateAccountValidationTests {

        @Test
        @DisplayName("should return 400 for blank document")
        void shouldReturn400ForBlankDocument() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest(
                    "",
                    "Valid Name"
            );

            // When
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    BASE_URL,
                    request,
                    ErrorResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("should return 400 for blank name")
        void shouldReturn400ForBlankName() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest(
                    "11111111111",
                    ""
            );

            // When
            ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
                    BASE_URL,
                    request,
                    ErrorResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("TASK-295: GET /api/v1/accounts/{id} - 200 OK")
    class GetAccountSuccessTests {

        @Test
        @DisplayName("should return account with 200 OK")
        void shouldReturnAccountSuccessfully() {
            // Given - Create an account first
            CreateAccountRequest request = new CreateAccountRequest(
                    "55555555555",
                    "Test User"
            );
            ResponseEntity<AccountResponse> created = restTemplate.postForEntity(
                    BASE_URL,
                    request,
                    AccountResponse.class
            );
            UUID accountId = created.getBody().id();

            // When
            ResponseEntity<AccountResponse> response = restTemplate.getForEntity(
                    BASE_URL + "/" + accountId,
                    AccountResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isEqualTo(accountId);
            assertThat(response.getBody().document()).isEqualTo("55555555555");
        }
    }

    @Nested
    @DisplayName("TASK-296: GET /api/v1/accounts/{id} - 404 Not Found")
    class GetAccountNotFoundTests {

        @Test
        @DisplayName("should return 404 for non-existent account")
        void shouldReturn404ForNonExistentAccount() {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            // When
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                    BASE_URL + "/" + nonExistentId,
                    ErrorResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
        }
    }
}
