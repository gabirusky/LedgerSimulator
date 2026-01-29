# LedgerSimulator Methods Reference

Comprehensive analysis of all methods in the codebase with Big O complexity notation.

> **Legend:**
> - **n** = number of items in a collection
> - **m** = number of ledger entries for an account
> - **k** = number of accounts to lock

---

## Application Layer

### LedgerSimulatorApplication

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `main` | `main(String[] args)` | **O(1)** | Entry point; delegates to Spring Boot |

---

## Domain Entities

### Account

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `Account()` | Default constructor | **O(1)** | JPA-required no-args constructor |
| `Account(document, name)` | 2-arg constructor | **O(1)** | Creates account with document and name |
| `Account(id, document, name, createdAt, updatedAt)` | Full constructor | **O(1)** | Full initialization |
| `getId()` | Getter | **O(1)** | Returns UUID |
| `getDocument()` | Getter | **O(1)** | Returns document string |
| `getName()` | Getter | **O(1)** | Returns account holder name |
| `getCreatedAt()` | Getter | **O(1)** | Returns creation timestamp |
| `getUpdatedAt()` | Getter | **O(1)** | Returns update timestamp |
| `setId(UUID)` | Setter | **O(1)** | Sets UUID |
| `setDocument(String)` | Setter | **O(1)** | Sets document |
| `setName(String)` | Setter | **O(1)** | Sets name |
| `setCreatedAt(Instant)` | Setter | **O(1)** | Sets creation timestamp |
| `setUpdatedAt(Instant)` | Setter | **O(1)** | Sets update timestamp |
| `equals(Object)` | Override | **O(1)** | ID-based equality (JPA best practice) |
| `hashCode()` | Override | **O(1)** | ID-based hash |
| `toString()` | Override | **O(1)** | String representation |

### Transaction

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `Transaction()` | Default constructor | **O(1)** | JPA-required no-args constructor |
| `Transaction(idempotencyKey, sourceAccountId, targetAccountId, amount, status)` | 5-arg constructor | **O(1)** | Creates transaction |
| `Transaction(id, idempotencyKey, sourceAccountId, targetAccountId, amount, status, createdAt)` | Full constructor | **O(1)** | Full initialization |
| `getId()` | Getter | **O(1)** | Returns UUID |
| `getIdempotencyKey()` | Getter | **O(1)** | Returns idempotency key |
| `getSourceAccountId()` | Getter | **O(1)** | Returns source account UUID |
| `getTargetAccountId()` | Getter | **O(1)** | Returns target account UUID |
| `getAmount()` | Getter | **O(1)** | Returns transfer amount |
| `getStatus()` | Getter | **O(1)** | Returns transaction status |
| `getCreatedAt()` | Getter | **O(1)** | Returns creation timestamp |
| `setId(UUID)` | Setter | **O(1)** | Sets UUID |
| `setIdempotencyKey(String)` | Setter | **O(1)** | Sets idempotency key |
| `setSourceAccountId(UUID)` | Setter | **O(1)** | Sets source account |
| `setTargetAccountId(UUID)` | Setter | **O(1)** | Sets target account |
| `setAmount(BigDecimal)` | Setter | **O(1)** | Sets amount |
| `setStatus(TransactionStatus)` | Setter | **O(1)** | Sets status |
| `setCreatedAt(Instant)` | Setter | **O(1)** | Sets creation timestamp |
| `equals(Object)` | Override | **O(1)** | ID-based equality |
| `hashCode()` | Override | **O(1)** | ID-based hash |
| `toString()` | Override | **O(1)** | String representation |

