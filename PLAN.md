# 📋 Fintech Ledger Simulator - Implementation Plan

## 🎯 Project Overview

This document outlines the complete implementation plan for a **Double-Entry Bookkeeping** backend service designed to handle atomic financial transactions with high integrity and auditability.

---

## 📦 Project Structure

```
fintech-ledger-simulator/
├── src/
│   ├── main/
│   │   ├── java/com/fintech/ledger/
│   │   │   ├── LedgerSimulatorApplication.java
│   │   │   ├── config/
│   │   │   │   └── AppConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AccountController.java
│   │   │   │   ├── TransferController.java
│   │   │   │   └── LedgerController.java
│   │   │   ├── service/
│   │   │   │   ├── AccountService.java
│   │   │   │   ├── TransferService.java
│   │   │   │   └── LedgerService.java
│   │   │   ├── repository/
│   │   │   │   ├── AccountRepository.java
│   │   │   │   ├── LedgerEntryRepository.java
│   │   │   │   └── TransactionRepository.java
│   │   │   ├── domain/
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Account.java
│   │   │   │   │   ├── LedgerEntry.java
│   │   │   │   │   ├── Transaction.java
│   │   │   │   │   └── EntryType.java
│   │   │   │   └── dto/
│   │   │   │       ├── request/
│   │   │   │       │   ├── CreateAccountRequest.java
│   │   │   │       │   └── TransferRequest.java
│   │   │   │       └── response/
│   │   │   │           ├── AccountResponse.java
│   │   │   │           ├── TransferResponse.java
│   │   │   │           └── LedgerEntryResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── AccountNotFoundException.java
│   │   │   │   ├── InsufficientFundsException.java
│   │   │   │   ├── DuplicateTransactionException.java
│   │   │   │   └── ProblemDetail.java
│   │   │   └── validation/
│   │   │       └── MoneyValidator.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       └── db/migration/
│   │           ├── V1__create_accounts_table.sql
│   │           ├── V2__create_transactions_table.sql
│   │           └── V3__create_ledger_entries_table.sql
│   └── test/
│       └── java/com/fintech/ledger/
│           ├── unit/
│           │   ├── service/
│           │   │   ├── AccountServiceTest.java
│           │   │   ├── TransferServiceTest.java
│           │   │   └── LedgerServiceTest.java
│           │   └── domain/
│           │       └── AccountTest.java
│           ├── integration/
│           │   ├── repository/
│           │   │   ├── AccountRepositoryTest.java
│           │   │   └── LedgerEntryRepositoryTest.java
│           │   └── controller/
│           │       ├── AccountControllerIT.java
│           │       ├── TransferControllerIT.java
│           │       └── LedgerControllerIT.java
│           └── concurrency/
│               └── ConcurrentTransferTest.java
├── docker-compose.yml
├── pom.xml
├── Dockerfile
├── .github/
│   └── workflows/
│       └── ci.yml
└── README.md
```

---

## 🏛️ Architecture Layers

### Layer 1: Presentation (Controllers)
- REST API endpoints following RESTful conventions
- Request validation using Jakarta Bean Validation
- Idempotency-Key header handling
- RFC 7807 Problem Details error responses

### Layer 2: Business Logic (Services)
- Account management operations
- Atomic transfer operations with pessimistic locking
- Balance calculations (SUM(credits) - SUM(debits))
- Business rule enforcement (no overdraft, conservation of value)

### Layer 3: Data Access (Repositories)
- Spring Data JPA repositories
- Custom queries with pessimistic locking
- Idempotency key lookups

### Layer 4: Persistence (Database)
- PostgreSQL database
- Flyway migrations
- Immutable ledger entries (append-only)

---

## 📊 Domain Model

### Entity: Account
```
Account
├── id: UUID (PK)
├── document: String (unique, indexed)
├── name: String
├── createdAt: Instant
└── updatedAt: Instant
```

### Entity: Transaction
```
Transaction
├── id: UUID (PK)
├── idempotencyKey: String (unique, indexed)
├── sourceAccountId: UUID (FK)
├── targetAccountId: UUID (FK)
├── amount: BigDecimal
├── status: TransactionStatus
└── createdAt: Instant
```

### Entity: LedgerEntry
```
LedgerEntry
├── id: UUID (PK)
├── transactionId: UUID (FK)
├── accountId: UUID (FK)
├── entryType: EntryType (DEBIT/CREDIT)
├── amount: BigDecimal
├── balanceAfter: BigDecimal
└── createdAt: Instant
```

