package com.fintech.ledger.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.ledger.domain.dto.request.TransferRequest;
import com.fintech.ledger.domain.dto.response.TransferResponse;
import com.fintech.ledger.service.TransferService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for transfer/transaction operations.
 * <p>
 * Provides endpoints for executing atomic money transfers between accounts
 * and retrieving transfer details. All transfers require an idempotency key
 * to prevent duplicate processing.
 */
@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Transfers", description = "Money transfer operations")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * Executes an atomic transfer between two accounts.
     * <p>
     * This operation is idempotent based on the Idempotency-Key header.
     * If a transfer with the same key has already been processed, the
     * existing result is returned.
     *
     * @param idempotencyKey unique key for duplicate prevention
     * @param request the transfer request
     * @return the transfer response with 201 status for new transfers
     */
    @PostMapping
    @Operation(summary = "Execute a transfer", 
               description = "Executes an atomic P2P transfer between accounts. Requires Idempotency-Key header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transfer created successfully",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "200", description = "Idempotent retry - existing transfer returned",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or missing Idempotency-Key"),
            @ApiResponse(responseCode = "404", description = "Source or target account not found"),
            @ApiResponse(responseCode = "422", description = "Insufficient funds in source account")
    })
    public ResponseEntity<TransferResponse> executeTransfer(
            @Parameter(description = "Unique key for idempotency", required = true)
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        
        TransferResponse response = transferService.executeTransfer(request, idempotencyKey);
        // Note: The service handles idempotency internally and returns cached response for duplicates
        // We return 201 for simplicity; Phase 8 exception handling can refine this
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a transfer by its unique identifier.
     *
     * @param id the transaction UUID
     * @return the transfer details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get transfer by ID", description = "Retrieves details of a specific transfer/transaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer found",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "404", description = "Transfer not found")
    })
    public ResponseEntity<TransferResponse> getTransfer(
            @Parameter(description = "Transaction UUID") @PathVariable UUID id) {
        TransferResponse response = transferService.getTransfer(id);
        return ResponseEntity.ok(response);
    }
}
