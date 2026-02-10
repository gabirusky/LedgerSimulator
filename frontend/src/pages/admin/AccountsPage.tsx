import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export function AccountsPage() {
    return (
        <div className="space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold tracking-tight">Accounts</h1>
                    <p className="text-muted-foreground">
                        Manage accounts and view balances.
                    </p>
                </div>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Account List</CardTitle>
                    <CardDescription>
                        Account table with create dialog will be implemented in Phase 15 (TASK-413–414).
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <p className="text-sm text-muted-foreground">
                        Features: paginated account table, create account dialog,
                        balance display, document search.
                    </p>
                </CardContent>
            </Card>
        </div>
    );
}
