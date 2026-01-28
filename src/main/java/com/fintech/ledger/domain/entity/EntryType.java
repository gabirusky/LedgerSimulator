package com.fintech.ledger.domain.entity;

/**
 * Represents the type of entry in the double-entry bookkeeping ledger.
 * <p>
 * Every financial transaction creates exactly two entries:
 * <ul>
 *   <li>DEBIT - money leaving an account</li>
 *   <li>CREDIT - money entering an account</li>
 * </ul>
 * Balance = SUM(Credits) - SUM(Debits)
 */
public enum EntryType {
    /**
     * A debit entry representing money leaving an account.
     */
    DEBIT,
    
    /**
     * A credit entry representing money entering an account.
     */
    CREDIT
}
