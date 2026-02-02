package com.fintech.ledger.integration.controller;

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
import com.fintech.ledger.domain.dto.response.AccountStatementResponse;
import com.fintech.ledger.domain.dto.response.ErrorResponse;
import com.fintech.ledger.integration.AbstractIntegrationTest;

/**
 * Integration tests for LedgerController.
 * <p>
 * Tests HTTP endpoints for account statement retrieval using full Spring context
 * and real database via Testcontainers.
 * <p>
 * Tasks: 303-305
 */
class LedgerControllerIT extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String ACCOUNTS_URL = "/api/v1/accounts";
    private static final String LEDGER_URL = "/api/v1/ledger";

    @Nested
    @DisplayName("TASK-304: GET /api/v1/ledger/{accountId} - 200 OK")
    class GetStatementSuccessTests {

        @Test
        @DisplayName("should return account statement with 200 OK")
        void shouldReturnAccountStatementSuccessfully() {
            // Given - Create an account
            CreateAccountRequest request = new CreateAccountRequest(
                    "ledger-" + UUID.randomUUID().toString().substring(0, 8),
                    "Ledger Test User"
            );
            ResponseEntity<AccountResponse> accountResponse = restTemplate.postForEntity(
                    ACCOUNTS_URL, request, AccountResponse.class);
            UUID accountId = accountResponse.getBody().id();

            // When
            ResponseEntity<AccountStatementResponse> response = restTemplate.getForEntity(
                    LEDGER_URL + "/" + accountId,
                    AccountStatementResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().accountId()).isEqualTo(accountId);
            assertThat(response.getBody().accountName()).isEqualTo("Ledger Test User");
            assertThat(response.getBody().entries()).isEmpty(); // New account has no entries
        }

        @Test
        @DisplayName("should return empty entries for new account")
        void shouldReturnEmptyEntriesForNewAccount() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest(
                    "empty-" + UUID.randomUUID().toString().substring(0, 8),
                    "Empty Account"
            );
            ResponseEntity<AccountResponse> accountResponse = restTemplate.postForEntity(
                    ACCOUNTS_URL, request, AccountResponse.class);
            UUID accountId = accountResponse.getBody().id();

            // When
            ResponseEntity<AccountStatementResponse> response = restTemplate.getForEntity(
                    LEDGER_URL + "/" + accountId,
                    AccountStatementResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().entries()).isEmpty();
            assertThat(response.getBody().currentBalance()).isEqualByComparingTo(java.math.BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("TASK-305: GET /api/v1/ledger/{accountId} - 404 Not Found")
    class GetStatementNotFoundTests {

        @Test
        @DisplayName("should return 404 for non-existent account")
        void shouldReturn404ForNonExistentAccount() {
            // Given
            UUID nonExistentId = UUID.randomUUID();

            // When
            ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                    LEDGER_URL + "/" + nonExistentId,
                    ErrorResponse.class
            );

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
        }
    }
}
