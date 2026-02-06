# 📋 Fintech Ledger Simulator - Atomic Tasks

This document contains all atomic coding tasks for implementing the Fintech Ledger Simulator. Each task is designed to be small, focused, and independently completable.

---

## 📊 Task Status Legend
- `[ ]` Not started
- `[/]` In progress
- `[x]` Completed
- `[!]` Blocked
- `[-]` Skipped

---

## Phase 1: Project Setup (Tasks 001-025)

### Maven & Dependencies

- [x] **TASK-001**: Create `pom.xml` with Spring Boot 3.x parent
- [x] **TASK-002**: Add `spring-boot-starter-web` dependency
- [x] **TASK-003**: Add `spring-boot-starter-data-jpa` dependency
- [x] **TASK-004**: Add `spring-boot-starter-validation` dependency
- [x] **TASK-005**: Add `postgresql` JDBC driver dependency
- [x] **TASK-006**: Add `flyway-core` dependency for migrations
- [x] **TASK-007**: Add `mapstruct` dependency (for type-safe bean mapping)
- [x] **TASK-008**: Add `spring-boot-starter-test` dependency
- [x] **TASK-009**: Add `testcontainers-postgresql` dependency
- [x] **TASK-010**: Add `junit-jupiter` dependency
- [x] **TASK-011**: Add `assertj-core` dependency
- [x] **TASK-012**: Add `mockito-core` dependency
- [x] **TASK-013**: Configure Java 21 compiler settings in pom.xml
- [x] **TASK-014**: Configure Spring Boot Maven plugin
- [x] **TASK-015**: Add `spring-boot-devtools` dependency (dev scope)

### Application Configuration

- [x] **TASK-016**: Create main package structure `com.fintech.ledger`
- [x] **TASK-017**: Create `LedgerSimulatorApplication.java` main class
- [x] **TASK-018**: Create `application.yml` with base configuration
- [x] **TASK-019**: Create `application-dev.yml` with dev database config
- [x] **TASK-020**: Create `application-test.yml` with test config
- [x] **TASK-021**: Configure datasource properties (URL, credentials)
- [x] **TASK-022**: Configure JPA/Hibernate properties (ddl-auto: validate)
- [x] **TASK-023**: Configure Flyway migration settings
- [x] **TASK-024**: Configure logging levels for dev environment
- [x] **TASK-025**: Add startup banner customization (optional)

---

## Phase 2: Infrastructure (Tasks 026-040)

### Docker Setup

- [x] **TASK-026**: Create `docker-compose.yml` file
- [x] **TASK-027**: Configure PostgreSQL 16 service in docker-compose
- [x] **TASK-028**: Add volume for PostgreSQL data persistence
- [x] **TASK-029**: Configure environment variables for DB credentials
- [x] **TASK-030**: Add health check for PostgreSQL service
- [x] **TASK-031**: Create `.env.example` with sample environment variables
- [x] **TASK-032**: Add `docker-compose.override.yml` for local overrides

### Database Migrations

- [x] **TASK-033**: Create `db/migration` directory structure
- [x] **TASK-034**: Create `V1__create_accounts_table.sql` migration
  - id (UUID, PK)
  - document (VARCHAR, UNIQUE, NOT NULL)
  - name (VARCHAR, NOT NULL)
  - created_at (TIMESTAMP, NOT NULL)
  - updated_at (TIMESTAMP)
- [x] **TASK-035**: Create `V2__create_transactions_table.sql` migration
  - id (UUID, PK)
  - idempotency_key (VARCHAR, UNIQUE, NOT NULL)
  - source_account_id (UUID, FK)
  - target_account_id (UUID, FK)
  - amount (DECIMAL(19,2), NOT NULL)
  - status (VARCHAR, NOT NULL)
  - created_at (TIMESTAMP, NOT NULL)
- [x] **TASK-036**: Create `V3__create_ledger_entries_table.sql` migration
  - id (UUID, PK)
  - transaction_id (UUID, FK)
  - account_id (UUID, FK)
  - entry_type (VARCHAR, NOT NULL) -- 'DEBIT' or 'CREDIT'
  - amount (DECIMAL(19,2), NOT NULL)
  - balance_after (DECIMAL(19,2), NOT NULL)
  - created_at (TIMESTAMP, NOT NULL)
