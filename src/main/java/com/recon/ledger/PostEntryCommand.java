package com.recon.ledger;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// CONCEPT: a "command" = a request to change something, bundled into one immutable object.
// The service takes this instead of 5 separate parameters.
public record PostEntryCommand(
        UUID tenantId,
        LocalDate effectiveDate,
        String description,
        String idempotencyKey,
        List<JournalLineRequest> lines) {

    public PostEntryCommand {
        if (tenantId == null || effectiveDate == null || idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidJournalEntryException("tenantId, effectiveDate and idempotencyKey are required");
        }
        lines = List.copyOf(lines); // CONCEPT: defensive copy, so the caller can't modify the list afterward
    }
}