// TASK-397 to TASK-402: General Ledger Data Grid
// Column definitions, server-side pagination, sorting, filtering, styling, loading
import { useState, useMemo } from 'react';
import {
    useReactTable,
    getCoreRowModel,
    getSortedRowModel,
    getFilteredRowModel,
    flexRender,
    type ColumnDef,
    type SortingState,
    type ColumnFiltersState,
} from '@tanstack/react-table';
import { useQuery } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';

import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Skeleton } from '@/components/ui/skeleton';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import type { LedgerEntry } from '@/types/api';

// ── Column definitions ────────────────────────────────────────────────

const columns: ColumnDef<LedgerEntry>[] = [
    {
        accessorKey: 'transactionId',
        header: 'Transaction ID',
        cell: ({ row }) => (
            <span className="font-mono text-xs text-muted-foreground">
                {row.original.transactionId.slice(0, 8)}…
            </span>
        ),
        enableSorting: false,
    },
    {
        accessorKey: 'createdAt',
        header: 'Timestamp',
        cell: ({ row }) => {
            const date = new Date(row.original.createdAt);
            return (
                <span className="text-sm tabular-nums">
                    {date.toLocaleDateString(undefined, {
                        month: 'short',
                        day: 'numeric',
                    })}{' '}
                    <span className="text-muted-foreground">
                        {date.toLocaleTimeString(undefined, {
                            hour: '2-digit',
                            minute: '2-digit',
                            second: '2-digit',
                        })}
                    </span>
                </span>
            );
        },
    },
    {
        accessorKey: 'entryType',
        header: 'Type',
        cell: ({ row }) => {
            const type = row.original.entryType;
            return (
                <Badge
                    variant={type === 'CREDIT' ? 'default' : 'destructive'}
                    className={
                        type === 'CREDIT'
                            ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/25'
                            : 'bg-rose-500/15 text-rose-400 border-rose-500/30 hover:bg-rose-500/25'
                    }
                >
                    {type}
                </Badge>
            );
        },
        filterFn: (row, _columnId, filterValue) => {
            if (!filterValue || filterValue === 'ALL') return true;
            return row.original.entryType === filterValue;
        },
    },
    {
        accessorKey: 'amount',
        header: () => <div className="text-right">Amount</div>,
        cell: ({ row }) => (
            <div className="text-right font-mono text-sm tabular-nums">
                <span className={row.original.entryType === 'CREDIT' ? 'text-emerald-400' : 'text-rose-400'}>
                    {row.original.entryType === 'CREDIT' ? '+' : '-'}
                    {new Intl.NumberFormat('en-US', {
                        style: 'currency',
                        currency: 'USD',
                    }).format(row.original.amount)}
                </span>
            </div>
        ),
    },
    {
        accessorKey: 'balanceAfter',
        header: () => <div className="text-right">Balance After</div>,
        cell: ({ row }) => (
            <div className="text-right font-mono text-sm tabular-nums font-semibold">
                {new Intl.NumberFormat('en-US', {
                    style: 'currency',
                    currency: 'USD',
                }).format(row.original.balanceAfter)}
            </div>
        ),
    },
];

// ── Loading skeleton ──────────────────────────────────────────────────

function TableSkeleton() {
    return (
        <div className="space-y-3 p-1">
            {Array.from({ length: 8 }).map((_, i) => (
                <div key={i} className="flex gap-4 items-center">
                    <Skeleton className="h-4 w-[80px]" />
                    <Skeleton className="h-4 w-[140px]" />
                    <Skeleton className="h-6 w-[60px] rounded-full" />
                    <Skeleton className="h-4 w-[90px] ml-auto" />
                    <Skeleton className="h-4 w-[90px]" />
                </div>
            ))}
        </div>
    );
}

// ── Main component ────────────────────────────────────────────────────

interface GeneralLedgerGridProps {
    accountId: string;
}