- [x] **TASK-037**: Add index on `accounts.document` column
- [x] **TASK-038**: Add index on `transactions.idempotency_key` column
- [x] **TASK-039**: Add index on `ledger_entries.account_id` column
- [x] **TASK-040**: Add index on `ledger_entries.transaction_id` column
- [x] **TASK-040.1**: Create `V4__add_balance_query_index.sql` - composite index on (account_id, created_at DESC) for O(log n) balance queries

---

## Phase 3: Domain Entities (Tasks 041-070)

### Enums

- [x] **TASK-041**: Create `domain/entity/EntryType.java` enum (DEBIT, CREDIT)
- [x] **TASK-042**: Create `domain/entity/TransactionStatus.java` enum (PENDING, COMPLETED, FAILED)

### Account Entity

- [x] **TASK-043**: Create `domain/entity/Account.java` class
- [x] **TASK-044**: Add `@Entity` and `@Table(name = "accounts")` annotations
- [x] **TASK-045**: Add `id` field with UUID and @Id annotation
- [x] **TASK-046**: Add `@GeneratedValue(strategy = GenerationType.UUID)` to id
- [x] **TASK-047**: Add `document` field with unique constraint
- [x] **TASK-048**: Add `name` field with @NotBlank validation
- [x] **TASK-049**: Add `createdAt` field with @CreationTimestamp
- [x] **TASK-050**: Add `updatedAt` field with @UpdateTimestamp
- [x] **TASK-051**: Add no-args constructor (required by JPA)
- [x] **TASK-052**: Add all-args constructor
- [x] **TASK-053**: Add getters and setters for all fields
- [x] **TASK-054**: Override `equals()` using id only
- [x] **TASK-055**: Override `hashCode()` using id only

### Transaction Entity

- [x] **TASK-056**: Create `domain/entity/Transaction.java` class
- [x] **TASK-057**: Add `@Entity` and `@Table` annotations
- [x] **TASK-058**: Add `id` field (UUID, PK)
- [x] **TASK-059**: Add `idempotencyKey` field (unique)
- [x] **TASK-060**: Add `sourceAccountId` field (UUID)
- [x] **TASK-061**: Add `targetAccountId` field (UUID)
- [x] **TASK-062**: Add `amount` field (BigDecimal with precision 19, scale 2)
- [x] **TASK-063**: Add `status` field (enum TransactionStatus)
- [x] **TASK-064**: Add `createdAt` field with @CreationTimestamp
- [x] **TASK-065**: Add constructors, getters, setters, equals, hashCode

### LedgerEntry Entity

- [x] **TASK-066**: Create `domain/entity/LedgerEntry.java` class
- [x] **TASK-067**: Add `id`, `transactionId`, `accountId` fields
- [x] **TASK-068**: Add `entryType` field (enum EntryType)
- [x] **TASK-069**: Add `amount`, `balanceAfter` fields (BigDecimal)
- [x] **TASK-070**: Add `createdAt` field and all constructors/accessors

---

## Phase 4: DTOs (Tasks 071-095)

### Request DTOs

- [x] **TASK-071**: Create `dto/request` package
- [x] **TASK-072**: Create `CreateAccountRequest.java` record
  - document (String, @NotBlank)
  - name (String, @NotBlank)
- [x] **TASK-073**: Create `TransferRequest.java` record
  - sourceAccountId (UUID, @NotNull)
  - targetAccountId (UUID, @NotNull)
  - amount (BigDecimal, @NotNull, @Positive)
- [x] **TASK-074**: Add `@DecimalMin("0.01")` to transfer amount
- [x] **TASK-075**: Add custom validation: sourceId != targetId

### Response DTOs

- [x] **TASK-076**: Create `dto/response` package
- [x] **TASK-077**: Create `AccountResponse.java` record
  - id (UUID)
  - document (String)
  - name (String)
  - balance (BigDecimal)
  - createdAt (Instant)
- [x] **TASK-078**: Create `TransferResponse.java` record
  - transactionId (UUID)
  - sourceAccountId (UUID)
  - targetAccountId (UUID)
  - amount (BigDecimal)
  - status (String)
  - createdAt (Instant)
- [x] **TASK-079**: Create `LedgerEntryResponse.java` record
  - id (UUID)
  - transactionId (UUID)
  - entryType (String)
  - amount (BigDecimal)
  - balanceAfter (BigDecimal)
  - createdAt (Instant)
