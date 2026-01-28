package com.fintech.ledger.mapper;

import com.fintech.ledger.domain.dto.response.TransferResponse;
import com.fintech.ledger.domain.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Transaction entities and DTOs.
 * <p>
 * Implementation is auto-generated at compile time and registered as a Spring bean.
 */
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    /**
     * Converts a Transaction entity to a TransferResponse DTO.
     *
     * @param transaction the transaction entity
     * @return the transfer response DTO
     */
    @Mapping(target = "transactionId", source = "id")
    @Mapping(target = "status", expression = "java(transaction.getStatus() != null ? transaction.getStatus().name() : null)")
    TransferResponse toResponse(Transaction transaction);
}
