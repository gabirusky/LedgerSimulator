-- V2__create_transactions_table.sql
-- Creates the transactions table for the Ledger Simulator

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL,
    source_account_id UUID NOT NULL,
    target_account_id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint on idempotency_key for idempotent transfers
    CONSTRAINT uk_transactions_idempotency_key UNIQUE (idempotency_key),
    
    -- Foreign key constraints
    CONSTRAINT fk_transactions_source_account FOREIGN KEY (source_account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_transactions_target_account FOREIGN KEY (target_account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT,
    
    -- Check constraint for positive amount
    CONSTRAINT chk_transactions_amount_positive CHECK (amount > 0),
    
    -- Check constraint for valid status
    CONSTRAINT chk_transactions_status CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED'))
);

-- Index on idempotency_key for fast lookups
CREATE INDEX idx_transactions_idempotency_key ON transactions(idempotency_key);

-- Index on source_account_id for account statement queries
CREATE INDEX idx_transactions_source_account ON transactions(source_account_id);

-- Index on target_account_id for account statement queries
CREATE INDEX idx_transactions_target_account ON transactions(target_account_id);

-- Index on created_at for date range queries
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- Comment on table and columns
COMMENT ON TABLE transactions IS 'Stores financial transactions between accounts';
COMMENT ON COLUMN transactions.id IS 'Unique identifier for the transaction (UUID)';
COMMENT ON COLUMN transactions.idempotency_key IS 'Client-provided key to ensure idempotent transfers';
COMMENT ON COLUMN transactions.source_account_id IS 'Account from which funds are debited';
COMMENT ON COLUMN transactions.target_account_id IS 'Account to which funds are credited';
COMMENT ON COLUMN transactions.amount IS 'Transaction amount with precision 19 and scale 2';
COMMENT ON COLUMN transactions.status IS 'Transaction status: PENDING, COMPLETED, or FAILED';
COMMENT ON COLUMN transactions.created_at IS 'Timestamp when the transaction was created';
