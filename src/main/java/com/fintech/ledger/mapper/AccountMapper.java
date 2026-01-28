package com.fintech.ledger.mapper;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;
import com.fintech.ledger.domain.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

/**
 * MapStruct mapper for converting between Account entities and DTOs.
 * <p>
 * Implementation is auto-generated at compile time and registered as a Spring bean.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Converts a CreateAccountRequest DTO to an Account entity.
     *
     * @param request the create account request DTO
     * @return the account entity
     */
    Account toEntity(CreateAccountRequest request);

    /**
     * Converts an Account entity to an AccountResponse DTO.
     * <p>
     * Note: Balance must be provided separately as it's calculated from ledger entries.
     *
     * @param account the account entity
     * @param balance the calculated balance for the account
     * @return the account response DTO
     */
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "id", source = "account.id")
    @Mapping(target = "document", source = "account.document")
    @Mapping(target = "name", source = "account.name")
    @Mapping(target = "createdAt", source = "account.createdAt")
    AccountResponse toResponse(Account account, BigDecimal balance);
}
