// TASK-411: Dashboard Page — compose SystemHealthChart + BalanceIntegrityWidget
import { useQuery } from '@tanstack/react-query';
import { ledgerProvider } from '@/services/ledgerProvider';
import { SystemHealthChart } from '@/features/admin/SystemHealthChart';
import { BalanceIntegrityWidget } from '@/features/admin/BalanceIntegrityWidget';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import type { Account, Page } from '@/types/api';

function QuickStatCard({
    title,
    value,
    icon,
    description,
    accent,
}: {
    title: string;
    value: string | number;
    icon: string;
    description: string;
    accent: string;
}) {
    return (
        <Card className="border-border/40 bg-card/70 backdrop-blur-sm group hover:border-border/60 transition-all duration-300">
            <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                    <CardTitle className="text-sm font-medium text-muted-foreground">
                        {title}
                    </CardTitle>
                    <span className={`text-lg transition-transform duration-300 group-hover:scale-110 ${accent}`}>
                        {icon}
                    </span>
                </div>
            </CardHeader>
            <CardContent>
                <p className="text-2xl font-bold tabular-nums tracking-tight">
                    {value}
                </p>
                <p className="text-xs text-muted-foreground mt-1">{description}</p>
            </CardContent>
        </Card>
    );
}

export function DashboardPage() {
    const { data: accountsData, isLoading } = useQuery<Page<Account>>({
        queryKey: ['accounts', 0, 100],
        queryFn: () => ledgerProvider.getAccounts(0, 100),
    });

    const accountCount = accountsData?.totalElements ?? 0;
    const totalBalance = accountsData?.content.reduce(
        (sum, acc) => sum + acc.balance,
        0
    ) ?? 0;

    const healthQuery = useQuery({
        queryKey: ['health'],
        queryFn: () => ledgerProvider.getHealth(),
        retry: 1,
    });

    const isUp = healthQuery.data?.status === 'UP';

    return (
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
            {/* Page header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text">
                        Dashboard
                    </h1>
                    <p className="text-muted-foreground text-sm mt-1">
                        System health, analytics, and balance integrity overview.
                    </p>
                </div>
                <Badge
                    variant="outline"
                    className={`gap-1.5 text-xs px-3 py-1 transition-colors ${healthQuery.isLoading
                            ? 'border-amber-500/40 text-amber-400'
                            : isUp
                                ? 'border-emerald-500/40 text-emerald-400'
                                : 'border-rose-500/40 text-rose-400'
                        }`}
                >
                    <span className={`inline-block w-1.5 h-1.5 rounded-full ${healthQuery.isLoading
                            ? 'bg-amber-400 animate-pulse'
                            : isUp
                                ? 'bg-emerald-400 animate-pulse'
                                : 'bg-rose-400'
                        }`} />
                    {healthQuery.isLoading ? 'Connecting…' : isUp ? 'System Online' : 'System Offline'}
                </Badge>
            </div>

            {/* Quick stats row */}
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <QuickStatCard
                    title="Total Accounts"
                    value={isLoading ? '—' : accountCount}
                    icon="👥"
                    description="Registered accounts"
                    accent=""
                />
                <QuickStatCard
                    title="System Balance"
                    value={
                        isLoading
                            ? '—'
                            : new Intl.NumberFormat('en-US', {
                                style: 'currency',
                                currency: 'USD',
                                maximumFractionDigits: 0,
                            }).format(totalBalance)
                    }
                    icon="💰"
                    description="Total value in system"
                    accent=""
                />
                <QuickStatCard
                    title="API Status"
                    value={
                        healthQuery.isLoading
                            ? 'Checking…'
                            : isUp
                                ? 'Healthy'
                                : 'Unreachable'
                    }
                    icon={isUp ? '🟢' : '🔴'}
                    description="Spring Boot backend"
                    accent=""
                />
                <QuickStatCard
                    title="Database"
                    value={
                        healthQuery.isLoading
                            ? 'Checking…'
                            : healthQuery.data?.components?.db?.status === 'UP'
                                ? 'Connected'
                                : isUp
                                    ? 'Active'
                                    : 'Unknown'
                    }
                    icon="🗄️"
                    description="PostgreSQL 16"
                    accent=""
                />
            </div>

            {/* Charts */}
            <SystemHealthChart />

            {/* Balance Integrity */}
            <div className="grid gap-4 lg:grid-cols-3">
                <div className="lg:col-span-1">
                    <BalanceIntegrityWidget />
                </div>
                <Card className="lg:col-span-2 border-border/40 bg-card/70 backdrop-blur-sm">
                    <CardHeader>
                        <CardTitle className="text-base font-semibold flex items-center gap-2">
                            <span className="text-lg">📋</span>
                            Recent Activity
                        </CardTitle>
                    </CardHeader>
                    <CardContent>
                        {accountsData?.content.length ? (
                            <div className="space-y-3">
                                {accountsData.content.slice(0, 5).map((account) => (
                                    <div
                                        key={account.id}
                                        className="flex items-center justify-between rounded-lg bg-muted/20 px-4 py-3 transition-colors hover:bg-muted/40"
                                    >
                                        <div className="flex items-center gap-3">
                                            <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-sm font-bold text-primary">
                                                {account.name.charAt(0).toUpperCase()}
                                            </div>
                                            <div>
                                                <p className="text-sm font-medium">{account.name}</p>
                                                <p className="text-xs text-muted-foreground font-mono">
                                                    {account.document}
                                                </p>
                                            </div>
                                        </div>
                                        <span className="text-sm font-semibold tabular-nums">
                                            {new Intl.NumberFormat('en-US', {
                                                style: 'currency',
                                                currency: 'USD',
                                            }).format(account.balance)}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="flex flex-col items-center justify-center h-32 gap-2">
                                <span className="text-3xl opacity-30">📭</span>
                                <p className="text-sm text-muted-foreground">
                                    No accounts yet. Create one to get started.
                                </p>
                            </div>
                        )}
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
