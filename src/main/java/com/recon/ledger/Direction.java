package com.recon.ledger;

public enum Direction {
    DEBIT, CREDIT;

    // CONCEPT: a reversal mirrors every line with the opposite direction
    public Direction opposite() {
        return this == DEBIT ? CREDIT : DEBIT;
    }
}