import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export function WalletPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Wallet</h1>
                <p className="text-muted-foreground">
                    View your balance and transfer funds.
                </p>
            </div>

            <div className="grid gap-6 md:grid-cols-2">
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <span>💳</span> Balance
                        </CardTitle>
                        <CardDescription>Your current holdings</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">
                            Wallet card with real-time balance polling will be implemented in Phase 16 (TASK-423–428).
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <span>💸</span> Transfer
                        </CardTitle>
                        <CardDescription>Send funds to another account</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">
                            Transfer form with idempotency support will be implemented in Phase 16 (TASK-429–435).
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
