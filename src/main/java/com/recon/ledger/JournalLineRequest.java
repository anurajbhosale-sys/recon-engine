package com.recon.ledger;

import java.util.UUID;

// CONCEPT: a record is an immutable data carrier. Java generates the constructor,
// getters, equals, hashCode, and toString for you.
public record JournalLineRequest(UUID accountId, Direction direction, long amountMinor, String currency) {

    // CONCEPT: a "compact constructor" runs on every creation, so an invalid line
    // can never exist, not even for a moment.
    public JournalLineRequest {
        if (amountMinor <= 0) {
            throw new InvalidJournalEntryException("Amount must be positive: " + amountMinor);
        }
    }
}