- [x] **TASK-080**: Create `AccountStatementResponse.java` record
  - accountId (UUID)
  - accountName (String)
  - currentBalance (BigDecimal)
  - entries (List<LedgerEntryResponse>)

### Error DTOs

- [x] **TASK-081**: Create `ErrorResponse.java` record (RFC 7807)
  - type (String)
  - title (String)
  - status (int)
  - detail (String)
  - instance (String)
- [x] **TASK-082**: Add timestamp field to ErrorResponse
- [x] **TASK-083**: Add errors field (List<FieldError>) for validation errors
- [x] **TASK-084**: Create `FieldError.java` record (field, message)

### Mapper Interfaces (MapStruct)

- [x] **TASK-085**: Create `mapper` package
- [x] **TASK-086**: Create `AccountMapper.java` interface with `@Mapper(componentModel = "spring")`
- [x] **TASK-087**: Add `toResponse(Account, BigDecimal balance)` method with `@Mapping` annotations
- [x] **TASK-088**: Add `toEntity(CreateAccountRequest)` method
- [x] **TASK-089**: Create `TransactionMapper.java` interface with `@Mapper(componentModel = "spring")`
- [x] **TASK-090**: Add `toResponse(Transaction)` method with enum→String conversion
- [x] **TASK-091**: Create `LedgerEntryMapper.java` interface with `@Mapper(componentModel = "spring")`
- [x] **TASK-092**: Add `toResponse(LedgerEntry)` method with enum→String conversion
- [x] **TASK-093**: Add `toResponseList(List<LedgerEntry>)` method
- [x] **TASK-094**: Configure MapStruct annotation processor in `pom.xml`
- [x] **TASK-095**: Verify MapStruct generates Spring beans at compile time

---

## Phase 5: Repositories (Tasks 096-120)

### AccountRepository

- [x] **TASK-096**: Create `repository` package
- [x] **TASK-097**: Create `AccountRepository.java` interface
- [x] **TASK-098**: Extend `JpaRepository<Account, UUID>`
- [x] **TASK-099**: Add `findByDocument(String document)` method
- [x] **TASK-100**: Add `existsByDocument(String document)` method
- [x] **TASK-101**: Add `findByIdForUpdate(UUID id)` with pessimistic lock
  ```java
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM Account a WHERE a.id = :id")
  Optional<Account> findByIdForUpdate(@Param("id") UUID id);
  ```
- [x] **TASK-102**: Add `@QueryHints` for lock timeout configuration

### TransactionRepository

- [x] **TASK-103**: Create `TransactionRepository.java` interface
- [x] **TASK-104**: Extend `JpaRepository<Transaction, UUID>`
- [x] **TASK-105**: Add `findByIdempotencyKey(String key)` method
- [x] **TASK-106**: Add `existsByIdempotencyKey(String key)` method
- [x] **TASK-107**: Add index hint for idempotency key lookup

### LedgerEntryRepository

- [x] **TASK-108**: Create `LedgerEntryRepository.java` interface
- [x] **TASK-109**: Extend `JpaRepository<LedgerEntry, UUID>`
- [x] **TASK-110**: ~~Add `findByAccountIdOrderByCreatedAtDesc(UUID accountId)` method~~ (REMOVED - O(m) anti-pattern)
- [x] **TASK-110.1**: Add `findByAccountIdWithCursor(UUID, Instant, int)` cursor-based pagination (O(log m + limit))
- [x] **TASK-110.2**: Add `findRecentByAccountId(UUID, int)` convenience method for first page
- [-] **TASK-110.3**: ~~Deprecate method~~ (Skipped - method removed entirely)
- [x] **TASK-111**: Add paginated version with `Pageable` parameter
- [x] **TASK-112**: Add `findByTransactionId(UUID transactionId)` method
- [x] **TASK-113**: Add balance calculation query:
  ```java
  @Query("SELECT COALESCE(SUM(CASE WHEN e.entryType = 'CREDIT' THEN e.amount ELSE 0 END) - " +
         "SUM(CASE WHEN e.entryType = 'DEBIT' THEN e.amount ELSE 0 END), 0) " +
         "FROM LedgerEntry e WHERE e.accountId = :accountId")
  BigDecimal calculateBalance(@Param("accountId") UUID accountId);
  ```
