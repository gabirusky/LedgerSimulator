import { AppLayout } from './AppLayout';
import { adminNav } from './nav-config';

export function AdminLayout() {
    return <AppLayout navItems={adminNav} title="Admin Panel" />;
}
