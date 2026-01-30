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

import com.fintech.ledger.domain.dto.response.TransferResponse;
import com.fintech.ledger.domain.entity.Transaction;
import com.fintech.ledger.domain.entity.TransactionStatus;
import com.fintech.ledger.mapper.TransactionMapper;

/**
 * Unit tests for TransactionMapper.
 */
@DisplayName("TransactionMapper")
class TransactionMapperTest {

    private TransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        transactionMapper = Mappers.getMapper(TransactionMapper.class);
    }

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should map all transaction fields correctly")
        void should_MapAllFields_Correctly() {
            // Given
            UUID transactionId = UUID.randomUUID();
            UUID sourceId = UUID.randomUUID();
            UUID targetId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(100);
            Instant createdAt = Instant.now();
            
            Transaction transaction = new Transaction(
                    transactionId, "idempotency-key-123", sourceId, targetId,
                    amount, TransactionStatus.COMPLETED, createdAt
            );

            // When
            TransferResponse response = transactionMapper.toResponse(transaction);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.transactionId()).isEqualTo(transactionId);
            assertThat(response.sourceAccountId()).isEqualTo(sourceId);
            assertThat(response.targetAccountId()).isEqualTo(targetId);
            assertThat(response.amount()).isEqualTo(amount);
            assertThat(response.createdAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("should convert COMPLETED status to string")
        void should_ConvertCompletedStatus_ToString() {
            // Given
            Transaction transaction = new Transaction(
                    UUID.randomUUID(), "key", UUID.randomUUID(), UUID.randomUUID(),
                    BigDecimal.valueOf(100), TransactionStatus.COMPLETED, Instant.now()
            );

            // When
            TransferResponse response = transactionMapper.toResponse(transaction);

            // Then
            assertThat(response.status()).isEqualTo("COMPLETED");
        }

        @Test
        @DisplayName("should convert PENDING status to string")
        void should_ConvertPendingStatus_ToString() {
            // Given
            Transaction transaction = new Transaction(
                    UUID.randomUUID(), "key", UUID.randomUUID(), UUID.randomUUID(),
                    BigDecimal.valueOf(100), TransactionStatus.PENDING, Instant.now()
            );

            // When
            TransferResponse response = transactionMapper.toResponse(transaction);

            // Then
            assertThat(response.status()).isEqualTo("PENDING");
        }

        @Test
        @DisplayName("should convert FAILED status to string")
        void should_ConvertFailedStatus_ToString() {
            // Given
            Transaction transaction = new Transaction(
                    UUID.randomUUID(), "key", UUID.randomUUID(), UUID.randomUUID(),
                    BigDecimal.valueOf(100), TransactionStatus.FAILED, Instant.now()
            );

            // When
            TransferResponse response = transactionMapper.toResponse(transaction);

            // Then
            assertThat(response.status()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("should handle null status gracefully")
        void should_HandleNullStatus_Gracefully() {
            // Given
            Transaction transaction = new Transaction(
                    UUID.randomUUID(), "key", UUID.randomUUID(), UUID.randomUUID(),
                    BigDecimal.valueOf(100), null, Instant.now()
            );

            // When
            TransferResponse response = transactionMapper.toResponse(transaction);

            // Then
            assertThat(response.status()).isNull();
        }

        @Test
        @DisplayName("should return null when transaction is null")
        void should_ReturnNull_When_TransactionIsNull() {
            // When
            TransferResponse response = transactionMapper.toResponse(null);

            // Then
            assertThat(response).isNull();
        }
    }
}
