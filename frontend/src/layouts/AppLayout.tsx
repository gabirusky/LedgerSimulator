// TASK-415, TASK-416, TASK-419: Enhanced AppLayout with dark theme, responsive sidebar, keyboard shortcuts
import { Outlet, NavLink, useLocation, useNavigate } from 'react-router-dom';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { Button } from '@/components/ui/button';
import { Separator } from '@/components/ui/separator';
import { cn } from '@/lib/utils';
import { useState, useEffect } from 'react';

interface NavItem {
    label: string;
    path: string;
    icon: string;
    shortcut?: string;
}

const adminNav: NavItem[] = [
    { label: 'Dashboard', path: '/admin', icon: '📊', shortcut: '1' },
    { label: 'General Ledger', path: '/admin/ledger', icon: '📒', shortcut: '2' },
    { label: 'Accounts', path: '/admin/accounts', icon: '👥', shortcut: '3' },
];

const userNav: NavItem[] = [
    { label: 'Wallet', path: '/user', icon: '💳', shortcut: '1' },
    { label: 'History', path: '/user/history', icon: '📜', shortcut: '2' },
];

function SidebarNav({ items, title }: { items: NavItem[]; title: string }) {
    return (
        <nav className="flex flex-col gap-1 p-4">
            <h2 className="mb-3 px-2 text-[10px] font-semibold uppercase tracking-[0.15em] text-muted-foreground/60">
                {title}
            </h2>
            {items.map((item) => (
                <NavLink
                    key={item.path}
                    to={item.path}
                    end={item.path === '/admin' || item.path === '/user'}
                    className={({ isActive }) =>
                        cn(
                            'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200',
                            isActive
                                ? 'bg-sidebar-primary text-sidebar-primary-foreground shadow-sm shadow-sidebar-primary/25'
                                : 'text-muted-foreground hover:bg-sidebar-accent hover:text-sidebar-accent-foreground'
                        )
                    }
                >
                    <span className="text-base">{item.icon}</span>
                    <span className="flex-1">{item.label}</span>
                    {item.shortcut && (
                        <kbd className="hidden lg:inline-block px-1.5 py-0.5 text-[10px] font-mono rounded bg-sidebar-accent/50 text-muted-foreground/40 border border-sidebar-border/50">
                            {item.shortcut}
                        </kbd>
                    )}
                </NavLink>
            ))}
        </nav>
    );
}

function ModeSwitch() {
    const location = useLocation();
    const isAdmin = location.pathname.startsWith('/admin');

    // TASK-450: Dark/light theme toggle
    const toggleTheme = () => {
        const html = document.documentElement;
        const isDark = html.classList.contains('dark');
        if (isDark) {
            html.classList.remove('dark');
            localStorage.setItem('theme', 'light');
        } else {
            html.classList.add('dark');
            localStorage.setItem('theme', 'dark');
        }
    };

    // Initialize theme on mount
    useEffect(() => {
        const saved = localStorage.getItem('theme');
        if (saved === 'light') {
            document.documentElement.classList.remove('dark');
        } else {
            document.documentElement.classList.add('dark');
        }
    }, []);

    return (
        <div className="p-4">
            <Separator className="mb-4 bg-sidebar-border/50" />
            <NavLink
                to={isAdmin ? '/user' : '/admin'}
                className="flex items-center gap-2 rounded-lg px-3 py-2.5 text-sm font-medium text-muted-foreground transition-all duration-200 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
            >
                <span className="text-base">{isAdmin ? '💳' : '🔧'}</span>
                {isAdmin ? 'User Simulator' : 'Admin Panel'}
            </NavLink>
            <button
                type="button"
                onClick={toggleTheme}
                className="mt-1 flex w-full items-center gap-2 rounded-lg px-3 py-2.5 text-sm font-medium text-muted-foreground transition-all duration-200 hover:bg-sidebar-accent hover:text-sidebar-accent-foreground"
            >
                <span className="text-base">🌓</span>
                Toggle Theme
            </button>
            <p className="mt-4 px-2 text-[10px] text-muted-foreground/40 font-mono">
                v0.1.0 · LedgerSimulator
            </p>
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
    const navigate = useNavigate();

    // TASK-419: Keyboard navigation (Alt+1, Alt+2, Alt+3)
    useEffect(() => {
        function handleKeyDown(e: KeyboardEvent) {
            if (e.altKey && !e.ctrlKey && !e.metaKey) {
                const index = parseInt(e.key) - 1;
                if (index >= 0 && index < navItems.length) {
                    e.preventDefault();
                    navigate(navItems[index].path);
                }
            }
        }
        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [navItems, navigate]);

    return (
        <div className="flex h-screen w-screen overflow-hidden bg-background" style={{ fontFamily: "'Inter', system-ui, sans-serif" }}>
            {/* Desktop Sidebar */}
            <aside className="hidden w-60 shrink-0 border-r border-sidebar-border bg-sidebar md:flex md:flex-col">
                <div className="flex h-14 items-center gap-2.5 border-b border-sidebar-border px-5">
                    <span className="text-lg">⚡</span>
                    <span className="font-semibold text-sm text-sidebar-foreground tracking-tight">
                        LedgerSimulator
                    </span>
                </div>
                <div className="flex flex-1 flex-col justify-between overflow-y-auto">
                    <SidebarNav items={navItems} title={title} />
                    <ModeSwitch />
                </div>
            </aside>

            {/* Mobile Sheet */}
            <Sheet open={sheetOpen} onOpenChange={setSheetOpen}>
                <div className="flex flex-1 flex-col overflow-hidden">
                    {/* Top bar (mobile) */}
                    <header className="flex h-14 items-center gap-3 border-b border-border bg-background px-4 md:hidden">
                        <SheetTrigger asChild>
                            <Button variant="ghost" size="icon" className="md:hidden">
                                <span className="text-lg">☰</span>
                            </Button>
                        </SheetTrigger>
                        <span className="font-semibold text-sm tracking-tight">LedgerSimulator</span>
                    </header>

                    {/* Page content */}
                    <main className="flex-1 overflow-y-auto p-4 md:p-6 lg:p-8">
                        <Outlet />
                    </main>
                </div>

                <SheetContent side="left" className="w-60 p-0 bg-sidebar border-sidebar-border">
                    <div className="flex h-14 items-center gap-2.5 border-b border-sidebar-border px-5">
                        <span className="text-lg">⚡</span>
                        <span className="font-semibold text-sm text-sidebar-foreground tracking-tight">
                            LedgerSimulator
                        </span>
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
