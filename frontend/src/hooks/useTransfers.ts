// TASK-394: useMutation for transfer execution with idempotency
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import type { TransferRequest, TransferResponse } from '@/types/api';
import { accountKeys } from './useAccounts';

interface TransferVariables {
    data: TransferRequest;
    idempotencyKey?: string;
}

export function useTransfers() {
    const queryClient = useQueryClient();

    return useMutation<TransferResponse, Error, TransferVariables>({
        mutationFn: ({ data, idempotencyKey }) => {
            const key = idempotencyKey ?? crypto.randomUUID();
            return ledgerProvider.executeTransfer(data, key);
        },
        onSuccess: () => {
            // Invalidate accounts (balances changed) and ledger entries
            queryClient.invalidateQueries({ queryKey: accountKeys.all });
            queryClient.invalidateQueries({ queryKey: ['ledger'] });
        },
    });
}
