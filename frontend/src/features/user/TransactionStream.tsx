// TASK-436 to TASK-441: Transaction Stream component
// Vertical timeline, color-coded DEBIT/CREDIT, metadata parsing,
// infinite scroll with ScrollArea + intersection observer,
// transaction detail expansion, loading skeleton
import { useState, useRef, useEffect, useCallback } from 'react';
import { useUserLedger } from '@/hooks/useUserLedger';
import { useAccounts } from '@/hooks/useAccounts';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import type { LedgerEntry, Account } from '@/types/api';

interface TransactionStreamProps {
    accountId: string | undefined;
}

// TASK-438: Parse metadata to show human-readable descriptions
function getTransactionDescription(entry: LedgerEntry, accounts: Account[]): string {
    const findAccount = (id: string) => accounts.find((a) => a.id === id);

    if (entry.entryType === 'CREDIT') {
        // Try to find sender from transaction metadata
        const sender = findAccount(entry.transactionId);
        return sender ? `Payment from ${sender.name}` : 'Incoming transfer';
    } else {
        const recipient = findAccount(entry.transactionId);
        return recipient ? `Payment to ${recipient.name}` : 'Outgoing transfer';
    }
}

function formatRelativeTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHr = Math.floor(diffMin / 60);
    const diffDay = Math.floor(diffHr / 24);

    if (diffSec < 60) return 'Just now';
    if (diffMin < 60) return `${diffMin}m ago`;
    if (diffHr < 24) return `${diffHr}h ago`;
    if (diffDay < 7) return `${diffDay}d ago`;
    return date.toLocaleDateString([], { month: 'short', day: 'numeric' });
}

// TASK-440: Expandable transaction detail
function TransactionItem({
    entry,
    accounts,
    isNew,
}: {
    entry: LedgerEntry;
    accounts: Account[];
    isNew?: boolean;
}) {
    const [expanded, setExpanded] = useState(false);
    const isCredit = entry.entryType === 'CREDIT';

    return (
        <button
            type="button"
            onClick={() => setExpanded(!expanded)}
            className={`
                w-full text-left group relative
                flex flex-col rounded-xl px-4 py-3.5
                transition-all duration-200 ease-out
                hover:bg-accent/30
                ${isNew ? 'animate-in slide-in-from-top-2 fade-in duration-500' : ''}
            `}
        >
            {/* TASK-437: Color-coded left border */}
            <div className={`
                absolute left-0 top-3 bottom-3 w-[2.5px] rounded-full
                ${isCredit
                    ? 'bg-emerald-400'
                    : 'bg-rose-400'
                }
            `} />

            <div className="flex items-center justify-between gap-3">
                <div className="flex items-center gap-3 min-w-0 flex-1">
                    {/* Icon */}
                    <div className={`
                        w-9 h-9 rounded-full shrink-0 flex items-center justify-center text-sm
                        ${isCredit
                            ? 'bg-emerald-500/10 text-emerald-400'
                            : 'bg-rose-500/10 text-rose-400'
                        }
                    `}>
                        {isCredit ? '↓' : '↑'}
                    </div>

                    <div className="min-w-0 flex-1">
                        {/* TASK-438: Human-readable description */}
                        <p className="text-sm font-medium truncate">
                            {getTransactionDescription(entry, accounts)}
                        </p>
                        <p className="text-[10px] text-muted-foreground mt-0.5">
                            {formatRelativeTime(entry.createdAt)}
                        </p>
                    </div>
                </div>

                {/* Amount */}
                <div className="text-right shrink-0">
                    <p className={`
                        text-sm font-semibold tabular-nums
                        ${isCredit ? 'text-emerald-400' : 'text-foreground'}
                    `}>
                        {isCredit ? '+' : '−'}
                        {new Intl.NumberFormat('en-US', {
                            style: 'currency',
                            currency: 'USD',
                        }).format(entry.amount)}
                    </p>
                    <p className="text-[10px] text-muted-foreground tabular-nums mt-0.5">
                        bal. {new Intl.NumberFormat('en-US', {
                            style: 'currency',
                            currency: 'USD',
                        }).format(entry.balanceAfter)}
                    </p>
                </div>
            </div>

            {/* TASK-440: Expanded details */}
            {expanded && (
                <div className="mt-3 pt-3 border-t border-border/30 space-y-2 animate-in slide-in-from-top-1 fade-in duration-200">
                    <div className="grid grid-cols-2 gap-2">
                        <div>
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Transaction ID</p>
                            <p className="text-xs font-mono text-foreground/70 break-all mt-0.5">
                                {entry.transactionId}
                            </p>
                        </div>
                        <div>
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Entry ID</p>
                            <p className="text-xs font-mono text-foreground/70 break-all mt-0.5">
                                {entry.id}
                            </p>
                        </div>
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                        <div>
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Type</p>
                            <Badge
                                variant="outline"
                                className={`text-[10px] mt-0.5 ${isCredit
                                    ? 'border-emerald-500/30 text-emerald-400'
                                    : 'border-rose-500/30 text-rose-400'
                                    }`}
                            >
                                {entry.entryType}
                            </Badge>
                        </div>
                        <div>
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wider">Timestamp</p>
                            <p className="text-xs font-mono text-foreground/70 mt-0.5">
                                {new Date(entry.createdAt).toLocaleString()}
                            </p>
                        </div>
                    </div>
                </div>
            )}
        </button>
    );
}

