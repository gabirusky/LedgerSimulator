// TASK-452: Unit tests for ledgerProvider.ts
// Mock fetch, verify URL construction, error handling
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';

// We need to mock import.meta.env before importing the module
vi.stubEnv('VITE_API_URL', 'http://test-api:8080/api/v1');

// Import after env is stubbed
const { ledgerProvider, ApiError } = await import('@/services/ledgerProvider');

describe('ledgerProvider', () => {
    const mockFetch = vi.fn();

    beforeEach(() => {
        vi.stubGlobal('fetch', mockFetch);
        mockFetch.mockReset();
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    // ── Helper: create a successful Response mock ──
    function okResponse<T>(data: T, status = 200): Response {
        return {
            ok: true,
            status,
            statusText: 'OK',
            json: () => Promise.resolve(data),
        } as Response;
    }

    // ── Helper: create an error Response mock ──
    function errorResponse(
        status: number,
        body: Record<string, unknown>
    ): Response {
        return {
            ok: false,
            status,
            statusText: 'Error',
            json: () => Promise.resolve(body),
        } as Response;
    }

    // ── Helper: create an error Response that fails to parse JSON ──
    function brokenErrorResponse(status: number): Response {
        return {
            ok: false,
            status,
            statusText: 'Internal Server Error',
            json: () => Promise.reject(new Error('not JSON')),
        } as Response;
    }

    // ========================================================================
    // getAccounts
    // ========================================================================
    describe('getAccounts', () => {
        it('constructs correct URL with default pagination', async () => {
            const mockPage = {
                content: [],
                totalElements: 0,
                totalPages: 0,
                size: 20,
                number: 0,
                first: true,
                last: true,
                empty: true,
            };
            mockFetch.mockResolvedValueOnce(okResponse(mockPage));

            await ledgerProvider.getAccounts();

            expect(mockFetch).toHaveBeenCalledWith(
                'http://test-api:8080/api/v1/accounts?page=0&size=20'
            );
        });

        it('constructs correct URL with custom pagination', async () => {
            const mockPage = {
                content: [],
                totalElements: 50,
                totalPages: 5,
                size: 10,
                number: 2,
                first: false,
                last: false,
                empty: false,
            };
            mockFetch.mockResolvedValueOnce(okResponse(mockPage));

            const result = await ledgerProvider.getAccounts(2, 10);

            expect(mockFetch).toHaveBeenCalledWith(
                'http://test-api:8080/api/v1/accounts?page=2&size=10'
            );
            expect(result.totalElements).toBe(50);
        });

        it('returns parsed account data', async () => {
            const mockPage = {
                content: [
                    {
                        id: 'uuid-1',
                        document: '123.456.789-00',
                        name: 'Alice',
                        balance: 1000.5,
                        createdAt: '2026-01-01T00:00:00Z',
                    },
                ],
                totalElements: 1,
                totalPages: 1,
                size: 20,
                number: 0,
                first: true,
                last: true,
                empty: false,
            };
            mockFetch.mockResolvedValueOnce(okResponse(mockPage));

            const result = await ledgerProvider.getAccounts();

            expect(result.content).toHaveLength(1);
            expect(result.content[0].name).toBe('Alice');
            expect(result.content[0].balance).toBe(1000.5);
        });
    });

    // ========================================================================
    // getAccount
    // ========================================================================
    describe('getAccount', () => {
        it('constructs correct URL with account ID', async () => {
            const mockAccount = {
                id: 'uuid-1',
                document: '123.456.789-00',
                name: 'Alice',
                balance: 500,
                createdAt: '2026-01-01T00:00:00Z',
            };
            mockFetch.mockResolvedValueOnce(okResponse(mockAccount));

            await ledgerProvider.getAccount('uuid-1');

            expect(mockFetch).toHaveBeenCalledWith(
                'http://test-api:8080/api/v1/accounts/uuid-1'
            );
        });

        it('throws ApiError for 404 response', async () => {
            const errorBody = {
                type: '/errors/account-not-found',
                title: 'Account Not Found',
                status: 404,
                detail: 'Account with ID uuid-missing not found',
                instance: '/api/v1/accounts/uuid-missing',
                timestamp: '2026-01-01T00:00:00Z',
            };
            mockFetch.mockResolvedValueOnce(errorResponse(404, errorBody));

            await expect(ledgerProvider.getAccount('uuid-missing')).rejects.toThrow(ApiError);

            try {
                mockFetch.mockResolvedValueOnce(errorResponse(404, errorBody));
                await ledgerProvider.getAccount('uuid-missing');
            } catch (e) {
                expect(e).toBeInstanceOf(ApiError);
                const apiErr = e as InstanceType<typeof ApiError>;
                expect(apiErr.status).toBe(404);
                expect(apiErr.detail).toBe('Account with ID uuid-missing not found');
            }
        });
    });

    // ========================================================================
    // createAccount
    // ========================================================================
    describe('createAccount', () => {
        it('sends POST with correct headers and body', async () => {
            const newAccount = {
                id: 'uuid-new',
                document: '999.888.777-66',
                name: 'Bob',
                balance: 0,
                createdAt: '2026-02-01T00:00:00Z',
            };
            mockFetch.mockResolvedValueOnce(okResponse(newAccount));

            await ledgerProvider.createAccount({
                document: '999.888.777-66',
                name: 'Bob',
            });

            expect(mockFetch).toHaveBeenCalledWith(
                'http://test-api:8080/api/v1/accounts',
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        document: '999.888.777-66',
                        name: 'Bob',
                    }),
                }
            );
        });

        it('throws ApiError for 409 duplicate', async () => {
            const errorBody = {
                type: '/errors/duplicate-document',
                title: 'Duplicate Document',
                status: 409,
                detail: 'Account with document already exists',
                instance: '/api/v1/accounts',
                timestamp: '2026-01-01T00:00:00Z',
            };
            mockFetch.mockResolvedValueOnce(errorResponse(409, errorBody));

            await expect(
                ledgerProvider.createAccount({ document: 'dup', name: 'Dup' })
            ).rejects.toThrow(ApiError);
        });
    });

    // ========================================================================
    // executeTransfer
    // ========================================================================
    describe('executeTransfer', () => {
        it('sends POST with Idempotency-Key header', async () => {
            const mockTransfer = {
                transactionId: 'tx-1',
                sourceAccountId: 'src-uuid',
                targetAccountId: 'tgt-uuid',
                amount: 250.0,
                status: 'COMPLETED',
                createdAt: '2026-02-01T00:00:00Z',
            };
            mockFetch.mockResolvedValueOnce(okResponse(mockTransfer));

            await ledgerProvider.executeTransfer(
                {
                    sourceAccountId: 'src-uuid',
                    targetAccountId: 'tgt-uuid',
                    amount: 250.0,
                },
                'idem-key-123'
            );

            expect(mockFetch).toHaveBeenCalledWith(
                'http://test-api:8080/api/v1/transfers',
                {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Idempotency-Key': 'idem-key-123',
                    },
                    body: JSON.stringify({
                        sourceAccountId: 'src-uuid',
                        targetAccountId: 'tgt-uuid',
                        amount: 250.0,
                    }),
                }
            );
        });

        it('throws ApiError for 422 insufficient funds', async () => {
            const errorBody = {
                type: '/errors/insufficient-funds',
                title: 'Insufficient Funds',
                status: 422,
                detail: 'Account has insufficient balance',
                instance: '/api/v1/transfers',
                timestamp: '2026-01-01T00:00:00Z',
            };
            mockFetch.mockResolvedValueOnce(errorResponse(422, errorBody));

            await expect(
                ledgerProvider.executeTransfer(
                    { sourceAccountId: 'a', targetAccountId: 'b', amount: 99999 },
                    'key-x'
                )
            ).rejects.toThrow(ApiError);
        });
    });

    // ========================================================================
    // getLedger
    // ========================================================================
    describe('getLedger', () => {
        it('constructs correct URL with account ID and pagination', async () => {
            const mockStatement = {
                accountId: 'acc-1',
                accountName: 'Alice',
                currentBalance: 1000,
                entries: [],
            };
            mockFetch.mockResolvedValueOnce(okResponse(mockStatement));

            await ledgerProvider.getLedger('acc-1', 1, 25);

            expect(mockFetch).toHaveBeenCalledWith(
                'http://test-api:8080/api/v1/ledger/acc-1?page=1&size=25'
            );
        });

        it('uses default pagination when no args provided', async () => {
            const mockStatement = {
                accountId: 'acc-2',
                accountName: 'Bob',
                currentBalance: 0,
                entries: [],
            };
            mockFetch.mockResolvedValueOnce(okResponse(mockStatement));

            await ledgerProvider.getLedger('acc-2');

            expect(mockFetch).toHaveBeenCalledWith(
                'http://test-api:8080/api/v1/ledger/acc-2?page=0&size=50'
            );
        });
    });

    // ========================================================================
    // getHealth
    // ========================================================================
    describe('getHealth', () => {
        it('constructs correct actuator health URL', async () => {
            const mockHealth = { status: 'UP' as const };
            mockFetch.mockResolvedValueOnce(okResponse(mockHealth));

            await ledgerProvider.getHealth();

            expect(mockFetch).toHaveBeenCalledWith(
                'http://test-api:8080/actuator/health'
            );
        });
    });

    // ========================================================================
    // Error Handling (handleResponse)
    // ========================================================================
    describe('error handling', () => {
        it('creates fallback ApiError when response body is not JSON', async () => {
            mockFetch.mockResolvedValueOnce(brokenErrorResponse(500));

            try {
                await ledgerProvider.getAccount('any');
            } catch (e) {
                expect(e).toBeInstanceOf(ApiError);
                const apiErr = e as InstanceType<typeof ApiError>;
                expect(apiErr.status).toBe(500);
                expect(apiErr.message).toBe('Request Failed');
            }
        });

        it('preserves RFC 7807 fields in ApiError', async () => {
            const errorBody = {
                type: '/errors/test',
                title: 'Test Error',
                status: 418,
                detail: 'I am a teapot',
                instance: '/api/v1/test',
                timestamp: '2026-01-01T00:00:00Z',
            };
            mockFetch.mockResolvedValueOnce(errorResponse(418, errorBody));

            try {
                await ledgerProvider.getAccount('any');
            } catch (e) {
                expect(e).toBeInstanceOf(ApiError);
                const apiErr = e as InstanceType<typeof ApiError>;
                expect(apiErr.type).toBe('/errors/test');
                expect(apiErr.detail).toBe('I am a teapot');
                expect(apiErr.message).toBe('Test Error');
            }
        });
    });
});
