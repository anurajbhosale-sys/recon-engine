package com.recon.ledger;

public class InvalidJournalEntryException extends RuntimeException {
    public InvalidJournalEntryException(String message) {
        super(message);
    }
}