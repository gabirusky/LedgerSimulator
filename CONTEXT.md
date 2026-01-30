# 🧠 Fintech Ledger Simulator - Context for AI Agents

This document contains essential context, conventions, and gotchas for AI coding agents working on this project.

---

## 📁 Project Type

- **Language**: Java 21+ (use Records, Pattern Matching, sealed classes where appropriate)
- **Framework**: Spring Boot 3.2.2
- **Build Tool**: Maven
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA / Hibernate

---

## 📦 Implementation Progress

### ✅ Phase 1: Project Setup (Complete)

| File | Description |
|------|-------------|
| `pom.xml` | Spring Boot 3.2.2, Java 21, all dependencies |
| `LedgerSimulatorApplication.java` | Main application class |
| `application.yml` | Base config (datasource, JPA, Flyway) |
| `application-dev.yml` | Dev profile (verbose logging) |
| `application-test.yml` | Test profile (Testcontainers) |
| `banner.txt` | Custom startup banner |
| 9x `package-info.java` | Package structure placeholders |

### ✅ Phase 2: Infrastructure (Complete)

**Docker Configuration:**
| File | Purpose |
|------|---------|
| `docker-compose.yml` | PostgreSQL 16 service with health checks |
| `docker-compose.override.yml` | Local dev overrides |
| `.env.example` | Sample environment variables |

**Database Migrations:**
| File | Purpose |
|------|---------|
| `V1__create_accounts_table.sql` | Accounts table with UUID PK, unique document |
| `V2__create_transactions_table.sql` | Transactions table with idempotency support |
| `V3__create_ledger_entries_table.sql` | Ledger entries for double-entry bookkeeping |

### ✅ Phase 3: Domain Entities (Complete)

**Enums:**
| File | Purpose |
|------|---------|
| `EntryType.java` | DEBIT/CREDIT enum for ledger entries |
| `TransactionStatus.java` | PENDING/COMPLETED/FAILED transaction states |

**JPA Entities:**
| File | Purpose |
|------|---------|
| `Account.java` | Account entity with UUID, document, name, timestamps |
| `Transaction.java` | Transaction with idempotency key, source/target accounts, amount |
| `LedgerEntry.java` | Immutable ledger entry for double-entry bookkeeping |

### ✅ Phase 4: DTOs & Mappers (Complete)

**Request DTOs (`domain/dto/request/`):**
| File | Purpose |
|------|---------|
| `CreateAccountRequest.java` | Account creation DTO with @NotBlank validation |
| `TransferRequest.java` | Transfer DTO with @NotNull, @Positive, @DecimalMin("0.01"), @DifferentAccounts |

**Response DTOs (`domain/dto/response/`):**
| File | Purpose |
|------|---------|
| `AccountResponse.java` | Account with calculated balance response |
| `TransferResponse.java` | Transaction/transfer details response |
| `LedgerEntryResponse.java` | Individual ledger entry response |
| `AccountStatementResponse.java` | Account statement with list of entries |
| `ErrorResponse.java` | RFC 7807 Problem Details format with factory methods |
| `FieldError.java` | Validation field error details |

**Custom Validation (`validation/`):**
| File | Purpose |
|------|---------|
| `DifferentAccounts.java` | Annotation ensuring source ≠ target account |
| `DifferentAccountsValidator.java` | Validator implementation for @DifferentAccounts |

**Mappers (`mapper/`) - Using MapStruct:**

MapStruct is configured with `@Mapper(componentModel = "spring")` for automatic Spring bean generation at compile time.

| File | Methods | Notes |
|------|---------|-------|
| `AccountMapper.java` | `toEntity(CreateAccountRequest)`, `toResponse(Account, BigDecimal balance)` | Balance passed separately (calculated from ledger) |
| `TransactionMapper.java` | `toResponse(Transaction)` | Uses `@Mapping` expression for enum→String: `transaction.getStatus().name()` |
| `LedgerEntryMapper.java` | `toResponse(LedgerEntry)`, `toResponseList(List<LedgerEntry>)` | Enum→String conversion for `entryType` |

**MapStruct Configuration (`pom.xml`):**
```xml
<properties>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>

<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
</dependency>

<!-- In maven-compiler-plugin annotationProcessorPaths -->
<path>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>${mapstruct.version}</version>
</path>
```

Generated implementations are created in `target/generated-sources/annotations/` during compilation and registered as Spring `@Component` beans.

### ✅ Phase 5: Repositories (Complete)

**Repository Interfaces (`repository/`):**

