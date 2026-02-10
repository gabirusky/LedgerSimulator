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

---

## 🖥️ Phase 8: Frontend Solution

A high-performance backend requires a responsive, type-safe frontend. The interface is split into two distinct applications: a **User Simulator** (digital wallet experience) and an **Admin Panel** (ledger oversight dashboard). Both live inside a shared Vite + React monorepo under `frontend/`.

### 8.1 Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18 | UI framework |
| Vite | 5+ | Build tooling (ESBuild HMR, fast dev server) |
| TypeScript | 5+ | Type safety for financial data structures |
| TanStack Query (React Query) | 5 | Server-state management (caching, re-fetching, optimistic updates) |
| shadcn/ui | latest | Component library (Radix primitives + Tailwind styling) |
| Tailwind CSS | 4 | Utility-first CSS (required by shadcn/ui) |
| Recharts | 2+ | Data visualizations (TPS, volume charts) |
| React Router | 6+ | Client-side routing (HashRouter for GitHub Pages) |

**Why shadcn/ui?** Unlike opinionated component libraries (MUI, Ant Design), shadcn/ui gives full ownership of component code. Components are copied into the project, allowing deep customization—critical for building both a consumer-grade wallet UI and an enterprise admin dashboard from the same primitives.

**Why TanStack Query over Redux?** Ledger data is inherently server-side state. TanStack Query provides caching, automatic re-fetching, background sync, and optimistic updates without Redux boilerplate.

### 8.2 Frontend Project Structure

```
frontend/
├── public/
│   └── .nojekyll                   # Disable Jekyll on GitHub Pages
├── src/
│   ├── main.tsx                    # Entry point
│   ├── App.tsx                     # Root with HashRouter
│   ├── lib/
│   │   └── utils.ts                # cn() helper for shadcn/ui
│   ├── components/
│   │   └── ui/                     # shadcn/ui components (Button, Card, Table, etc.)
│   ├── services/
│   │   └── ledgerProvider.ts       # API abstraction layer
│   ├── hooks/
│   │   ├── useAccounts.ts          # TanStack Query: accounts CRUD
│   │   ├── useBalance.ts           # TanStack Query: real-time balance polling
│   │   ├── useTransfers.ts         # TanStack Query: transfer mutations
│   │   └── useTransactionStream.ts # TanStack Query: paginated ledger entries
│   ├── pages/
│   │   ├── admin/
│   │   │   ├── DashboardPage.tsx   # System Health + Balance Integrity
│   │   │   ├── LedgerPage.tsx      # General Ledger Data Grid
│   │   │   └── AccountsPage.tsx    # Account management
│   │   └── user/
│   │       ├── WalletPage.tsx      # Wallet Card + balance
│   │       └── HistoryPage.tsx     # Transaction Stream
│   ├── features/
│   │   ├── admin/
│   │   │   ├── GeneralLedgerGrid.tsx    # Data Grid with cursor pagination
│   │   │   ├── SystemHealthChart.tsx    # TPS + Volume Recharts
│   │   │   └── BalanceIntegrityWidget.tsx # sum(credits) - sum(debits) check
│   │   └── user/
│   │       ├── WalletCard.tsx           # Balance display with optimistic UI
│   │       ├── TransferForm.tsx         # Transfer execution form
│   │       └── TransactionStream.tsx    # User-facing ledger view
│   └── types/
│       └── api.ts                  # TypeScript interfaces matching backend DTOs
├── index.html
├── vite.config.ts
├── tailwind.config.ts
├── tsconfig.json
├── package.json
└── components.json                 # shadcn/ui configuration
```

### 8.3 API Service Layer

The frontend uses a "Service" pattern for all API communication, decoupling components from fetch logic:

