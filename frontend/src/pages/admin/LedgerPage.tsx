// TASK-412: Ledger Page — compose GeneralLedgerGrid with page title and filters
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import { GeneralLedgerGrid } from '@/features/admin/GeneralLedgerGrid';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import type { Account, Page } from '@/types/api';

export function LedgerPage() {
    const [selectedAccountId, setSelectedAccountId] = useState<string>('');

    // Fetch accounts for the account selector
    const { data: accountsData, isLoading: accountsLoading } = useQuery<Page<Account>>({
        queryKey: ['accounts', 0, 100],
        queryFn: () => ledgerProvider.getAccounts(0, 100),
    });

    const accounts = accountsData?.content ?? [];

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Page header */}
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">General Ledger</h1>
                    <p className="text-muted-foreground text-sm mt-1">
                        View all ledger entries with server-side pagination and filtering.
                    </p>
                </div>
                <Badge variant="outline" className="text-xs font-mono border-border/50 self-start">
                    {accountsData?.totalElements ?? 0} accounts
                </Badge>
            </div>

            {/* Account selector */}
            <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                <CardHeader className="pb-3">
                    <CardTitle className="text-base font-semibold flex items-center gap-2">
                        <span className="text-lg">🔍</span>
                        Select Account
                    </CardTitle>
                </CardHeader>
                <CardContent>
                    <Select
                        value={selectedAccountId}
                        onValueChange={setSelectedAccountId}
                    >
                        <SelectTrigger className="w-full max-w-md bg-muted/50 border-border/50">
                            <SelectValue placeholder={
                                accountsLoading
                                    ? 'Loading accounts…'
                                    : 'Select an account to view ledger entries'
                            } />
                        </SelectTrigger>
                        <SelectContent>
                            {accounts.map((account) => (
                                <SelectItem key={account.id} value={account.id}>
                                    <div className="flex items-center gap-2">
                                        <span className="font-medium">{account.name}</span>
                                        <span className="text-xs text-muted-foreground font-mono">
                                            ({account.document})
                                        </span>
                                        <span className="text-xs font-semibold tabular-nums ml-auto">
                                            {new Intl.NumberFormat('en-US', {
                                                style: 'currency',
                                                currency: 'USD',
                                            }).format(account.balance)}
                                        </span>
                                    </div>
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </CardContent>
            </Card>

            {/* Ledger Grid */}
            {selectedAccountId ? (
                <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                    <CardHeader className="pb-3">
                        <CardTitle className="text-base font-semibold flex items-center gap-2">
                            <span className="text-lg">📒</span>
                            Ledger Entries
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        <GeneralLedgerGrid accountId={selectedAccountId} />
                    </CardContent>
                </Card>
            ) : (
                <div className="flex flex-col items-center justify-center h-64 gap-3 rounded-lg border border-dashed border-border/50 bg-muted/10">
                    <span className="text-5xl opacity-20">📒</span>
                    <p className="text-sm text-muted-foreground">
                        Select an account above to view its ledger entries.
                    </p>
                </div>
            )}
        </div>
    );
}