- [x] **TASK-114**: Add `findLatestByAccountId(UUID accountId)` for last entry
- [x] **TASK-114.1**: Add `findLatestBalance(UUID accountId)` for O(log n) balance reads
- [x] **TASK-114.2**: Add `getBalance(UUID accountId)` default method for fast balance with zero default

### Custom Repository Implementations

- [x] **TASK-115**: Create `CustomAccountRepository.java` interface
- [x] **TASK-116**: Create `CustomAccountRepositoryImpl.java` class
- [x] **TASK-117**: Implement batch account locking method
- [x] **TASK-118**: Add `@PersistenceContext` for EntityManager injection
- [x] **TASK-119**: Implement sorted lock acquisition for deadlock prevention
- [x] **TASK-120**: Make AccountRepository extend CustomAccountRepository

---

## Phase 6: Services (Tasks 121-165)

### AccountService

- [x] **TASK-121**: Create `service` package
- [x] **TASK-122**: Create `AccountService.java` interface
- [x] **TASK-123**: Define `createAccount(CreateAccountRequest)` method
- [x] **TASK-124**: Define `getAccount(UUID id)` method
- [x] **TASK-125**: Define `getAccountBalance(UUID id)` method
- [x] **TASK-126**: Define `getAllAccounts(Pageable)` method
- [x] **TASK-127**: Create `AccountServiceImpl.java` class
- [x] **TASK-128**: Inject AccountRepository dependency
- [x] **TASK-129**: Inject LedgerEntryRepository dependency
- [x] **TASK-130**: Inject AccountMapper dependency
- [x] **TASK-131**: Implement `createAccount` with duplicate check
- [x] **TASK-132**: Add `@Transactional` to createAccount
- [x] **TASK-133**: Implement `getAccount` with balance calculation
- [x] **TASK-134**: Throw `AccountNotFoundException` for missing accounts
- [x] **TASK-135**: Implement `getAccountBalance` method
- [x] **TASK-136**: Implement `getAllAccounts` with pagination

### TransferService

- [x] **TASK-137**: Create `TransferService.java` interface
- [x] **TASK-138**: Define `executeTransfer(TransferRequest, String idempotencyKey)` method
- [x] **TASK-139**: Define `getTransfer(UUID transactionId)` method
- [x] **TASK-140**: Create `TransferServiceImpl.java` class
- [x] **TASK-141**: Inject all required repositories
- [x] **TASK-142**: Add `@Transactional` annotation to class level
- [x] **TASK-143**: Implement idempotency check at start of transfer
- [x] **TASK-144**: Implement account ID sorting for deadlock prevention
- [x] **TASK-145**: Implement pessimistic lock acquisition
- [x] **TASK-146**: Implement balance validation (source >= amount)
- [x] **TASK-147**: Create Transaction entity with PENDING status
- [x] **TASK-148**: Create DEBIT LedgerEntry for source account
- [x] **TASK-149**: Create CREDIT LedgerEntry for target account
- [x] **TASK-150**: Calculate and store balanceAfter for each entry
- [x] **TASK-151**: Update Transaction status to COMPLETED
- [x] **TASK-152**: Handle `InsufficientFundsException`
- [x] **TASK-153**: Handle `AccountNotFoundException` for source/target
- [x] **TASK-154**: Add logging for transaction start/complete
- [x] **TASK-155**: Implement `getTransfer` method

### LedgerService

- [x] **TASK-156**: Create `LedgerService.java` interface
- [x] **TASK-157**: Define `getAccountStatement(UUID accountId)` method
- [x] **TASK-158**: Define `getAccountStatement(UUID, Pageable)` method
- [x] **TASK-159**: Create `LedgerServiceImpl.java` class
- [x] **TASK-160**: Inject LedgerEntryRepository
- [x] **TASK-161**: Inject AccountRepository (for validation)
- [x] **TASK-162**: Implement unpaginated statement retrieval
- [x] **TASK-163**: Implement paginated statement retrieval
- [x] **TASK-164**: Validate account exists before fetching entries
- [x] **TASK-165**: Map entries to response DTOs

---

## Phase 7: Controllers (Tasks 166-200)

### AccountController

