// TASK-393: useMutation for account creation
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import type { Account, CreateAccountRequest } from '@/types/api';
import { accountKeys } from './useAccounts';

export function useCreateAccount() {
    const queryClient = useQueryClient();

    return useMutation<Account, Error, CreateAccountRequest>({
        mutationFn: (data) => ledgerProvider.createAccount(data),
        onSuccess: () => {
            // Invalidate all account list queries so they refetch
            queryClient.invalidateQueries({ queryKey: accountKeys.lists() });
        },
    });
}
