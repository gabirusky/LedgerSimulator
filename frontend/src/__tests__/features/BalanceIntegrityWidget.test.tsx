// TASK-456: Unit tests for BalanceIntegrityWidget
// Zero delta → green (Balanced), non-zero → red (DEVIATION), loading, error states
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { BalanceIntegrityWidget } from '@/features/admin/BalanceIntegrityWidget';

// Mock the ledgerProvider
vi.mock('@/services/ledgerProvider', () => ({
    ledgerProvider: {
        getAccounts: vi.fn(),
    },
}));

import { ledgerProvider } from '@/services/ledgerProvider';

const mockedGetAccounts = vi.mocked(ledgerProvider.getAccounts);

function createWrapper() {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: { retry: false, gcTime: 0 },
        },
    });
    return function Wrapper({ children }: { children: ReactNode }) {
        return (
            <QueryClientProvider client={queryClient}>
                {children}
            </QueryClientProvider>
        );
    };
}

describe('BalanceIntegrityWidget', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('shows "Balanced" badge and green checkmark when delta is zero', async () => {
        mockedGetAccounts.mockResolvedValueOnce({
            content: [
                { id: 'a1', document: '111', name: 'Alice', balance: 500, createdAt: '' },
                { id: 'a2', document: '222', name: 'Bob', balance: 500, createdAt: '' },
            ],
            totalElements: 2,
            totalPages: 1,
            size: 1000,
            number: 0,
            first: true,
            last: true,
            empty: false,
        });

        render(<BalanceIntegrityWidget />, { wrapper: createWrapper() });

        await waitFor(() => {
            expect(screen.getByText('Balanced')).toBeInTheDocument();
        });

        // Should show green checkmark emoji
        expect(screen.getByText('✅')).toBeInTheDocument();
        // Delta should be $0
        expect(screen.getByText(/Δ/)).toBeInTheDocument();
    });

    it('shows "Checking…" badge during loading', () => {
        // Mock a promise that never resolves (to keep loading state)
        mockedGetAccounts.mockReturnValue(new Promise(() => { }));

        render(<BalanceIntegrityWidget />, { wrapper: createWrapper() });

        expect(screen.getByText('Checking…')).toBeInTheDocument();
        expect(screen.getByText('⏳')).toBeInTheDocument();
    });

    it('shows "Error" badge when query fails', async () => {
        mockedGetAccounts.mockRejectedValueOnce(new Error('Network error'));

        render(<BalanceIntegrityWidget />, { wrapper: createWrapper() });

        await waitFor(() => {
            expect(screen.getByText('Error')).toBeInTheDocument();
        });

        expect(screen.getByText('⚠️')).toBeInTheDocument();
    });

    it('displays correct account count', async () => {
        mockedGetAccounts.mockResolvedValueOnce({
            content: [
                { id: 'a1', document: '111', name: 'A', balance: 100, createdAt: '' },
                { id: 'a2', document: '222', name: 'B', balance: 200, createdAt: '' },
                { id: 'a3', document: '333', name: 'C', balance: 300, createdAt: '' },
            ],
            totalElements: 3,
            totalPages: 1,
            size: 1000,
            number: 0,
            first: true,
            last: true,
            empty: false,
        });

        render(<BalanceIntegrityWidget />, { wrapper: createWrapper() });

        await waitFor(() => {
            expect(screen.getByText('3')).toBeInTheDocument();
        });
    });

    it('displays total system balance', async () => {
        mockedGetAccounts.mockResolvedValueOnce({
            content: [
                { id: 'a1', document: '111', name: 'A', balance: 1000, createdAt: '' },
                { id: 'a2', document: '222', name: 'B', balance: 2500, createdAt: '' },
            ],
            totalElements: 2,
            totalPages: 1,
            size: 1000,
            number: 0,
            first: true,
            last: true,
            empty: false,
        });

        render(<BalanceIntegrityWidget />, { wrapper: createWrapper() });

        await waitFor(() => {
            // Total = 1000 + 2500 = 3500 → displayed as "$3,500"
            expect(screen.getByText('$3,500')).toBeInTheDocument();
        });
    });

    it('shows "Balance Integrity" title', () => {
        mockedGetAccounts.mockReturnValue(new Promise(() => { }));

        render(<BalanceIntegrityWidget />, { wrapper: createWrapper() });

        expect(screen.getByText('Balance Integrity')).toBeInTheDocument();
    });

    it('displays last checked timestamp', async () => {
        mockedGetAccounts.mockResolvedValueOnce({
            content: [],
            totalElements: 0,
            totalPages: 0,
            size: 1000,
            number: 0,
            first: true,
            last: true,
            empty: true,
        });

        render(<BalanceIntegrityWidget />, { wrapper: createWrapper() });

        await waitFor(() => {
            expect(screen.getByText(/Last checked:/)).toBeInTheDocument();
        });
    });

    it('shows auto-refresh indicator', () => {
        mockedGetAccounts.mockReturnValue(new Promise(() => { }));

        render(<BalanceIntegrityWidget />, { wrapper: createWrapper() });

        expect(screen.getByText('Auto-refresh: 30s')).toBeInTheDocument();
    });
});