### LedgerEntry

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `LedgerEntry()` | Default constructor | **O(1)** | JPA-required no-args constructor |
| `LedgerEntry(transactionId, accountId, entryType, amount, balanceAfter)` | 5-arg constructor | **O(1)** | Creates entry |
| `LedgerEntry(id, transactionId, accountId, entryType, amount, balanceAfter, createdAt)` | Full constructor | **O(1)** | Full initialization |
| `getId()` | Getter | **O(1)** | Returns UUID |
| `getTransactionId()` | Getter | **O(1)** | Returns transaction UUID |
| `getAccountId()` | Getter | **O(1)** | Returns account UUID |
| `getEntryType()` | Getter | **O(1)** | Returns DEBIT/CREDIT |
| `getAmount()` | Getter | **O(1)** | Returns entry amount |
| `getBalanceAfter()` | Getter | **O(1)** | Returns balance after entry |
| `getCreatedAt()` | Getter | **O(1)** | Returns creation timestamp |
| `setId(UUID)` | Setter | **O(1)** | Sets UUID |
| `setTransactionId(UUID)` | Setter | **O(1)** | Sets transaction ID |
| `setAccountId(UUID)` | Setter | **O(1)** | Sets account ID |
| `setEntryType(EntryType)` | Setter | **O(1)** | Sets entry type |
| `setAmount(BigDecimal)` | Setter | **O(1)** | Sets amount |
| `setBalanceAfter(BigDecimal)` | Setter | **O(1)** | Sets balance after |
| `setCreatedAt(Instant)` | Setter | **O(1)** | Sets creation timestamp |
| `equals(Object)` | Override | **O(1)** | ID-based equality |
| `hashCode()` | Override | **O(1)** | ID-based hash |
| `toString()` | Override | **O(1)** | String representation |

---

## Repositories

### AccountRepository (extends JpaRepository)

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `findByDocument(String)` | Query by unique index | **O(log n)** | Finds account by document. Uses B-tree index on `document` column |
| `existsByDocument(String)` | Existence check | **O(log n)** | Checks if document exists. More efficient than `findByDocument` |
| `findByIdForUpdate(UUID)` | Pessimistic lock query | **O(log n)** | Acquires `PESSIMISTIC_WRITE` lock with 5s timeout. Prevents race conditions |

> **Inherited from JpaRepository**:
> - `save(S entity)` - **O(log n)** (insert) or **O(1)** (update with existing entity)
> - `findById(ID)` - **O(log n)** (primary key lookup)
> - `findAll()` - **O(n)** (full table scan)
> - `deleteById(ID)` - **O(log n)** (find + delete)
> - `count()` - **O(1)** (table statistics)

### CustomAccountRepository

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `findAllByIdForUpdateSorted(List<UUID>)` | Batch locking | **O(k log n)** | Locks k accounts in sorted UUID order to prevent deadlocks |

### CustomAccountRepositoryImpl

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `findAllByIdForUpdateSorted(List<UUID>)` | Implementation | **O(k log k + k log n)** | Sorts IDs (**O(k log k)**), then acquires k locks (**O(k log n)**) |

> **Note**: The sorting step (`O(k log k)`) ensures consistent lock ordering, which is critical for deadlock prevention in concurrent transfers.

### TransactionRepository (extends JpaRepository)

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `findByIdempotencyKey(String)` | Query by unique index | **O(log n)** | Finds transaction by idempotency key. Uses B-tree index |
| `existsByIdempotencyKey(String)` | Existence check | **O(log n)** | Checks key exists without loading entity. Efficient for idempotency checks |

