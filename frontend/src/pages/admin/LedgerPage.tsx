import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

export function LedgerPage() {
    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-3xl font-bold tracking-tight">General Ledger</h1>
                <p className="text-muted-foreground">
                    View all ledger entries with server-side pagination and filtering.
                </p>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Ledger Entries</CardTitle>
                    <CardDescription>
                        Full data grid will be implemented in Phase 15 (TASK-396–402).
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <p className="text-sm text-muted-foreground">
                        The General Ledger Grid will use @tanstack/react-table with cursor-based pagination,
                        column sorting, and filtering by account ID, date range, and status.
                    </p>
                </CardContent>
            </Card>
        </div>
    );
}
