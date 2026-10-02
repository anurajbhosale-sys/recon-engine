package com.recon.ledger;

import java.util.UUID;

public class EntryNotFoundException extends RuntimeException {
    public EntryNotFoundException(UUID entryId) {
        super("Journal entry not found: " + entryId);
    }
}