### LedgerEntryRepository (extends JpaRepository)

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `findByAccountIdOrderByCreatedAtDesc(UUID, Pageable)` | Offset pagination | **O(log m + page)** | Offset-based pagination. For deep pagination prefer cursor-based |
| `findByAccountIdWithCursor(UUID, Instant, int)` | **Cursor pagination** | **O(log m + limit)** | **RECOMMENDED** - Constant time at any depth. Uses composite index |
| `findRecentByAccountId(UUID, int)` | First page query | **O(log m + limit)** | Convenience method for first page of entries |
| `findByTransactionId(UUID)` | Query by transaction | **O(log n + 2)** | Returns exactly 2 entries (DEBIT + CREDIT) per transaction |
| `calculateBalance(UUID)` | Aggregate query | **O(m)** | **SUM(credits) - SUM(debits)**. Full scan of account entries. Use for auditing only |
| `findLatestByAccountId(UUID)` | Latest entry query | **O(log m)** | Uses composite index `(account_id, created_at DESC)`. Fast |
| `countByAccountId(UUID)` | Count query | **O(log m)** | Counts entries for account |
| `findLatestBalance(UUID)` | Fast balance read | **O(log m)** | **PRIMARY read method**. Uses `ORDER BY created_at DESC LIMIT 1` |
| `getBalance(UUID)` | Convenience method | **O(log m)** | Returns `findLatestBalance()` or `BigDecimal.ZERO` |

> [!IMPORTANT]
> **Balance Reading Strategy**:
> - **Fast path** (`findLatestBalance`/`getBalance`): **O(log m)** - Use for normal operations
> - **Slow path** (`calculateBalance`): **O(m)** - Use only for reconciliation/auditing

---

## Mappers (MapStruct Generated)

### AccountMapper

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `toEntity(CreateAccountRequest)` | DTO → Entity | **O(1)** | Maps request DTO to Account entity |
| `toResponse(Account, BigDecimal)` | Entity → DTO | **O(1)** | Maps Account + balance to response DTO |

### LedgerEntryMapper

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `toResponse(LedgerEntry)` | Entity → DTO | **O(1)** | Maps single entry to response DTO |
| `toResponseList(List<LedgerEntry>)` | List mapping | **O(n)** | Maps n entries. Linear in list size |

### TransactionMapper

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `toResponse(Transaction)` | Entity → DTO | **O(1)** | Maps Transaction to TransferResponse DTO |

---

## Validation

### DifferentAccountsValidator

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `initialize(DifferentAccounts)` | Lifecycle hook | **O(1)** | No-op initialization |
| `isValid(TransferRequest, ConstraintValidatorContext)` | Validation logic | **O(1)** | Compares two UUIDs; returns false if same |

---

## DTOs (Java Records)

### CreateAccountRequest

| Component | Type | Complexity | Description |
|-----------|------|------------|-------------|
| `document()` | String accessor | **O(1)** | Auto-generated by record |
| `name()` | String accessor | **O(1)** | Auto-generated by record |
| `equals()` | Generated | **O(1)** | Component-based equality |
| `hashCode()` | Generated | **O(1)** | Component-based hash |
| `toString()` | Generated | **O(1)** | Component-based string |

### TransferRequest

| Component | Type | Complexity | Description |
|-----------|------|------------|-------------|
| `sourceAccountId()` | UUID accessor | **O(1)** | Auto-generated by record |
| `targetAccountId()` | UUID accessor | **O(1)** | Auto-generated by record |
| `amount()` | BigDecimal accessor | **O(1)** | Auto-generated by record |
| `equals()` | Generated | **O(1)** | Component-based equality |
| `hashCode()` | Generated | **O(1)** | Component-based hash |
| `toString()` | Generated | **O(1)** | Component-based string |

### AccountResponse

| Component | Type | Complexity | Description |
|-----------|------|------------|-------------|
| `id()` | UUID accessor | **O(1)** | Auto-generated by record |
| `document()` | String accessor | **O(1)** | Auto-generated by record |
| `name()` | String accessor | **O(1)** | Auto-generated by record |
| `balance()` | BigDecimal accessor | **O(1)** | Auto-generated by record |
| `createdAt()` | Instant accessor | **O(1)** | Auto-generated by record |

---

## Services

### AccountService / AccountServiceImpl

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `createAccount` | `createAccount(CreateAccountRequest)` | **O(log n)** | Checks duplicate + saves account. Dominated by DB operations |
| `getAccount` | `getAccount(UUID id)` | **O(log n + log m)** | Account lookup + fast balance query |
| `getAccountBalance` | `getAccountBalance(UUID id)` | **O(log n + log m)** | Account exists check + fast balance query |
| `getAllAccounts` | `getAllAccounts(Pageable)` | **O(page × log m)** | Paginated accounts with balance for each |