```typescript
// src/services/ledgerProvider.ts
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const ledgerProvider = {
    getAccounts: (page = 0, size = 20) =>
        fetch(`${API_URL}/accounts?page=${page}&size=${size}`).then(r => r.json()),

    getAccount: (id: string) =>
        fetch(`${API_URL}/accounts/${id}`).then(r => r.json()),

    createAccount: (data: CreateAccountRequest) =>
        fetch(`${API_URL}/accounts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        }).then(r => r.json()),

    executeTransfer: (data: TransferRequest, idempotencyKey: string) =>
        fetch(`${API_URL}/transfers`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Idempotency-Key': idempotencyKey,
            },
            body: JSON.stringify(data),
        }).then(r => r.json()),

    getLedger: (accountId: string, page = 0, size = 50) =>
        fetch(`${API_URL}/ledger/${accountId}?page=${page}&size=${size}`).then(r => r.json()),
};
```

### 8.4 Component Architecture: Admin Dashboard

The Admin Dashboard is the "God View" of the system, exposing raw ledger data for audit operations.

#### 8.4.1 General Ledger Data Grid

Central component handling high-cardinality data:
- **Columns**: TransactionID, Timestamp, Source, Destination, Amount, Status
- **Pagination**: Cursor-based (not offset-based) for consistent performance as ledger grows
- **Filtering**: Column filters for account ID, date range, amount range, status
- **Built with**: shadcn/ui `Table` + `DataTable` pattern with `@tanstack/react-table`

#### 8.4.2 System Health & Analytics

- **TPS Chart**: Recharts `LineChart` plotting transactions per second over time
- **Volume Chart**: Recharts `AreaChart` showing total transfer volume over time
- **Built with**: shadcn/ui `Card` components for layout

#### 8.4.3 Balance Integrity Widget

Critical audit widget:
- Queries backend for `sum(all_credits) - sum(all_debits)`
- Must always equal **zero** (conservation of value)
- Displays green ✅ when balanced, red 🚨 alert when deviation detected
- Auto-refreshes every 30 seconds via TanStack Query

### 8.5 Component Architecture: User Simulator

The User Simulator mimics a consumer-grade digital wallet experience.

#### 8.5.1 Wallet Card

Displays user's current holdings:
- **Real-Time Data**: Short-polling (5-10s) via TanStack Query `refetchInterval`
- **Optimistic UI**: On transfer initiation, immediately deduct amount from displayed balance; roll back only if API call fails
- **Built with**: shadcn/ui `Card` with custom styling for the "fintech wallet" look

#### 8.5.2 Transaction Stream

Simplified ledger view relative to a single identity:
- Shows human-readable descriptions rather than raw Account IDs
- Color-coded: green for incoming (CREDIT), red for outgoing (DEBIT)
- Infinite scroll pagination via TanStack Query `useInfiniteQuery`
- **Built with**: shadcn/ui `ScrollArea` + virtualized list

#### 8.5.3 Transfer Form

- Source/target account selection via shadcn/ui `Select` + `Command` (searchable)
- Amount input with `BigDecimal`-safe validation (no floating point issues)
- Auto-generated `Idempotency-Key` (UUID v4) per submission
- **Built with**: shadcn/ui `Form` + `Input` + `Button` components

### 8.6 TypeScript Interfaces

Strict type-safety matching backend DTOs:

```typescript
// src/types/api.ts
export interface Account {
    id: string;
    document: string;
    name: string;
    balance: number;
    createdAt: string;
}

export interface TransferRequest {
    sourceAccountId: string;
    targetAccountId: string;
    amount: number;
}

export interface TransferResponse {
    transactionId: string;
    sourceAccountId: string;
    targetAccountId: string;
    amount: number;
    status: 'PENDING' | 'COMPLETED' | 'FAILED';
    createdAt: string;
}

export interface LedgerEntry {
    id: string;
    transactionId: string;
    entryType: 'DEBIT' | 'CREDIT';
    amount: number;
    balanceAfter: number;
    createdAt: string;
}

export interface AccountStatement {
    accountId: string;
    accountName: string;
    currentBalance: number;
    entries: LedgerEntry[];
}
```

---

## 🚀 Phase 9: CI/CD & GitHub Pages Deployment

### 9.1 The Deployment Challenge: Client-Side Routing

GitHub Pages is a static file host—it does not support server-side routing. If a user navigates to `/transactions` and refreshes, GitHub Pages returns a 404.

**Solutions:**
1. **HashRouter**: Use `HashRouter` in React Router (e.g., `https://domain.com/#/transactions`). The fragment identifier is handled entirely by client-side JavaScript.
2. **`.nojekyll` file**: GitHub Pages uses Jekyll by default, which ignores files starting with `_` (like `_assets` from Vite). An empty `.nojekyll` file disables this behavior.

### 9.2 Vite Configuration for Production

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
    plugins: [react()],
    base: '/LedgerSimulator/',  // Must match GitHub repo name
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src'),
        },
    },
    build: {
        outDir: 'dist',
        sourcemap: false,  // Disable for production security
    },
})
```

### 9.3 GitHub Actions Deploy Workflow

```yaml
# .github/workflows/deploy-frontend.yml
name: Deploy Frontend to GitHub Pages

on:
  push:
    branches: ["main"]
    paths: ['frontend/**']
  workflow_dispatch:

permissions:
  contents: read
  pages: write
  id-token: write

concurrency:
  group: "pages"
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: frontend
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      - run: npm ci
      - run: npm test -- --passWithNoTests
      - run: npm run build
        env:
          VITE_API_URL: ${{ vars.API_URL }}
      - uses: actions/upload-pages-artifact@v3
        with:
          path: frontend/dist

  deploy:
    environment:
      name: github-pages
      url: ${{ steps.deployment.outputs.page_url }}
    runs-on: ubuntu-latest
    needs: build
    steps:
      - id: deployment
        uses: actions/deploy-pages@v4
```

### 9.4 Strategic Recommendations

#### Build vs. Buy
Building a ledger from scratch (LedgerSimulator pattern) is advisable when extreme customization is needed. For most fintech startups, leveraging battle-tested cores (TigerBeetle for performance, Blnk for features) allows faster time-to-market.

#### Metadata & Reconciliation
Every transaction should be linkable to an external event via metadata (`{"order_id": "ORD-123"}`). Future work should implement an ingestion interface for standard banking formats (MT940, BAI2, CSV) for automated reconciliation.

#### Frontend Read Amplification
A highly active ledger produces millions of rows. The Admin Panel uses cursor-based pagination and pre-computed aggregations (daily closing balances) rather than summing transactions on-the-fly. TanStack Query caches expensive reads to reduce backend API load.
