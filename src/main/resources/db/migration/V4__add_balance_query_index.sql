-- =====================================================
-- V4: Add composite index for optimized balance queries
-- =====================================================
-- This index enables O(log n) lookups for the latest 
-- ledger entry per account, used by:
--   - findLatestBalance(accountId) 
--   - getBalance(accountId)
--
-- Without this index, balance queries degrade to O(n)
-- as PostgreSQL must scan all entries for an account.
-- =====================================================

CREATE INDEX IF NOT EXISTS idx_ledger_entries_account_created 
    ON ledger_entries(account_id, created_at DESC);

-- Note: The DESC ordering is critical for efficient 
-- "ORDER BY created_at DESC LIMIT 1" queries.
