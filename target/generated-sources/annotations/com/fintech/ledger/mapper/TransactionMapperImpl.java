package com.fintech.ledger.mapper;

import com.fintech.ledger.domain.dto.response.TransferResponse;
import com.fintech.ledger.domain.entity.Transaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-28T20:38:13-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Microsoft)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransferResponse toResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        UUID transactionId = null;
        UUID sourceAccountId = null;
        UUID targetAccountId = null;
        BigDecimal amount = null;
        Instant createdAt = null;

        transactionId = transaction.getId();
        sourceAccountId = transaction.getSourceAccountId();
        targetAccountId = transaction.getTargetAccountId();
        amount = transaction.getAmount();
        createdAt = transaction.getCreatedAt();

        String status = transaction.getStatus() != null ? transaction.getStatus().name() : null;

        TransferResponse transferResponse = new TransferResponse( transactionId, sourceAccountId, targetAccountId, amount, status, createdAt );

        return transferResponse;
    }
}
