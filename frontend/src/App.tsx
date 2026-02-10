import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AdminLayout } from '@/layouts/AdminLayout';
import { UserLayout } from '@/layouts/UserLayout';
import { NotFoundPage } from '@/pages/NotFoundPage';

// Lazy-loaded page placeholders (to be implemented in Phase 15 & 16)
import { DashboardPage } from '@/pages/admin/DashboardPage';
import { LedgerPage } from '@/pages/admin/LedgerPage';
import { AccountsPage } from '@/pages/admin/AccountsPage';
import { WalletPage } from '@/pages/user/WalletPage';
import { HistoryPage } from '@/pages/user/HistoryPage';

/** TASK-389: QueryClient with default staleTime and gcTime */
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 30,    // 30 seconds before data is considered stale
      gcTime: 1000 * 60 * 5,   // 5 minutes garbage collection
      retry: 2,
      refetchOnWindowFocus: false,
    },
  },
});

export default function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <HashRouter>
        <Routes>
          {/* Default redirect to admin dashboard */}
          <Route path="/" element={<Navigate to="/admin" replace />} />

          {/* Admin Panel (TASK-379) */}
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<DashboardPage />} />
            <Route path="ledger" element={<LedgerPage />} />
            <Route path="accounts" element={<AccountsPage />} />
          </Route>

          {/* User Simulator (TASK-380) */}
          <Route path="/user" element={<UserLayout />}>
            <Route index element={<WalletPage />} />
            <Route path="history" element={<HistoryPage />} />
          </Route>

          {/* 404 fallback (TASK-381) */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </HashRouter>
    </QueryClientProvider>
  );
}
