// TASK-442: Wallet Page — compose WalletCard + TransferForm
// TASK-444: Account selector for users with multiple accounts
import { useState, useCallback } from 'react';
import { useAccounts } from '@/hooks/useAccounts';
import { useBalance } from '@/hooks/useBalance';
import { WalletCard } from '@/features/user/WalletCard';
import { TransferForm } from '@/features/user/TransferForm';
import { TransactionStream } from '@/features/user/TransactionStream';
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select';
import { Card, CardContent } from '@/components/ui/card';
import type { Account } from '@/types/api';

export function WalletPage() {
    const { data: accountsData, isLoading: loadingAccounts } = useAccounts(0, 100);
    const accounts = accountsData?.content ?? [];

    // TASK-444: Account selector state
    const [selectedAccountId, setSelectedAccountId] = useState<string>('');

    // Auto-select first account when data loads
    const activeAccountId = selectedAccountId || accounts[0]?.id || '';
    const activeAccount = accounts.find((a: Account) => a.id === activeAccountId);

    // Balance for optimistic UI
    const { data: balance } = useBalance(activeAccountId || undefined);

    // Optimistic UI state (TASK-425, TASK-426)
    const [pendingDeduction, setPendingDeduction] = useState(0);

    const handleTransferStart = useCallback((amount: number) => {
        setPendingDeduction(amount);
    }, []);

    const handleTransferSuccess = useCallback(() => {
        setPendingDeduction(0);
    }, []);

    const handleTransferError = useCallback(() => {
        // TASK-426: Rollback — reset pending deduction
        setPendingDeduction(0);
    }, []);

    // Empty state — no accounts
    if (!loadingAccounts && accounts.length === 0) {
        return (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Wallet</h1>
                    <p className="text-muted-foreground text-sm mt-1">
                        Your digital wallet for sending and receiving funds.
                    </p>
                </div>
                {/* TASK-448: Empty state */}
                <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                    <CardContent className="flex flex-col items-center justify-center py-20 gap-4">
                        <div className="w-20 h-20 rounded-full bg-muted/20 flex items-center justify-center">
                            <span className="text-4xl opacity-30">🏦</span>
                        </div>
                        <div className="text-center max-w-sm">
                            <p className="text-lg font-semibold">No accounts found</p>
                            <p className="text-sm text-muted-foreground mt-2">
                                Create an account in the Admin Panel to start using the wallet simulator.
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
                    <h1 className="text-3xl font-bold tracking-tight">Wallet</h1>
                    <p className="text-muted-foreground text-sm mt-1">
                        Your digital wallet for sending and receiving funds.
                    </p>
                </div>

                {/* TASK-444: Account selector */}
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

            {/* Main content grid */}
            <div className="grid gap-6 lg:grid-cols-5">
                {/* Wallet Card — spans 3 columns */}
                <div className="lg:col-span-3 space-y-6">
                    <WalletCard
                        accountId={activeAccountId || undefined}
                        accountName={activeAccount?.name}
                        pendingDeduction={pendingDeduction}
                        isTransferring={pendingDeduction > 0}
                    />

                    {/* Recent transactions (mini stream on wallet page) */}
                    <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                        <div className="px-6 pt-5 pb-2">
                            <div className="flex items-center justify-between">
                                <h3 className="text-sm font-semibold text-muted-foreground flex items-center gap-2">
                                    <span className="text-base">📋</span>
                                    Recent Transactions
                                </h3>
                            </div>
                        </div>
                        <CardContent className="px-2 pb-4">
                            <TransactionStream accountId={activeAccountId || undefined} />
                        </CardContent>
                    </Card>
                </div>

                {/* Transfer Form — spans 2 columns */}
                <div className="lg:col-span-2 space-y-6">
                    <TransferForm
                        sourceAccountId={activeAccountId || undefined}
                        sourceBalance={balance ?? activeAccount?.balance ?? 0}
                        onTransferStart={handleTransferStart}
                        onTransferSuccess={handleTransferSuccess}
                        onTransferError={handleTransferError}
                    />

                    {/* Quick account info */}
                    {activeAccount && (
                        <Card className="border-border/40 bg-card/70 backdrop-blur-sm">
                            <CardContent className="p-5">
                                <div className="space-y-3">
                                    <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-[0.12em]">
                                        Account Details
                                    </h3>
                                    <div className="space-y-2.5">
                                        <div className="flex items-center justify-between">
                                            <span className="text-xs text-muted-foreground">Account ID</span>
                                            <span className="text-xs font-mono text-foreground/70 max-w-[180px] truncate">
                                                {activeAccount.id}
                                            </span>
                                        </div>
                                        <div className="flex items-center justify-between">
                                            <span className="text-xs text-muted-foreground">Document</span>
                                            <span className="text-xs font-mono text-foreground/70">
                                                {activeAccount.document}
                                            </span>
                                        </div>
                                        <div className="flex items-center justify-between">
                                            <span className="text-xs text-muted-foreground">Created</span>
                                            <span className="text-xs text-foreground/70">
                                                {new Date(activeAccount.createdAt).toLocaleDateString()}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    )}
                </div>
            </div>
        </div>
    );
}