| File | Extends | Key Methods |
|------|---------|-------------|
| `AccountRepository.java` | `JpaRepository<Account, UUID>`, `CustomAccountRepository` | `findByDocument()`, `existsByDocument()`, `findByIdForUpdate()` |
| `TransactionRepository.java` | `JpaRepository<Transaction, UUID>` | `findByIdempotencyKey()`, `existsByIdempotencyKey()` |
| `LedgerEntryRepository.java` | `JpaRepository<LedgerEntry, UUID>` | `findByAccountIdWithCursor()`, `findRecentByAccountId()`, `calculateBalance()`, `getBalance()`, `findLatestBalance()` |

**Custom Repository Implementation:**

| File | Purpose |
|------|---------|
| `CustomAccountRepository.java` | Interface for batch locking operations |
| `CustomAccountRepositoryImpl.java` | Sorted pessimistic locking for deadlock prevention |

**Key Features:**

- **Pessimistic Locking**: `findByIdForUpdate()` uses `@Lock(LockModeType.PESSIMISTIC_WRITE)` with 5-second timeout
- **Balance Calculation (Two-Tier Strategy)**:
  - `getBalance()` / `findLatestBalance()`: **O(log n)** - Uses `balanceAfter` from latest entry (primary/fast path)
  - `calculateBalance()`: **O(n)** - Full aggregation for reconciliation/audit
- **Cursor-Based Pagination (RECOMMENDED)**:
  - `findByAccountIdWithCursor(accountId, cursor, limit)`: **O(log m + limit)** - Constant time at any depth
  - `findRecentByAccountId(accountId, limit)`: **O(log m + limit)** - First page convenience method
- **Deadlock Prevention**: `findAllByIdForUpdateSorted()` sorts UUIDs before acquiring locks
- **Idempotency Support**: Index-backed lookups for idempotency keys
- **Pagination**: Cursor-based (preferred) and offset-based (`Pageable`) support for account statements
- **Recommended Index**: `CREATE INDEX idx_ledger_entries_account_created ON ledger_entries(account_id, created_at DESC)`

### ✅ Phase 6: Services (Complete)

**Service Interfaces (`service/`):**

| File | Methods |
|------|---------|
| `AccountService.java` | `createAccount()`, `getAccount()`, `getAccountBalance()`, `getAllAccounts()` |
| `TransferService.java` | `executeTransfer()`, `getTransfer()` |
| `LedgerService.java` | `getAccountStatement()` (2 overloads: full and paginated) |

**Service Implementations (`service/impl/`):**

| File | Key Features |
|------|--------------|
| `AccountServiceImpl.java` | Duplicate document check, fast-path balance lookup, privacy-aware logging |
| `TransferServiceImpl.java` | Idempotency, sorted locking, double-entry booking, PENDING→COMPLETED status |
| `LedgerServiceImpl.java` | Cursor-based and offset-based statement retrieval |

**Exception Classes (`exception/`):**

| File | Purpose |
|------|---------|
| `AccountNotFoundException.java` | Thrown when account ID lookup fails |
| `TransactionNotFoundException.java` | Thrown when transaction ID lookup fails |
| `DuplicateDocumentException.java` | Thrown when creating account with existing document |
| `InsufficientFundsException.java` | Thrown when transfer amount exceeds available balance |

**Key Features:**

- **Transactional Boundaries**: All write operations use `@Transactional`; read operations use `@Transactional(readOnly = true)`
- **Double-Entry Bookkeeping**: Every transfer creates exactly 2 ledger entries (DEBIT + CREDIT)
- **Idempotency**: Duplicate transfer requests with same key return cached response
- **Deadlock Prevention**: Account locks acquired in sorted UUID order
- **Balance Validation**: Source account balance verified before transfer execution
- **Fast Balance Lookup**: Uses `getBalance()` (O(log n)) for normal operations

### ✅ Phase 7: Controllers (Complete)

**REST Controllers (`controller/`):**

| File | Base Path | Endpoints |
|------|-----------|-----------|
| `AccountController.java` | `/api/v1/accounts` | POST `/`, GET `/{id}`, GET `/` |
| `TransferController.java` | `/api/v1/transfers` | POST `/`, GET `/{id}` |
| `LedgerController.java` | `/api/v1/ledger` | GET `/{accountId}` |

**API Endpoints:**