- [x] **TASK-166**: Create `controller` package
- [x] **TASK-167**: Create `AccountController.java` class
- [x] **TASK-168**: Add `@RestController` annotation
- [x] **TASK-169**: Add `@RequestMapping("/api/v1/accounts")` annotation
- [x] **TASK-170**: Inject AccountService
- [x] **TASK-171**: Implement `POST /` - create account endpoint
- [x] **TASK-172**: Add `@Valid` to request body
- [x] **TASK-173**: Return `ResponseEntity<AccountResponse>` with 201 status
- [x] **TASK-174**: Implement `GET /{id}` - get account endpoint
- [x] **TASK-175**: Parse UUID from path variable
- [x] **TASK-176**: Implement `GET /` - list accounts endpoint
- [x] **TASK-177**: Add pagination parameters (@PageableDefault)
- [x] **TASK-178**: Return `Page<AccountResponse>`

### TransferController

- [x] **TASK-179**: Create `TransferController.java` class
- [x] **TASK-180**: Add `@RestController` and `@RequestMapping("/api/v1/transfers")`
- [x] **TASK-181**: Inject TransferService
- [x] **TASK-182**: Implement `POST /` - execute transfer endpoint
- [x] **TASK-183**: Extract `Idempotency-Key` from header
- [x] **TASK-184**: Validate Idempotency-Key is present (return 400 if missing)
- [x] **TASK-185**: Add `@Valid` to TransferRequest body
- [x] **TASK-186**: Return 201 Created for new transactions
- [x] **TASK-187**: Return 200 OK for idempotent retries
- [x] **TASK-188**: Implement `GET /{id}` - get transfer endpoint
- [x] **TASK-189**: Map response to TransferResponse DTO

### LedgerController

- [x] **TASK-190**: Create `LedgerController.java` class
- [x] **TASK-191**: Add `@RestController` and `@RequestMapping("/api/v1/ledger")`
- [x] **TASK-192**: Inject LedgerService
- [x] **TASK-193**: Implement `GET /{accountId}` - get account statement
- [x] **TASK-194**: Add pagination support
- [x] **TASK-195**: Return AccountStatementResponse

### API Documentation

- [x] **TASK-196**: Add `springdoc-openapi-starter-webmvc-ui` dependency
- [x] **TASK-197**: Add `@Tag` annotations to controllers
- [x] **TASK-198**: Add `@Operation` annotations to endpoints
- [x] **TASK-199**: Add `@ApiResponse` annotations for HTTP statuses
- [x] **TASK-200**: Add `@Schema` annotations to DTOs

---

## Phase 8: Exception Handling (Tasks 201-225)

### Custom Exceptions

- [x] **TASK-201**: Create `exception` package
- [x] **TASK-202**: Create `AccountNotFoundException.java` (extends RuntimeException)
- [x] **TASK-203**: Add constructor with accountId parameter
- [x] **TASK-204**: Create `InsufficientFundsException.java`
- [x] **TASK-205**: Add fields: accountId, available, requested
- [x] **TASK-206**: Create `DuplicateDocumentException.java`
- [x] **TASK-207**: Create `TransferToSelfException.java`
- [x] **TASK-208**: Create `TransactionNotFoundException.java`
- [x] **TASK-209**: Create `InvalidIdempotencyKeyException.java`
- [x] **TASK-210**: Create `MissingIdempotencyKeyException.java`

### Global Exception Handler

- [x] **TASK-211**: Create `GlobalExceptionHandler.java` class
- [x] **TASK-212**: Add `@RestControllerAdvice` annotation
- [x] **TASK-213**: Handle `AccountNotFoundException` → 404
- [x] **TASK-214**: Handle `InsufficientFundsException` → 422
- [x] **TASK-215**: Handle `DuplicateDocumentException` → 409
- [x] **TASK-216**: Handle `TransferToSelfException` → 400
- [x] **TASK-217**: Handle `MissingIdempotencyKeyException` → 400
- [x] **TASK-218**: Handle `MethodArgumentNotValidException` → 400
- [x] **TASK-219**: Handle `ConstraintViolationException` → 400
- [x] **TASK-220**: Handle generic `Exception` → 500
- [x] **TASK-221**: Format all errors as RFC 7807 Problem Details
- [x] **TASK-222**: Add `type` URI for each error category
- [x] **TASK-223**: Include `instance` (request URI) in responses
- [x] **TASK-224**: Add timestamp to all error responses
- [x] **TASK-225**: Log all exceptions at appropriate levels

---

## Phase 9: Unit Tests (Tasks 226-265) - Completed

### Domain Tests

