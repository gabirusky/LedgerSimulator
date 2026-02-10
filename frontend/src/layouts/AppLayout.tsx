import { Outlet, NavLink, useLocation } from 'react-router-dom';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { cn } from '@/lib/utils';
import { useState } from 'react';

interface NavItem {
    label: string;
    path: string;
    icon: string;
}

const adminNav: NavItem[] = [
    { label: 'Dashboard', path: '/admin', icon: '📊' },
    { label: 'General Ledger', path: '/admin/ledger', icon: '📒' },
    { label: 'Accounts', path: '/admin/accounts', icon: '👥' },
];

const userNav: NavItem[] = [
    { label: 'Wallet', path: '/user', icon: '💳' },
    { label: 'History', path: '/user/history', icon: '📜' },
];

function SidebarNav({ items, title }: { items: NavItem[]; title: string }) {
    return (
        <nav className="flex flex-col gap-1 p-4">
            <h2 className="mb-3 px-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {title}
            </h2>
            {items.map((item) => (
                <NavLink
                    key={item.path}
                    to={item.path}
                    end={item.path === '/admin' || item.path === '/user'}
                    className={({ isActive }) =>
                        cn(
                            'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors',
                            isActive
                                ? 'bg-primary text-primary-foreground'
                                : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                        )
                    }
                >
                    <span className="text-base">{item.icon}</span>
                    {item.label}
                </NavLink>
            ))}
        </nav>
    );
}

function ModeSwitch() {
    const location = useLocation();
    const isAdmin = location.pathname.startsWith('/admin');

    return (
        <div className="p-4">
            <Separator className="mb-4" />
            <NavLink
                to={isAdmin ? '/user' : '/admin'}
                className="flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground"
            >
                <span className="text-base">{isAdmin ? '💳' : '🔧'}</span>
                {isAdmin ? 'User Simulator' : 'Admin Panel'}
            </NavLink>
        </div>
    );
}

export function AppLayout({
    navItems,
    title,
}: {
    navItems: NavItem[];
    title: string;
}) {
    const [sheetOpen, setSheetOpen] = useState(false);

    return (
        <div className="flex h-screen w-screen overflow-hidden bg-background">
            {/* Desktop Sidebar */}
            <aside className="hidden w-64 shrink-0 border-r border-border bg-sidebar md:flex md:flex-col">
                <div className="flex h-14 items-center gap-2 border-b border-border px-4">
                    <span className="text-lg">⚡</span>
                    <span className="font-semibold text-sidebar-foreground">LedgerSimulator</span>
                </div>
                <div className="flex flex-1 flex-col justify-between overflow-y-auto">
                    <SidebarNav items={navItems} title={title} />
                    <ModeSwitch />
                </div>
            </aside>

            {/* Mobile Sheet */}
            <Sheet open={sheetOpen} onOpenChange={setSheetOpen}>
                <div className="flex flex-1 flex-col overflow-hidden">
                    {/* Top bar */}
                    <header className="flex h-14 items-center gap-3 border-b border-border bg-background px-4 md:hidden">
                        <SheetTrigger asChild>
                            <Button variant="ghost" size="icon" className="md:hidden">
                                <span className="text-lg">☰</span>
                            </Button>
                        </SheetTrigger>
                        <span className="font-semibold">LedgerSimulator</span>
                    </header>

                    {/* Page content */}
                    <main className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8">
                        <Outlet />
                    </main>
                </div>

                <SheetContent side="left" className="w-64 p-0">
                    <div className="flex h-14 items-center gap-2 border-b border-border px-4">
                        <span className="text-lg">⚡</span>
                        <span className="font-semibold">LedgerSimulator</span>
                    </div>
                    <div className="flex flex-1 flex-col justify-between">
                        <SidebarNav items={navItems} title={title} />
                        <ModeSwitch />
                    </div>
                </SheetContent>
            </Sheet>
        </div>
    );
}

// Pre-configured layouts for admin and user
export { adminNav, userNav };
