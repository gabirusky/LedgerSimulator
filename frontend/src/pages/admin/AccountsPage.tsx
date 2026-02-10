// TASK-413: Accounts Page — account table with create account dialog
import { useState } from 'react';
import { useAccounts } from '@/hooks/useAccounts';
import { CreateAccountDialog } from '@/features/admin/CreateAccountDialog';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
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
import { Skeleton } from '@/components/ui/skeleton';

export function AccountsPage() {
    const [page, setPage] = useState(0);
    const pageSize = 20;
    const { data, isLoading, isError } = useAccounts(page, pageSize);

    const accounts = data?.content ?? [];

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Page header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Accounts</h1>
                    <p className="text-muted-foreground text-sm mt-1">
                        Manage accounts and view balances.
                    </p>
                </div>
                <CreateAccountDialog />
            </div>

            {/* Account table */}
            <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between">
                        <CardTitle className="text-base font-semibold flex items-center gap-2">
                            <span className="text-lg">👥</span>
                            Account List
                        </CardTitle>
                        <Badge variant="outline" className="text-xs font-mono border-border/50">
                            {data?.totalElements ?? 0} total
                        </Badge>
                    </div>
                </CardHeader>
                <CardContent>
                    {isLoading ? (
                        <div className="space-y-3">
                            {Array.from({ length: 5 }).map((_, i) => (
                                <div key={i} className="flex gap-4 items-center py-3">
                                    <Skeleton className="h-9 w-9 rounded-full" />
                                    <div className="space-y-2 flex-1">
                                        <Skeleton className="h-4 w-[200px]" />
                                        <Skeleton className="h-3 w-[140px]" />
                                    </div>
                                    <Skeleton className="h-4 w-[100px]" />
                                    <Skeleton className="h-4 w-[80px]" />
                                </div>
                            ))}
                        </div>
                    ) : isError ? (
                        <div className="flex items-center justify-center h-32 text-sm text-muted-foreground">
                            <span className="flex items-center gap-2">
                                <span className="text-destructive text-lg">⚠</span>
                                Failed to load accounts. Check API connection.
                            </span>
                        </div>
                    ) : accounts.length === 0 ? (
                        <div className="flex flex-col items-center justify-center h-40 gap-3">
                            <span className="text-5xl opacity-20">👤</span>
                            <p className="text-sm text-muted-foreground">
                                No accounts yet. Click "Create Account" to add one.
                            </p>
                        </div>
                    ) : (
                        <>
                            <div className="rounded-lg border border-border/50 overflow-hidden">
                                <Table>
                                    <TableHeader>
                                        <TableRow className="border-border/50 hover:bg-transparent">
                                            <TableHead className="text-xs font-semibold uppercase tracking-wider text-muted-foreground/70 bg-muted/30 h-10">
                                                Account
                                            </TableHead>
                                            <TableHead className="text-xs font-semibold uppercase tracking-wider text-muted-foreground/70 bg-muted/30 h-10">
                                                Document
                                            </TableHead>
                                            <TableHead className="text-xs font-semibold uppercase tracking-wider text-muted-foreground/70 bg-muted/30 h-10 text-right">
                                                Balance
                                            </TableHead>
                                            <TableHead className="text-xs font-semibold uppercase tracking-wider text-muted-foreground/70 bg-muted/30 h-10">
                                                Created
                                            </TableHead>
                                            <TableHead className="text-xs font-semibold uppercase tracking-wider text-muted-foreground/70 bg-muted/30 h-10">
                                                ID
                                            </TableHead>
                                        </TableRow>
                                    </TableHeader>
                                    <TableBody>
                                        {accounts.map((account) => (
                                            <TableRow
                                                key={account.id}
                                                className="border-border/30 transition-colors hover:bg-muted/40 group"
                                            >
                                                <TableCell className="py-3">
                                                    <div className="flex items-center gap-3">
                                                        <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-sm font-bold text-primary group-hover:bg-primary/20 transition-colors">
                                                            {account.name.charAt(0).toUpperCase()}
                                                        </div>
                                                        <span className="font-medium text-sm">
                                                            {account.name}
                                                        </span>
                                                    </div>
                                                </TableCell>
                                                <TableCell className="py-3">
                                                    <span className="font-mono text-xs text-muted-foreground">
                                                        {account.document}
                                                    </span>
                                                </TableCell>
                                                <TableCell className="py-3 text-right">
                                                    <span className={`font-mono text-sm font-semibold tabular-nums ${account.balance > 0
                                                            ? 'text-emerald-400'
                                                            : account.balance === 0
                                                                ? 'text-muted-foreground'
                                                                : 'text-rose-400'
                                                        }`}>
                                                        {new Intl.NumberFormat('en-US', {
                                                            style: 'currency',
                                                            currency: 'USD',
                                                        }).format(account.balance)}
                                                    </span>
                                                </TableCell>
                                                <TableCell className="py-3">
                                                    <span className="text-xs text-muted-foreground">
                                                        {new Date(account.createdAt).toLocaleDateString(
                                                            undefined,
                                                            { year: 'numeric', month: 'short', day: 'numeric' }
                                                        )}
                                                    </span>
                                                </TableCell>
                                                <TableCell className="py-3">
                                                    <span className="font-mono text-xs text-muted-foreground/60">
                                                        {account.id.slice(0, 8)}…
                                                    </span>
                                                </TableCell>
                                            </TableRow>
                                        ))}
                                    </TableBody>
                                </Table>
                            </div>

                            {/* Pagination */}
                            <div className="flex items-center justify-between pt-4">
                                <p className="text-xs text-muted-foreground tabular-nums">
                                    Page {page + 1} of {data?.totalPages ?? 1}
                                </p>
                                <div className="flex gap-2">
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => setPage((p) => Math.max(0, p - 1))}
                                        disabled={data?.first ?? true}
                                        className="text-xs"
                                    >
                                        ← Previous
                                    </Button>
                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() => setPage((p) => p + 1)}
                                        disabled={data?.last ?? true}
                                        className="text-xs"
                                    >
                                        Next →
                                    </Button>
                                </div>
                            </div>
                        </>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}
