package com.fintech.ledger.mapper;

import com.fintech.ledger.domain.dto.response.LedgerEntryResponse;
import com.fintech.ledger.domain.entity.LedgerEntry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper for converting between LedgerEntry entities and DTOs.
 * <p>
 * Implementation is auto-generated at compile time and registered as a Spring bean.
 */
@Mapper(componentModel = "spring")
public interface LedgerEntryMapper {

    /**
     * Converts a LedgerEntry entity to a LedgerEntryResponse DTO.
     *
     * @param entry the ledger entry entity
     * @return the ledger entry response DTO
     */
    @Mapping(target = "entryType", expression = "java(entry.getEntryType() != null ? entry.getEntryType().name() : null)")
    LedgerEntryResponse toResponse(LedgerEntry entry);

    /**
     * Converts a list of LedgerEntry entities to a list of LedgerEntryResponse DTOs.
     *
     * @param entries the list of ledger entry entities
     * @return the list of ledger entry response DTOs
     */
    List<LedgerEntryResponse> toResponseList(List<LedgerEntry> entries);
}
