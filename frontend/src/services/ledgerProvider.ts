// ============================================================================
// API Service Layer — all backend communication flows through this module.
// Never call fetch() directly in components; always use ledgerProvider.
// ============================================================================

import type {
    Account,
    AccountStatement,
    CreateAccountRequest,
    ErrorResponse,
    HealthStatus,
    Page,
    TransferRequest,
    TransferResponse,
} from '@/types/api';

/** TASK-387: VITE_API_URL with fallback to local Spring Boot */
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

/** Custom error class wrapping backend RFC 7807 Problem Details */
export class ApiError extends Error {
    status: number;
    detail: string;
    type: string;

    constructor(response: ErrorResponse) {
        super(response.title);
        this.name = 'ApiError';
        this.status = response.status;
        this.detail = response.detail;
        this.type = response.type;
    }
}

/**
 * Shared response handler — parses JSON and throws ApiError for non-OK statuses.
 */
async function handleResponse<T>(response: Response): Promise<T> {
    if (!response.ok) {
        let errorBody: ErrorResponse;
        try {
            errorBody = await response.json();
        } catch {
            throw new ApiError({
                type: '/errors/unknown',
                title: 'Request Failed',
                status: response.status,
                detail: response.statusText,
                instance: '',
                timestamp: new Date().toISOString(),
            });
        }
        throw new ApiError(errorBody);
    }
    return response.json() as Promise<T>;
}

// ============================================================================
// Public API
// ============================================================================

export const ledgerProvider = {
    // ── Accounts ────────────────────────────────────────────────────────────

    /** TASK-383: Fetch paginated account list */
    getAccounts: (page = 0, size = 20): Promise<Page<Account>> =>
        fetch(`${API_URL}/accounts?page=${page}&size=${size}`)
            .then((r) => handleResponse<Page<Account>>(r)),

    /** TASK-383: Fetch single account by UUID */
    getAccount: (id: string): Promise<Account> =>
        fetch(`${API_URL}/accounts/${id}`)
            .then((r) => handleResponse<Account>(r)),

    /** TASK-383: Create a new account */
    createAccount: (data: CreateAccountRequest): Promise<Account> =>
        fetch(`${API_URL}/accounts`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data),
        }).then((r) => handleResponse<Account>(r)),

    // ── Transfers ───────────────────────────────────────────────────────────

    /** TASK-384: Execute transfer with Idempotency-Key header support */
    executeTransfer: (data: TransferRequest, idempotencyKey: string): Promise<TransferResponse> =>
        fetch(`${API_URL}/transfers`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Idempotency-Key': idempotencyKey,
            },
            body: JSON.stringify(data),
        }).then((r) => handleResponse<TransferResponse>(r)),

    // ── Ledger ──────────────────────────────────────────────────────────────

    /** TASK-385: Fetch paginated ledger entries for an account */
    getLedger: (accountId: string, page = 0, size = 50): Promise<AccountStatement> =>
        fetch(`${API_URL}/ledger/${accountId}?page=${page}&size=${size}`)
            .then((r) => handleResponse<AccountStatement>(r)),

    // ── Health ──────────────────────────────────────────────────────────────

    /** TASK-386: Query Spring Boot Actuator health endpoint */
    getHealth: (): Promise<HealthStatus> =>
        fetch(`${API_URL.replace('/api/v1', '')}/actuator/health`)
            .then((r) => handleResponse<HealthStatus>(r)),
};