- [x] **TASK-226**: Create `test/java/com/fintech/ledger/unit` package
- [x] **TASK-227**: Create `AccountTest.java` class
- [x] **TASK-228**: Test Account entity equals/hashCode with same ID
- [x] **TASK-229**: Test Account entity equals/hashCode with different ID
- [x] **TASK-230**: Test Account entity creation

### Service Unit Tests - AccountService

- [x] **TASK-231**: Create `AccountServiceTest.java` class
- [x] **TASK-232**: Mock AccountRepository and LedgerEntryRepository
- [x] **TASK-233**: Test `createAccount` happy path
- [x] **TASK-234**: Test `createAccount` with duplicate document
- [x] **TASK-235**: Test `getAccount` happy path with balance
- [x] **TASK-236**: Test `getAccount` with non-existent account
- [x] **TASK-237**: Test `getAccountBalance` calculation

### Service Unit Tests - TransferService

- [x] **TASK-238**: Create `TransferServiceTest.java` class
- [x] **TASK-239**: Mock all dependencies
- [x] **TASK-240**: Test `executeTransfer` happy path
- [x] **TASK-241**: Test transfer with insufficient funds
- [x] **TASK-242**: Test transfer with non-existent source account
- [x] **TASK-243**: Test transfer with non-existent target account
- [x] **TASK-244**: Test idempotency - duplicate key returns cached response
- [x] **TASK-245**: Test account ID sorting for lock ordering
- [x] **TASK-246**: Test balance calculation after transfer
- [x] **TASK-247**: Verify DEBIT entry created for source
- [x] **TASK-248**: Verify CREDIT entry created for target
- [x] **TASK-249**: Verify Transaction status is COMPLETED

### Service Unit Tests - LedgerService

- [x] **TASK-250**: Create `LedgerServiceTest.java` class
- [x] **TASK-251**: Test `getAccountStatement` happy path
- [x] **TASK-252**: Test statement for non-existent account
- [x] **TASK-253**: Test statement ordering (newest first)
- [x] **TASK-254**: Test empty statement for new account

### Mapper Tests

- [x] **TASK-255**: Create `AccountMapperTest.java`
- [x] **TASK-256**: Test `toResponse` mapping
- [x] **TASK-257**: Test `toEntity` mapping
- [x] **TASK-258**: Test null handling
- [x] **TASK-259**: Create `TransactionMapperTest.java`
- [x] **TASK-260**: Create `LedgerEntryMapperTest.java`

### Validation Tests

- [x] **TASK-261**: Test CreateAccountRequest validation - blank document
- [x] **TASK-262**: Test CreateAccountRequest validation - blank name
- [x] **TASK-263**: Test TransferRequest validation - null amount
- [x] **TASK-264**: Test TransferRequest validation - negative amount
- [x] **TASK-265**: Test TransferRequest validation - zero amount

---

## Phase 10: Integration Tests (Tasks 266-305) - Completed

### Repository Integration Tests

- [x] **TASK-266**: Create `test/java/com/fintech/ledger/integration` package
- [x] **TASK-267**: Create `AbstractIntegrationTest.java` base class
- [x] **TASK-268**: Configure Testcontainers PostgreSQL
- [x] **TASK-269**: Add `@Testcontainers` annotation
- [x] **TASK-270**: Create PostgreSQL container definition
- [x] **TASK-271**: Configure dynamic datasource properties

### AccountRepository Tests

- [x] **TASK-272**: Create `AccountRepositoryTest.java` class
- [x] **TASK-273**: Extend AbstractIntegrationTest
- [x] **TASK-274**: Test `save` and `findById`
- [x] **TASK-275**: Test `findByDocument`
- [x] **TASK-276**: Test `existsByDocument`
- [x] **TASK-277**: Test `findByIdForUpdate` acquires lock
- [x] **TASK-278**: Test unique constraint on document

### LedgerEntryRepository Tests

- [x] **TASK-279**: Create `LedgerEntryRepositoryTest.java` class
- [x] **TASK-280**: Test `calculateBalance` with credits only
- [x] **TASK-281**: Test `calculateBalance` with debits only
- [x] **TASK-282**: Test `calculateBalance` with mixed entries
- [x] **TASK-283**: Test `calculateBalance` for new account (returns 0)
- [x] **TASK-284**: Test `findByAccountIdOrderByCreatedAtDesc`

### TransactionRepository Tests

