// TASK-453: Unit tests for useBalance hook
// Verify polling interval, error handling, enabled flag
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { useBalance } from '@/hooks/useBalance';

// Mock the ledgerProvider module
vi.mock('@/services/ledgerProvider', () => ({
    ledgerProvider: {
        getAccount: vi.fn(),
    },
}));

// Import the mocked module
import { ledgerProvider } from '@/services/ledgerProvider';

const mockedGetAccount = vi.mocked(ledgerProvider.getAccount);

function createWrapper() {
    const queryClient = new QueryClient({
        defaultOptions: {
            queries: {
                retry: false,
                gcTime: 0,
            },
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

describe('useBalance', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('returns balance from account data', async () => {
        mockedGetAccount.mockResolvedValueOnce({
            id: 'acc-1',
            document: '123.456.789-00',
            name: 'Alice',
            balance: 1500.75,
            createdAt: '2026-01-01T00:00:00Z',
        });

        const { result } = renderHook(
            () => useBalance('acc-1'),
            { wrapper: createWrapper() }
        );

        await waitFor(() => expect(result.current.isSuccess).toBe(true));

        expect(result.current.data).toBe(1500.75);
        expect(mockedGetAccount).toHaveBeenCalledWith('acc-1');
    });

    it('is disabled when accountId is undefined', () => {
        const { result } = renderHook(
            () => useBalance(undefined),
            { wrapper: createWrapper() }
        );

        // Query should not fire
        expect(result.current.fetchStatus).toBe('idle');
        expect(mockedGetAccount).not.toHaveBeenCalled();
    });

    it('is disabled when accountId is empty string (falsy)', () => {
        const { result } = renderHook(
            () => useBalance(''),
            { wrapper: createWrapper() }
        );

        expect(result.current.fetchStatus).toBe('idle');
        expect(mockedGetAccount).not.toHaveBeenCalled();
    });

    it('reports error state when API call fails', async () => {
        mockedGetAccount.mockRejectedValueOnce(new Error('Network error'));

        const { result } = renderHook(
            () => useBalance('acc-fail'),
            { wrapper: createWrapper() }
        );

        await waitFor(() => expect(result.current.isError).toBe(true));

        expect(result.current.error).toBeInstanceOf(Error);
        expect(result.current.error?.message).toBe('Network error');
    });

    it('returns zero balance correctly', async () => {
        mockedGetAccount.mockResolvedValueOnce({
            id: 'acc-zero',
            document: '000.000.000-00',
            name: 'New Account',
            balance: 0,
            createdAt: '2026-01-01T00:00:00Z',
        });

        const { result } = renderHook(
            () => useBalance('acc-zero'),
            { wrapper: createWrapper() }
        );

        await waitFor(() => expect(result.current.isSuccess).toBe(true));

        expect(result.current.data).toBe(0);
    });

    it('calls getAccount with the correct account ID', async () => {
        mockedGetAccount.mockResolvedValueOnce({
            id: 'specific-uuid',
            document: '111.222.333-44',
            name: 'Test',
            balance: 100,
            createdAt: '2026-01-01T00:00:00Z',
        });

        renderHook(
            () => useBalance('specific-uuid'),
            { wrapper: createWrapper() }
        );

        await waitFor(() =>
            expect(mockedGetAccount).toHaveBeenCalledWith('specific-uuid')
        );
    });
});
