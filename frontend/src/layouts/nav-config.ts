// Navigation configuration — shared between layouts
// Extracted to its own file to satisfy react-refresh/only-export-components

export interface NavItem {
    label: string;
    path: string;
    icon: string;
    shortcut?: string;
}

export const adminNav: NavItem[] = [
    { label: 'Dashboard', path: '/admin', icon: '📊', shortcut: '1' },
    { label: 'General Ledger', path: '/admin/ledger', icon: '📒', shortcut: '2' },
    { label: 'Accounts', path: '/admin/accounts', icon: '👥', shortcut: '3' },
];

export const userNav: NavItem[] = [
    { label: 'Wallet', path: '/user', icon: '💳', shortcut: '1' },
    { label: 'History', path: '/user/history', icon: '📜', shortcut: '2' },
];
