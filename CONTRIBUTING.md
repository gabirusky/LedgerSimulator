# Contributing to Fintech Ledger Simulator

Thank you for your interest in contributing to the Fintech Ledger Simulator! This document provides guidelines and best practices for contributing to this project.

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker & Docker Compose
- Git

### Development Setup

1. **Fork and clone the repository:**
   ```bash
   git clone https://github.com/your-username/fintech-ledger-simulator.git
   cd fintech-ledger-simulator
   ```

2. **Set up environment:**
   ```bash
   cp .env.example .env
   ```

3. **Start PostgreSQL:**
   ```bash
   docker-compose up -d
   ```

4. **Build and test:**
   ```bash
   mvn clean verify
   ```

---

## 📋 Code Guidelines

### Java Style

- Use **Java 21** features where appropriate (Records, Pattern Matching, etc.)
- Use `BigDecimal` for ALL monetary values (never `double` or `float`)
- Use `UUID` for entity identifiers
- Use `Instant` for timestamps
- Use `Optional` for nullable returns (never return null)
- Use explicit imports (no wildcards)

### Package Structure

```
com.fintech.ledger
├── config/          → Spring configuration
├── controller/      → REST controllers
├── service/         → Business logic
├── repository/      → Data access
├── domain/
│   ├── entity/      → JPA entities
│   └── dto/         → Request/Response DTOs
├── exception/       → Custom exceptions
├── mapper/          → MapStruct mappers
└── validation/      → Custom validators
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Entity | Singular noun | `Account`, `LedgerEntry` |
| Repository | EntityRepository | `AccountRepository` |
| Service | FeatureService | `TransferService` |
| Controller | EntityController | `AccountController` |
| DTO | Entity + Request/Response | `CreateAccountRequest` |
| Exception | Description + Exception | `InsufficientFundsException` |

---

## 🧪 Testing Requirements

### Required Test Coverage

All contributions must include tests:

| Type | When Required | Tools |
|------|---------------|-------|
| Unit Tests | All services and mappers | JUnit 5, Mockito, AssertJ |
| Integration Tests | Repository/Controller changes | Testcontainers, @SpringBootTest |
| Concurrency Tests | Thread-safety changes | ExecutorService, CountDownLatch |

### Test Naming Convention

```java
@Test
void should_RejectTransfer_When_InsufficientFunds() { ... }

@Test
void should_ReturnCachedResponse_When_DuplicateIdempotencyKey() { ... }
```

### Running Tests

```bash
# Unit tests only
mvn test

# All tests including integration
mvn verify

# Specific test class
mvn test -Dtest=TransferServiceTest

# Integration tests only
mvn failsafe:integration-test
```

---

## 🔄 Git Workflow

### Branch Naming

| Type | Pattern | Example |
|------|---------|---------|
| Feature | `feature/description` | `feature/add-audit-logging` |
| Bugfix | `bugfix/description` | `bugfix/fix-balance-calculation` |
| Hotfix | `hotfix/description` | `hotfix/security-patch` |

### Commit Messages

Use conventional commit format:

```
type(scope): description

[optional body]

[optional footer]
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `test`: Adding tests
- `refactor`: Code refactoring
- `chore`: Build/config changes

**Examples:**
```
feat(transfer): add idempotency key validation

fix(balance): correct calculation for zero amounts

test(concurrency): add 100-thread stress test
```

---

## 📝 Pull Request Process

1. **Ensure tests pass locally:**
   ```bash
   mvn clean verify
   ```

2. **Update documentation** if you changed:
   - API endpoints → Update Swagger annotations
   - Environment requirements → Update README
   - Architecture → Update CONTEXT.md

3. **Create a descriptive PR:**
   - Title: Use conventional commit format
   - Description: Explain what and why
   - Reference any related issues

4. **Code Review:**
   - At least one approval required
   - All CI checks must pass
   - No unresolved conversations

---

## ⚠️ Critical Rules

### Financial Integrity

These rules are **non-negotiable** for a financial system:

1. **Never store balance as a column** - Calculate from ledger entries
2. **Never modify ledger entries** - They are immutable (append-only)
3. **Always use @Transactional** for write operations
4. **Always acquire locks in sorted order** - Prevents deadlocks
5. **Always validate idempotency** - Prevents duplicate transactions

### Testing Concurrency

When modifying transfer logic:

```java
// Run concurrency tests to verify no race conditions
mvn failsafe:integration-test -Dit.test=ConcurrentTransferTest
```

---

## 🐛 Reporting Issues

Use GitHub Issues with appropriate labels:

- `bug` - Something isn't working
- `enhancement` - New feature request
- `documentation` - Documentation improvement
- `question` - General questions

Include in bug reports:
- Expected behavior
- Actual behavior
- Steps to reproduce
- Environment details (Java version, OS)

---

## 📖 Documentation

- **README.md**: User-facing documentation
- **CONTEXT.md**: Technical context for developers
- **PLAN.md**: Architecture and design decisions
- **METHODS.md**: Method reference with Big O complexity
- **TASKS.md**: Implementation task tracking

---

## 🎯 First-Time Contributors

Look for issues tagged with `good first issue`:

- Documentation improvements
- Test coverage additions
- Minor bug fixes
- Code formatting

---

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing! 🙏
