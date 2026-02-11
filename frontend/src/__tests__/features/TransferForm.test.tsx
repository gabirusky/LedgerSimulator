// TASK-454: Unit tests for TransferForm validation
// source ≠ target, amount > 0, amount ≤ balance, disabled during mutation
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { TransferForm } from '@/features/user/TransferForm';

// Mock hooks used by TransferForm
vi.mock('@/hooks/useAccounts', () => ({
    useAccounts: vi.fn(() => ({
        data: {
            content: [
                { id: 'acc-1', document: '111', name: 'Alice', balance: 1000, createdAt: '' },
                { id: 'acc-2', document: '222', name: 'Bob', balance: 500, createdAt: '' },
                { id: 'acc-3', document: '333', name: 'Charlie', balance: 300, createdAt: '' },
            ],
        },
        isLoading: false,
    })),
    accountKeys: {
        all: ['accounts'] as const,
        lists: () => ['accounts', 'list'] as const,
        list: (page: number, size: number) => ['accounts', 'list', { page, size }] as const,
        details: () => ['accounts', 'detail'] as const,
        detail: (id: string) => ['accounts', 'detail', id] as const,
    },
}));

const mockMutate = vi.fn();
vi.mock('@/hooks/useTransfers', () => ({
    useTransfers: vi.fn(() => ({
        mutate: mockMutate,
        isPending: false,
    })),
}));

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

describe('TransferForm', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders the form with send money header', () => {
        render(
            <TransferForm sourceAccountId="acc-1" sourceBalance={1000} />,
            { wrapper: createWrapper() }
        );

        // "Send Money" appears in card title and on submit button
        expect(screen.getAllByText(/Send Money/)).toHaveLength(2);
        expect(screen.getByText('Select recipient…')).toBeInTheDocument();
    });

    it('shows available balance', () => {
        render(
            <TransferForm sourceAccountId="acc-1" sourceBalance={1250.50} />,
            { wrapper: createWrapper() }
        );

        expect(screen.getByText(/Available:/)).toBeInTheDocument();
    });

    it('filters out source account from recipient list', async () => {
        render(
            <TransferForm sourceAccountId="acc-1" sourceBalance={1000} />,
            { wrapper: createWrapper() }
        );

        // Open account search
        await userEvent.click(screen.getByText('Select recipient…'));

        // Alice (acc-1) should NOT be listed since she is the source
        expect(screen.queryByText('Alice')).not.toBeInTheDocument();
        // Bob and Charlie should be available
        expect(screen.getByText('Bob')).toBeInTheDocument();
        expect(screen.getByText('Charlie')).toBeInTheDocument();
    });

    it('shows validation error for amount exceeding balance', async () => {
        render(
            <TransferForm sourceAccountId="acc-1" sourceBalance={100} />,
            { wrapper: createWrapper() }
        );

        // Type an amount exceeding balance
        const amountInput = screen.getByPlaceholderText('0.00');
        await userEvent.type(amountInput, '200');

        expect(screen.getByText('Insufficient funds')).toBeInTheDocument();
    });

    it('shows validation error for zero amount', async () => {
        render(
            <TransferForm sourceAccountId="acc-1" sourceBalance={100} />,
            { wrapper: createWrapper() }
        );

        const amountInput = screen.getByPlaceholderText('0.00');
        await userEvent.type(amountInput, '0');

        expect(screen.getByText('Amount must be greater than 0')).toBeInTheDocument();
    });

    it('submit button is disabled when form is invalid (no recipient)', () => {
        render(
            <TransferForm sourceAccountId="acc-1" sourceBalance={100} />,
            { wrapper: createWrapper() }
        );

        // Find the submit button by type attribute (avoid matching "Send Max" which also contains "send")
        const submitButtons = screen.getAllByRole('button').filter(
            (btn) => btn.getAttribute('type') === 'submit'
        );
        expect(submitButtons[0]).toBeDisabled();
    });

    it('submit button is disabled when sourceAccountId is missing', () => {
        render(
            <TransferForm sourceAccountId={undefined} sourceBalance={0} />,
            { wrapper: createWrapper() }
        );

        const submitButtons = screen.getAllByRole('button').filter(
            (btn) => btn.getAttribute('type') === 'submit'
        );
        expect(submitButtons[0]).toBeDisabled();
    });

    it('calls mutate with correct data on valid submission', async () => {
        render(
            <TransferForm sourceAccountId="acc-1" sourceBalance={1000} />,
            { wrapper: createWrapper() }
        );

        // 1. Select recipient
        await userEvent.click(screen.getByText('Select recipient…'));
        await userEvent.click(screen.getByText('Bob'));

        // 2. Enter amount
        const amountInput = screen.getByPlaceholderText('0.00');
        await userEvent.type(amountInput, '50');

        // 3. Submit (find the submit button by type attribute)
        const submitButtons = screen.getAllByRole('button').filter(
            (btn) => btn.getAttribute('type') === 'submit'
        );
        expect(submitButtons[0]).toBeEnabled();
        await userEvent.click(submitButtons[0]);

        expect(mockMutate).toHaveBeenCalledTimes(1);
        const callArgs = mockMutate.mock.calls[0];
        expect(callArgs[0].data.sourceAccountId).toBe('acc-1');
        expect(callArgs[0].data.targetAccountId).toBe('acc-2');
        expect(callArgs[0].data.amount).toBe(50);
        // Idempotency key should be a UUID
        expect(callArgs[0].idempotencyKey).toMatch(
            /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i
        );
    });

    it('notifies parent via onTransferStart callback', async () => {
        const onTransferStart = vi.fn();

        render(
            <TransferForm
                sourceAccountId="acc-1"
                sourceBalance={1000}
                onTransferStart={onTransferStart}
            />,
            { wrapper: createWrapper() }
        );

        // Select recipient and enter amount
        await userEvent.click(screen.getByText('Select recipient…'));
        await userEvent.click(screen.getByText('Bob'));
        const amountInput = screen.getByPlaceholderText('0.00');
        await userEvent.type(amountInput, '75');

        // Submit
        const submitButtons = screen.getAllByRole('button').filter(
            (btn) => btn.getAttribute('type') === 'submit'
        );
        await userEvent.click(submitButtons[0]);

        expect(onTransferStart).toHaveBeenCalledWith(75);
    });

    it('shows "Send Max" button that fills balance', async () => {
        render(
            <TransferForm sourceAccountId="acc-1" sourceBalance={1234.56} />,
            { wrapper: createWrapper() }
        );

        await userEvent.click(screen.getByText('Send Max'));

        const amountInput = screen.getByPlaceholderText('0.00') as HTMLInputElement;
        expect(amountInput.value).toBe('1234.56');
    });
});
