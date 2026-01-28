# 🧠 Fintech Ledger Simulator - Context for AI Agents

This document contains essential context, conventions, and gotchas for AI coding agents working on this project.

---

## 📁 Project Type

- **Language**: Java 21+ (use Records, Pattern Matching, sealed classes where appropriate)
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven
- **Database**: PostgreSQL 16
- **ORM**: Spring Data JPA / Hibernate

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
