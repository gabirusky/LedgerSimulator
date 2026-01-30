package com.fintech.ledger.unit.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;
import com.fintech.ledger.domain.entity.Account;
import com.fintech.ledger.mapper.AccountMapper;

/**
 * Unit tests for AccountMapper.
 */
@DisplayName("AccountMapper")
class AccountMapperTest {

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = Mappers.getMapper(AccountMapper.class);
    }

    @Nested
    @DisplayName("toEntity()")
    class ToEntityTests {

        @Test
        @DisplayName("should map document and name from request")
        void should_MapDocumentAndName_FromRequest() {
            // Given
            CreateAccountRequest request = new CreateAccountRequest("12345678901", "John Doe");

            // When
            Account account = accountMapper.toEntity(request);

            // Then
            assertThat(account).isNotNull();
            assertThat(account.getDocument()).isEqualTo("12345678901");
            assertThat(account.getName()).isEqualTo("John Doe");
            assertThat(account.getId()).isNull(); // ID should be ignored
            assertThat(account.getCreatedAt()).isNull(); // Timestamp should be ignored
            assertThat(account.getUpdatedAt()).isNull(); // Timestamp should be ignored
        }

        @Test
        @DisplayName("should return null when request is null")
        void should_ReturnNull_When_RequestIsNull() {
            // When
            Account account = accountMapper.toEntity(null);

            // Then
            assertThat(account).isNull();
        }
    }

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should map all fields including balance")
        void should_MapAllFields_IncludingBalance() {
            // Given
            UUID id = UUID.randomUUID();
            Instant createdAt = Instant.now();
            Account account = new Account(id, "12345678901", "John Doe", createdAt, null);
            BigDecimal balance = BigDecimal.valueOf(1000.50);

            // When
            AccountResponse response = accountMapper.toResponse(account, balance);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.document()).isEqualTo("12345678901");
            assertThat(response.name()).isEqualTo("John Doe");
            assertThat(response.balance()).isEqualTo(balance);
            assertThat(response.createdAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("should handle zero balance")
        void should_HandleZeroBalance() {
            // Given
            UUID id = UUID.randomUUID();
            Account account = new Account(id, "12345678901", "John Doe", Instant.now(), null);
            BigDecimal balance = BigDecimal.ZERO;

            // When
            AccountResponse response = accountMapper.toResponse(account, balance);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.balance()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should handle null account with provided balance")
        void should_HandleNullAccount_WithProvidedBalance() {
            // When
            AccountResponse response = accountMapper.toResponse(null, BigDecimal.ZERO);

            // Then - MapStruct returns object with null fields but provided balance
            assertThat(response).isNotNull();
            assertThat(response.id()).isNull();
            assertThat(response.document()).isNull();
            assertThat(response.name()).isNull();
            assertThat(response.balance()).isEqualTo(BigDecimal.ZERO);
            assertThat(response.createdAt()).isNull();
        }
    }
}
