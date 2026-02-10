// TASK-392: useQuery for single account by ID
import { useQuery } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import type { Account } from '@/types/api';
import { accountKeys } from './useAccounts';

export function useAccount(id: string | undefined) {
    return useQuery<Account>({
        queryKey: accountKeys.detail(id!),
        queryFn: () => ledgerProvider.getAccount(id!),
        enabled: !!id,
    });
}
