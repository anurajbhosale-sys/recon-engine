package com.recon.ledger;

// CONCEPT: enums can have fields and constructors. Each value carries its own data.
public enum AccountType {
    ASSET(true),
    LIABILITY(false),
    EQUITY(false),
    REVENUE(false),
    EXPENSE(true);

    private final boolean debitNormal;

    AccountType(boolean debitNormal) {
        this.debitNormal = debitNormal;
    }

    // true → grows with debits (assets, expenses); false → grows with credits
    public boolean isDebitNormal() {
        return debitNormal;
    }
}