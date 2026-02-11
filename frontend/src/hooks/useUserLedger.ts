// TASK-422: useInfiniteQuery for paginated user-facing ledger entries
import { useInfiniteQuery } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import type { AccountStatement } from '@/types/api';

export function useUserLedger(accountId: string | undefined, pageSize = 20) {
    return useInfiniteQuery<AccountStatement, Error>({
        queryKey: ['userLedger', accountId, pageSize],
        queryFn: ({ pageParam }) =>
            ledgerProvider.getLedger(accountId!, pageParam as number, pageSize),
        initialPageParam: 0,
        getNextPageParam: (lastPage, allPages) => {
            // If we got fewer entries than the page size, no more pages
            if (lastPage.entries.length < pageSize) return undefined;
            return allPages.length;
        },
        enabled: !!accountId,
    });
}
