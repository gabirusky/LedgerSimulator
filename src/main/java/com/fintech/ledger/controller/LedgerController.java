package com.fintech.ledger.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.ledger.domain.dto.response.AccountStatementResponse;
import com.fintech.ledger.service.LedgerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for ledger/statement operations.
 * <p>
 * Provides endpoints for retrieving account transaction history
 * as statements containing ledger entries.
 */
@RestController
@RequestMapping("/api/v1/ledger")
@Tag(name = "Ledger", description = "Account statement and ledger operations")
public class LedgerController {

    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * Retrieves the account statement with ledger entries.
     * <p>
     * Entries are ordered by creation time descending (newest first).
     * Supports pagination via query parameters.
     *
     * @param accountId the account UUID
     * @param pageable pagination parameters
     * @return the account statement with entries
     */
    @GetMapping("/{accountId}")
    @Operation(summary = "Get account statement", 
               description = "Retrieves account transaction history with paginated ledger entries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statement retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AccountStatementResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountStatementResponse> getAccountStatement(
            @Parameter(description = "Account UUID") @PathVariable UUID accountId,
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {
        AccountStatementResponse statement = ledgerService.getAccountStatement(accountId, pageable);
        return ResponseEntity.ok(statement);
    }
}
