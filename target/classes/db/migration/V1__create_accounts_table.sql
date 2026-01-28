-- V1__create_accounts_table.sql
-- Creates the accounts table for the Ledger Simulator

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    document VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Unique constraint on document (CPF/CNPJ)
    CONSTRAINT uk_accounts_document UNIQUE (document)
);

-- Index on document for fast lookups
CREATE INDEX idx_accounts_document ON accounts(document);

-- Comment on table and columns
COMMENT ON TABLE accounts IS 'Stores account information for the ledger system';
COMMENT ON COLUMN accounts.id IS 'Unique identifier for the account (UUID)';
COMMENT ON COLUMN accounts.document IS 'Document number (CPF or CNPJ) - unique per account';
COMMENT ON COLUMN accounts.name IS 'Account holder name';
COMMENT ON COLUMN accounts.created_at IS 'Timestamp when the account was created';
COMMENT ON COLUMN accounts.updated_at IS 'Timestamp when the account was last updated';
