package com.fintech.ledger.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;
import com.fintech.ledger.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for account management operations.
 * <p>
 * Provides endpoints for creating, retrieving, and listing accounts.
 * All accounts are returned with their current calculated balance.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Account management operations")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Creates a new account with the given details.
     *
     * @param request the account creation request
     * @return the created account with 201 status
     */
    @PostMapping
    @Operation(summary = "Create a new account", description = "Creates a new financial account with the provided document and name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "409", description = "Account with this document already exists")
    })
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an account by its unique identifier.
     *
     * @param id the account UUID
     * @return the account with current balance
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID", description = "Retrieves an account with its current calculated balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<AccountResponse> getAccount(
            @Parameter(description = "Account UUID") @PathVariable UUID id) {
        AccountResponse response = accountService.getAccount(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all accounts with pagination support.
     *
     * @param pageable pagination parameters
     * @return a page of accounts with their balances
     */
    @GetMapping
    @Operation(summary = "List all accounts", description = "Retrieves a paginated list of all accounts with their balances")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully")
    })
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @Parameter(description = "Pagination parameters")
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<AccountResponse> accounts = accountService.getAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }
}
