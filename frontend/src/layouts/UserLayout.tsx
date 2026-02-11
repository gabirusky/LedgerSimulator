import { AppLayout } from './AppLayout';
import { userNav } from './nav-config';

export function UserLayout() {
    return <AppLayout navItems={userNav} title="User Simulator" />;
}