| Method | Path | Description | Response Code |
|--------|------|-------------|---------------|
| POST | `/api/v1/accounts` | Create new account | 201 Created |
| GET | `/api/v1/accounts/{id}` | Get account by ID | 200 OK |
| GET | `/api/v1/accounts` | List accounts (paginated) | 200 OK |
| POST | `/api/v1/transfers` | Execute transfer (requires `Idempotency-Key` header) | 201 Created |
| GET | `/api/v1/transfers/{id}` | Get transfer by ID | 200 OK |
| GET | `/api/v1/ledger/{accountId}` | Get account statement (paginated) | 200 OK |

**OpenAPI/Swagger Integration:**

- **Dependency**: `springdoc-openapi-starter-webmvc-ui:2.3.0`
- **Swagger UI**: Available at `/swagger-ui.html`
- **OpenAPI Spec**: Available at `/v3/api-docs`
- **Annotations Used**:
  - `@Tag` - Groups endpoints by controller
  - `@Operation` - Documents endpoint purpose
  - `@ApiResponses` / `@ApiResponse` - Documents HTTP status codes
  - `@Schema` - Documents DTO fields with examples

**Key Features:**

- **Validation**: All request bodies validated with `@Valid`
- **Idempotency**: `Idempotency-Key` header required for transfers (returns 400 if missing)
- **Pagination**: `@PageableDefault` used for accounts list and ledger statements
- **Content Negotiation**: JSON responses with proper content types

### ✅ Phase 8: Exception Handling (Complete)

**Exception Classes (`exception/`):**

| File | HTTP Status | Purpose |
|------|-------------|---------|
| `AccountNotFoundException.java` | 404 | Account ID lookup fails |
| `TransactionNotFoundException.java` | 404 | Transaction ID lookup fails |
| `DuplicateDocumentException.java` | 409 | Creating account with existing document |
| `InsufficientFundsException.java` | 422 | Transfer amount exceeds available balance |
| `TransferToSelfException.java` | 400 | Source and target account IDs are the same |
| `InvalidIdempotencyKeyException.java` | 400 | Invalid idempotency key format |
| `MissingIdempotencyKeyException.java` | 400 | Missing Idempotency-Key header |

**Global Exception Handler (`GlobalExceptionHandler.java`):**

`@RestControllerAdvice` class that provides centralized exception handling:

| Exception | HTTP Status | Type URI |
|-----------|-------------|----------|
| `AccountNotFoundException` | 404 | `/errors/account-not-found` |
| `TransactionNotFoundException` | 404 | `/errors/transaction-not-found` |
| `InsufficientFundsException` | 422 | `/errors/insufficient-funds` |
| `DuplicateDocumentException` | 409 | `/errors/duplicate-document` |
| `TransferToSelfException` | 400 | `/errors/transfer-to-self` |
| `MissingIdempotencyKeyException` | 400 | `/errors/missing-idempotency-key` |
| `InvalidIdempotencyKeyException` | 400 | `/errors/invalid-idempotency-key` |
| `MethodArgumentNotValidException` | 400 | `/errors/validation-failed` |
| `ConstraintViolationException` | 400 | `/errors/constraint-violation` |
| Generic `Exception` | 500 | `/errors/internal-error` |

**Key Features:**

- **RFC 7807 Problem Details**: All errors formatted as standard Problem Details
- **Type URIs**: Each error category has a unique type URI prefix
- **Instance Field**: Request URI included in all error responses
- **Timestamps**: All responses include `Instant.now()` timestamp
- **Logging**: WARN level for 4xx errors, ERROR level for 5xx errors
- **Privacy Protection**: Document numbers masked in logs

### 🔧 Fixes Applied During Implementation

1. **Removed invalid `flyway-database-postgresql:9.22.3`** - Not compatible with Flyway 9.x in Spring Boot 3.2
2. **Removed `--enable-preview` compiler flags** - Not needed for Java 21 standard features
3. **Fixed default DB password** - Matched `.env.example` values in `application.yml`
4. **Removed Lombok from annotation processor paths** - Project uses Java records for DTOs (no Lombok needed); removed to avoid Java 25 compatibility issues with Lombok's javac integration

---

## 🗺️ MapStruct Usage Guidelines

MapStruct is used for type-safe bean mapping with Spring integration.

### Dependencies (pom.xml)
```xml
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>${mapstruct.version}</version>
</dependency>
<!-- Annotation processor configured in maven-compiler-plugin -->
```

### Define Mapper Interface
```java
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CarMapper {

    @Mapping(source = "seatConfiguration", target = "seats")
    CarDto carToCarDto(Car car);
}
```

