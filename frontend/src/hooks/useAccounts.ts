// TASK-391: useQuery for paginated account list
import { useQuery } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import type { Account, Page } from '@/types/api';

export const accountKeys = {
    all: ['accounts'] as const,
    lists: () => [...accountKeys.all, 'list'] as const,
    list: (page: number, size: number) => [...accountKeys.lists(), { page, size }] as const,
    details: () => [...accountKeys.all, 'detail'] as const,
    detail: (id: string) => [...accountKeys.details(), id] as const,
};

export function useAccounts(page = 0, size = 20) {
    return useQuery<Page<Account>>({
        queryKey: accountKeys.list(page, size),
        queryFn: () => ledgerProvider.getAccounts(page, size),
    });
}