> **Note**: `n` = number of accounts, `m` = number of ledger entries for an account

### TransferService / TransferServiceImpl

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `executeTransfer` | `executeTransfer(TransferRequest, String)` | **O(log n + log m)** | Idempotency check, lock 2 accounts, validate balance, create 2 entries |
| `getTransfer` | `getTransfer(UUID)` | **O(log n)** | Transaction lookup by primary key |

**Transfer Flow Breakdown:**
1. Idempotency check: O(log n)
2. Sort account IDs: O(1)
3. Lock 2 accounts: O(2 log n)
4. Fast balance read: O(log m)
5. Create Transaction: O(log n)
6. Create 2 LedgerEntries: O(2 log n)
7. Update Transaction status: O(1)

### LedgerService / LedgerServiceImpl

| Method | Signature | Complexity | Description |
|--------|-----------|------------|-------------|
| `getAccountStatement` | `getAccountStatement(UUID)` | **O(log n + log m + limit)** | Account lookup + balance + limited entries |
| `getAccountStatement` | `getAccountStatement(UUID, Pageable)` | **O(log n + log m + offset + page)** | Paginated version; offset degrades for deep pages |

---

## Exceptions

### Custom Exception Classes

| Exception | Fields | Description |
|-----------|--------|-------------|
| `AccountNotFoundException` | `accountId: UUID` | Thrown when account ID lookup fails |
| `TransactionNotFoundException` | `transactionId: UUID` | Thrown when transaction ID lookup fails |
| `DuplicateDocumentException` | `document: String` | Thrown when creating account with existing document |
| `InsufficientFundsException` | `accountId, available, requested` | Thrown when transfer amount exceeds balance |

---

## Performance Summary

### Critical Paths

| Operation | Complexity | Notes |
|-----------|------------|-------|
| **Account lookup by ID** | O(log n) | Primary key index |
| **Account lookup by document** | O(log n) | Unique index on `document` |
| **Fast balance read** | O(log m) | Index on `(account_id, created_at DESC)` |
| **Balance calculation (audit)** | O(m) | Full scan - use sparingly |
| **Pessimistic lock (single)** | O(log n) | 5s timeout configured |
| **Pessimistic lock (batch)** | O(k log k + k log n) | Sorted for deadlock prevention |
| **Idempotency check** | O(log n) | Unique index on `idempotency_key` |
| **Execute transfer** | O(log n + log m) | Full atomic transfer operation |
| **Create account** | O(log n) | Duplicate check + insert |

### Required Database Indexes

```sql
-- Primary indexes (auto-created)
CREATE INDEX idx_accounts_pkey ON accounts(id);
CREATE INDEX idx_transactions_pkey ON transactions(id);
CREATE INDEX idx_ledger_entries_pkey ON ledger_entries(id);

-- Critical performance indexes
CREATE UNIQUE INDEX idx_accounts_document ON accounts(document);
CREATE UNIQUE INDEX idx_transactions_idempotency ON transactions(idempotency_key);
CREATE INDEX idx_ledger_entries_account_created ON ledger_entries(account_id, created_at DESC);
CREATE INDEX idx_ledger_entries_transaction ON ledger_entries(transaction_id);
```

---

## Method Count Summary

| Layer | Files | Methods |
|-------|-------|---------|
| Application | 1 | 1 |
| Entities | 3 | 51 |
| Repositories | 5 | 15 |
| **Services** | **6** | **10** |
| Mappers | 3 | 5 |
| Validation | 1 | 2 |
| Exceptions | 4 | 8 |
| DTOs (records) | ~6 | ~18 (auto-generated) |
| **Total** | **~29** | **~110** |

> **Note**: Package-info files and enums (`EntryType`, `TransactionStatus`) excluded as they contain no methods.