During compilation, MapStruct generates an implementation class (e.g., `CarMapperImpl`) in `target/generated-sources` marked with `@Component` for Spring's component scanning.

### Inject and Use
```java
@Service
public class CarService {

    private final CarMapper carMapper;

    @Autowired
    public CarService(CarMapper carMapper) {
        this.carMapper = carMapper;
    }

    public CarDto getCarDto(Car car) {
        return carMapper.carToCarDto(car);
    }
}
```

This eliminates manual instantiation and integrates with Spring IoC container.

---

## 🏛️ Domain Knowledge

### Double-Entry Bookkeeping
- Every financial transaction creates TWO ledger entries: a DEBIT and a CREDIT
- Balance = SUM(Credits) - SUM(Debits)
- Money is never "updated" - it's always moved through immutable entries
- The ledger is APPEND-ONLY - entries can never be modified or deleted

### Conservation of Value
- For every transaction: Total Debits MUST equal Total Credits
- Money cannot be created or destroyed, only transferred

### ACID Compliance
- All financial operations require strict ACID transactions
- Use `@Transactional` on all service methods that modify data
- Never catch and swallow transaction exceptions

---

## 🔧 Project Conventions

### Package Structure
```
com.fintech.ledger
├── config/          → Spring configuration classes
├── controller/      → REST API controllers
├── service/         → Business logic
├── repository/      → Data access layer
├── domain/
│   ├── entity/      → JPA entities
│   └── dto/         → Request/Response DTOs
├── exception/       → Custom exceptions & handlers
└── validation/      → Custom validators
```

### Naming Conventions
- **Entities**: Singular nouns (Account, LedgerEntry, Transaction)
- **Repositories**: [Entity]Repository
- **Services**: [Entity]Service or [Feature]Service
- **Controllers**: [Entity]Controller
- **DTOs**: [Entity][Request/Response] (e.g., CreateAccountRequest)
- **Exceptions**: [Description]Exception (e.g., InsufficientFundsException)

### Java Style
- Use `record` for DTOs and immutable data
- Use `Optional` for nullable returns (never return null)
- Use `BigDecimal` for ALL monetary values (NEVER use double/float)
- Use `UUID` for all entity IDs
- Use `Instant` for timestamps (not LocalDateTime)
- Always use explicit imports (no wildcards)

### API Design
- API version prefix: `/api/v1/`
- Use HTTP status codes correctly:
  - 201 Created for successful resource creation
  - 200 OK for successful retrieval/update
  - 400 Bad Request for validation errors
  - 404 Not Found for missing resources
  - 422 Unprocessable Entity for business rule violations
  - 409 Conflict for idempotency violations

---

## ⚠️ Common Errors to Avoid

### 1. Floating Point Money
```java
// ❌ NEVER DO THIS
double amount = 100.00;

// ✅ ALWAYS DO THIS
BigDecimal amount = new BigDecimal("100.00");
```

### 2. Missing Transactional Boundaries
```java
// ❌ NEVER DO THIS - Operations not atomic
public void transfer(UUID from, UUID to, BigDecimal amount) {
    accountRepo.debit(from, amount);
    accountRepo.credit(to, amount);
}

// ✅ ALWAYS DO THIS - Wrapped in transaction
@Transactional
public void transfer(UUID from, UUID to, BigDecimal amount) {
    // All operations are atomic
}
```

### 3. Lock Ordering (Deadlock Risk)
```java
// ❌ NEVER DO THIS - Deadlock risk if A→B and B→A run concurrently
Account source = repo.findByIdForUpdate(sourceId);
Account target = repo.findByIdForUpdate(targetId);

// ✅ ALWAYS DO THIS - Consistent lock ordering
UUID firstId = sourceId.compareTo(targetId) < 0 ? sourceId : targetId;
UUID secondId = sourceId.compareTo(targetId) < 0 ? targetId : sourceId;
Account first = repo.findByIdForUpdate(firstId);
Account second = repo.findByIdForUpdate(secondId);
```

### 4. Balance Stored as Column
```java
// ❌ NEVER DO THIS - Balance as stored column leads to race conditions
@Column
private BigDecimal balance;

// ✅ ALWAYS DO THIS - Calculate balance from ledger entries
@Query("SELECT COALESCE(SUM(...), 0) FROM LedgerEntry e WHERE e.accountId = :id")
BigDecimal calculateBalance(@Param("id") UUID accountId);
```

