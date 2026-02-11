// TASK-455: Unit tests for WalletCard optimistic UI
// Deduct on submit, rollback on error, pulse animation, loading state
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { WalletCard } from '@/features/user/WalletCard';

// Mock useBalance hook
vi.mock('@/hooks/useBalance', () => ({
    useBalance: vi.fn(),
}));

import { useBalance } from '@/hooks/useBalance';

const mockedUseBalance = vi.mocked(useBalance);

function createWrapper() {
    const queryClient = new QueryClient({
        defaultOptions: { queries: { retry: false } },
    });
    return function Wrapper({ children }: { children: ReactNode }) {
        return (
            <QueryClientProvider client={queryClient}>
                {children}
            </QueryClientProvider>
        );
    };
}

describe('WalletCard', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('displays formatted balance when loaded', () => {
        mockedUseBalance.mockReturnValue({
            data: 2500.75,
            isLoading: false,
            isError: false,
            dataUpdatedAt: Date.now(),
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" />,
            { wrapper: createWrapper() }
        );

        // Balance should be split into whole and decimal parts
        expect(screen.getByText('$2,500')).toBeInTheDocument();
        expect(screen.getByText('.75')).toBeInTheDocument();
    });

    it('shows loading skeleton when data is loading', () => {
        mockedUseBalance.mockReturnValue({
            data: undefined,
            isLoading: true,
            isError: false,
            dataUpdatedAt: 0,
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" />,
            { wrapper: createWrapper() }
        );

        // Should show "Available Balance" label
        expect(screen.getByText('Available Balance')).toBeInTheDocument();
        // Should NOT show a balance number — skeleton is rendered instead
        expect(screen.queryByText('$0')).not.toBeInTheDocument();
        expect(screen.queryByText('$1,000')).not.toBeInTheDocument();
    });

    it('optimistically deducts pending amount from displayed balance', () => {
        mockedUseBalance.mockReturnValue({
            data: 1000,
            isLoading: false,
            isError: false,
            dataUpdatedAt: Date.now(),
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" pendingDeduction={250} />,
            { wrapper: createWrapper() }
        );

        // Balance should show 1000 - 250 = 750
        expect(screen.getByText('$750')).toBeInTheDocument();
        expect(screen.getByText('.00')).toBeInTheDocument();
    });

    it('shows rollback balance (no deduction) when pendingDeduction is 0', () => {
        mockedUseBalance.mockReturnValue({
            data: 1000,
            isLoading: false,
            isError: false,
            dataUpdatedAt: Date.now(),
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" pendingDeduction={0} />,
            { wrapper: createWrapper() }
        );

        // Full balance shown
        expect(screen.getByText('$1,000')).toBeInTheDocument();
        expect(screen.getByText('.00')).toBeInTheDocument();
    });

    it('clamps optimistic balance to zero (never shows negative)', () => {
        mockedUseBalance.mockReturnValue({
            data: 100,
            isLoading: false,
            isError: false,
            dataUpdatedAt: Date.now(),
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" pendingDeduction={999} />,
            { wrapper: createWrapper() }
        );

        // Math.max(0, 100 - 999) = 0
        expect(screen.getByText('$0')).toBeInTheDocument();
    });

    it('shows pending deduction indicator during transfer', () => {
        mockedUseBalance.mockReturnValue({
            data: 1000,
            isLoading: false,
            isError: false,
            dataUpdatedAt: Date.now(),
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" pendingDeduction={300} isTransferring />,
            { wrapper: createWrapper() }
        );

        expect(screen.getByText(/Pending/)).toBeInTheDocument();
        expect(screen.getByText(/\$300\.00/)).toBeInTheDocument();
    });

    it('shows account name when provided', () => {
        mockedUseBalance.mockReturnValue({
            data: 500,
            isLoading: false,
            isError: false,
            dataUpdatedAt: Date.now(),
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" accountName="Alice Wonderland" />,
            { wrapper: createWrapper() }
        );

        expect(screen.getByText('Alice Wonderland')).toBeInTheDocument();
    });

    it('shows "Live" badge when connected and not in error', () => {
        mockedUseBalance.mockReturnValue({
            data: 100,
            isLoading: false,
            isError: false,
            dataUpdatedAt: Date.now(),
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" />,
            { wrapper: createWrapper() }
        );

        expect(screen.getByText('Live')).toBeInTheDocument();
    });

    it('shows "Offline" badge when in error state', () => {
        mockedUseBalance.mockReturnValue({
            data: undefined,
            isLoading: false,
            isError: true,
            dataUpdatedAt: 0,
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" />,
            { wrapper: createWrapper() }
        );

        expect(screen.getByText('Offline')).toBeInTheDocument();
    });

    it('shows last updated timestamp', () => {
        const now = Date.now();
        mockedUseBalance.mockReturnValue({
            data: 500,
            isLoading: false,
            isError: false,
            dataUpdatedAt: now,
        } as ReturnType<typeof useBalance>);

        render(
            <WalletCard accountId="acc-1" />,
            { wrapper: createWrapper() }
        );

        expect(screen.getByText(/Updated/)).toBeInTheDocument();
    });
});
