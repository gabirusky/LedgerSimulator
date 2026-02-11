// TASK-429 to TASK-435: Transfer Form component
// shadcn/ui Form + Input + Select, searchable accounts, BigDecimal-safe validation,
// auto-generate Idempotency-Key (UUID v4), form validation, toast notifications,
// disable submit during pending mutation
import { useState, useMemo } from 'react';
import { useAccounts } from '@/hooks/useAccounts';
import { useTransfers } from '@/hooks/useTransfers';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
    Command,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList,
} from '@/components/ui/command';
import type { Account } from '@/types/api';

interface TransferFormProps {
    sourceAccountId: string | undefined;
    sourceBalance?: number;
    onTransferStart?: (amount: number) => void;
    onTransferSuccess?: () => void;
    onTransferError?: () => void;
}

type ToastState = {
    type: 'success' | 'error';
    message: string;
} | null;

export function TransferForm({
    sourceAccountId,
    sourceBalance = 0,
    onTransferStart,
    onTransferSuccess,
    onTransferError,
}: TransferFormProps) {
    const [targetAccountId, setTargetAccountId] = useState<string>('');
    const [amount, setAmount] = useState<string>('');
    const [showAccountSearch, setShowAccountSearch] = useState(false);
    const [toast, setToast] = useState<ToastState>(null);

    const { data: accountsData, isLoading: loadingAccounts } = useAccounts(0, 100);
    const transferMutation = useTransfers();

    // Filter out the source account from target list (TASK-433)
    const availableTargets = useMemo(() => {
        if (!accountsData?.content) return [];
        return accountsData.content.filter((a: Account) => a.id !== sourceAccountId);
    }, [accountsData, sourceAccountId]);

    const selectedTarget = availableTargets.find((a: Account) => a.id === targetAccountId);

    // TASK-431: BigDecimal-safe validation
    const parsedAmount = amount ? parseFloat(parseFloat(amount).toFixed(2)) : 0;

    // TASK-433: Form validation
    const validationErrors: string[] = [];
    if (amount && parsedAmount <= 0) validationErrors.push('Amount must be greater than 0');
    if (amount && parsedAmount > sourceBalance) validationErrors.push('Insufficient funds');
    if (targetAccountId && targetAccountId === sourceAccountId) validationErrors.push('Cannot transfer to self');
    const isValid = targetAccountId !== '' && parsedAmount > 0 && parsedAmount <= sourceBalance && targetAccountId !== sourceAccountId;

    // TASK-434: Toast notifications
    const showToast = (type: 'success' | 'error', message: string) => {
        setToast({ type, message });
        setTimeout(() => setToast(null), 4000);
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!isValid || !sourceAccountId) return;

        // TASK-432: Auto-generate Idempotency-Key (UUID v4)
        const idempotencyKey = crypto.randomUUID();
        const safeAmount = parseFloat(parsedAmount.toFixed(2));

        // TASK-425: Notify parent for optimistic UI
        onTransferStart?.(safeAmount);

        transferMutation.mutate(
            {
                data: {
                    sourceAccountId,
                    targetAccountId,
                    amount: safeAmount,
                },
                idempotencyKey,
            },
            {
                onSuccess: () => {
                    showToast('success', `Sent ${new Intl.NumberFormat('en-US', {
                        style: 'currency',
                        currency: 'USD',
                    }).format(safeAmount)} to ${selectedTarget?.name ?? 'account'}`);
                    setAmount('');
                    setTargetAccountId('');
                    onTransferSuccess?.();
                },
                onError: (error) => {
                    showToast('error', error.message || 'Transfer failed. Please try again.');
                    // TASK-426: Trigger rollback
                    onTransferError?.();
                },
            }
        );
    };

    return (
        <Card className="border-border/40 bg-card/70 backdrop-blur-sm relative overflow-hidden">
            {/* Decorative corner gradient */}
            <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-chart-1/5 to-transparent pointer-events-none" />

            <CardHeader className="pb-4">
                <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-lg bg-primary/10 flex items-center justify-center">
                        <span className="text-base">💸</span>
                    </div>
                    <div>
                        <CardTitle className="text-base font-semibold">Send Money</CardTitle>
                        <CardDescription className="text-xs">
                            Transfer funds to another account instantly
                        </CardDescription>
                    </div>
                </div>
            </CardHeader>

            <CardContent className="relative z-10">
                <form onSubmit={handleSubmit} className="space-y-5">
                    {/* Recipient Selection (TASK-430: Searchable via Command) */}
                    <div className="space-y-2">
                        <Label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
                            Recipient
                        </Label>
                        {showAccountSearch ? (
                            <Command className="border border-input rounded-lg bg-popover">
                                <CommandInput placeholder="Search accounts…" className="text-sm" />
                                <CommandList className="max-h-48">
                                    <CommandEmpty>
                                        {loadingAccounts ? 'Loading accounts…' : 'No accounts found.'}
                                    </CommandEmpty>
                                    <CommandGroup>
                                        {availableTargets.map((account: Account) => (
                                            <CommandItem
                                                key={account.id}
                                                value={`${account.name} ${account.document}`}
                                                onSelect={() => {
                                                    setTargetAccountId(account.id);
                                                    setShowAccountSearch(false);
                                                }}
                                                className="cursor-pointer"
                                            >
                                                <div className="flex items-center justify-between w-full">
                                                    <div className="flex items-center gap-2.5">
                                                        <div className="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center text-xs font-bold text-primary">
                                                            {account.name.charAt(0).toUpperCase()}
                                                        </div>
                                                        <div>
                                                            <p className="text-sm font-medium">{account.name}</p>
                                                            <p className="text-[10px] text-muted-foreground font-mono">
                                                                {account.document}
                                                            </p>
                                                        </div>
                                                    </div>
                                                    <span className="text-xs text-muted-foreground tabular-nums">
                                                        {new Intl.NumberFormat('en-US', {
                                                            style: 'currency',
                                                            currency: 'USD',
                                                        }).format(account.balance)}
                                                    </span>
                                                </div>
                                            </CommandItem>
                                        ))}
                                    </CommandGroup>
                                </CommandList>
                            </Command>
                        ) : (
                            <button
                                type="button"
                                onClick={() => setShowAccountSearch(true)}
                                className="w-full flex items-center gap-3 rounded-lg border border-input bg-background/50 px-3 py-2.5 text-left text-sm transition-colors hover:bg-accent/50 focus:outline-none focus:ring-2 focus:ring-ring"
                            >
                                {selectedTarget ? (
                                    <>
                                        <div className="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center text-xs font-bold text-primary shrink-0">
                                            {selectedTarget.name.charAt(0).toUpperCase()}
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <p className="font-medium truncate">{selectedTarget.name}</p>
                                            <p className="text-[10px] text-muted-foreground font-mono truncate">
                                                {selectedTarget.document}
                                            </p>
                                        </div>
                                        <span className="text-xs text-muted-foreground">Change</span>
                                    </>
                                ) : (
                                    <>
                                        <span className="text-muted-foreground">🔍</span>
                                        <span className="text-muted-foreground">Select recipient…</span>
                                    </>
                                )}
                            </button>
                        )}
                    </div>

                    {/* Amount Input (TASK-431: BigDecimal-safe) */}
                    <div className="space-y-2">
                        <div className="flex items-center justify-between">
                            <Label className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
                                Amount
                            </Label>
                            <button
                                type="button"
                                onClick={() => setAmount(sourceBalance.toFixed(2))}
                                className="text-[10px] text-chart-1 hover:text-chart-1/80 font-medium transition-colors"
                            >
                                Send Max
                            </button>
                        </div>
                        <div className="relative">
                            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground text-sm font-medium">
                                $
                            </span>
                            <Input
                                type="number"
                                step="0.01"
                                min="0.01"
                                max={sourceBalance}
                                placeholder="0.00"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                                className="pl-7 text-lg font-semibold tabular-nums h-12 bg-background/50"
                            />
                        </div>
                        <div className="flex items-center justify-between text-[10px] text-muted-foreground">
                            <span>
                                Available: {new Intl.NumberFormat('en-US', {
                                    style: 'currency',
                                    currency: 'USD',
                                }).format(sourceBalance)}
                            </span>
                            {amount && parsedAmount > 0 && parsedAmount <= sourceBalance && (
                                <span className="text-foreground/60">
                                    Remaining: {new Intl.NumberFormat('en-US', {
                                        style: 'currency',
                                        currency: 'USD',
                                    }).format(sourceBalance - parsedAmount)}
                                </span>
                            )}
                        </div>
                    </div>

                    {/* Validation errors */}
                    {validationErrors.length > 0 && amount !== '' && (
                        <div className="space-y-1">
                            {validationErrors.map((error, i) => (
                                <p key={i} className="text-xs text-destructive-foreground flex items-center gap-1.5">
                                    <span>⚠️</span> {error}
                                </p>
                            ))}
                        </div>
                    )}

                    {/* TASK-435: Disable submit during pending mutation */}
                    <Button
                        type="submit"
                        disabled={!isValid || transferMutation.isPending || !sourceAccountId}
                        className="w-full h-11 font-semibold text-sm relative overflow-hidden group transition-all duration-300"
                    >
                        {transferMutation.isPending ? (
                            <span className="flex items-center gap-2">
                                <span className="inline-block w-4 h-4 border-2 border-current rounded-full border-t-transparent animate-spin" />
                                Processing…
                            </span>
                        ) : (
                            <span className="flex items-center gap-2">
                                Send {amount && parsedAmount > 0 ? new Intl.NumberFormat('en-US', {
                                    style: 'currency',
                                    currency: 'USD',
                                }).format(parsedAmount) : 'Money'}
                            </span>
                        )}
                    </Button>
                </form>
            </CardContent>

            {/* TASK-434: Toast notification */}
            {toast && (
                <div className={`
                    absolute bottom-4 left-4 right-4 z-50
                    flex items-center gap-2 rounded-lg px-4 py-3 text-sm font-medium
                    animate-in slide-in-from-bottom-4
                    ${toast.type === 'success'
                        ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/20'
                        : 'bg-rose-500/15 text-rose-400 border border-rose-500/20'
                    }
                `}>
                    <span>{toast.type === 'success' ? '✅' : '❌'}</span>
                    <span className="flex-1">{toast.message}</span>
                    <button
                        onClick={() => setToast(null)}
                        className="text-current opacity-50 hover:opacity-100 transition-opacity"
                    >
                        ✕
                    </button>
                </div>
            )}
        </Card>
    );
}
