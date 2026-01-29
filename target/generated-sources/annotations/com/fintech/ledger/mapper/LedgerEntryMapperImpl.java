package com.fintech.ledger.mapper;

import com.fintech.ledger.domain.dto.response.LedgerEntryResponse;
import com.fintech.ledger.domain.entity.LedgerEntry;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-28T20:56:54-0300",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260101-2150, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class LedgerEntryMapperImpl implements LedgerEntryMapper {

    @Override
    public LedgerEntryResponse toResponse(LedgerEntry entry) {
        if ( entry == null ) {
            return null;
        }

        UUID id = null;
        UUID transactionId = null;
        BigDecimal amount = null;
        BigDecimal balanceAfter = null;
        Instant createdAt = null;

        id = entry.getId();
        transactionId = entry.getTransactionId();
        amount = entry.getAmount();
        balanceAfter = entry.getBalanceAfter();
        createdAt = entry.getCreatedAt();

        String entryType = entry.getEntryType() != null ? entry.getEntryType().name() : null;

        LedgerEntryResponse ledgerEntryResponse = new LedgerEntryResponse( id, transactionId, entryType, amount, balanceAfter, createdAt );

        return ledgerEntryResponse;
    }

    @Override
    public List<LedgerEntryResponse> toResponseList(List<LedgerEntry> entries) {
        if ( entries == null ) {
            return null;
        }

        List<LedgerEntryResponse> list = new ArrayList<LedgerEntryResponse>( entries.size() );
        for ( LedgerEntry ledgerEntry : entries ) {
            list.add( toResponse( ledgerEntry ) );
        }

        return list;
    }
}
