import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export function DashboardPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">Dashboard</h1>
                <p className="text-muted-foreground">
                    System health, analytics, and balance integrity overview.
                </p>
            </div>

            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <span>📊</span> System Health
                        </CardTitle>
                        <CardDescription>TPS and volume metrics</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">
                            Charts will be implemented in Phase 15 (TASK-403–406).
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <span>✅</span> Balance Integrity
                        </CardTitle>
                        <CardDescription>Conservation of value check</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">
                            Widget will be implemented in Phase 15 (TASK-407–410).
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <span>⚡</span> Quick Stats
                        </CardTitle>
                        <CardDescription>Total accounts and transactions</CardDescription>
                    </CardHeader>
                    <CardContent>
                        <p className="text-sm text-muted-foreground">
                            Stats will be populated when admin hooks are created.
                        </p>
                    </CardContent>
                </Card>
            </div>
        </div>
    );
}
