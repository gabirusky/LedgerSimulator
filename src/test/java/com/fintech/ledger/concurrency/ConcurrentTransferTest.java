package com.fintech.ledger.concurrency;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fintech.ledger.domain.dto.request.CreateAccountRequest;
import com.fintech.ledger.domain.dto.request.TransferRequest;
import com.fintech.ledger.domain.dto.response.AccountResponse;
import com.fintech.ledger.domain.dto.response.TransferResponse;
import com.fintech.ledger.domain.entity.EntryType;
import com.fintech.ledger.domain.entity.LedgerEntry;
import com.fintech.ledger.domain.entity.Transaction;
import com.fintech.ledger.domain.entity.TransactionStatus;
import com.fintech.ledger.integration.AbstractIntegrationTest;
import com.fintech.ledger.repository.LedgerEntryRepository;
import com.fintech.ledger.repository.TransactionRepository;

/**
 * Concurrency tests for the transfer service.
 * <p>
 * Tests verify:
 * <ul>
 *   <li>No overdrafts under concurrent withdrawals</li>
 *   <li>No deadlocks with bi-directional transfers</li>
 *   <li>Conservation of value (total money unchanged)</li>
 *   <li>All balances remain non-negative</li>
 * </ul>
 * <p>
 * Tasks: 306-325
 */
