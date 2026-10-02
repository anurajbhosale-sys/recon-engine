package com.recon.ledger;

// Already reversed, or is itself a reversal → conflicts with the entry's current state
public class EntryNotReversibleException extends RuntimeException {
    public EntryNotReversibleException(String message) {
        super(message);
    }
}