- [x] **TASK-285**: Create `TransactionRepositoryTest.java` class
- [x] **TASK-286**: Test `findByIdempotencyKey`
- [x] **TASK-287**: Test `existsByIdempotencyKey`
- [x] **TASK-288**: Test unique constraint on idempotency_key

### Controller Integration Tests

- [x] **TASK-289**: Create `AccountControllerIT.java` class
- [x] **TASK-290**: Use `@SpringBootTest` with random port
- [x] **TASK-291**: Use `TestRestTemplate` for HTTP calls
- [x] **TASK-292**: Test `POST /api/v1/accounts` - 201 Created
- [x] **TASK-293**: Test `POST /api/v1/accounts` - 409 Duplicate
- [x] **TASK-294**: Test `POST /api/v1/accounts` - 400 Validation Error
- [x] **TASK-295**: Test `GET /api/v1/accounts/{id}` - 200 OK
- [x] **TASK-296**: Test `GET /api/v1/accounts/{id}` - 404 Not Found

### TransferController Integration Tests

- [x] **TASK-297**: Create `TransferControllerIT.java` class
- [x] **TASK-298**: Test `POST /api/v1/transfers` - 201 Created
- [x] **TASK-299**: Test `POST /api/v1/transfers` - 422 Insufficient Funds
- [x] **TASK-300**: Test `POST /api/v1/transfers` - 400 Missing Idempotency Key
- [x] **TASK-301**: Test `POST /api/v1/transfers` - 200 OK (idempotent retry)
- [x] **TASK-302**: Test `POST /api/v1/transfers` - 404 Account Not Found

### LedgerController Integration Tests

- [x] **TASK-303**: Create `LedgerControllerIT.java` class
- [x] **TASK-304**: Test `GET /api/v1/ledger/{accountId}` - 200 OK
- [x] **TASK-305**: Test `GET /api/v1/ledger/{accountId}` - 404 Not Found

---

## Phase 11: Concurrency Tests (Tasks 306-325)

### Setup

- [x] **TASK-306**: Create `test/java/com/fintech/ledger/concurrency` package
- [x] **TASK-307**: Create `ConcurrentTransferTest.java` class
- [x] **TASK-308**: Extend AbstractIntegrationTest
- [x] **TASK-309**: Create helper method to seed test accounts with balance

### Concurrent Withdrawal Tests

- [x] **TASK-310**: Test 10 concurrent withdrawals from same account
- [x] **TASK-311**: Verify no overdraft occurred
- [x] **TASK-312**: Verify final balance = initial - (withdrawals that succeeded)
- [x] **TASK-313**: Test 50 concurrent withdrawals from same account
- [x] **TASK-314**: Test 100 concurrent withdrawals from same account
- [x] **TASK-315**: Verify all failed withdrawals got 422 response

### Concurrent Transfer Tests

- [x] **TASK-316**: Test A→B transfer while B→A transfer occurs
- [x] **TASK-317**: Verify no deadlock (test completes within timeout)
- [x] **TASK-318**: Verify conservation of value (total money unchanged)
- [x] **TASK-319**: Test circular transfers: A→B→C→A concurrently
- [x] **TASK-320**: Verify all balances are non-negative after test

### Stress Tests

- [x] **TASK-321**: Test 100 threads, 10 transfers each
- [x] **TASK-322**: Measure and log execution time
- [x] **TASK-323**: Verify zero data integrity violations
- [x] **TASK-324**: Test with random delays to simulate network latency
- [x] **TASK-325**: Add assertions for transaction count matching

---

## Phase 12: DevOps & CI/CD (Tasks 326-345)

### Dockerfile

- [x] **TASK-326**: Create multi-stage `Dockerfile`
- [x] **TASK-327**: Stage 1: Maven build with dependency caching
- [x] **TASK-328**: Stage 2: Runtime with slim JRE image
- [x] **TASK-329**: Configure JVM options for container
- [x] **TASK-330**: Add health check endpoint configuration
- [x] **TASK-331**: Create `.dockerignore` file

### Docker Compose Production

- [x] **TASK-332**: Create `docker-compose.prod.yml`
- [x] **TASK-333**: Configure app service with built image
- [x] **TASK-334**: Configure production database settings
- [x] **TASK-335**: Add network configuration
- [x] **TASK-336**: Add resource limits

### GitHub Actions CI

