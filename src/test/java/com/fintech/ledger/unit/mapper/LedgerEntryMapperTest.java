package com.fintech.ledger.unit.mapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.fintech.ledger.domain.dto.response.LedgerEntryResponse;
import com.fintech.ledger.domain.entity.EntryType;
import com.fintech.ledger.domain.entity.LedgerEntry;
import com.fintech.ledger.mapper.LedgerEntryMapper;

/**
 * Unit tests for LedgerEntryMapper.
 */
@DisplayName("LedgerEntryMapper")
class LedgerEntryMapperTest {

    private LedgerEntryMapper ledgerEntryMapper;

    @BeforeEach
    void setUp() {
        ledgerEntryMapper = Mappers.getMapper(LedgerEntryMapper.class);
    }

    @Nested
    @DisplayName("toResponse()")
    class ToResponseTests {

        @Test
        @DisplayName("should map all ledger entry fields correctly")
        void should_MapAllFields_Correctly() {
            // Given
            UUID entryId = UUID.randomUUID();
            UUID transactionId = UUID.randomUUID();
            UUID accountId = UUID.randomUUID();
            BigDecimal amount = BigDecimal.valueOf(100);
            BigDecimal balanceAfter = BigDecimal.valueOf(1000);
            Instant createdAt = Instant.now();
            
            LedgerEntry entry = new LedgerEntry(
                    entryId, transactionId, accountId,
                    EntryType.CREDIT, amount, balanceAfter, createdAt
            );

            // When
            LedgerEntryResponse response = ledgerEntryMapper.toResponse(entry);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(entryId);
            assertThat(response.transactionId()).isEqualTo(transactionId);
            assertThat(response.amount()).isEqualTo(amount);
            assertThat(response.balanceAfter()).isEqualTo(balanceAfter);
            assertThat(response.createdAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("should convert CREDIT entry type to string")
        void should_ConvertCreditEntryType_ToString() {
            // Given
            LedgerEntry entry = new LedgerEntry(
                    UUID.randomUUID(), // transactionId
                    UUID.randomUUID(), // accountId
                    EntryType.CREDIT,
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(1000)
            );

            // When
            LedgerEntryResponse response = ledgerEntryMapper.toResponse(entry);

            // Then
            assertThat(response.entryType()).isEqualTo("CREDIT");
        }

        @Test
        @DisplayName("should convert DEBIT entry type to string")
        void should_ConvertDebitEntryType_ToString() {
            // Given
            LedgerEntry entry = new LedgerEntry(
                    UUID.randomUUID(), // transactionId
                    UUID.randomUUID(), // accountId
                    EntryType.DEBIT,
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(900)
            );

            // When
            LedgerEntryResponse response = ledgerEntryMapper.toResponse(entry);

            // Then
            assertThat(response.entryType()).isEqualTo("DEBIT");
        }

        @Test
        @DisplayName("should handle null entry type gracefully")
        void should_HandleNullEntryType_Gracefully() {
            // Given
            LedgerEntry entry = new LedgerEntry(
                    UUID.randomUUID(), // transactionId
                    UUID.randomUUID(), // accountId
                    null,              // entryType
                    BigDecimal.valueOf(100),
                    BigDecimal.valueOf(1000)
            );

            // When
            LedgerEntryResponse response = ledgerEntryMapper.toResponse(entry);

            // Then
            assertThat(response.entryType()).isNull();
        }

        @Test
        @DisplayName("should return null when entry is null")
        void should_ReturnNull_When_EntryIsNull() {
            // When
            LedgerEntryResponse response = ledgerEntryMapper.toResponse(null);

            // Then
            assertThat(response).isNull();
        }
    }

    @Nested
    @DisplayName("toResponseList()")
    class ToResponseListTests {

        @Test
        @DisplayName("should map list of entries correctly")
        void should_MapListOfEntries_Correctly() {
            // Given
            LedgerEntry entry1 = new LedgerEntry(
                    UUID.randomUUID(), UUID.randomUUID(),
                    EntryType.CREDIT, BigDecimal.valueOf(100), BigDecimal.valueOf(100)
            );
            LedgerEntry entry2 = new LedgerEntry(
                    UUID.randomUUID(), UUID.randomUUID(),
                    EntryType.DEBIT, BigDecimal.valueOf(50), BigDecimal.valueOf(50)
            );

            // When
            List<LedgerEntryResponse> responses = ledgerEntryMapper.toResponseList(List.of(entry1, entry2));

            // Then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).entryType()).isEqualTo("CREDIT");
            assertThat(responses.get(1).entryType()).isEqualTo("DEBIT");
        }

        @Test
        @DisplayName("should return empty list when input is empty")
        void should_ReturnEmptyList_When_InputIsEmpty() {
            // When
            List<LedgerEntryResponse> responses = ledgerEntryMapper.toResponseList(Collections.emptyList());

            // Then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("should return null when input list is null")
        void should_ReturnNull_When_InputListIsNull() {
            // When
            List<LedgerEntryResponse> responses = ledgerEntryMapper.toResponseList(null);

            // Then
            assertThat(responses).isNull();
        }
    }
}
