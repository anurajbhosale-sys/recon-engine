package com.recon.ledger;

import java.util.UUID;

// CONCEPT: a record is an immutable data carrier. Java generates the constructor,
// getters, equals, hashCode, and toString for you.
public record JournalLineRequest(UUID accountId, Direction direction, long amountMinor, String currency) {

    // CONCEPT: a "compact constructor" runs on every creation, so an invalid line
    // can never exist, not even for a moment.
    public static final long MAX_AMOUNT_MINOR = 1_000_000_000_000L;
    public JournalLineRequest {
        // CONCEPT: validate at the core. Not every caller goes through the web DTO's @NotNull.
        if (accountId == null || direction == null || currency == null) {
            throw new InvalidJournalEntryException("accountId, direction and currency are required");
        }
        if (amountMinor <= 0) {
            throw new InvalidJournalEntryException("Amount must be positive: " + amountMinor);
        }
        if (amountMinor > MAX_AMOUNT_MINOR) {
            throw new InvalidJournalEntryException(
                    "Amount exceeds maximum of " + MAX_AMOUNT_MINOR + ": " + amountMinor);
        }
    }
}