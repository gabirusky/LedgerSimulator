// TASK-421: useQuery with refetchInterval: 5000 for real-time balance polling
import { useQuery } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import type { Account } from '@/types/api';
import { accountKeys } from './useAccounts';

export function useBalance(accountId: string | undefined) {
    return useQuery<number>({
        queryKey: [...accountKeys.detail(accountId!), 'balance'],
        queryFn: async () => {
            const account: Account = await ledgerProvider.getAccount(accountId!);
            return account.balance;
        },
        enabled: !!accountId,
        refetchInterval: 5_000, // Poll every 5 seconds for real-time balance
        staleTime: 2_000,       // Consider data stale after 2s for faster refresh
    });
}
