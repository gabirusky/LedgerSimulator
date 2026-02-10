import { AppLayout, adminNav } from './AppLayout';

export function AdminLayout() {
    return <AppLayout navItems={adminNav} title="Admin Panel" />;
}
