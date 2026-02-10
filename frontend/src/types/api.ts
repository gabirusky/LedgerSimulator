// ============================================================================
// TypeScript interfaces matching backend DTOs
// All financial amounts use `number` for display but string-based input
// for BigDecimal-safe form handling.
// ============================================================================

/** Matches backend AccountResponse.java */
export interface Account {
    id: string;
    document: string;
    name: string;
    balance: number;
    createdAt: string;
}

/** Matches backend CreateAccountRequest.java */
export interface CreateAccountRequest {
    document: string;
    name: string;
}

/** Matches backend TransferRequest.java */
export interface TransferRequest {
    sourceAccountId: string;
    targetAccountId: string;
    amount: number;
}

/** Matches backend TransferResponse.java */
export interface TransferResponse {
    transactionId: string;
    sourceAccountId: string;
    targetAccountId: string;
    amount: number;
    status: 'PENDING' | 'COMPLETED' | 'FAILED';
    createdAt: string;
}

/** Matches backend LedgerEntryResponse.java */
export interface LedgerEntry {
    id: string;
    transactionId: string;
    entryType: 'DEBIT' | 'CREDIT';
    amount: number;
    balanceAfter: number;
    createdAt: string;
}

/** Matches backend AccountStatementResponse.java */
export interface AccountStatement {
    accountId: string;
    accountName: string;
    currentBalance: number;
    entries: LedgerEntry[];
}

/** Spring Boot paginated response */
export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

/** Spring Boot Actuator health response */
export interface HealthStatus {
    status: 'UP' | 'DOWN' | 'UNKNOWN';
    components?: Record<string, { status: string; details?: Record<string, unknown> }>;
}

/** RFC 7807 Problem Details error from backend */
export interface ErrorResponse {
    type: string;
    title: string;
    status: number;
    detail: string;
    instance: string;
    timestamp: string;
    errors?: FieldError[];
}

/** Field-level validation error */
export interface FieldError {
    field: string;
    message: string;
}
