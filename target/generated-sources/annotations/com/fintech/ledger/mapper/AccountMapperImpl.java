package com.fintech.ledger.mapper;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;
import com.fintech.ledger.domain.entity.Account;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-28T20:02:57-0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.1 (Microsoft)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public Account toEntity(CreateAccountRequest request) {
        if ( request == null ) {
            return null;
        }

        Account account = new Account();

        account.setDocument( request.document() );
        account.setName( request.name() );

        return account;
    }

    @Override
    public AccountResponse toResponse(Account account, BigDecimal balance) {
        if ( account == null && balance == null ) {
            return null;
        }

        UUID id = null;
        String document = null;
        String name = null;
        Instant createdAt = null;
        if ( account != null ) {
            id = account.getId();
            document = account.getDocument();
            name = account.getName();
            createdAt = account.getCreatedAt();
        }
        BigDecimal balance1 = null;
        balance1 = balance;

        AccountResponse accountResponse = new AccountResponse( id, document, name, balance1, createdAt );

        return accountResponse;
    }
}