### 5. Missing Idempotency Check
```java
// ❌ NEVER DO THIS - Duplicate transactions possible
public TransferResponse transfer(TransferRequest request) {
    return processTransfer(request);
}

// ✅ ALWAYS DO THIS - Check idempotency key first
public TransferResponse transfer(TransferRequest request, String idempotencyKey) {
    Optional<Transaction> existing = repo.findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
        return mapToResponse(existing.get());
    }
    return processTransfer(request, idempotencyKey);
}
```

### 6. Modifying Ledger Entries
```java
// ❌ NEVER DO THIS - Ledger is immutable
ledgerEntry.setAmount(newAmount);
repository.save(ledgerEntry);

// ✅ Create compensating entries for corrections
LedgerEntry reversal = new LedgerEntry(/* opposite entry */);
repository.save(reversal);
```

---

## 🔍 Edge Cases to Handle

### Account Operations
- Creating account with duplicate document number → 409 Conflict
- Creating account with empty/blank name → 400 Bad Request
- Retrieving non-existent account → 404 Not Found

### Transfer Operations
- Transfer to self (sourceId == targetId) → 400 Bad Request
- Transfer amount ≤ 0 → 400 Bad Request
- Transfer with insufficient funds → 422 Unprocessable Entity
- Transfer from non-existent account → 404 Not Found
- Transfer to non-existent account → 404 Not Found
- Duplicate idempotency key → Return cached response (200 OK)
- Missing idempotency key header → 400 Bad Request

### Concurrency Scenarios
- Two threads withdrawing from same account simultaneously
- Transfer A→B while B→A transfer in progress
- 100+ concurrent requests on same account

### Data Integrity
- Amount precision (2 decimal places for currency)
- Negative amounts in request → Reject immediately
- Very large amounts (BigDecimal max precision)

---

## 🛠️ Technical Gotchas

### 1. Hibernate Proxy Issues
When comparing entities, use `getId()` not `equals()` directly:
```java
// ✅ Safe comparison
if (account.getId().equals(otherId)) { ... }
```

### 2. Transaction Propagation
Default is `REQUIRED` - nested calls join existing transaction.
For independent transactions use `REQUIRES_NEW` (rarely needed).

### 3. Lazy Loading Exceptions
Access lazy collections within `@Transactional` method or use `@EntityGraph`.

### 4. PostgreSQL Specific
- Use `@GeneratedValue(strategy = GenerationType.UUID)` for UUID columns
- Use `@Column(precision = 19, scale = 2)` for money columns
- Pessimistic locks are table-row locks (SELECT ... FOR UPDATE)

### 5. Testcontainers
- Use `@Testcontainers` and `@Container` annotations
- PostgreSQL container must match production version (16)
- Clean database between tests with `@DirtiesContext` or manual cleanup

### 6. Validation Order
1. DTO validation (Jakarta Bean Validation) → 400
2. Entity existence checks → 404
3. Business rule validation → 422

---

## 📝 Testing Guidelines

### Unit Tests
- Mock all external dependencies
- Test one behavior per test method
- Use descriptive test names: `should_RejectTransfer_When_InsufficientFunds`

### Integration Tests
- Use Testcontainers for real PostgreSQL
- Test repository queries actually work
- Test transaction boundaries

### Concurrency Tests
- Use `ExecutorService` with fixed thread pool
- Use `CountDownLatch` for coordinated starts
- Assert final state, not intermediate states
- Run at least 100 concurrent operations

---

## 🚫 Do NOT

1. **Store balance as a column** - Calculate from ledger entries
2. **Use floating point for money** - Use BigDecimal
3. **Modify ledger entries** - They are immutable
4. **Skip idempotency** - All mutations need idempotency keys
5. **Use local time** - Use Instant for timestamps
6. **Return null** - Use Optional
7. **Catch and swallow exceptions** - Let them propagate for rollback
8. **Hard-code connection strings** - Use environment variables
9. **Skip validation** - Validate at controller AND service level
10. **Forget lock ordering** - Always sort account IDs before locking

---

## ✅ Always DO

1. **Use @Transactional** for all write operations
2. **Log transactions** at INFO level with amounts and accounts
3. **Validate early** - Fail fast on bad input
4. **Test edge cases** - Especially concurrency
5. **Document APIs** - Use OpenAPI/Swagger annotations
6. **Handle all error cases** - Return proper HTTP status codes
7. **Use meaningful names** - Code should be self-documenting
8. **Keep methods small** - Single responsibility
9. **Write tests first** - TDD where possible
10. **Review lock ordering** - Prevent deadlocks