---

## 🔌 API Endpoints

### Accounts API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/accounts` | Create new account |
| GET | `/api/v1/accounts/{id}` | Get account details & balance |
| GET | `/api/v1/accounts` | List all accounts (paginated) |

### Transfers API
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/transfers` | Execute atomic transfer |
| GET | `/api/v1/transfers/{id}` | Get transfer details |

### Ledger API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/ledger/{accountId}` | Get account transaction history |

---

## 🔐 Technical Implementation Details

### 1. Pessimistic Locking Strategy
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") UUID id);
```

### 2. Deadlock Prevention
- Always acquire locks in consistent order (sort account IDs before locking)
- Lock lower UUID first, then higher UUID

### 3. Balance Calculation
```java
@Query("SELECT COALESCE(SUM(CASE WHEN e.entryType = 'CREDIT' THEN e.amount ELSE 0 END) - " +
       "SUM(CASE WHEN e.entryType = 'DEBIT' THEN e.amount ELSE 0 END), 0) " +
       "FROM LedgerEntry e WHERE e.accountId = :accountId")
BigDecimal calculateBalance(@Param("accountId") UUID accountId);
```

### 4. Idempotency Implementation
- Check idempotency key before processing
- Store key with transaction on successful completion
- Return cached response for duplicate keys

### 5. Transaction Flow
1. Receive transfer request with Idempotency-Key header
2. Check if idempotency key exists → return cached response
3. Sort account IDs to prevent deadlocks
4. Acquire pessimistic locks on both accounts
5. Validate source account balance ≥ transfer amount
6. Create Transaction record
7. Create DEBIT LedgerEntry for source
8. Create CREDIT LedgerEntry for target
9. Commit transaction

---

## ⚙️ Configuration

### application.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ledger_db
    username: ${DB_USERNAME:ledger_user}
    password: ${DB_PASSWORD:ledger_pass}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration

logging:
  level:
    com.fintech.ledger: DEBUG
    org.hibernate.SQL: DEBUG
```

### docker-compose.yml
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ledger_db
      POSTGRES_USER: ledger_user
      POSTGRES_PASSWORD: ledger_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

---

## 🧪 Testing Strategy

### Unit Tests ✅
- Domain logic validation
- Balance calculation
- Insufficient funds detection
- Non-negative amount validation
- **74 unit tests completed**

### Integration Tests ✅
- Repository layer with @DataJpaTest
- Controller layer with @WebMvcTest
- Full stack with @SpringBootTest + Testcontainers
- **27 integration tests completed**

### Concurrency Tests ✅
- 100+ concurrent threads withdrawing from same account
- Verify no lost updates
- Verify no overdrafts
- Verify total balance conservation
- **10 concurrency tests completed**

### Test Coverage Targets
- Line coverage: >80%
- Branch coverage: >75%
- Critical paths: 100%

---

## 📈 Implementation Phases

### Phase 1: Project Setup (Tasks 1-20)
- Maven project initialization
- Dependency configuration
- Application properties
- Docker Compose setup

### Phase 2: Domain Model (Tasks 21-50)
- Entity classes
- Enums and value objects
- DTOs for requests/responses
- Validation annotations

### Phase 3: Data Layer (Tasks 51-80)
- Repository interfaces
- Custom queries
- Flyway migrations
- Pessimistic locking queries

### Phase 4: Business Layer (Tasks 81-130)
- Service implementations
- Transaction management
- Balance calculation
- Idempotency handling

### Phase 5: API Layer (Tasks 131-170)
- Controller implementations
- Request validation
- Response mapping
- Exception handling

### Phase 6: Testing (Tasks 171-230)
- Unit tests
- Integration tests
- Concurrency tests
- Test utilities

### Phase 7: DevOps & Polish (Tasks 231-250)
- Dockerfile
- CI/CD pipeline
- Documentation
- Final cleanup

---

## 🎯 Success Criteria

1. ✅ All functional requirements implemented
2. ✅ All non-functional requirements met
3. ✅ All business rules enforced
4. ✅ Test coverage >80%
5. ✅ Concurrency tests passing
6. ✅ No data integrity violations under load
7. ✅ Clean, maintainable code
8. ✅ Comprehensive documentation
