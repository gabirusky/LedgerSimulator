import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router-dom';

export function NotFoundPage() {
    const navigate = useNavigate();

    return (
        <div className="flex min-h-screen flex-col items-center justify-center gap-6 bg-background text-foreground">
            <div className="text-center">
                <h1 className="text-8xl font-bold tracking-tighter text-muted-foreground/30">
                    404
                </h1>
                <p className="mt-2 text-xl font-medium text-muted-foreground">
                    Page not found
                </p>
                <p className="mt-1 text-sm text-muted-foreground/70">
                    The ledger entry you're looking for doesn't exist.
                </p>
            </div>
            <div className="flex gap-3">
                <Button variant="outline" onClick={() => navigate(-1)}>
                    Go Back
                </Button>
                <Button onClick={() => navigate('/admin')}>
                    Dashboard
                </Button>
            </div>
        </div>
    );
}
