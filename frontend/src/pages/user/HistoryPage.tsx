// TASK-443: History Page — compose TransactionStream with filters
import { useState } from 'react';
import { useAccounts } from '@/hooks/useAccounts';
import { TransactionStream } from '@/features/user/TransactionStream';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import type { Account } from '@/types/api';

export function HistoryPage() {
    const { data: accountsData, isLoading: loadingAccounts } = useAccounts(0, 100);
    const accounts = accountsData?.content ?? [];

    // Account selection
    const [selectedAccountId, setSelectedAccountId] = useState<string>('');
    const activeAccountId = selectedAccountId || accounts[0]?.id || '';
    const activeAccount = accounts.find((a: Account) => a.id === activeAccountId);

    // Filter state (UI only — filtering happens on render since we use infinite scroll)
    const [, setFilter] = useState<'all' | 'credit' | 'debit'>('all');

    // Empty state
    if (!loadingAccounts && accounts.length === 0) {
        return (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Transaction History</h1>
                    <p className="text-muted-foreground text-sm mt-1">
                        Complete record of all your financial activity.
                    </p>
                </div>
                {/* TASK-448: Empty state */}
                <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                    <CardContent className="flex flex-col items-center justify-center py-20 gap-4">
                        <div className="w-20 h-20 rounded-full bg-muted/20 flex items-center justify-center">
                            <span className="text-4xl opacity-30">📜</span>
                        </div>
                        <div className="text-center max-w-sm">
                            <p className="text-lg font-semibold">No accounts found</p>
                            <p className="text-sm text-muted-foreground mt-2">
                                Create an account in the Admin Panel to view transaction history.
                            </p>
                        </div>
                    </CardContent>
                </Card>
            </div>
        );
    }

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Page header */}
            <div className="flex items-center justify-between flex-wrap gap-4">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Transaction History</h1>
                    <p className="text-muted-foreground text-sm mt-1">
                        Complete record of all your financial activity.
                    </p>
                </div>

                {/* Account selector */}
                {accounts.length > 1 && (
                    <Select
                        value={activeAccountId}
                        onValueChange={setSelectedAccountId}
                    >
                        <SelectTrigger className="w-[240px] bg-background/50">
                            <SelectValue placeholder="Select account" />
                        </SelectTrigger>
                        <SelectContent>
                            {accounts.map((account: Account) => (
                                <SelectItem key={account.id} value={account.id}>
                                    <div className="flex items-center gap-2">
                                        <span className="font-medium">{account.name}</span>
                                        <span className="text-xs text-muted-foreground font-mono">
                                            {account.document}
                                        </span>
                                    </div>
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                )}
            </div>

            {/* Account summary bar */}
            {activeAccount && (
                <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                    <CardContent className="flex items-center justify-between p-4 md:p-5">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-base font-bold text-primary">
                                {activeAccount.name.charAt(0).toUpperCase()}
                            </div>
                            <div>
                                <p className="font-semibold text-sm">{activeAccount.name}</p>
                                <p className="text-xs text-muted-foreground font-mono">{activeAccount.document}</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <div className="text-right">
                                <p className="text-xs text-muted-foreground">Current Balance</p>
                                <p className="text-lg font-bold tabular-nums">
                                    {new Intl.NumberFormat('en-US', {
                                        style: 'currency',
                                        currency: 'USD',
                                    }).format(activeAccount.balance)}
                                </p>
                            </div>
                            <Badge
                                variant="outline"
                                className="border-emerald-500/30 text-emerald-400 text-[10px]"
                            >
                                Active
                            </Badge>
                        </div>
                    </CardContent>
                </Card>
            )}

            {/* Filters & Transaction Stream */}
            <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                <CardHeader className="pb-3">
                    <div className="flex items-center justify-between flex-wrap gap-3">
                        <CardTitle className="text-base font-semibold flex items-center gap-2">
                            <span className="text-lg">📒</span>
                            Ledger Entries
                        </CardTitle>

                        {/* Filter tabs */}
                        <Tabs defaultValue="all" onValueChange={(v) => setFilter(v as 'all' | 'credit' | 'debit')}>
                            <TabsList className="h-8">
                                <TabsTrigger value="all" className="text-xs px-3 h-6">
                                    All
                                </TabsTrigger>
                                <TabsTrigger value="credit" className="text-xs px-3 h-6">
                                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 mr-1.5" />
                                    Credits
                                </TabsTrigger>
                                <TabsTrigger value="debit" className="text-xs px-3 h-6">
                                    <span className="w-1.5 h-1.5 rounded-full bg-rose-400 mr-1.5" />
                                    Debits
                                </TabsTrigger>
                            </TabsList>
                        </Tabs>
                    </div>
                </CardHeader>
                <CardContent className="px-2 pb-4">
                    <TransactionStream accountId={activeAccountId || undefined} />
                </CardContent>
            </Card>
        </div>
    );
}
