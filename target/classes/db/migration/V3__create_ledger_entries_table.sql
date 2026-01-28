-- V3__create_ledger_entries_table.sql
-- Creates the ledger_entries table for the Ledger Simulator

CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account_id UUID NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_ledger_entries_transaction FOREIGN KEY (transaction_id) 
        REFERENCES transactions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_ledger_entries_account FOREIGN KEY (account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT,
    
    -- Check constraint for valid entry_type
    CONSTRAINT chk_ledger_entries_type CHECK (entry_type IN ('DEBIT', 'CREDIT')),
    
    -- Check constraint for positive amount
    CONSTRAINT chk_ledger_entries_amount_positive CHECK (amount > 0)
);

-- Index on account_id for account statement queries (order by created_at desc)
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);

-- Index on transaction_id for retrieving all entries of a transaction
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);

-- Composite index for account statement queries
CREATE INDEX idx_ledger_entries_account_created ON ledger_entries(account_id, created_at DESC);

-- Comment on table and columns
COMMENT ON TABLE ledger_entries IS 'Double-entry ledger entries for each transaction';
COMMENT ON COLUMN ledger_entries.id IS 'Unique identifier for the ledger entry (UUID)';
COMMENT ON COLUMN ledger_entries.transaction_id IS 'Reference to the parent transaction';
COMMENT ON COLUMN ledger_entries.account_id IS 'Account associated with this entry';
COMMENT ON COLUMN ledger_entries.entry_type IS 'Type of entry: DEBIT (money out) or CREDIT (money in)';
COMMENT ON COLUMN ledger_entries.amount IS 'Entry amount with precision 19 and scale 2';
COMMENT ON COLUMN ledger_entries.balance_after IS 'Account balance after this entry was applied';
COMMENT ON COLUMN ledger_entries.created_at IS 'Timestamp when the entry was created';
