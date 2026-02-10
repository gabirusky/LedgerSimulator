// TASK-414: Create Account Dialog with shadcn/ui Dialog + Form
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod/v4';
import { useCreateAccount } from '@/hooks/useCreateAccount';
import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ApiError } from '@/services/ledgerProvider';

const createAccountSchema = z.object({
    document: z
        .string()
        .min(1, 'Document is required')
        .max(20, 'Document must be at most 20 characters'),
    name: z
        .string()
        .min(1, 'Name is required')
        .max(100, 'Name must be at most 100 characters'),
});

type CreateAccountForm = z.infer<typeof createAccountSchema>;

export function CreateAccountDialog() {
    const [open, setOpen] = useState(false);
    const createAccount = useCreateAccount();

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<CreateAccountForm>({
        resolver: zodResolver(createAccountSchema),
        defaultValues: {
            document: '',
            name: '',
        },
    });

    const onSubmit = async (data: CreateAccountForm) => {
        try {
            await createAccount.mutateAsync(data);
            reset();
            setOpen(false);
        } catch {
            // Error is handled by the mutation state
        }
    };

    return (
        <Dialog open={open} onOpenChange={(isOpen) => {
            setOpen(isOpen);
            if (!isOpen) {
                reset();
                createAccount.reset();
            }
        }}>
            <DialogTrigger asChild>
                <Button className="gap-2">
                    <span>+</span> Create Account
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[425px] border-border/50 bg-card/95 backdrop-blur-md">
                <DialogHeader>
                    <DialogTitle>Create New Account</DialogTitle>
                    <DialogDescription>
                        Enter the document number and holder name for the new account.
                    </DialogDescription>
                </DialogHeader>

                <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 mt-2">
                    <div className="space-y-2">
                        <Label htmlFor="document">Document Number</Label>
                        <Input
                            id="document"
                            placeholder="e.g. 12345678901"
                            className="bg-muted/50 border-border/50"
                            {...register('document')}
                        />
                        {errors.document && (
                            <p className="text-xs text-destructive">{errors.document.message}</p>
                        )}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="name">Account Holder Name</Label>
                        <Input
                            id="name"
                            placeholder="e.g. John Doe"
                            className="bg-muted/50 border-border/50"
                            {...register('name')}
                        />
                        {errors.name && (
                            <p className="text-xs text-destructive">{errors.name.message}</p>
                        )}
                    </div>

                    {/* API error display */}
                    {createAccount.isError && (
                        <div className="rounded-lg bg-destructive/10 border border-destructive/30 p-3">
                            <p className="text-xs text-destructive font-medium">
                                {createAccount.error instanceof ApiError
                                    ? createAccount.error.detail
                                    : 'Failed to create account. Please try again.'}
                            </p>
                        </div>
                    )}

                    <div className="flex justify-end gap-2 pt-2">
                        <Button
                            type="button"
                            variant="outline"
                            onClick={() => setOpen(false)}
                        >
                            Cancel
                        </Button>
                        <Button
                            type="submit"
                            disabled={createAccount.isPending}
                        >
                            {createAccount.isPending ? (
                                <span className="flex items-center gap-2">
                                    <span className="inline-block w-3 h-3 border-2 border-current border-t-transparent rounded-full animate-spin" />
                                    Creating…
                                </span>
                            ) : (
                                'Create Account'
                            )}
                        </Button>
                    </div>
                </form>
            </DialogContent>
        </Dialog>
    );
}
