import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export function HistoryPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Transaction History</h1>
                <p className="text-muted-foreground">
                    View your past transactions with full detail.
                </p>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Transaction Stream</CardTitle>
                    <CardDescription>
                        Color-coded transaction history with infinite scroll.
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <p className="text-sm text-muted-foreground">
                        Transaction stream with DEBIT/CREDIT color-coding and infinite scroll
                        will be implemented in Phase 16 (TASK-436–441).
                    </p>
                </CardContent>
            </Card>
        </div>
    );
}