// TASK-441: Loading skeleton for transaction stream items
function TransactionSkeleton() {
    return (
        <div className="flex items-center gap-3 px-4 py-3.5">
            <Skeleton className="w-9 h-9 rounded-full shrink-0" />
            <div className="flex-1 space-y-1.5">
                <Skeleton className="h-3.5 w-32" />
                <Skeleton className="h-2.5 w-16" />
            </div>
            <div className="text-right space-y-1.5">
                <Skeleton className="h-3.5 w-16 ml-auto" />
                <Skeleton className="h-2.5 w-20 ml-auto" />
            </div>
        </div>
    );
}

export function TransactionStream({ accountId }: TransactionStreamProps) {
    const { data, isLoading, fetchNextPage, hasNextPage, isFetchingNextPage } = useUserLedger(accountId, 20);
    const { data: accountsData } = useAccounts(0, 100);
    const accounts = accountsData?.content ?? [];

    // TASK-439: Intersection observer for infinite scroll
    const observerRef = useRef<HTMLDivElement | null>(null);
    const loadMoreRef = useCallback(
        (node: HTMLDivElement | null) => {
            if (isFetchingNextPage) return;
            if (observerRef.current) return;

            const observer = new IntersectionObserver(
                (entries) => {
                    if (entries[0].isIntersecting && hasNextPage) {
                        fetchNextPage();
                    }
                },
                { threshold: 0.5 }
            );

            if (node) {
                observer.observe(node);
                observerRef.current = node;
            }

            return () => {
                observer.disconnect();
                observerRef.current = null;
            };
        },
        [fetchNextPage, hasNextPage, isFetchingNextPage]
    );

    // Reset observer when dependencies change
    useEffect(() => {
        return () => {
            observerRef.current = null;
        };
    }, [accountId]);

    // Flatten all pages into a single array
    const allEntries = data?.pages.flatMap((page) => page.entries) ?? [];

    if (isLoading) {
        return (
            <div className="space-y-0">
                {Array.from({ length: 5 }).map((_, i) => (
                    <TransactionSkeleton key={i} />
                ))}
            </div>
        );
    }

    // TASK-448: Empty state
    if (allEntries.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center py-16 gap-4">
                <div className="w-16 h-16 rounded-full bg-muted/30 flex items-center justify-center">
                    <span className="text-3xl opacity-40">📭</span>
                </div>
                <div className="text-center">
                    <p className="text-sm font-medium text-muted-foreground">No transactions yet</p>
                    <p className="text-xs text-muted-foreground/60 mt-1">
                        Send your first transfer to see it here.
                    </p>
                </div>
            </div>
        );
    }

    // Group transactions by date
    const grouped = allEntries.reduce<Record<string, LedgerEntry[]>>((acc, entry) => {
        const date = new Date(entry.createdAt).toLocaleDateString([], {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
        });
        if (!acc[date]) acc[date] = [];
        acc[date].push(entry);
        return acc;
    }, {});

    return (
        // TASK-439: Infinite scroll with ScrollArea
        <ScrollArea className="h-[500px] pr-1">
            <div className="space-y-6">
                {Object.entries(grouped).map(([date, entries]) => (
                    <div key={date}>
                        <div className="sticky top-0 z-10 bg-background/80 backdrop-blur-sm px-4 py-2">
                            <p className="text-[10px] font-semibold text-muted-foreground uppercase tracking-[0.12em]">
                                {date}
                            </p>
                        </div>
                        <div className="space-y-0.5">
                            {entries.map((entry) => (
                                <TransactionItem
                                    key={entry.id}
                                    entry={entry}
                                    accounts={accounts}
                                />
                            ))}
                        </div>
                    </div>
                ))}

                {/* Infinite scroll trigger */}
                {hasNextPage && (
                    <div ref={loadMoreRef} className="py-4">
                        {isFetchingNextPage && (
                            <div className="space-y-0">
                                <TransactionSkeleton />
                                <TransactionSkeleton />
                            </div>
                        )}
                    </div>
                )}

                {/* End of list indicator */}
                {!hasNextPage && allEntries.length > 0 && (
                    <div className="text-center py-4">
                        <p className="text-[10px] text-muted-foreground/40 font-mono">
                            — End of transactions —
                        </p>
                    </div>
                )}
            </div>
        </ScrollArea>
    );
}