export function GeneralLedgerGrid({ accountId }: GeneralLedgerGridProps) {
    const [page, setPage] = useState(0);
    const [pageSize] = useState(20);
    const [sorting, setSorting] = useState<SortingState>([
        { id: 'createdAt', desc: true },
    ]);
    const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
    const [accountFilter, setAccountFilter] = useState('');

    // Fetch ledger entries
    const { data: statement, isLoading, isError } = useQuery({
        queryKey: ['ledger', accountId, page, pageSize],
        queryFn: () => ledgerProvider.getLedger(accountId, page, pageSize),
        enabled: !!accountId,
    });

    const entries = useMemo(() => statement?.entries ?? [], [statement]);

    // eslint-disable-next-line react-hooks/incompatible-library -- TanStack Table returns mutable functions by design
    const table = useReactTable({
        data: entries,
        columns,
        state: {
            sorting,
            columnFilters,
        },
        onSortingChange: setSorting,
        onColumnFiltersChange: setColumnFilters,
        getCoreRowModel: getCoreRowModel(),
        getSortedRowModel: getSortedRowModel(),
        getFilteredRowModel: getFilteredRowModel(),
    });

    return (
        <div className="space-y-4">
            {/* Filters bar */}
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex gap-2 flex-1">
                    <Input
                        placeholder="Filter by account ID…"
                        value={accountFilter}
                        onChange={(e) => setAccountFilter(e.target.value)}
                        className="max-w-[280px] bg-muted/50 border-border/50"
                    />
                    <Select
                        value={
                            (table.getColumn('entryType')?.getFilterValue() as string) ??
                            'ALL'
                        }
                        onValueChange={(value) =>
                            table
                                .getColumn('entryType')
                                ?.setFilterValue(value === 'ALL' ? undefined : value)
                        }
                    >
                        <SelectTrigger className="w-[140px] bg-muted/50 border-border/50">
                            <SelectValue placeholder="Entry type" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="ALL">All types</SelectItem>
                            <SelectItem value="CREDIT">Credit</SelectItem>
                            <SelectItem value="DEBIT">Debit</SelectItem>
                        </SelectContent>
                    </Select>
                </div>
                <div className="text-xs text-muted-foreground tabular-nums">
                    {statement && (
                        <>
                            Showing <span className="font-semibold text-foreground">{entries.length}</span> entries
                            {statement.accountName && (
                                <> for <span className="font-semibold text-foreground">{statement.accountName}</span></>
                            )}
                        </>
                    )}
                </div>
            </div>

            {/* Table */}
            <div className="rounded-lg border border-border/50 bg-card/50 overflow-hidden">
                {isLoading ? (
                    <TableSkeleton />
                ) : isError ? (
                    <div className="flex items-center justify-center h-40 text-sm text-muted-foreground">
                        <span className="flex items-center gap-2">
                            <span className="text-destructive text-lg">⚠</span>
                            Failed to load ledger entries. Check API connection.
                        </span>
                    </div>
                ) : entries.length === 0 ? (
                    <div className="flex flex-col items-center justify-center h-40 gap-2">
                        <span className="text-4xl opacity-30">📒</span>
                        <p className="text-sm text-muted-foreground">
                            No ledger entries found for this account.
                        </p>
                    </div>
                ) : (
                    <Table>
                        <TableHeader>
                            {table.getHeaderGroups().map((headerGroup) => (
                                <TableRow
                                    key={headerGroup.id}
                                    className="border-border/50 hover:bg-transparent"
                                >
                                    {headerGroup.headers.map((header) => (
                                        <TableHead
                                            key={header.id}
                                            className="text-xs font-semibold uppercase tracking-wider text-muted-foreground/70 bg-muted/30 h-10 cursor-pointer select-none"
                                            onClick={header.column.getToggleSortingHandler()}
                                        >
                                            <div className="flex items-center gap-1">
                                                {flexRender(
                                                    header.column.columnDef.header,
                                                    header.getContext()
                                                )}
                                                {header.column.getIsSorted() === 'asc' && ' ↑'}
                                                {header.column.getIsSorted() === 'desc' && ' ↓'}
                                            </div>
                                        </TableHead>
                                    ))}
                                </TableRow>
                            ))}
                        </TableHeader>
                        <TableBody>
                            {table.getRowModel().rows.map((row) => (
                                <TableRow
                                    key={row.id}
                                    className="border-border/30 transition-colors hover:bg-muted/40 group"
                                >
                                    {row.getVisibleCells().map((cell) => (
                                        <TableCell
                                            key={cell.id}
                                            className="py-3"
                                        >
                                            {flexRender(
                                                cell.column.columnDef.cell,
                                                cell.getContext()
                                            )}
                                        </TableCell>
                                    ))}
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                )}
            </div>

            {/* Pagination */}
            <div className="flex items-center justify-between">
                <p className="text-xs text-muted-foreground tabular-nums">
                    Page {page + 1}
                </p>
                <div className="flex gap-2">
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPage((p) => Math.max(0, p - 1))}
                        disabled={page === 0}
                        className="text-xs"
                    >
                        ← Previous
                    </Button>
                    <Button
                        variant="outline"
                        size="sm"
                        onClick={() => setPage((p) => p + 1)}
                        disabled={entries.length < pageSize}
                        className="text-xs"
                    >
                        Next →
                    </Button>
                </div>
            </div>
        </div>
    );
}