public class ConcurrentTransferTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentTransferTest.class);

    private static final String ACCOUNTS_URL = "/api/v1/accounts";
    private static final String TRANSFERS_URL = "/api/v1/transfers";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Creates an account with the specified initial balance.
     * Uses direct repository access to create a genesis credit entry for seeding.
     */
    private UUID seedAccountWithBalance(String name, BigDecimal initialBalance) {
        // Create the target account
        CreateAccountRequest request = new CreateAccountRequest("DOC-" + UUID.randomUUID(), name);
        ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                ACCOUNTS_URL, request, AccountResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID accountId = response.getBody().id();

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            // Create a genesis transaction for seeding (external deposit simulation)
            Transaction genesisTransaction = new Transaction(
                    "GENESIS-" + UUID.randomUUID(),  // idempotencyKey
                    accountId,  // sourceAccountId
                    accountId,  // targetAccountId (self for genesis)
                    initialBalance,
                    TransactionStatus.COMPLETED
            );
            Transaction savedTransaction = transactionRepository.save(genesisTransaction);

            // Create credit entry to fund the account
            LedgerEntry creditEntry = new LedgerEntry(
                    savedTransaction.getId(),
                    accountId,
                    EntryType.CREDIT,
                    initialBalance,
                    initialBalance
            );
            ledgerEntryRepository.save(creditEntry);
        }

        return accountId;
    }

    /**
     * Helper to execute a transfer with idempotency key.
     */
    private ResponseEntity<TransferResponse> executeTransfer(
            UUID sourceId, UUID targetId, BigDecimal amount, String idempotencyKey) {
        TransferRequest request = new TransferRequest(sourceId, targetId, amount);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", idempotencyKey);
        HttpEntity<TransferRequest> entity = new HttpEntity<>(request, headers);
        return restTemplate.postForEntity(TRANSFERS_URL, entity, TransferResponse.class);
    }

    /**
     * Gets the current balance of an account.
     */
    private BigDecimal getBalance(UUID accountId) {
        ResponseEntity<AccountResponse> response = restTemplate.getForEntity(
                ACCOUNTS_URL + "/" + accountId, AccountResponse.class);
        return response.getBody().balance();
    }

    // =========================================================================
    // CONCURRENT WITHDRAWAL TESTS (Tasks 310-315)
    // =========================================================================

    @Nested
    @DisplayName("Concurrent Withdrawal Tests")
    class ConcurrentWithdrawalTests {

        private UUID sourceAccountId;
        private UUID targetAccountId;

        @BeforeEach
        void setUp() {
            // Create source with $1000 and a target account
            sourceAccountId = seedAccountWithBalance("Source", new BigDecimal("1000.00"));
            targetAccountId = seedAccountWithBalance("Target", BigDecimal.ZERO);
        }

        @Test
        @DisplayName("TASK-310/311/312: 10 concurrent withdrawals - no overdraft, correct balance")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void test10ConcurrentWithdrawals_NoOverdraft() throws Exception {
            int threadCount = 10;
            BigDecimal withdrawalAmount = new BigDecimal("150.00"); // 10 x 150 = 1500, only 6 can succeed

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        ResponseEntity<TransferResponse> response = executeTransfer(
                                sourceAccountId, targetAccountId, withdrawalAmount,
                                "WITHDRAW-10-" + index + "-" + UUID.randomUUID());

                        if (response.getStatusCode() == HttpStatus.CREATED) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // Release all threads
            doneLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            BigDecimal finalBalance = getBalance(sourceAccountId);
            BigDecimal expectedBalance = new BigDecimal("1000.00")
                    .subtract(withdrawalAmount.multiply(BigDecimal.valueOf(successCount.get())));

            log.info("10 concurrent withdrawals: success={}, failures={}, finalBalance={}",
                    successCount.get(), failureCount.get(), finalBalance);

            // Assertions
            assertThat(finalBalance).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(finalBalance).isEqualByComparingTo(expectedBalance);
            assertThat(successCount.get()).isLessThanOrEqualTo(6); // Max 6 x $150 = $900 < $1000
        }

        @Test
        @DisplayName("TASK-313: 50 concurrent withdrawals from same account")
        @Timeout(value = 60, unit = TimeUnit.SECONDS)
        void test50ConcurrentWithdrawals() throws Exception {
            int threadCount = 50;
            BigDecimal withdrawalAmount = new BigDecimal("50.00"); // 50 x 50 = 2500, only 20 can succeed

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        ResponseEntity<TransferResponse> response = executeTransfer(
                                sourceAccountId, targetAccountId, withdrawalAmount,
                                "WITHDRAW-50-" + index + "-" + UUID.randomUUID());

                        if (response.getStatusCode() == HttpStatus.CREATED) {
                            successCount.incrementAndGet();
                        } else {
                            failureCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(60, TimeUnit.SECONDS);
            executor.shutdown();

            BigDecimal finalBalance = getBalance(sourceAccountId);

            log.info("50 concurrent withdrawals: success={}, failures={}, finalBalance={}",
                    successCount.get(), failureCount.get(), finalBalance);

            assertThat(finalBalance).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(successCount.get()).isLessThanOrEqualTo(20); // Max 20 x $50 = $1000
        }

        @Test
        @DisplayName("TASK-314/315: 100 concurrent withdrawals - verify 422 responses for failures")
        @Timeout(value = 90, unit = TimeUnit.SECONDS)
        void test100ConcurrentWithdrawals_Verify422Responses() throws Exception {
            int threadCount = 100;
            BigDecimal withdrawalAmount = new BigDecimal("20.00"); // 100 x 20 = 2000, only 50 can succeed

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger insufficientFundsCount = new AtomicInteger(0);
            AtomicInteger otherErrorCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        ResponseEntity<TransferResponse> response = executeTransfer(
                                sourceAccountId, targetAccountId, withdrawalAmount,
                                "WITHDRAW-100-" + index + "-" + UUID.randomUUID());

                        if (response.getStatusCode() == HttpStatus.CREATED) {
                            successCount.incrementAndGet();
                        } else if (response.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                            insufficientFundsCount.incrementAndGet();
                        } else {
                            otherErrorCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        otherErrorCount.incrementAndGet();
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(90, TimeUnit.SECONDS);
            executor.shutdown();

            BigDecimal finalBalance = getBalance(sourceAccountId);

            log.info("100 concurrent withdrawals: success={}, insufficientFunds={}, other={}, finalBalance={}",
                    successCount.get(), insufficientFundsCount.get(), otherErrorCount.get(), finalBalance);

            // Assertions
            assertThat(finalBalance).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            assertThat(successCount.get() + insufficientFundsCount.get() + otherErrorCount.get())
                    .isEqualTo(threadCount);
            assertThat(otherErrorCount.get()).isZero(); // No unexpected errors
            assertThat(insufficientFundsCount.get()).isGreaterThan(0); // Some failed due to insufficient funds
        }
    }

    // =========================================================================
    // CONCURRENT TRANSFER TESTS (Tasks 316-320)
    // =========================================================================

    @Nested
    @DisplayName("Concurrent Transfer Tests")
    class ConcurrentTransferTests {

        @Test
        @DisplayName("TASK-316/317: A→B while B→A - no deadlock")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void testBidirectionalTransfers_NoDeadlock() throws Exception {
            UUID accountA = seedAccountWithBalance("Account A", new BigDecimal("500.00"));
            UUID accountB = seedAccountWithBalance("Account B", new BigDecimal("500.00"));

            BigDecimal initialTotal = new BigDecimal("1000.00");
            BigDecimal transferAmount = new BigDecimal("100.00");

            int iterations = 10;
            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(iterations * 2);

            // A → B transfers
            for (int i = 0; i < iterations; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        executeTransfer(accountA, accountB, transferAmount, "A-TO-B-" + index + "-" + UUID.randomUUID());
                    } catch (Exception e) {
                        log.warn("A→B transfer failed: {}", e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // B → A transfers
            for (int i = 0; i < iterations; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        executeTransfer(accountB, accountA, transferAmount, "B-TO-A-" + index + "-" + UUID.randomUUID());
                    } catch (Exception e) {
                        log.warn("B→A transfer failed: {}", e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).isTrue().describedAs("Test should complete without deadlock");

            // Verify conservation of value
            BigDecimal balanceA = getBalance(accountA);
            BigDecimal balanceB = getBalance(accountB);
            BigDecimal totalBalance = balanceA.add(balanceB);

            log.info("Bidirectional transfers: A={}, B={}, total={}", balanceA, balanceB, totalBalance);

            assertThat(totalBalance).isEqualByComparingTo(initialTotal);
        }

        @Test
        @DisplayName("TASK-318: Conservation of value after concurrent transfers")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void testConservationOfValue() throws Exception {
            UUID accountA = seedAccountWithBalance("Account A", new BigDecimal("1000.00"));
            UUID accountB = seedAccountWithBalance("Account B", new BigDecimal("1000.00"));
            UUID accountC = seedAccountWithBalance("Account C", new BigDecimal("1000.00"));

            BigDecimal initialTotal = new BigDecimal("3000.00");

            int transferCount = 30;
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(transferCount);

            List<UUID> accounts = List.of(accountA, accountB, accountC);
            java.util.Random random = new java.util.Random(42); // Deterministic for reproducibility

            for (int i = 0; i < transferCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        int fromIdx = random.nextInt(3);
                        int toIdx = (fromIdx + 1 + random.nextInt(2)) % 3;
                        BigDecimal amount = new BigDecimal(random.nextInt(100) + 1);

                        executeTransfer(accounts.get(fromIdx), accounts.get(toIdx), amount,
                                "CONSERVATION-" + index + "-" + UUID.randomUUID());
                    } catch (Exception e) {
                        // Some transfers may fail due to insufficient funds
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            BigDecimal balanceA = getBalance(accountA);
            BigDecimal balanceB = getBalance(accountB);
            BigDecimal balanceC = getBalance(accountC);
            BigDecimal totalBalance = balanceA.add(balanceB).add(balanceC);

            log.info("Conservation test: A={}, B={}, C={}, total={}", balanceA, balanceB, balanceC, totalBalance);

            assertThat(totalBalance).isEqualByComparingTo(initialTotal);
        }

        @Test
        @DisplayName("TASK-319: Circular transfers A→B→C→A concurrently")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void testCircularTransfers() throws Exception {
            UUID accountA = seedAccountWithBalance("Account A", new BigDecimal("500.00"));
            UUID accountB = seedAccountWithBalance("Account B", new BigDecimal("500.00"));
            UUID accountC = seedAccountWithBalance("Account C", new BigDecimal("500.00"));

            BigDecimal initialTotal = new BigDecimal("1500.00");
            BigDecimal transferAmount = new BigDecimal("50.00");

            int iterations = 5;
            ExecutorService executor = Executors.newFixedThreadPool(3);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(iterations * 3);

            // A → B
            for (int i = 0; i < iterations; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        executeTransfer(accountA, accountB, transferAmount, "CIRCULAR-AB-" + index + "-" + UUID.randomUUID());
                    } catch (Exception e) {
                        // May fail
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // B → C
            for (int i = 0; i < iterations; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        executeTransfer(accountB, accountC, transferAmount, "CIRCULAR-BC-" + index + "-" + UUID.randomUUID());
                    } catch (Exception e) {
                        // May fail
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // C → A
            for (int i = 0; i < iterations; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        executeTransfer(accountC, accountA, transferAmount, "CIRCULAR-CA-" + index + "-" + UUID.randomUUID());
                    } catch (Exception e) {
                        // May fail
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).isTrue();

            BigDecimal balanceA = getBalance(accountA);
            BigDecimal balanceB = getBalance(accountB);
            BigDecimal balanceC = getBalance(accountC);
            BigDecimal totalBalance = balanceA.add(balanceB).add(balanceC);

            log.info("Circular transfers: A={}, B={}, C={}, total={}", balanceA, balanceB, balanceC, totalBalance);

            assertThat(totalBalance).isEqualByComparingTo(initialTotal);
        }

        @Test
        @DisplayName("TASK-320: All balances non-negative after concurrent transfers")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void testAllBalancesNonNegative() throws Exception {
            List<UUID> accounts = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                accounts.add(seedAccountWithBalance("Account " + i, new BigDecimal("200.00")));
            }

            int transferCount = 50;
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(transferCount);

            java.util.Random random = new java.util.Random(123);

            for (int i = 0; i < transferCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        int fromIdx = random.nextInt(5);
                        int toIdx = (fromIdx + 1 + random.nextInt(4)) % 5;
                        BigDecimal amount = new BigDecimal(random.nextInt(50) + 1);

                        executeTransfer(accounts.get(fromIdx), accounts.get(toIdx), amount,
                                "NONNEG-" + index + "-" + UUID.randomUUID());
                    } catch (Exception e) {
                        // May fail
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            for (int i = 0; i < accounts.size(); i++) {
                BigDecimal balance = getBalance(accounts.get(i));
                log.info("Account {} balance: {}", i, balance);
                assertThat(balance).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            }
        }
    }

    // =========================================================================
    // STRESS TESTS (Tasks 321-325)
    // =========================================================================

    @Nested
    @DisplayName("Stress Tests")
    class StressTests {

        @Test
        @DisplayName("TASK-321/322: 100 threads, 10 transfers each - with timing")
        @Timeout(value = 120, unit = TimeUnit.SECONDS)
        void test100Threads10TransfersEach() throws Exception {
            List<UUID> accounts = new ArrayList<>();
            BigDecimal initialBalance = new BigDecimal("10000.00");
            for (int i = 0; i < 10; i++) {
                accounts.add(seedAccountWithBalance("Stress Account " + i, initialBalance));
            }

            BigDecimal initialTotal = initialBalance.multiply(BigDecimal.valueOf(10));

            int threadCount = 100;
            int transfersPerThread = 10;
            int totalTransfers = threadCount * transfersPerThread;

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(totalTransfers);

            java.util.Random random = new java.util.Random(999);

            long startTime = System.currentTimeMillis();

            for (int t = 0; t < threadCount; t++) {
                final int threadId = t;
                for (int i = 0; i < transfersPerThread; i++) {
                    final int transferId = i;
                    executor.submit(() -> {
                        try {
                            startLatch.await();
                            int fromIdx = random.nextInt(10);
                            int toIdx = (fromIdx + 1 + random.nextInt(9)) % 10;
                            BigDecimal amount = new BigDecimal(random.nextInt(100) + 1);

                            ResponseEntity<TransferResponse> response = executeTransfer(
                                    accounts.get(fromIdx), accounts.get(toIdx), amount,
                                    "STRESS-" + threadId + "-" + transferId + "-" + UUID.randomUUID());

                            if (response.getStatusCode() == HttpStatus.CREATED) {
                                successCount.incrementAndGet();
                            } else {
                                failCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }
            }

            startLatch.countDown();
            doneLatch.await(120, TimeUnit.SECONDS);
            executor.shutdown();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            BigDecimal totalBalance = BigDecimal.ZERO;
            for (UUID accountId : accounts) {
                totalBalance = totalBalance.add(getBalance(accountId));
            }

            log.info("Stress test completed: {} transfers in {} ms ({} success, {} failed), total balance={}",
                    totalTransfers, duration, successCount.get(), failCount.get(), totalBalance);

            // TASK-323: Verify zero data integrity violations
            assertThat(totalBalance).isEqualByComparingTo(initialTotal);
        }

        @Test
        @DisplayName("TASK-324: Stress test with random delays")
        @Timeout(value = 90, unit = TimeUnit.SECONDS)
        void testWithRandomDelays() throws Exception {
            List<UUID> accounts = new ArrayList<>();
            BigDecimal initialBalance = new BigDecimal("5000.00");
            for (int i = 0; i < 5; i++) {
                accounts.add(seedAccountWithBalance("Delay Account " + i, initialBalance));
            }

            BigDecimal initialTotal = initialBalance.multiply(BigDecimal.valueOf(5));

            int threadCount = 50;
            AtomicInteger successCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);

            java.util.Random random = new java.util.Random(777);

            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        // Random delay 0-100ms to simulate network latency
                        Thread.sleep(random.nextInt(100));

                        int fromIdx = random.nextInt(5);
                        int toIdx = (fromIdx + 1 + random.nextInt(4)) % 5;
                        BigDecimal amount = new BigDecimal(random.nextInt(200) + 1);

                        ResponseEntity<TransferResponse> response = executeTransfer(
                                accounts.get(fromIdx), accounts.get(toIdx), amount,
                                "DELAY-" + index + "-" + UUID.randomUUID());

                        if (response.getStatusCode() == HttpStatus.CREATED) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // May fail
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(90, TimeUnit.SECONDS);
            executor.shutdown();

            BigDecimal totalBalance = BigDecimal.ZERO;
            for (UUID accountId : accounts) {
                totalBalance = totalBalance.add(getBalance(accountId));
            }

            log.info("Random delay test: {} successful transfers, total balance={}", successCount.get(), totalBalance);

            assertThat(totalBalance).isEqualByComparingTo(initialTotal);
        }

        @Test
        @DisplayName("TASK-325: Verify transaction count matches")
        @Timeout(value = 60, unit = TimeUnit.SECONDS)
        void testTransactionCountMatches() throws Exception {
            UUID accountA = seedAccountWithBalance("Counter A", new BigDecimal("10000.00"));
            UUID accountB = seedAccountWithBalance("Counter B", new BigDecimal("10000.00"));

            int expectedTransfers = 20;
            AtomicInteger actualSuccessCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(expectedTransfers);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(expectedTransfers);

            for (int i = 0; i < expectedTransfers; i++) {
                final int index = i;
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        BigDecimal amount = new BigDecimal("10.00");
                        ResponseEntity<TransferResponse> response = executeTransfer(
                                accountA, accountB, amount,
                                "COUNT-" + index + "-" + UUID.randomUUID());

                        if (response.getStatusCode() == HttpStatus.CREATED) {
                            actualSuccessCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // May fail
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            doneLatch.await(60, TimeUnit.SECONDS);
            executor.shutdown();

            BigDecimal balanceA = getBalance(accountA);
            BigDecimal balanceB = getBalance(accountB);

            // Each successful transfer moves $10 from A to B
            BigDecimal expectedA = new BigDecimal("10000.00")
                    .subtract(new BigDecimal("10.00").multiply(BigDecimal.valueOf(actualSuccessCount.get())));
            BigDecimal expectedB = new BigDecimal("10000.00")
                    .add(new BigDecimal("10.00").multiply(BigDecimal.valueOf(actualSuccessCount.get())));

            log.info("Transaction count test: {} successful, A={} (expected {}), B={} (expected {})",
                    actualSuccessCount.get(), balanceA, expectedA, balanceB, expectedB);

            assertThat(balanceA).isEqualByComparingTo(expectedA);
            assertThat(balanceB).isEqualByComparingTo(expectedB);
            assertThat(actualSuccessCount.get()).isEqualTo(expectedTransfers);
        }
    }
}