- [x] **TASK-337**: Create `.github/workflows` directory
- [x] **TASK-338**: Create `ci.yml` workflow file
- [x] **TASK-339**: Configure Java 21 setup
- [x] **TASK-340**: Add Maven cache configuration
- [x] **TASK-341**: Run unit tests step
- [x] **TASK-342**: Run integration tests step (with Testcontainers)
- [x] **TASK-343**: Add test report upload
- [x] **TASK-344**: Add code coverage report
- [x] **TASK-345**: Configure branch protection triggers

---

## Phase 13: Documentation & Polish (Tasks 346-365)

### API Documentation

- [x] **TASK-346**: Verify OpenAPI spec is generated at `/v3/api-docs`
- [x] **TASK-347**: Verify Swagger UI accessible at `/swagger-ui.html`
- [x] **TASK-348**: Add detailed descriptions to all endpoints
- [x] **TASK-349**: Add request/response examples
- [x] **TASK-350**: Document all error response codes

### README Updates

- [x] **TASK-351**: Update README with actual endpoint examples
- [x] **TASK-352**: Add curl examples for each endpoint
- [x] **TASK-353**: Add example responses
- [x] **TASK-354**: Document environment variables
- [x] **TASK-355**: Add troubleshooting section

### Code Quality

- [x] **TASK-356**: Add missing JavaDoc to public methods
- [x] **TASK-357**: Run code formatter (google-java-format)
- [x] **TASK-358**: Fix any remaining compiler warnings
- [x] **TASK-359**: Add `CONTRIBUTING.md` file
- [x] **TASK-360**: Add `LICENSE` file (MIT)

### Final Verification

- [x] **TASK-361**: Run full test suite locally
- [-] **TASK-362**: Verify all tests pass in CI (Skipped - requires GitHub Actions runner)
- [x] **TASK-363**: Manual testing of happy paths
- [x] **TASK-364**: Manual testing of error scenarios
- [-] **TASK-365**: Performance baseline test (optional - skipped)

---

## 📈 Summary

| Phase | Tasks | Description |
|-------|-------|-------------|
| 1 | 001-025 | Project Setup |
| 2 | 026-040 | Infrastructure |
| 3 | 041-070 | Domain Entities |
| 4 | 071-095 | DTOs & Mappers |
| 5 | 096-120 | Repositories |
| 6 | 121-165 | Services |
| 7 | 166-200 | Controllers |
| 8 | 201-225 | Exception Handling |
| 9 | 226-265 | Unit Tests |
| 10 | 266-305 | Integration Tests |
| 11 | 306-325 | Concurrency Tests |
| 12 | 326-345 | DevOps & CI/CD |
| 13 | 346-365 | Documentation & Polish |

**Total Tasks: 365**

---

## 📝 Notes for Coding Agent

1. Complete tasks in order within each phase
2. Mark task as `[/]` when starting, `[x]` when complete
3. If blocked, mark as `[!]` and note the blocker
4. Refer to `CONTEXT.md` for conventions and gotchas
5. Refer to `PLAN.md` for architecture decisions
6. Run tests frequently - after every major change
7. Commit after completing each phase

---

## 🐛 Bug Fixes Log

### Phase 11 Concurrency Testing (2026-02-06)

| Error | Root Cause | Fix |
|-------|------------|-----|
| `PSQLException: relation "idx_ledger_entries_account_created" already exists` | Testcontainers with `withReuse(true)` preserves DB state; `CREATE INDEX` fails on rerun | Added `IF NOT EXISTS` to `V4__add_balance_query_index.sql` |
| `DataIntegrityViolationException: violates FK constraint "fk_ledger_entries_transaction"` | `seedAccountWithBalance` created LedgerEntry with fake transaction ID | Refactored to create valid Transaction record before LedgerEntry |
| `NoSuchBeanDefinitionException: AccountMapper not available` | Stale compiled classes; MapStruct-generated impl not recompiled | Run `mvn clean install -DskipTests` to force full recompilation |

### Affected Files

| File | Change |
|------|--------|
| `src/main/resources/db/migration/V4__add_balance_query_index.sql` | Added `IF NOT EXISTS` to CREATE INDEX |
| `src/test/java/com/fintech/ledger/concurrency/ConcurrentTransferTest.java` | Fixed `seedAccountWithBalance` to create valid Transaction before LedgerEntry |

### Commands Used

```powershell
# Force full recompilation
mvn clean install -DskipTests

# Run concurrency tests
mvn failsafe:integration-test "-Dit.test=ConcurrentTransferTest"
```

