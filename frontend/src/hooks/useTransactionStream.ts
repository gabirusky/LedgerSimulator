// TASK-395: useInfiniteQuery for paginated ledger entries
import { useInfiniteQuery } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import type { AccountStatement } from '@/types/api';

export function useTransactionStream(accountId: string | undefined, pageSize = 50) {
    return useInfiniteQuery<AccountStatement, Error>({
        queryKey: ['ledger', accountId, pageSize],
        queryFn: ({ pageParam }) =>
            ledgerProvider.getLedger(accountId!, pageParam as number, pageSize),
        initialPageParam: 0,
        getNextPageParam: (lastPage, allPages) => {
            // If we got fewer entries than the page size, there are no more pages
            if (lastPage.entries.length < pageSize) return undefined;
            return allPages.length;
        },
        enabled: !!accountId,
    });
}
