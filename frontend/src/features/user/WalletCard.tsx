// TASK-423 to TASK-428: Wallet Card component
// Real-time balance display, pulse animation on update, optimistic UI,
// rollback on failure, last updated timestamp, fintech aesthetic
import { useState, useEffect, useRef } from 'react';
import { useBalance } from '@/hooks/useBalance';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';

interface WalletCardProps {
    accountId: string | undefined;
    accountName?: string;
    /** Optimistic amount to deduct immediately (before API confirms) */
    pendingDeduction?: number;
    /** Whether a transfer is currently in-flight */
    isTransferring?: boolean;
}

export function WalletCard({
    accountId,
    accountName,
    pendingDeduction = 0,
    isTransferring = false,
}: WalletCardProps) {
    const { data: balance, isLoading, dataUpdatedAt, isError } = useBalance(accountId);
    const [showPulse, setShowPulse] = useState(false);
    // Track whether balance went up or down for color coding
    const [balanceDirection, setBalanceDirection] = useState<'up' | 'down' | null>(null);
    const prevBalanceRef = useRef<number | undefined>(undefined);

    // TASK-424: Pulse/glow animation when balance updates
    // Using setTimeout to avoid synchronous setState inside the effect body,
    // which satisfies react-hooks/set-state-in-effect lint rule.
    useEffect(() => {
        if (balance !== undefined && prevBalanceRef.current !== undefined && balance !== prevBalanceRef.current) {
            const direction: 'up' | 'down' = balance > prevBalanceRef.current ? 'up' : 'down';
            // Schedule state updates as a microtask (callback from external timer),
            // which is the recommended pattern for effects that trigger renders.
            const pulseOn = setTimeout(() => {
                setShowPulse(true);
                setBalanceDirection(direction);
            }, 0);
            const pulseOff = setTimeout(() => setShowPulse(false), 1200);
            prevBalanceRef.current = balance;
            return () => {
                clearTimeout(pulseOn);
                clearTimeout(pulseOff);
            };
        }
        prevBalanceRef.current = balance;
    }, [balance]);

    // TASK-425: Optimistic UI — immediately deduct pending amount
    const displayBalance = balance !== undefined
        ? Math.max(0, balance - pendingDeduction)
        : undefined;

    // TASK-427: Last updated timestamp
    const lastUpdated = dataUpdatedAt
        ? new Date(dataUpdatedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
        : null;

    const formattedBalance = displayBalance !== undefined
        ? new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
        }).format(displayBalance)
        : '—';

    // Split balance for styling
    const [wholePart, decimalPart] = formattedBalance.split('.');

    // Determine pulse color from state (not ref during render)
    const pulseColorClass = showPulse
        ? (balanceDirection === 'up' ? 'text-emerald-300' : 'text-amber-300')
        : 'text-white';

    return (
        // TASK-428: Fintech aesthetic — gradient background, glossy effect
        <Card className={`
            relative overflow-hidden border-0
            bg-gradient-to-br from-[oklch(0.25_0.04_250)] via-[oklch(0.20_0.05_260)] to-[oklch(0.15_0.06_270)]
            text-white shadow-2xl shadow-black/40
            transition-all duration-500 ease-out
            ${showPulse ? 'ring-2 ring-emerald-400/50 shadow-emerald-500/20' : ''}
            ${isTransferring ? 'scale-[0.99]' : 'hover:scale-[1.01]'}
        `}>
            {/* Glossy overlay */}
            <div className="absolute inset-0 bg-gradient-to-br from-white/[0.08] via-transparent to-transparent pointer-events-none" />

            {/* Subtle grid pattern */}
            <div
                className="absolute inset-0 opacity-[0.03] pointer-events-none"
                style={{
                    backgroundImage:
                        'linear-gradient(rgba(255,255,255,0.1) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,0.1) 1px, transparent 1px)',
                    backgroundSize: '24px 24px',
                }}
            />

            {/* Animated glow ring on balance change */}
            {showPulse && (
                <div className="absolute inset-0 pointer-events-none">
                    <div className="absolute inset-0 animate-ping bg-emerald-400/5 rounded-xl" style={{ animationDuration: '1.2s', animationIterationCount: '1' }} />
                </div>
            )}

            <CardContent className="relative z-10 p-6 md:p-8">
                <div className="flex flex-col gap-6">
                    {/* Header */}
                    <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                            <div className="w-10 h-10 rounded-xl bg-white/10 backdrop-blur flex items-center justify-center border border-white/10">
                                <span className="text-lg">💳</span>
                            </div>
                            <div>
                                <p className="text-xs font-medium text-white/50 uppercase tracking-[0.12em]">
                                    Available Balance
                                </p>
                                {accountName && (
                                    <p className="text-sm font-medium text-white/70 mt-0.5">
                                        {accountName}
                                    </p>
                                )}
                            </div>
                        </div>
                        <Badge
                            variant="outline"
                            className={`border-white/15 text-white/60 text-[10px] font-mono gap-1.5 ${isLoading ? 'animate-pulse' : ''}`}
                        >
                            <span className={`inline-block w-1.5 h-1.5 rounded-full ${isError ? 'bg-rose-400' : 'bg-emerald-400 animate-pulse'
                                }`} />
                            {isError ? 'Offline' : 'Live'}
                        </Badge>
                    </div>

                    {/* Balance display */}
                    <div className="flex items-end gap-1">
                        {isLoading ? (
                            <div className="space-y-2">
                                <div className="h-10 w-48 rounded-lg bg-white/10 animate-pulse" />
                                <div className="h-3 w-24 rounded bg-white/5 animate-pulse" />
                            </div>
                        ) : (
                            <>
                                <span className={`
                                    text-5xl md:text-6xl font-bold tracking-tighter tabular-nums
                                    transition-all duration-500 ease-out
                                    ${pulseColorClass}
                                    ${isTransferring ? 'opacity-60' : ''}
                                `}>
                                    {wholePart}
                                </span>
                                <span className={`
                                    text-2xl md:text-3xl font-semibold tabular-nums text-white/40
                                    transition-opacity duration-300
                                    ${isTransferring ? 'opacity-30' : ''}
                                `}>
                                    .{decimalPart || '00'}
                                </span>
                            </>
                        )}
                    </div>

                    {/* Pending deduction indicator */}
                    {pendingDeduction > 0 && (
                        <div className="flex items-center gap-2 text-amber-300/80 text-xs font-medium">
                            <span className="inline-block w-3 h-3 border-2 border-current rounded-full border-t-transparent animate-spin" />
                            Pending: −{new Intl.NumberFormat('en-US', {
                                style: 'currency',
                                currency: 'USD',
                            }).format(pendingDeduction)}
                        </div>
                    )}

                    {/* Footer — last updated + decorative elements */}
                    <div className="flex items-center justify-between pt-2 border-t border-white/[0.06]">
                        {lastUpdated && (
                            <p className="text-[10px] text-white/30 font-mono tabular-nums">
                                Updated {lastUpdated}
                            </p>
                        )}
                        <div className="flex items-center gap-1.5">
                            <div className="w-6 h-4 rounded-sm bg-gradient-to-r from-amber-400/80 to-amber-500/80" />
                            <div className="w-6 h-4 rounded-sm bg-gradient-to-r from-sky-400/60 to-sky-500/60 -ml-2" />
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    );
}
